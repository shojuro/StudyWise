package com.studywise.ai.data.local.entity.sync

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.studywise.ai.domain.repository.ConflictType

/**
 * Entity for storing unresolved sync conflicts
 */
@Entity(
    tableName = "sync_conflicts",
    indices = [
        Index(value = ["entityType", "entityId"]),
        Index(value = ["createdAt"])
    ]
)
data class SyncConflictEntity(
    @PrimaryKey
    val id: String,
    val entityType: String,
    val entityId: String,
    val localData: String, // JSON representation
    val serverData: String, // JSON representation
    val localTimestamp: Long,
    val serverTimestamp: Long,
    val conflictType: ConflictType,
    val isResolved: Boolean = false,
    val resolutionType: String? = null, // Type of resolution applied
    val resolvedData: String? = null, // Final resolved data
    val resolvedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)