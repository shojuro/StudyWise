package com.studywise.ai.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.data.local.preferences.ThemeMode
import com.studywise.ai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    
    // Appearance
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textSize: Float = 1.0f,
    val highContrast: Boolean = false,
    
    // Notifications
    val notificationsEnabled: Boolean = true,
    val dailyReminders: Boolean = true,
    val progressUpdates: Boolean = true,
    val studyReminderTime: String? = null,
    
    // Account
    val userEmail: String = "",
    val userName: String = "",
    val userRole: String = "",
    val schoolName: String = "",
    
    // Dialog states
    val showThemeDialog: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showReminderTimeDialog: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val isChangingPassword: Boolean = false,
    
    // Messages
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        observePreferences()
    }
    
    private fun observePreferences() {
        viewModelScope.launch {
            preferencesManager.userPreferences.collect { preferences ->
                _uiState.value = _uiState.value.copy(
                    themeMode = preferences.themeMode,
                    textSize = preferences.textSize,
                    highContrast = preferences.highContrast,
                    notificationsEnabled = preferences.notificationsEnabled,
                    studyReminderTime = preferences.studyReminderTime,
                    userEmail = preferences.userEmail ?: "",
                    userName = preferences.userName ?: "",
                    userRole = preferences.userRole ?: ""
                )
            }
        }
    }
    
    fun onThemeModeChange(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.updateThemeMode(themeMode)
            _uiState.value = _uiState.value.copy(
                themeMode = themeMode,
                showThemeDialog = false,
                successMessage = "Theme updated"
            )
        }
    }
    
    fun onTextSizeChange(textSize: Float) {
        viewModelScope.launch {
            preferencesManager.updateTextSize(textSize)
            _uiState.value = _uiState.value.copy(
                textSize = textSize,
                successMessage = "Text size updated"
            )
        }
    }
    
    fun onHighContrastChange(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateHighContrast(enabled)
            _uiState.value = _uiState.value.copy(
                highContrast = enabled,
                successMessage = if (enabled) "High contrast enabled" else "High contrast disabled"
            )
        }
    }
    
    fun onNotificationsEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateNotificationsEnabled(enabled)
            _uiState.value = _uiState.value.copy(
                notificationsEnabled = enabled,
                successMessage = if (enabled) "Notifications enabled" else "Notifications disabled"
            )
        }
    }
    
    fun onDailyRemindersChange(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateNotificationSettings(
                enableDailyReminders = enabled,
                enableProgressUpdates = _uiState.value.progressUpdates
            )
            _uiState.value = _uiState.value.copy(
                dailyReminders = enabled,
                successMessage = if (enabled) "Daily reminders enabled" else "Daily reminders disabled"
            )
        }
    }
    
    fun onProgressUpdatesChange(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateNotificationSettings(
                enableDailyReminders = _uiState.value.dailyReminders,
                enableProgressUpdates = enabled
            )
            _uiState.value = _uiState.value.copy(
                progressUpdates = enabled,
                successMessage = if (enabled) "Progress updates enabled" else "Progress updates disabled"
            )
        }
    }
    
    fun onStudyReminderTimeChange(time: String) {
        viewModelScope.launch {
            preferencesManager.updateStudyReminderTime(time)
            _uiState.value = _uiState.value.copy(
                studyReminderTime = time,
                showReminderTimeDialog = false,
                successMessage = "Study reminder time updated"
            )
        }
    }
    
    fun updateReminderTime(hour: Int, minute: Int) {
        val time = String.format("%02d:%02d", hour, minute)
        onStudyReminderTimeChange(time)
    }
    
    fun showThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = true)
    }
    
    fun dismissThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = false)
    }
    
    fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }
    
    fun dismissLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }
    
    fun showReminderTimeDialog() {
        _uiState.value = _uiState.value.copy(showReminderTimeDialog = true)
    }
    
    fun dismissReminderTimeDialog() {
        _uiState.value = _uiState.value.copy(showReminderTimeDialog = false)
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
    
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                authRepository.logout()
                preferencesManager.clearUserSession()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showLogoutDialog = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to logout"
                )
            }
        }
    }
    
    fun showChangePasswordDialog() {
        _uiState.value = _uiState.value.copy(showChangePasswordDialog = true)
    }
    
    fun dismissChangePasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showChangePasswordDialog = false,
            isChangingPassword = false
        )
    }
    
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChangingPassword = true)
            
            authRepository.changePassword(currentPassword, newPassword).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isChangingPassword = false,
                        showChangePasswordDialog = false,
                        successMessage = "Password changed successfully"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isChangingPassword = false,
                        errorMessage = error.message ?: "Failed to change password"
                    )
                }
            )
        }
    }
}