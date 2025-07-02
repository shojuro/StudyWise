package com.studywise.ai.domain.repository

import com.studywise.ai.domain.model.sync.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing data synchronization
 */
interface SyncRepository {
    
    /**
     * Get all pending sync operations
     */
    suspend fun getPendingSyncOperations(): List<SyncOperation>
    
    /**
     * Add a new sync operation to the queue
     */
    suspend fun enqueueSyncOperation(operation: SyncOperation)
    
    /**
     * Mark an operation as completed
     */
    suspend fun markOperationCompleted(operationId: String)
    
    /**
     * Mark an operation as failed
     */
    suspend fun markOperationFailed(operationId: String, error: String, isRetryable: Boolean)
    
    /**
     * Get failed operations that can be retried
     */
    suspend fun getRetryableOperations(): List<SyncOperation>
    
    /**
     * Clear all completed operations
     */
    suspend fun clearCompletedOperations()
    
    /**
     * Sync local changes to server
     */
    suspend fun syncLocalChanges(): SyncBatchResult
    
    /**
     * Sync server changes to local
     */
    suspend fun syncServerChanges(lastSyncTimestamp: Long): SyncBatchResult
    
    /**
     * Resolve conflicts between local and server data
     */
    suspend fun resolveConflict(
        conflict: SyncConflict,
        resolution: ConflictResolution
    ): ConflictResolutionResult
    
    /**
     * Get all unresolved conflicts
     */
    suspend fun getUnresolvedConflicts(): List<SyncConflict>
    
    /**
     * Observe sync queue size
     */
    fun observeSyncQueueSize(): Flow<Int>
    
    /**
     * Get sync statistics
     */
    suspend fun getSyncStatistics(): SyncStatistics
}

/**
 * Represents a sync operation in the queue
 */
data class SyncOperation(
    val id: String,
    val type: SyncOperationType,
    val entityType: String,
    val entityId: String,
    val data: String, // JSON representation
    val timestamp: Long,
    val retryCount: Int = 0,
    val status: SyncOperationStatus = SyncOperationStatus.PENDING,
    val error: String? = null,
    val isRetryable: Boolean = true
)

/**
 * Types of sync operations
 */
enum class SyncOperationType {
    CREATE,
    UPDATE,
    DELETE
}

/**
 * Status of a sync operation
 */
enum class SyncOperationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

/**
 * Represents a sync conflict
 */
data class SyncConflict(
    val id: String,
    val entityType: String,
    val entityId: String,
    val localData: String,
    val serverData: String,
    val localTimestamp: Long,
    val serverTimestamp: Long,
    val conflictType: ConflictType
)

/**
 * Types of conflicts
 */
enum class ConflictType {
    UPDATE_UPDATE, // Both local and server updated
    UPDATE_DELETE, // Local updated, server deleted
    DELETE_UPDATE, // Local deleted, server updated
    CREATE_CREATE  // Both created with same ID
}

/**
 * Conflict resolution strategies
 */
sealed class ConflictResolution {
    object KeepLocal : ConflictResolution()
    object KeepServer : ConflictResolution()
    data class Merge(val mergedData: String) : ConflictResolution()
    object KeepBoth : ConflictResolution() // Create duplicate
}

/**
 * Result of conflict resolution
 */
data class ConflictResolutionResult(
    val success: Boolean,
    val resolvedData: String? = null,
    val error: String? = null
)

/**
 * Result of a batch sync operation
 */
data class SyncBatchResult(
    val success: Boolean,
    val itemsSynced: Int,
    val itemsFailed: Int,
    val conflicts: List<SyncConflict>,
    val errors: List<String>
)

/**
 * Sync statistics
 */
data class SyncStatistics(
    val totalOperations: Int,
    val pendingOperations: Int,
    val completedOperations: Int,
    val failedOperations: Int,
    val conflicts: Int,
    val lastSuccessfulSync: Long?,
    val averageSyncTime: Long
)