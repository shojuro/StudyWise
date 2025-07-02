package com.studywise.ai.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.repository.SyncRepository
import com.studywise.ai.domain.service.NetworkStateMonitor
import com.studywise.ai.domain.service.RetryPolicy
import com.studywise.ai.domain.service.ExponentialBackoffRetryPolicy
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Worker that handles data synchronization in the background
 */
@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val networkStateMonitor: NetworkStateMonitor,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {
    
    private val retryPolicy: RetryPolicy = ExponentialBackoffRetryPolicy(
        baseDelayMs = 2000L,
        maxDelayMs = 30000L,
        maxRetries = 3,
        factor = 2.0
    )
    
    override suspend fun doWork(): Result {
        // Check network availability
        if (!networkStateMonitor.isNetworkAvailable()) {
            return Result.retry()
        }
        
        // Check if WiFi-only sync is enabled
        val preferences = preferencesManager.userPreferences.first()
        if (preferences.syncWifiOnly && !networkStateMonitor.isWifiConnected()) {
            return Result.retry()
        }
        
        return try {
            // Update sync state
            setProgress(workDataOf("phase" to "starting"))
            
            // 1. Sync local changes to server
            setProgress(workDataOf("phase" to "uploading"))
            val uploadResult = retryPolicy.executeWithRetry {
                syncRepository.syncLocalChanges()
            }
            
            if (uploadResult.isFailure) {
                return Result.retry()
            }
            
            val localSyncResult = uploadResult.getOrThrow()
            
            // 2. Sync server changes to local
            setProgress(workDataOf("phase" to "downloading"))
            val lastSyncTime = preferences.lastSyncTime
            val downloadResult = retryPolicy.executeWithRetry {
                syncRepository.syncServerChanges(lastSyncTime)
            }
            
            if (downloadResult.isFailure) {
                return Result.retry()
            }
            
            val serverSyncResult = downloadResult.getOrThrow()
            
            // 3. Process any conflicts
            setProgress(workDataOf("phase" to "resolving_conflicts"))
            val conflicts = localSyncResult.conflicts + serverSyncResult.conflicts
            if (conflicts.isNotEmpty()) {
                // Auto-resolve conflicts where possible
                for (conflict in conflicts) {
                    // For now, use default resolution strategy
                    // In a real app, you might want user intervention for some conflicts
                    val resolution = com.studywise.ai.domain.service.DefaultConflictResolver()
                        .determineResolutionStrategy(conflict)
                    syncRepository.resolveConflict(conflict, resolution)
                }
            }
            
            // 4. Update last sync time
            preferencesManager.updateLastSyncTime(System.currentTimeMillis())
            
            // 5. Clean up old completed operations
            syncRepository.clearCompletedOperations()
            
            // Return success with summary data
            val outputData = workDataOf(
                "itemsSynced" to (localSyncResult.itemsSynced + serverSyncResult.itemsSynced),
                "itemsFailed" to (localSyncResult.itemsFailed + serverSyncResult.itemsFailed),
                "conflicts" to conflicts.size
            )
            
            Result.success(outputData)
            
        } catch (e: Exception) {
            // Log error and retry
            e.printStackTrace()
            
            // Check if we've exceeded retry attempts
            if (runAttemptCount >= MAX_RUN_ATTEMPTS) {
                Result.failure(
                    workDataOf("error" to (e.message ?: "Unknown error"))
                )
            } else {
                Result.retry()
            }
        }
    }
    
    companion object {
        const val MAX_RUN_ATTEMPTS = 5
        const val WORK_NAME = "data_sync"
        const val PERIODIC_WORK_NAME = "periodic_data_sync"
    }
}