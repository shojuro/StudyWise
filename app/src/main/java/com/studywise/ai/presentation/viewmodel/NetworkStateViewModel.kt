package com.studywise.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.service.NetworkState
import com.studywise.ai.domain.service.NetworkStateMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for managing network state across the application
 */
@HiltViewModel
class NetworkStateViewModel @Inject constructor(
    private val networkStateMonitor: NetworkStateMonitor
) : ViewModel() {
    
    /**
     * Current network state as a StateFlow
     */
    val networkState: StateFlow<NetworkState> = networkStateMonitor.networkState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkStateMonitor.networkState.value
        )
    
    /**
     * Convenience properties for UI
     */
    val isOnline: Boolean
        get() = networkState.value.isConnected
        
    val isOffline: Boolean
        get() = !networkState.value.isConnected
        
    val isMetered: Boolean
        get() = networkState.value.isMetered
        
    val isWifi: Boolean
        get() = networkState.value.isWifi
        
    init {
        // Start monitoring network state
        networkStateMonitor.startMonitoring()
    }
    
    override fun onCleared() {
        super.onCleared()
        networkStateMonitor.stopMonitoring()
    }
}