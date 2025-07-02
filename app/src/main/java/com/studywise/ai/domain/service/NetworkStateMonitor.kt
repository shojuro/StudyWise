package com.studywise.ai.domain.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Service interface for monitoring network connectivity state
 */
interface NetworkStateMonitor {
    
    /**
     * Current network state as a StateFlow for reactive updates
     */
    val networkState: StateFlow<NetworkState>
    
    /**
     * Flow of network state changes
     */
    val networkStateChanges: Flow<NetworkState>
    
    /**
     * Check if network is currently available
     */
    fun isNetworkAvailable(): Boolean
    
    /**
     * Check if connected to WiFi
     */
    fun isWifiConnected(): Boolean
    
    /**
     * Check if connected to cellular network
     */
    fun isCellularConnected(): Boolean
    
    /**
     * Check if current connection is metered (e.g., cellular data)
     */
    fun isMeteredConnection(): Boolean
    
    /**
     * Start monitoring network state changes
     */
    fun startMonitoring()
    
    /**
     * Stop monitoring network state changes
     */
    fun stopMonitoring()
}

/**
 * Represents the current network state
 */
data class NetworkState(
    val isConnected: Boolean,
    val isWifi: Boolean,
    val isCellular: Boolean,
    val isMetered: Boolean,
    val isValidated: Boolean, // Network has internet connectivity
    val networkType: NetworkType
)

/**
 * Types of network connections
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    BLUETOOTH,
    VPN,
    NONE
}