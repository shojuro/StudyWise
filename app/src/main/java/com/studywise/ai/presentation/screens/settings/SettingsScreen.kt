package com.studywise.ai.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studywise.ai.data.local.preferences.ThemeMode
import com.studywise.ai.presentation.components.AccessibleButton
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onOpenTermsOfService: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenHelpSupport: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success/error messages
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val message = uiState.successMessage ?: uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    title = "Theme",
                    description = when (uiState.themeMode) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.SYSTEM -> "System default"
                    },
                    icon = Icons.Default.Palette,
                    onClick = { viewModel.showThemeDialog() }
                )
                
                SettingsItem(
                    title = "Text Size",
                    description = when {
                        uiState.textSize < 0.9f -> "Small"
                        uiState.textSize > 1.1f -> "Large"
                        else -> "Normal"
                    },
                    icon = Icons.Default.FormatSize
                ) {
                    TextSizeSlider(
                        textSize = uiState.textSize,
                        onTextSizeChange = viewModel::onTextSizeChange
                    )
                }
                
                SettingsSwitch(
                    title = "High Contrast",
                    description = "Improve readability with higher contrast",
                    icon = Icons.Default.Contrast,
                    checked = uiState.highContrast,
                    onCheckedChange = viewModel::onHighContrastChange
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsSwitch(
                    title = "Enable Notifications",
                    description = "Receive study reminders and updates",
                    icon = Icons.Default.Notifications,
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::onNotificationsEnabledChange
                )
                
                if (uiState.notificationsEnabled) {
                    SettingsSwitch(
                        title = "Daily Reminders",
                        description = "Get reminded to study every day",
                        icon = Icons.Default.Schedule,
                        checked = uiState.dailyReminders,
                        onCheckedChange = viewModel::onDailyRemindersChange
                    )
                    
                    if (uiState.dailyReminders) {
                        SettingsItem(
                            title = "Reminder Time",
                            description = uiState.studyReminderTime ?: "4:00 PM",
                            icon = Icons.Default.AccessTime,
                            onClick = { viewModel.showReminderTimeDialog() }
                        )
                    }
                    
                    SettingsSwitch(
                        title = "Progress Updates",
                        description = "Weekly summary of your progress",
                        icon = Icons.Default.TrendingUp,
                        checked = uiState.progressUpdates,
                        onCheckedChange = viewModel::onProgressUpdatesChange
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Account Section
            SettingsSection(title = "Account") {
                SettingsItem(
                    title = uiState.userName,
                    description = uiState.userEmail,
                    icon = Icons.Default.Person
                )
                
                SettingsItem(
                    title = "Change Password",
                    description = "Update your account password",
                    icon = Icons.Default.Lock,
                    onClick = onNavigateToChangePassword
                )
                
                SettingsItem(
                    title = "Logout",
                    description = "Sign out of your account",
                    icon = Icons.Default.Logout,
                    onClick = { viewModel.showLogoutDialog() }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Developer Section
            SettingsSection(title = "Developer") {
                SettingsItem(
                    title = "Analytics Dashboard",
                    description = "View app usage analytics",
                    icon = Icons.Default.Analytics,
                    onClick = onNavigateToAnalytics
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    title = "Version",
                    description = "1.0.0 (MVP)",
                    icon = Icons.Default.Info
                )
                
                SettingsItem(
                    title = "Terms of Service",
                    icon = Icons.Default.Description,
                    onClick = onOpenTermsOfService
                )
                
                SettingsItem(
                    title = "Privacy Policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = onOpenPrivacyPolicy
                )
                
                SettingsItem(
                    title = "Help & Support",
                    icon = Icons.Default.Help,
                    onClick = onOpenHelpSupport
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (uiState.showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeMode,
            onThemeSelected = viewModel::onThemeModeChange,
            onDismiss = { viewModel.dismissThemeDialog() }
        )
    }

    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutDialog() },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogoutDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showReminderTimeDialog) {
        ReminderTimePickerDialog(
            currentTime = uiState.studyReminderTime ?: "09:00",
            onTimeSelected = { hour, minute ->
                viewModel.updateReminderTime(hour, minute)
                viewModel.dismissReminderTimeDialog()
            },
            onDismiss = { viewModel.dismissReminderTimeDialog() }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    } else {
        Modifier.fillMaxWidth()
    }
    
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (content != null) {
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun TextSizeSlider(
    textSize: Float,
    onTextSizeChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Small",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Normal",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Large",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Slider(
            value = textSize,
            onValueChange = onTextSizeChange,
            valueRange = 0.8f..1.5f,
            steps = 6,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                ThemeMode.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (theme) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.SYSTEM -> "System default"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimePickerDialog(
    currentTime: String,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Parse current time (format: "HH:mm")
    val timeParts = currentTime.split(":")
    val currentHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
    val currentMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    
    val timePickerState = rememberTimePickerState(
        initialHour = currentHour,
        initialMinute = currentMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Reminder Time") },
        text = {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(16.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("Set Time")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}