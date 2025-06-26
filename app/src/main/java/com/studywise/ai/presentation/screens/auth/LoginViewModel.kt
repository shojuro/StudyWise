package com.studywise.ai.presentation.screens.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.StudyWiseApp
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.domain.repository.AuthRepository
import com.studywise.ai.domain.service.AnalyticsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsService: AnalyticsService,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        // Validate inputs
        var hasError = false
        
        // Validate email
        if (email.isEmpty()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
            hasError = true
        }

        // Basic password presence check (not full validation for login)
        if (password.isEmpty()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password is required")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, generalError = null)

            authRepository.login(email, password)
                .onSuccess { user ->
                    // Log successful login
                    analyticsService.logEvent(
                        AnalyticsEvent.FeatureUsed(
                            featureName = "login_success",
                            userId = user.id
                        )
                    )
                    analyticsService.setUserId(user.id)
                    analyticsService.setUserProperty("user_role", user.role.name)
                    
                    // Start data sync after successful login
                    (application as StudyWiseApp).startDataSync()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true,
                        userRole = user.role
                    )
                }
                .onFailure { exception ->
                    // Log login failure
                    analyticsService.logEvent(
                        AnalyticsEvent.ErrorOccurred(
                            errorType = "login_failed",
                            errorMessage = exception.message ?: "Unknown error",
                            screen = "login"
                        )
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = exception.message ?: "Login failed"
                    )
                }
        }
    }

}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val userRole: UserRole? = null
)