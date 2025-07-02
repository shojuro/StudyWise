package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.sync.SyncConflictEntity
import com.studywise.ai.data.local.entity.sync.SyncOperationEntity
import com.studywise.ai.domain.repository.SyncOperationStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO for sync operations and conflicts
 */
@Dao
interface SyncDao {
    
    // Sync Operations
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperation(operation: SyncOperationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperations(operations: List<SyncOperationEntity>)
    
    @Update
    suspend fun updateSyncOperation(operation: SyncOperationEntity)
    
    @Query("SELECT * FROM sync_operations WHERE status = :status ORDER BY timestamp ASC")
    suspend fun getOperationsByStatus(status: SyncOperationStatus): List<SyncOperationEntity>
    
    @Query("SELECT * FROM sync_operations WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingOperations(): List<SyncOperationEntity>
    
    @Query("SELECT * FROM sync_operations WHERE status = 'FAILED' AND isRetryable = 1 ORDER BY timestamp ASC")
    suspend fun getRetryableOperations(): List<SyncOperationEntity>
    
    @Query("UPDATE sync_operations SET status = :status, updatedAt = :timestamp WHERE id = :operationId")
    suspend fun updateOperationStatus(operationId: String, status: SyncOperationStatus, timestamp: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE sync_operations 
        SET status = 'FAILED', error = :error, isRetryable = :isRetryable, 
            retryCount = retryCount + 1, updatedAt = :timestamp 
        WHERE id = :operationId
    """)
    suspend fun markOperationFailed(
        operationId: String, 
        error: String, 
        isRetryable: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("DELETE FROM sync_operations WHERE status = 'COMPLETED' AND updatedAt < :beforeTimestamp")
    suspend fun deleteCompletedOperationsBefore(beforeTimestamp: Long)
    
    @Query("DELETE FROM sync_operations WHERE status = 'COMPLETED'")
    suspend fun deleteAllCompletedOperations()
    
    @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'PENDING'")
    fun observePendingOperationCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM sync_operations WHERE status = :status")
    suspend fun getOperationCountByStatus(status: SyncOperationStatus): Int
    
    @Query("SELECT * FROM sync_operations WHERE entityType = :entityType AND entityId = :entityId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestOperationForEntity(entityType: String, entityId: String): SyncOperationEntity?
    
    @Transaction
    suspend fun replaceOperationForEntity(entityType: String, entityId: String, newOperation: SyncOperationEntity) {
        // Delete existing pending operations for this entity
        deletePendingOperationsForEntity(entityType, entityId)
        // Insert the new operation
        insertSyncOperation(newOperation)
    }
    
    @Query("DELETE FROM sync_operations WHERE entityType = :entityType AND entityId = :entityId AND status = 'PENDING'")
    suspend fun deletePendingOperationsForEntity(entityType: String, entityId: String)
    
    // Sync Conflicts
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncConflict(conflict: SyncConflictEntity)
    
    @Update
    suspend fun updateSyncConflict(conflict: SyncConflictEntity)
    
    @Query("SELECT * FROM sync_conflicts WHERE isResolved = 0 ORDER BY createdAt DESC")
    suspend fun getUnresolvedConflicts(): List<SyncConflictEntity>
    
    @Query("SELECT * FROM sync_conflicts WHERE id = :conflictId")
    suspend fun getConflictById(conflictId: String): SyncConflictEntity?
    
    @Query("UPDATE sync_conflicts SET isResolved = 1, resolutionType = :resolutionType, resolvedData = :resolvedData, resolvedAt = :resolvedAt WHERE id = :conflictId")
    suspend fun markConflictResolved(
        conflictId: String,
        resolutionType: String,
        resolvedData: String?,
        resolvedAt: Long = System.currentTimeMillis()
    )
    
    @Query("DELETE FROM sync_conflicts WHERE isResolved = 1 AND resolvedAt < :beforeTimestamp")
    suspend fun deleteResolvedConflictsBefore(beforeTimestamp: Long)
    
    @Query("SELECT COUNT(*) FROM sync_conflicts WHERE isResolved = 0")
    suspend fun getUnresolvedConflictCount(): Int
    
    // Statistics
    
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM sync_operations) as totalOperations,
            (SELECT COUNT(*) FROM sync_operations WHERE status = 'PENDING') as pendingOperations,
            (SELECT COUNT(*) FROM sync_operations WHERE status = 'COMPLETED') as completedOperations,
            (SELECT COUNT(*) FROM sync_operations WHERE status = 'FAILED') as failedOperations,
            (SELECT COUNT(*) FROM sync_conflicts WHERE isResolved = 0) as unresolvedConflicts,
            (SELECT MAX(updatedAt) FROM sync_operations WHERE status = 'COMPLETED') as lastSuccessfulSync
    """)
    suspend fun getSyncStatistics(): SyncStatisticsDto
    
    data class SyncStatisticsDto(
        val totalOperations: Int,
        val pendingOperations: Int,
        val completedOperations: Int,
        val failedOperations: Int,
        val unresolvedConflicts: Int,
        val lastSuccessfulSync: Long?
    )
}