package com.studywise.ai.presentation.screens.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileSetupUiState(
    val displayName: String = "",
    val displayNameError: String? = null,
    val bio: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    
    // Notification preferences
    val enableDailyReminders: Boolean = true,
    val enableProgressUpdates: Boolean = true,
    
    // Student-specific
    val favoriteSubjects: Set<String> = emptySet(),
    val learningGoals: String = "",
    
    // Teacher-specific
    val subjectsTaught: String = "",
    val yearsExperience: String = "",
    val teachingPhilosophy: String = "",
    
    // Parent-specific
    val numberOfChildren: String = "",
    val parentalGoals: String = ""
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun onDisplayNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            displayName = name,
            displayNameError = if (name.length < 2) {
                "Display name must be at least 2 characters"
            } else null
        )
    }

    fun onBioChange(bio: String) {
        _uiState.value = _uiState.value.copy(bio = bio)
    }

    fun onProfileImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri)
    }

    fun onDailyRemindersChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableDailyReminders = enabled)
    }

    fun onProgressUpdatesChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableProgressUpdates = enabled)
    }

    // Student-specific
    fun toggleFavoriteSubject(subject: String) {
        val currentSubjects = _uiState.value.favoriteSubjects.toMutableSet()
        if (subject in currentSubjects) {
            currentSubjects.remove(subject)
        } else {
            currentSubjects.add(subject)
        }
        _uiState.value = _uiState.value.copy(favoriteSubjects = currentSubjects)
    }

    fun onLearningGoalsChange(goals: String) {
        _uiState.value = _uiState.value.copy(learningGoals = goals)
    }

    // Teacher-specific
    fun onSubjectsTaughtChange(subjects: String) {
        _uiState.value = _uiState.value.copy(subjectsTaught = subjects)
    }

    fun onYearsExperienceChange(years: String) {
        _uiState.value = _uiState.value.copy(yearsExperience = years)
    }

    fun onTeachingPhilosophyChange(philosophy: String) {
        _uiState.value = _uiState.value.copy(teachingPhilosophy = philosophy)
    }

    // Parent-specific
    fun onNumberOfChildrenChange(number: String) {
        _uiState.value = _uiState.value.copy(numberOfChildren = number)
    }

    fun onParentalGoalsChange(goals: String) {
        _uiState.value = _uiState.value.copy(parentalGoals = goals)
    }

    fun saveProfile() {
        if (_uiState.value.displayName.length < 2) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val currentState = _uiState.value
                
                // Get current user to update
                val currentUser = userRepository.getCurrentUser().getOrNull()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        displayNameError = "Unable to load user profile"
                    )
                    return@launch
                }
                
                // Upload profile image if selected
                val profileImageUrl = currentState.profileImageUri?.let { uri ->
                    userRepository.uploadProfileImage(uri).getOrNull()
                }
                
                // Update user profile with basic available data
                val updatedUser = currentUser.copy(
                    name = currentState.displayName
                    // Note: Extended profile fields (bio, profileImageUrl, etc.) would require 
                    // a separate UserProfileEntity table in a full implementation
                )
                
                // Save profile to database
                userRepository.updateUserProfile(updatedUser).getOrThrow()
                
                // Save notification preferences
                userRepository.updateNotificationPreferences(
                    enableDailyReminders = currentState.enableDailyReminders,
                    enableProgressUpdates = currentState.enableProgressUpdates
                ).getOrThrow()
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    displayNameError = "Failed to save profile: ${e.message}"
                )
            }
        }
    }
}