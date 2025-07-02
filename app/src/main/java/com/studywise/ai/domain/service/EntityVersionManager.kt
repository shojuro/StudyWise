package com.studywise.ai.domain.service

import com.studywise.ai.data.local.entity.base.SyncStatus
import com.studywise.ai.data.local.entity.base.VersionedEntity

/**
 * Service for managing entity versions during sync operations
 */
interface EntityVersionManager {
    
    /**
     * Compare versions to determine if server version is newer
     */
    fun isServerVersionNewer(localVersion: Int, serverVersion: Int): Boolean
    
    /**
     * Check if versions are in conflict
     */
    fun hasVersionConflict(
        localEntity: VersionedEntity,
        serverVersion: Int,
        serverLastModified: Long
    ): Boolean
    
    /**
     * Determine sync action based on version comparison
     */
    fun determineSyncAction(
        localEntity: VersionedEntity?,
        serverVersion: Int,
        serverLastModified: Long
    ): SyncAction
    
    /**
     * Create a version stamp for an entity
     */
    fun createVersionStamp(): VersionStamp
}

/**
 * Sync actions based on version comparison
 */
enum class SyncAction {
    UPLOAD,          // Local is newer, upload to server
    DOWNLOAD,        // Server is newer, download from server
    CONFLICT,        // Both modified, needs resolution
    NO_ACTION,       // Already in sync
    CREATE_LOCAL,    // Doesn't exist locally, create
    CREATE_REMOTE,   // Doesn't exist on server, create
    DELETE_LOCAL,    // Deleted on server, remove locally
    DELETE_REMOTE    // Deleted locally, remove on server
}

/**
 * Version stamp for tracking entity versions
 */
data class VersionStamp(
    val version: Int,
    val lastModified: Long,
    val checksum: String? = null
)

/**
 * Default implementation of EntityVersionManager
 */
class DefaultEntityVersionManager : EntityVersionManager {
    
    override fun isServerVersionNewer(localVersion: Int, serverVersion: Int): Boolean {
        return serverVersion > localVersion
    }
    
    override fun hasVersionConflict(
        localEntity: VersionedEntity,
        serverVersion: Int,
        serverLastModified: Long
    ): Boolean {
        // Conflict if both have been modified since last sync
        return localEntity.syncStatus == SyncStatus.PENDING && 
               serverVersion > localEntity.version &&
               localEntity.lastModified > serverLastModified - CONFLICT_WINDOW_MS
    }
    
    override fun determineSyncAction(
        localEntity: VersionedEntity?,
        serverVersion: Int,
        serverLastModified: Long
    ): SyncAction {
        return when {
            // Entity doesn't exist locally
            localEntity == null -> SyncAction.CREATE_LOCAL
            
            // Local entity has pending changes
            localEntity.syncStatus == SyncStatus.PENDING -> {
                when {
                    // Server version is newer - conflict
                    serverVersion > localEntity.version -> SyncAction.CONFLICT
                    // Local version is newer - upload
                    else -> SyncAction.UPLOAD
                }
            }
            
            // Local entity is synced
            localEntity.syncStatus == SyncStatus.SYNCED -> {
                when {
                    // Server version is newer - download
                    serverVersion > localEntity.version -> SyncAction.DOWNLOAD
                    // Versions match - no action
                    serverVersion == localEntity.version -> SyncAction.NO_ACTION
                    // Local version is somehow newer (shouldn't happen) - upload
                    else -> SyncAction.UPLOAD
                }
            }
            
            // Local entity has conflict
            localEntity.syncStatus == SyncStatus.CONFLICT -> SyncAction.CONFLICT
            
            // Local entity failed sync
            localEntity.syncStatus == SyncStatus.FAILED -> SyncAction.UPLOAD
            
            else -> SyncAction.NO_ACTION
        }
    }
    
    override fun createVersionStamp(): VersionStamp {
        return VersionStamp(
            version = 1,
            lastModified = System.currentTimeMillis()
        )
    }
    
    companion object {
        // 5 minute window for conflict detection
        private const val CONFLICT_WINDOW_MS = 5 * 60 * 1000L
    }
}