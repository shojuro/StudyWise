package com.studywise.ai.data.local.entity.sync

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.studywise.ai.domain.repository.SyncOperationStatus
import com.studywise.ai.domain.repository.SyncOperationType

/**
 * Entity for storing sync operations in the queue
 */
@Entity(
    tableName = "sync_operations",
    indices = [
        Index(value = ["status"]),
        Index(value = ["entityType", "entityId"]),
        Index(value = ["timestamp"])
    ]
)
data class SyncOperationEntity(
    @PrimaryKey
    val id: String,
    val type: SyncOperationType,
    val entityType: String,
    val entityId: String,
    val data: String, // JSON representation of the entity
    val timestamp: Long,
    val retryCount: Int = 0,
    val status: SyncOperationStatus = SyncOperationStatus.PENDING,
    val error: String? = null,
    val isRetryable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)