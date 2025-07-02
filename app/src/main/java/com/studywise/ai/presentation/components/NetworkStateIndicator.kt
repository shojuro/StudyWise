package com.studywise.ai.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studywise.ai.domain.service.NetworkState
import com.studywise.ai.domain.service.NetworkType
import com.studywise.ai.presentation.viewmodel.NetworkStateViewModel
import kotlinx.coroutines.delay

/**
 * Displays a network state indicator banner when offline or on metered connection
 */
@Composable
fun NetworkStateIndicator(
    modifier: Modifier = Modifier,
    viewModel: NetworkStateViewModel = hiltViewModel()
) {
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()
    
    NetworkStateIndicatorContent(
        networkState = networkState,
        modifier = modifier
    )
}

@Composable
fun NetworkStateIndicatorContent(
    networkState: NetworkState,
    modifier: Modifier = Modifier
) {
    var showIndicator by remember { mutableStateOf(false) }
    
    LaunchedEffect(networkState) {
        if (!networkState.isConnected) {
            showIndicator = true
        } else if (showIndicator) {
            // Keep showing for 2 seconds after reconnection
            delay(2000)
            showIndicator = false
        }
    }
    
    AnimatedVisibility(
        visible = showIndicator || networkState.isMetered,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = when {
                !networkState.isConnected -> MaterialTheme.colorScheme.error
                networkState.isMetered -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            },
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when {
                        !networkState.isConnected -> Icons.Default.WifiOff
                        networkState.isWifi -> Icons.Default.Wifi
                        networkState.isCellular -> Icons.Default.SignalCellularAlt
                        else -> Icons.Default.NetworkCheck
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onError
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = when {
                        !networkState.isConnected -> "No internet connection"
                        networkState.isConnected && showIndicator -> "Back online"
                        networkState.isMetered -> "Using metered connection"
                        else -> "Connected"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        !networkState.isConnected -> MaterialTheme.colorScheme.onError
                        networkState.isMetered -> MaterialTheme.colorScheme.onTertiary
                        else -> MaterialTheme.colorScheme.onPrimary
                    }
                )
            }
        }
    }
}

/**
 * Small network state icon for app bars
 */
@Composable
fun NetworkStateIcon(
    modifier: Modifier = Modifier,
    viewModel: NetworkStateViewModel = hiltViewModel()
) {
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()
    
    if (!networkState.isConnected) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = "Offline",
            modifier = modifier,
            tint = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Detailed network state card for settings or debug screens
 */
@Composable
fun NetworkStateCard(
    modifier: Modifier = Modifier,
    viewModel: NetworkStateViewModel = hiltViewModel()
) {
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (networkState.isConnected) 
                        Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (networkState.isConnected) 
                        Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Network Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            NetworkDetailRow("Connection", if (networkState.isConnected) "Connected" else "Disconnected")
            NetworkDetailRow("Type", networkState.networkType.name)
            NetworkDetailRow("WiFi", if (networkState.isWifi) "Yes" else "No")
            NetworkDetailRow("Cellular", if (networkState.isCellular) "Yes" else "No")
            NetworkDetailRow("Metered", if (networkState.isMetered) "Yes" else "No")
            NetworkDetailRow("Validated", if (networkState.isValidated) "Yes" else "No")
        }
    }
}

@Composable
private fun NetworkDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}