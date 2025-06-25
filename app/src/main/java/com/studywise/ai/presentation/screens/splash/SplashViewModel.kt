package com.studywise.ai.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.data.local.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            // Add a minimum delay for splash screen
            delay(1500)

            preferencesManager.userPreferences.collect { preferences ->
                if (preferences.isLoggedIn && preferences.userRole != null) {
                    val userRole = try {
                        UserRole.valueOf(preferences.userRole)
                    } catch (e: Exception) {
                        null
                    }

                    if (userRole != null) {
                        _uiState.value = SplashUiState(userRole = userRole)
                    } else {
                        _uiState.value = SplashUiState(shouldNavigateToLogin = true)
                    }
                } else {
                    _uiState.value = SplashUiState(shouldNavigateToLogin = true)
                }
            }
        }
    }
}

data class SplashUiState(
    val shouldNavigateToLogin: Boolean = false,
    val userRole: UserRole? = null
)