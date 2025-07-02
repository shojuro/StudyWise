package com.studywise.ai.data.local.entity.base

/**
 * Base interface for entities that support version tracking for sync
 */
interface VersionedEntity {
    val version: Int
    val lastModified: Long
    val syncStatus: SyncStatus
}

/**
 * Sync status for entities
 */
enum class SyncStatus {
    PENDING,    // Local changes not yet synced
    SYNCED,     // Synced with server
    CONFLICT,   // Has sync conflict
    FAILED      // Sync failed
}

/**
 * Base class for versioned entities
 */
abstract class BaseVersionedEntity : VersionedEntity {
    abstract override val version: Int
    abstract override val lastModified: Long
    abstract override val syncStatus: SyncStatus
    
    /**
     * Check if entity has local changes
     */
    fun hasLocalChanges(): Boolean = syncStatus == SyncStatus.PENDING
    
    /**
     * Check if entity has sync conflict
     */
    fun hasConflict(): Boolean = syncStatus == SyncStatus.CONFLICT
    
    /**
     * Create a copy with incremented version
     */
    fun incrementVersion(): Int = version + 1
}