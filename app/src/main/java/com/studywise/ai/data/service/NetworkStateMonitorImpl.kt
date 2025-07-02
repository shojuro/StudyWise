package com.studywise.ai.data.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.studywise.ai.domain.service.NetworkState
import com.studywise.ai.domain.service.NetworkStateMonitor
import com.studywise.ai.domain.service.NetworkType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NetworkStateMonitor using Android's ConnectivityManager
 */
@Singleton
class NetworkStateMonitorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkStateMonitor {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _networkState = MutableStateFlow(getCurrentNetworkState())
    override val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    override val networkStateChanges: Flow<NetworkState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val state = getCurrentNetworkState()
                _networkState.value = state
                trySend(state)
            }
            
            override fun onLost(network: Network) {
                val state = getCurrentNetworkState()
                _networkState.value = state
                trySend(state)
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val state = getCurrentNetworkState()
                _networkState.value = state
                trySend(state)
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Send current state immediately
        trySend(getCurrentNetworkState())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    override fun isNetworkAvailable(): Boolean = networkState.value.isConnected
    
    override fun isWifiConnected(): Boolean = networkState.value.isWifi
    
    override fun isCellularConnected(): Boolean = networkState.value.isCellular
    
    override fun isMeteredConnection(): Boolean = networkState.value.isMetered
    
    override fun startMonitoring() {
        // Monitoring starts automatically when networkStateChanges is collected
        // Update current state
        _networkState.value = getCurrentNetworkState()
    }
    
    override fun stopMonitoring() {
        // Monitoring stops automatically when flow collection is cancelled
    }
    
    private fun getCurrentNetworkState(): NetworkState {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = activeNetwork?.let { 
            connectivityManager.getNetworkCapabilities(it) 
        }
        
        return if (networkCapabilities != null) {
            NetworkState(
                isConnected = true,
                isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
                isCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
                isMetered = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                } else {
                    connectivityManager.isActiveNetworkMetered
                },
                isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                networkType = when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NetworkType.BLUETOOTH
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
                    else -> NetworkType.NONE
                }
            )
        } else {
            NetworkState(
                isConnected = false,
                isWifi = false,
                isCellular = false,
                isMetered = false,
                isValidated = false,
                networkType = NetworkType.NONE
            )
        }
    }
}