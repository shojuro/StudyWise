package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.studywise.ai.data.local.entity.base.SyncStatus
import com.studywise.ai.data.local.entity.base.VersionedEntity
import java.util.Date

/**
 * Example of UserEntity with version tracking for sync
 * This shows how to add versioning to existing entities
 */
@Entity(tableName = "versioned_users")
data class VersionedUserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val password: String, // Will be hashed
    val role: UserRole,
    val grade: Int? = null, // Only for students
    val createdAt: Date,
    val lastLoginAt: Date? = null,
    val isActive: Boolean = true,
    val parentId: String? = null, // For linking students to parents
    
    // Version tracking fields
    override val version: Int = 1,
    override val lastModified: Long = System.currentTimeMillis(),
    override val syncStatus: SyncStatus = SyncStatus.PENDING
) : VersionedEntity {
    
    /**
     * Create a copy with updated fields and incremented version
     */
    fun updateWith(
        name: String = this.name,
        email: String = this.email,
        password: String = this.password,
        role: UserRole = this.role,
        grade: Int? = this.grade,
        lastLoginAt: Date? = this.lastLoginAt,
        isActive: Boolean = this.isActive,
        parentId: String? = this.parentId
    ): VersionedUserEntity {
        return copy(
            name = name,
            email = email,
            password = password,
            role = role,
            grade = grade,
            lastLoginAt = lastLoginAt,
            isActive = isActive,
            parentId = parentId,
            version = version + 1,
            lastModified = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
    }
    
    /**
     * Mark as synced with server
     */
    fun markAsSynced(): VersionedUserEntity {
        return copy(syncStatus = SyncStatus.SYNCED)
    }
    
    /**
     * Mark as having conflict
     */
    fun markAsConflict(): VersionedUserEntity {
        return copy(syncStatus = SyncStatus.CONFLICT)
    }
    
    /**
     * Mark as sync failed
     */
    fun markAsFailed(): VersionedUserEntity {
        return copy(syncStatus = SyncStatus.FAILED)
    }
}

/**
 * Extension function to convert regular UserEntity to VersionedUserEntity
 */
fun UserEntity.toVersioned(): VersionedUserEntity {
    return VersionedUserEntity(
        id = id,
        name = name,
        email = email,
        password = password,
        role = role,
        grade = grade,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt,
        isActive = isActive,
        parentId = parentId
    )
}