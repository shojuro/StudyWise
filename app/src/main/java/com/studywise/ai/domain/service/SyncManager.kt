package com.studywise.ai.domain.service

import kotlinx.coroutines.flow.Flow

/**
 * Service interface for managing data synchronization
 */
interface SyncManager {
    
    /**
     * Current sync state
     */
    val syncState: Flow<SyncState>
    
    /**
     * Trigger an immediate sync
     */
    suspend fun syncNow(): SyncResult
    
    /**
     * Schedule a sync to happen later
     */
    fun scheduleSync(delayMillis: Long = 0)
    
    /**
     * Cancel any pending sync operations
     */
    fun cancelPendingSync()
    
    /**
     * Check if a sync is currently in progress
     */
    fun isSyncing(): Boolean
    
    /**
     * Get the last sync timestamp
     */
    suspend fun getLastSyncTime(): Long?
    
    /**
     * Clear all sync data and reset
     */
    suspend fun clearSyncData()
}

/**
 * Represents the current state of synchronization
 */
data class SyncState(
    val isSyncing: Boolean = false,
    val progress: Float = 0f,
    val currentOperation: String? = null,
    val lastSyncTime: Long? = null,
    val pendingSyncCount: Int = 0,
    val failedSyncCount: Int = 0
)

/**
 * Result of a sync operation
 */
sealed class SyncResult {
    data class Success(
        val itemsSynced: Int,
        val duration: Long
    ) : SyncResult()
    
    data class PartialSuccess(
        val itemsSynced: Int,
        val itemsFailed: Int,
        val errors: List<SyncError>
    ) : SyncResult()
    
    data class Failure(
        val error: SyncError
    ) : SyncResult()
    
    object NoDataToSync : SyncResult()
}

/**
 * Represents a sync error
 */
data class SyncError(
    val code: String,
    val message: String,
    val entityType: String? = null,
    val entityId: String? = null,
    val isRetryable: Boolean = true
)