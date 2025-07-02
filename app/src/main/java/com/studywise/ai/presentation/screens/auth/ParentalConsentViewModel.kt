package com.studywise.ai.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.model.privacy.ConsentStatus
import com.studywise.ai.domain.model.privacy.ConsentType
import com.studywise.ai.domain.service.privacy.ConsentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentalConsentUiState(
    val consentStatus: ConsentStatus? = null,
    val parentEmail: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastCheckedTime: Long = 0
)

@HiltViewModel
class ParentalConsentViewModel @Inject constructor(
    private val consentManager: ConsentManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentalConsentUiState())
    val uiState: StateFlow<ParentalConsentUiState> = _uiState.asStateFlow()

    private var childUserId: String = ""
    private var isPolling = false

    fun loadConsentStatus(userId: String) {
        childUserId = userId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Get user's consent records
                val consents = consentManager.getUserConsents(userId)
                val registrationConsent = consents.find { 
                    it.consentType == ConsentType.REGISTRATION 
                }
                
                if (registrationConsent != null) {
                    _uiState.value = _uiState.value.copy(
                        consentStatus = registrationConsent.status,
                        parentEmail = registrationConsent.parentEmail,
                        isLoading = false
                    )
                    
                    // Start polling for status updates if consent is pending
                    if (registrationConsent.status == ConsentStatus.PENDING && !isPolling) {
                        startPollingForConsentStatus()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No consent request found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load consent status: ${e.message}"
                )
            }
        }
    }

    private fun startPollingForConsentStatus() {
        isPolling = true
        viewModelScope.launch {
            while (isPolling && _uiState.value.consentStatus == ConsentStatus.PENDING) {
                delay(5000) // Check every 5 seconds
                checkConsentStatus()
            }
        }
    }

    private suspend fun checkConsentStatus() {
        try {
            val consents = consentManager.getUserConsents(childUserId)
            val registrationConsent = consents.find { 
                it.consentType == ConsentType.REGISTRATION 
            }
            
            registrationConsent?.let { consent ->
                if (consent.status != _uiState.value.consentStatus) {
                    _uiState.value = _uiState.value.copy(
                        consentStatus = consent.status,
                        lastCheckedTime = System.currentTimeMillis()
                    )
                    
                    // Stop polling if consent is no longer pending
                    if (consent.status != ConsentStatus.PENDING) {
                        isPolling = false
                    }
                }
            }
        } catch (e: Exception) {
            // Silently ignore polling errors
        }
    }

    fun resendConsentEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Get existing consent to get parent email
                val consents = consentManager.getUserConsents(childUserId)
                val registrationConsent = consents.find { 
                    it.consentType == ConsentType.REGISTRATION 
                }
                
                if (registrationConsent != null) {
                    // Request consent again (this will resend the email)
                    val result = consentManager.requestParentalConsent(
                        childUserId = childUserId,
                        parentEmail = registrationConsent.parentEmail,
                        consentTypes = listOf(ConsentType.REGISTRATION)
                    )
                    
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = null
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Failed to resend email: ${exception.message}"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No consent request found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to resend email: ${e.message}"
                )
            }
        }
    }

    fun requestConsentAgain() {
        // This would typically navigate back to a screen where parent email can be updated
        // For now, we'll just try to resend to the same email
        resendConsentEmail()
    }

    override fun onCleared() {
        super.onCleared()
        isPolling = false
    }
}