package com.studywise.ai.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.domain.model.privacy.AgeVerification
import com.studywise.ai.domain.model.privacy.ConsentType
import com.studywise.ai.domain.repository.AuthRepository
import com.studywise.ai.domain.service.privacy.ConsentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

data class RegisterUiStateWithAge(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val birthDate: LocalDate? = null,
    val parentEmail: String = "",
    val selectedRole: UserRole = UserRole.STUDENT,
    val selectedGrade: Int = 6,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val birthDateError: String? = null,
    val parentEmailError: String? = null,
    val generalError: String? = null,
    val registerSuccess: Boolean = false,
    val requiresParentalConsent: Boolean = false,
    val showAgeNotice: Boolean = false,
    val userId: String = ""
)

@HiltViewModel
class RegisterViewModelWithAge @Inject constructor(
    private val authRepository: AuthRepository,
    private val consentManager: ConsentManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiStateWithAge())
    val uiState: StateFlow<RegisterUiStateWithAge> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = null
        )
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            confirmPasswordError = if (_uiState.value.confirmPassword.isNotEmpty() && password != _uiState.value.confirmPassword) {
                "Passwords do not match"
            } else null
        )
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = if (confirmPassword != _uiState.value.password) {
                "Passwords do not match"
            } else null
        )
    }

    fun onBirthDateChange(birthDate: LocalDate) {
        val age = Period.between(birthDate, LocalDate.now()).years
        val requiresConsent = age < AgeVerification.COPPA_AGE_LIMIT
        
        _uiState.value = _uiState.value.copy(
            birthDate = birthDate,
            birthDateError = null,
            requiresParentalConsent = requiresConsent,
            showAgeNotice = requiresConsent
        )
    }

    fun onParentEmailChange(parentEmail: String) {
        _uiState.value = _uiState.value.copy(
            parentEmail = parentEmail,
            parentEmailError = null
        )
    }

    fun onRoleChange(role: UserRole) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun onGradeChange(grade: Int) {
        _uiState.value = _uiState.value.copy(selectedGrade = grade)
    }

    fun register() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, generalError = null)

            try {
                // First verify age
                val birthDate = _uiState.value.birthDate!!
                val ageVerification = consentManager.verifyAge(birthDate)

                // Create user account
                val result = authRepository.register(
                    name = _uiState.value.name.trim(),
                    email = _uiState.value.email.trim(),
                    password = _uiState.value.password,
                    role = _uiState.value.selectedRole.name,
                    grade = if (_uiState.value.selectedRole == UserRole.STUDENT) _uiState.value.selectedGrade else null,
                    birthDate = birthDate
                )

                result.fold(
                    onSuccess = { user ->
                        if (ageVerification.requiresParentalConsent && _uiState.value.selectedRole == UserRole.STUDENT) {
                            // Request parental consent
                            val consentResult = consentManager.requestParentalConsent(
                                childUserId = user.id,
                                parentEmail = _uiState.value.parentEmail,
                                consentTypes = listOf(
                                    ConsentType.REGISTRATION,
                                    ConsentType.DATA_COLLECTION,
                                    ConsentType.ANALYTICS
                                )
                            )
                            
                            consentResult.fold(
                                onSuccess = {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        registerSuccess = true,
                                        userId = user.id
                                    )
                                },
                                onFailure = { exception ->
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        generalError = "Failed to send parental consent request: ${exception.message}"
                                    )
                                }
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                registerSuccess = true,
                                userId = user.id
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            generalError = exception.message ?: "Registration failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = "Registration failed: ${e.message}"
                )
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = "Name is required")
            isValid = false
        }

        if (currentState.email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
            isValid = false
        }

        if (currentState.birthDate == null) {
            _uiState.value = _uiState.value.copy(birthDateError = "Birth date is required")
            isValid = false
        } else {
            val age = Period.between(currentState.birthDate, LocalDate.now()).years
            if (age < 4) {
                _uiState.value = _uiState.value.copy(birthDateError = "Must be at least 4 years old")
                isValid = false
            } else if (age > 100) {
                _uiState.value = _uiState.value.copy(birthDateError = "Please enter a valid birth date")
                isValid = false
            }
        }

        if (currentState.requiresParentalConsent && currentState.selectedRole == UserRole.STUDENT) {
            if (currentState.parentEmail.isBlank()) {
                _uiState.value = _uiState.value.copy(parentEmailError = "Parent email is required for users under 13")
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.parentEmail).matches()) {
                _uiState.value = _uiState.value.copy(parentEmailError = "Invalid email format")
                isValid = false
            } else if (currentState.parentEmail.equals(currentState.email, ignoreCase = true)) {
                _uiState.value = _uiState.value.copy(parentEmailError = "Parent email must be different from your email")
                isValid = false
            }
        }

        if (currentState.password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password is required")
            isValid = false
        } else if (currentState.password.length < 8) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must be at least 8 characters")
            isValid = false
        } else if (!currentState.password.matches(Regex(".*[A-Z].*"))) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must contain at least one uppercase letter")
            isValid = false
        } else if (!currentState.password.matches(Regex(".*[a-z].*"))) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must contain at least one lowercase letter")
            isValid = false
        } else if (!currentState.password.matches(Regex(".*[0-9].*"))) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must contain at least one number")
            isValid = false
        }

        if (currentState.confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Please confirm your password")
            isValid = false
        } else if (currentState.password != currentState.confirmPassword) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Passwords do not match")
            isValid = false
        }

        return isValid
    }
}