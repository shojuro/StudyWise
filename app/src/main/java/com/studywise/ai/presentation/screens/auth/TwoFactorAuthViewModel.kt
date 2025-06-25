package com.studywise.ai.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VerificationMethod {
    SMS,
    EMAIL,
    AUTHENTICATOR
}

data class TwoFactorAuthUiState(
    val verificationMethod: VerificationMethod = VerificationMethod.SMS,
    val maskedPhoneNumber: String = "****1234",
    val maskedEmail: String = "u***@example.com",
    val otpCode: List<String> = List(6) { "" },
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val canResend: Boolean = false,
    val resendCountdown: Int = 30,
    val trustDevice: Boolean = false,
    val hasSmsOption: Boolean = true,
    val hasAuthenticatorOption: Boolean = false
)

@HiltViewModel
class TwoFactorAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TwoFactorAuthUiState())
    val uiState: StateFlow<TwoFactorAuthUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        startResendCountdown()
    }

    fun onOtpDigitChange(index: Int, digit: String) {
        val newOtpCode = _uiState.value.otpCode.toMutableList()
        newOtpCode[index] = digit
        _uiState.value = _uiState.value.copy(
            otpCode = newOtpCode,
            otpError = null
        )
    }

    fun verifyCode(onSuccess: () -> Unit) {
        val code = _uiState.value.otpCode.joinToString("")
        
        if (code.length != 6) {
            _uiState.value = _uiState.value.copy(
                otpError = "Please enter all 6 digits"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // For MVP, simulate verification
            delay(1500)
            
            if (code == "123456") { // Mock valid code
                if (_uiState.value.trustDevice) {
                    // Save device trust token
                    saveTrustedDevice()
                }
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    otpError = "Invalid code. Please try again.",
                    otpCode = List(6) { "" }
                )
            }
        }
    }

    fun resendCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                canResend = false
            )

            // Simulate sending code
            delay(1000)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                otpCode = List(6) { "" },
                otpError = null
            )

            startResendCountdown()
        }
    }

    fun switchToSms() {
        _uiState.value = _uiState.value.copy(
            verificationMethod = VerificationMethod.SMS,
            otpCode = List(6) { "" },
            otpError = null
        )
        resendCode()
    }

    fun switchToEmail() {
        _uiState.value = _uiState.value.copy(
            verificationMethod = VerificationMethod.EMAIL,
            otpCode = List(6) { "" },
            otpError = null
        )
        resendCode()
    }

    fun switchToAuthenticator() {
        _uiState.value = _uiState.value.copy(
            verificationMethod = VerificationMethod.AUTHENTICATOR,
            otpCode = List(6) { "" },
            otpError = null,
            canResend = true // No need to wait for authenticator
        )
    }

    fun onTrustDeviceChange(trust: Boolean) {
        _uiState.value = _uiState.value.copy(trustDevice = trust)
    }

    private fun startResendCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var countdown = 30
            while (countdown > 0) {
                _uiState.value = _uiState.value.copy(
                    resendCountdown = countdown,
                    canResend = false
                )
                delay(1000)
                countdown--
            }
            _uiState.value = _uiState.value.copy(
                canResend = true,
                resendCountdown = 0
            )
        }
    }

    private suspend fun saveTrustedDevice() {
        // Save device identifier and trust token
        // This would be used to skip 2FA on this device for 30 days
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}