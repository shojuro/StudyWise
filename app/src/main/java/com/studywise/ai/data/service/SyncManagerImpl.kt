package com.studywise.ai.data.service

import android.content.Context
import androidx.work.*
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.data.worker.DataSyncWorker
import com.studywise.ai.domain.service.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SyncManager that coordinates data synchronization
 */
@Singleton
class SyncManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkStateMonitor: NetworkStateMonitor,
    private val preferencesManager: PreferencesManager,
    private val analyticsService: AnalyticsService
) : SyncManager {
    
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _syncState = MutableStateFlow(SyncState())
    override val syncState: Flow<SyncState> = _syncState.asStateFlow()
    
    init {
        // Listen to network state changes and trigger sync when network becomes available
        scope.launch {
            networkStateMonitor.networkStateChanges
                .distinctUntilChanged()
                .collect { networkState ->
                    if (networkState.isConnected && !isSyncing()) {
                        // Check if we have pending items to sync
                        val preferences = preferencesManager.userPreferences.first()
                        val lastSyncTime = preferences.lastSyncTime
                        val timeSinceLastSync = System.currentTimeMillis() - lastSyncTime
                        
                        // Auto-sync if more than 15 minutes since last sync
                        if (timeSinceLastSync > TimeUnit.MINUTES.toMillis(15)) {
                            scheduleSync(delayMillis = 5000) // Wait 5 seconds before syncing
                        }
                    }
                }
        }
    }
    
    override suspend fun syncNow(): SyncResult {
        if (!networkStateMonitor.isNetworkAvailable()) {
            return SyncResult.Failure(
                SyncError(
                    code = "NO_NETWORK",
                    message = "No network connection available",
                    isRetryable = true
                )
            )
        }
        
        // Check if WiFi-only sync is enabled
        val preferences = preferencesManager.userPreferences.first()
        if (preferences.syncWifiOnly && !networkStateMonitor.isWifiConnected()) {
            return SyncResult.Failure(
                SyncError(
                    code = "WIFI_ONLY",
                    message = "Sync is configured for WiFi only",
                    isRetryable = true
                )
            )
        }
        
        return withContext(Dispatchers.IO) {
            try {
                _syncState.update { it.copy(isSyncing = true, progress = 0f) }
                
                // Create one-time sync work request
                val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
                
                // Enqueue and wait for result
                workManager.enqueueUniqueWork(
                    "immediate_sync",
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                ).await()
                
                // Monitor work progress
                val workInfo = workManager.getWorkInfoById(syncRequest.id).await()
                
                // Update last sync time
                preferencesManager.updateLastSyncTime(System.currentTimeMillis())
                
                // Log analytics event
                analyticsService.logEvent(
                    AnalyticsEvent.CustomEvent(
                        name = "data_sync_completed",
                        parameters = mapOf(
                            "trigger" -> "manual",
                            "network_type" -> networkStateMonitor.networkState.value.networkType.name
                        )
                    )
                )
                
                _syncState.update { 
                    it.copy(
                        isSyncing = false, 
                        progress = 1f,
                        lastSyncTime = System.currentTimeMillis()
                    ) 
                }
                
                SyncResult.Success(
                    itemsSynced = 0, // TODO: Get actual count from worker
                    duration = 0 // TODO: Calculate actual duration
                )
            } catch (e: Exception) {
                _syncState.update { it.copy(isSyncing = false, progress = 0f) }
                
                SyncResult.Failure(
                    SyncError(
                        code = "SYNC_FAILED",
                        message = e.message ?: "Unknown error occurred",
                        isRetryable = true
                    )
                )
            }
        }
    }
    
    override fun scheduleSync(delayMillis: Long) {
        val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "scheduled_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
    
    override fun cancelPendingSync() {
        workManager.cancelUniqueWork("scheduled_sync")
        workManager.cancelUniqueWork("immediate_sync")
    }
    
    override fun isSyncing(): Boolean = syncState.value.isSyncing
    
    override suspend fun getLastSyncTime(): Long? {
        return preferencesManager.userPreferences.first().lastSyncTime.takeIf { it > 0 }
    }
    
    override suspend fun clearSyncData() {
        cancelPendingSync()
        preferencesManager.updateLastSyncTime(0)
        _syncState.value = SyncState()
    }
    
    fun onCleared() {
        scope.cancel()
    }
}