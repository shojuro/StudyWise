package com.studywise.ai.domain.service

import com.studywise.ai.domain.repository.ConflictResolution
import com.studywise.ai.domain.repository.ConflictType
import com.studywise.ai.domain.repository.SyncConflict

/**
 * Service interface for resolving sync conflicts
 */
interface ConflictResolver {
    
    /**
     * Determine the appropriate resolution strategy for a conflict
     */
    suspend fun determineResolutionStrategy(conflict: SyncConflict): ConflictResolution
    
    /**
     * Apply a resolution strategy to merge conflicting data
     */
    suspend fun applyResolution(
        conflict: SyncConflict,
        resolution: ConflictResolution
    ): ResolvedData
    
    /**
     * Check if a conflict can be automatically resolved
     */
    fun canAutoResolve(conflict: SyncConflict): Boolean
    
    /**
     * Get resolution options for a specific conflict
     */
    fun getResolutionOptions(conflict: SyncConflict): List<ResolutionOption>
}

/**
 * Result of applying a conflict resolution
 */
data class ResolvedData(
    val data: String,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Available resolution options for a conflict
 */
data class ResolutionOption(
    val resolution: ConflictResolution,
    val title: String,
    val description: String,
    val isRecommended: Boolean = false
)

/**
 * Default implementation of ConflictResolver
 */
class DefaultConflictResolver : ConflictResolver {
    
    override suspend fun determineResolutionStrategy(conflict: SyncConflict): ConflictResolution {
        // Default strategy based on conflict type and timestamps
        return when (conflict.conflictType) {
            ConflictType.UPDATE_UPDATE -> {
                // For update-update conflicts, use last-write-wins by default
                if (conflict.localTimestamp > conflict.serverTimestamp) {
                    ConflictResolution.KeepLocal
                } else {
                    ConflictResolution.KeepServer
                }
            }
            ConflictType.UPDATE_DELETE -> {
                // Server deleted, respect deletion
                ConflictResolution.KeepServer
            }
            ConflictType.DELETE_UPDATE -> {
                // Local deleted, keep deletion
                ConflictResolution.KeepLocal
            }
            ConflictType.CREATE_CREATE -> {
                // Both created, keep both as duplicates
                ConflictResolution.KeepBoth
            }
        }
    }
    
    override suspend fun applyResolution(
        conflict: SyncConflict,
        resolution: ConflictResolution
    ): ResolvedData {
        return when (resolution) {
            is ConflictResolution.KeepLocal -> ResolvedData(
                data = conflict.localData,
                metadata = mapOf("resolution" to "local", "timestamp" to conflict.localTimestamp)
            )
            is ConflictResolution.KeepServer -> ResolvedData(
                data = conflict.serverData,
                metadata = mapOf("resolution" to "server", "timestamp" to conflict.serverTimestamp)
            )
            is ConflictResolution.Merge -> ResolvedData(
                data = resolution.mergedData,
                metadata = mapOf("resolution" to "merged", "timestamp" to System.currentTimeMillis())
            )
            is ConflictResolution.KeepBoth -> ResolvedData(
                data = conflict.localData, // Keep local, server will be created as duplicate
                metadata = mapOf(
                    "resolution" to "both",
                    "duplicate_data" to conflict.serverData
                )
            )
        }
    }
    
    override fun canAutoResolve(conflict: SyncConflict): Boolean {
        // Auto-resolve if timestamps differ by more than 5 minutes
        val timeDiff = kotlin.math.abs(conflict.localTimestamp - conflict.serverTimestamp)
        return when (conflict.conflictType) {
            ConflictType.UPDATE_UPDATE -> timeDiff > 5 * 60 * 1000 // 5 minutes
            ConflictType.UPDATE_DELETE -> true // Always auto-resolve to deletion
            ConflictType.DELETE_UPDATE -> true // Always auto-resolve to deletion
            ConflictType.CREATE_CREATE -> false // Never auto-resolve duplicates
        }
    }
    
    override fun getResolutionOptions(conflict: SyncConflict): List<ResolutionOption> {
        return when (conflict.conflictType) {
            ConflictType.UPDATE_UPDATE -> listOf(
                ResolutionOption(
                    resolution = ConflictResolution.KeepLocal,
                    title = "Keep Local Changes",
                    description = "Use your local version and discard server changes",
                    isRecommended = conflict.localTimestamp > conflict.serverTimestamp
                ),
                ResolutionOption(
                    resolution = ConflictResolution.KeepServer,
                    title = "Keep Server Changes",
                    description = "Use the server version and discard your local changes",
                    isRecommended = conflict.serverTimestamp > conflict.localTimestamp
                ),
                ResolutionOption(
                    resolution = ConflictResolution.Merge(""),
                    title = "Merge Changes",
                    description = "Manually merge both versions",
                    isRecommended = false
                )
            )
            ConflictType.UPDATE_DELETE -> listOf(
                ResolutionOption(
                    resolution = ConflictResolution.KeepLocal,
                    title = "Restore Item",
                    description = "Keep your local version and restore the deleted item",
                    isRecommended = false
                ),
                ResolutionOption(
                    resolution = ConflictResolution.KeepServer,
                    title = "Accept Deletion",
                    description = "Accept the server deletion and remove local item",
                    isRecommended = true
                )
            )
            ConflictType.DELETE_UPDATE -> listOf(
                ResolutionOption(
                    resolution = ConflictResolution.KeepLocal,
                    title = "Confirm Deletion",
                    description = "Delete the item as intended locally",
                    isRecommended = true
                ),
                ResolutionOption(
                    resolution = ConflictResolution.KeepServer,
                    title = "Restore Item",
                    description = "Cancel deletion and keep the server version",
                    isRecommended = false
                )
            )
            ConflictType.CREATE_CREATE -> listOf(
                ResolutionOption(
                    resolution = ConflictResolution.KeepLocal,
                    title = "Keep Local Version",
                    description = "Use your local version only",
                    isRecommended = false
                ),
                ResolutionOption(
                    resolution = ConflictResolution.KeepServer,
                    title = "Keep Server Version",
                    description = "Use the server version only",
                    isRecommended = false
                ),
                ResolutionOption(
                    resolution = ConflictResolution.KeepBoth,
                    title = "Keep Both Versions",
                    description = "Create duplicates with both versions",
                    isRecommended = true
                )
            )
        }
    }
}