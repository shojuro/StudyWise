package com.studywise.ai.data.repository

import com.studywise.ai.data.local.dao.SyncDao
import com.studywise.ai.data.local.entity.sync.SyncConflictEntity
import com.studywise.ai.data.local.entity.sync.SyncOperationEntity
import com.studywise.ai.data.remote.api.StudyWiseApi
import com.studywise.ai.domain.repository.*
import com.studywise.ai.domain.service.ConflictResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SyncRepository
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val syncDao: SyncDao,
    private val api: StudyWiseApi,
    private val conflictResolver: ConflictResolver
) : SyncRepository {
    
    override suspend fun getPendingSyncOperations(): List<SyncOperation> {
        return syncDao.getPendingOperations().map { it.toDomainModel() }
    }
    
    override suspend fun enqueueSyncOperation(operation: SyncOperation) {
        // Check if there's already a pending operation for this entity
        val existingOperation = syncDao.getLatestOperationForEntity(
            operation.entityType,
            operation.entityId
        )
        
        if (existingOperation != null && existingOperation.status == SyncOperationStatus.PENDING) {
            // Replace the existing operation with the new one
            syncDao.replaceOperationForEntity(
                operation.entityType,
                operation.entityId,
                operation.toEntity()
            )
        } else {
            syncDao.insertSyncOperation(operation.toEntity())
        }
    }
    
    override suspend fun markOperationCompleted(operationId: String) {
        syncDao.updateOperationStatus(operationId, SyncOperationStatus.COMPLETED)
    }
    
    override suspend fun markOperationFailed(
        operationId: String,
        error: String,
        isRetryable: Boolean
    ) {
        syncDao.markOperationFailed(operationId, error, isRetryable)
    }
    
    override suspend fun getRetryableOperations(): List<SyncOperation> {
        return syncDao.getRetryableOperations()
            .filter { it.retryCount < MAX_RETRY_COUNT }
            .map { it.toDomainModel() }
    }
    
    override suspend fun clearCompletedOperations() {
        syncDao.deleteAllCompletedOperations()
    }
    
    override suspend fun syncLocalChanges(): SyncBatchResult {
        val pendingOperations = getPendingSyncOperations()
        var successCount = 0
        var failureCount = 0
        val conflicts = mutableListOf<SyncConflict>()
        val errors = mutableListOf<String>()
        
        for (operation in pendingOperations) {
            try {
                // Mark as in progress
                syncDao.updateOperationStatus(operation.id, SyncOperationStatus.IN_PROGRESS)
                
                // Send to server based on operation type
                val result = when (operation.type) {
                    SyncOperationType.CREATE -> api.createEntity(operation.entityType, operation.data)
                    SyncOperationType.UPDATE -> api.updateEntity(operation.entityType, operation.entityId, operation.data)
                    SyncOperationType.DELETE -> api.deleteEntity(operation.entityType, operation.entityId)
                }
                
                when (result) {
                    is com.studywise.ai.data.remote.dto.SyncResult.Success -> {
                        markOperationCompleted(operation.id)
                        successCount++
                    }
                    is com.studywise.ai.data.remote.dto.SyncResult.Conflict -> {
                        // Create conflict record
                        val conflict = SyncConflict(
                            id = UUID.randomUUID().toString(),
                            entityType = operation.entityType,
                            entityId = operation.entityId,
                            localData = operation.data,
                            serverData = result.serverData,
                            localTimestamp = operation.timestamp,
                            serverTimestamp = result.serverTimestamp,
                            conflictType = determineConflictType(operation.type, result.serverDeleted)
                        )
                        syncDao.insertSyncConflict(conflict.toEntity())
                        conflicts.add(conflict)
                        
                        // Mark operation as failed but retryable
                        markOperationFailed(operation.id, "Conflict detected", true)
                        failureCount++
                    }
                    is com.studywise.ai.data.remote.dto.SyncResult.Error -> {
                        markOperationFailed(operation.id, result.message, result.isRetryable)
                        errors.add("${operation.entityType}:${operation.entityId} - ${result.message}")
                        failureCount++
                    }
                }
            } catch (e: Exception) {
                markOperationFailed(operation.id, e.message ?: "Unknown error", true)
                errors.add("${operation.entityType}:${operation.entityId} - ${e.message}")
                failureCount++
            }
        }
        
        return SyncBatchResult(
            success = failureCount == 0,
            itemsSynced = successCount,
            itemsFailed = failureCount,
            conflicts = conflicts,
            errors = errors
        )
    }
    
    override suspend fun syncServerChanges(lastSyncTimestamp: Long): SyncBatchResult {
        return try {
            val serverChanges = api.getChanges(lastSyncTimestamp)
            var successCount = 0
            val conflicts = mutableListOf<SyncConflict>()
            val errors = mutableListOf<String>()
            
            for (change in serverChanges.changes) {
                try {
                    // Check if we have local changes for this entity
                    val localOperation = syncDao.getLatestOperationForEntity(
                        change.entityType,
                        change.entityId
                    )
                    
                    if (localOperation != null && localOperation.status == SyncOperationStatus.PENDING) {
                        // We have a conflict
                        val conflict = SyncConflict(
                            id = UUID.randomUUID().toString(),
                            entityType = change.entityType,
                            entityId = change.entityId,
                            localData = localOperation.data,
                            serverData = change.data,
                            localTimestamp = localOperation.timestamp,
                            serverTimestamp = change.timestamp,
                            conflictType = ConflictType.UPDATE_UPDATE
                        )
                        syncDao.insertSyncConflict(conflict.toEntity())
                        conflicts.add(conflict)
                    } else {
                        // No conflict, apply server change
                        applyServerChange(change)
                        successCount++
                    }
                } catch (e: Exception) {
                    errors.add("Failed to apply server change for ${change.entityType}:${change.entityId} - ${e.message}")
                }
            }
            
            SyncBatchResult(
                success = errors.isEmpty(),
                itemsSynced = successCount,
                itemsFailed = errors.size,
                conflicts = conflicts,
                errors = errors
            )
        } catch (e: Exception) {
            SyncBatchResult(
                success = false,
                itemsSynced = 0,
                itemsFailed = 0,
                conflicts = emptyList(),
                errors = listOf("Failed to fetch server changes: ${e.message}")
            )
        }
    }
    
    override suspend fun resolveConflict(
        conflict: SyncConflict,
        resolution: ConflictResolution
    ): ConflictResolutionResult {
        return try {
            val resolvedData = conflictResolver.applyResolution(conflict, resolution)
            
            // Apply the resolution
            when (resolution) {
                is ConflictResolution.KeepLocal -> {
                    // Re-queue the local operation
                    val operation = SyncOperation(
                        id = UUID.randomUUID().toString(),
                        type = SyncOperationType.UPDATE,
                        entityType = conflict.entityType,
                        entityId = conflict.entityId,
                        data = resolvedData.data,
                        timestamp = System.currentTimeMillis()
                    )
                    enqueueSyncOperation(operation)
                }
                is ConflictResolution.KeepServer -> {
                    // Apply server data locally
                    applyServerData(conflict.entityType, conflict.entityId, resolvedData.data)
                }
                is ConflictResolution.Merge -> {
                    // Queue the merged data for sync
                    val operation = SyncOperation(
                        id = UUID.randomUUID().toString(),
                        type = SyncOperationType.UPDATE,
                        entityType = conflict.entityType,
                        entityId = conflict.entityId,
                        data = resolvedData.data,
                        timestamp = System.currentTimeMillis()
                    )
                    enqueueSyncOperation(operation)
                }
                is ConflictResolution.KeepBoth -> {
                    // Create a new entity with server data
                    val newEntityId = UUID.randomUUID().toString()
                    applyServerData(conflict.entityType, newEntityId, conflict.serverData)
                }
            }
            
            // Mark conflict as resolved
            syncDao.markConflictResolved(
                conflict.id,
                resolution.javaClass.simpleName,
                resolvedData.data
            )
            
            ConflictResolutionResult(
                success = true,
                resolvedData = resolvedData.data
            )
        } catch (e: Exception) {
            ConflictResolutionResult(
                success = false,
                error = e.message
            )
        }
    }
    
    override suspend fun getUnresolvedConflicts(): List<SyncConflict> {
        return syncDao.getUnresolvedConflicts().map { it.toDomainModel() }
    }
    
    override fun observeSyncQueueSize(): Flow<Int> {
        return syncDao.observePendingOperationCount()
    }
    
    override suspend fun getSyncStatistics(): SyncStatistics {
        val stats = syncDao.getSyncStatistics()
        return SyncStatistics(
            totalOperations = stats.totalOperations,
            pendingOperations = stats.pendingOperations,
            completedOperations = stats.completedOperations,
            failedOperations = stats.failedOperations,
            conflicts = stats.unresolvedConflicts,
            lastSuccessfulSync = stats.lastSuccessfulSync,
            averageSyncTime = 0 // TODO: Calculate from operation timestamps
        )
    }
    
    private fun determineConflictType(operationType: SyncOperationType, serverDeleted: Boolean): ConflictType {
        return when (operationType) {
            SyncOperationType.UPDATE -> if (serverDeleted) ConflictType.UPDATE_DELETE else ConflictType.UPDATE_UPDATE
            SyncOperationType.DELETE -> ConflictType.DELETE_UPDATE
            SyncOperationType.CREATE -> ConflictType.CREATE_CREATE
        }
    }
    
    private suspend fun applyServerChange(change: com.studywise.ai.data.remote.dto.ServerChange) {
        // TODO: Apply the server change to local database based on entity type
        // This would involve parsing the JSON data and updating the appropriate table
    }
    
    private suspend fun applyServerData(entityType: String, entityId: String, data: String) {
        // TODO: Apply server data to local database
        // This would involve parsing the JSON data and updating the appropriate table
    }
    
    companion object {
        private const val MAX_RETRY_COUNT = 3
    }
}

// Extension functions for conversion
private fun SyncOperationEntity.toDomainModel() = SyncOperation(
    id = id,
    type = type,
    entityType = entityType,
    entityId = entityId,
    data = data,
    timestamp = timestamp,
    retryCount = retryCount,
    status = status,
    error = error,
    isRetryable = isRetryable
)

private fun SyncOperation.toEntity() = SyncOperationEntity(
    id = id,
    type = type,
    entityType = entityType,
    entityId = entityId,
    data = data,
    timestamp = timestamp,
    retryCount = retryCount,
    status = status,
    error = error,
    isRetryable = isRetryable
)

private fun SyncConflictEntity.toDomainModel() = SyncConflict(
    id = id,
    entityType = entityType,
    entityId = entityId,
    localData = localData,
    serverData = serverData,
    localTimestamp = localTimestamp,
    serverTimestamp = serverTimestamp,
    conflictType = conflictType
)

private fun SyncConflict.toEntity() = SyncConflictEntity(
    id = id,
    entityType = entityType,
    entityId = entityId,
    localData = localData,
    serverData = serverData,
    localTimestamp = localTimestamp,
    serverTimestamp = serverTimestamp,
    conflictType = conflictType
)