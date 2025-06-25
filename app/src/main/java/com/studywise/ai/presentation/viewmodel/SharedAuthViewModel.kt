package com.studywise.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.studywise.ai.data.local.entity.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AuthState(
    val isAuthenticated: Boolean = false,
    val userRole: UserRole? = null,
    val userId: String? = null,
    val schoolId: String? = null,
    val requiresTwoFactor: Boolean = false,
    val isFirstTimeUser: Boolean = true,
    val isDemoMode: Boolean = false
)

@HiltViewModel
class SharedAuthViewModel @Inject constructor() : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun setUserRole(role: UserRole) {
        _authState.value = _authState.value.copy(userRole = role)
    }
    
    fun setSchoolId(schoolId: String) {
        _authState.value = _authState.value.copy(schoolId = schoolId)
    }
    
    fun setAuthenticated(authenticated: Boolean, userId: String? = null) {
        _authState.value = _authState.value.copy(
            isAuthenticated = authenticated,
            userId = userId
        )
    }
    
    fun setFirstTimeUser(isFirstTime: Boolean) {
        _authState.value = _authState.value.copy(isFirstTimeUser = isFirstTime)
    }
    
    fun setDemoMode(isDemoMode: Boolean) {
        _authState.value = _authState.value.copy(isDemoMode = isDemoMode)
    }
    
    fun clearAuthState() {
        _authState.value = AuthState()
    }
}