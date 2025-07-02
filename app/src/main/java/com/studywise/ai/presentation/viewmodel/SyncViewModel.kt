package com.studywise.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.service.SyncManager
import com.studywise.ai.domain.service.SyncResult
import com.studywise.ai.domain.service.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing sync state and operations
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {
    
    /**
     * Current sync state
     */
    val syncState: StateFlow<SyncState> = syncManager.syncState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncState()
        )
    
    /**
     * Trigger an immediate sync
     */
    fun syncNow() {
        viewModelScope.launch {
            val result = syncManager.syncNow()
            
            // Handle sync result
            when (result) {
                is SyncResult.Success -> {
                    // Sync completed successfully
                }
                is SyncResult.PartialSuccess -> {
                    // Some items failed to sync
                }
                is SyncResult.Failure -> {
                    // Sync failed completely
                }
                SyncResult.NoDataToSync -> {
                    // Nothing to sync
                }
            }
        }
    }
    
    /**
     * Schedule a sync for later
     */
    fun scheduleSync(delayMillis: Long = 0) {
        syncManager.scheduleSync(delayMillis)
    }
    
    /**
     * Cancel any pending sync operations
     */
    fun cancelPendingSync() {
        syncManager.cancelPendingSync()
    }
}