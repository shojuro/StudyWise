package com.studywise.ai.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserEntity? = null,
    val displayName: String = "",
    val email: String = "",
    val role: String = "",
    val grade: Int? = null,
    val schoolName: String = "",
    val memberSince: String = "",
    val profileImageUrl: String? = null,
    
    // Statistics
    val totalSessions: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val averageMastery: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val skillsMastered: Int = 0,
    val totalPointsEarned: Int = 0,
    
    // Recent achievements
    val recentAchievements: List<Achievement> = emptyList(),
    
    // Edit mode
    val isEditMode: Boolean = false,
    val editDisplayName: String = "",
    val editBio: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val earnedDate: String
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
        loadUserStatistics()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        displayName = user.name,
                        email = user.email,
                        role = user.role.name,
                        grade = user.grade,
                        memberSince = formatDate(user.createdAt),
                        editDisplayName = user.name
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        saveError = "Failed to load profile"
                    )
                }
            )
        }
    }
    
    private fun loadUserStatistics() {
        // For MVP, use mock data
        // In production, these would come from a statistics repository
        _uiState.value = _uiState.value.copy(
            totalSessions = 42,
            totalQuestionsAnswered = 156,
            averageMastery = 0.78f,
            currentStreak = 7,
            longestStreak = 14,
            skillsMastered = 8,
            totalPointsEarned = 1250,
            recentAchievements = listOf(
                Achievement(
                    id = "1",
                    title = "Week Warrior",
                    description = "Complete 7 days in a row",
                    iconName = "star",
                    earnedDate = "2 days ago"
                ),
                Achievement(
                    id = "2",
                    title = "Math Master",
                    description = "Achieve 90% mastery in Algebra",
                    iconName = "calculate",
                    earnedDate = "1 week ago"
                ),
                Achievement(
                    id = "3",
                    title = "Quick Learner",
                    description = "Complete 5 sessions in one day",
                    iconName = "speed",
                    earnedDate = "2 weeks ago"
                )
            )
        )
    }
    
    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode,
            editDisplayName = _uiState.value.displayName,
            saveError = null
        )
    }
    
    fun onDisplayNameChange(name: String) {
        _uiState.value = _uiState.value.copy(editDisplayName = name)
    }
    
    fun onBioChange(bio: String) {
        _uiState.value = _uiState.value.copy(editBio = bio)
    }
    
    fun saveProfile() {
        val currentUser = _uiState.value.user ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
            
            val updatedUser = currentUser.copy(name = _uiState.value.editDisplayName)
            
            userRepository.updateUserProfile(updatedUser).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isEditMode = false,
                        displayName = _uiState.value.editDisplayName,
                        user = updatedUser
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveError = error.message
                    )
                }
            )
        }
    }
    
    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            editDisplayName = _uiState.value.displayName,
            saveError = null
        )
    }
    
    fun uploadProfileImage(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            userRepository.uploadProfileImage(uri).fold(
                onSuccess = { imageUrl ->
                    val currentUser = _uiState.value.user ?: return@fold
                    val updatedUser = currentUser.copy(profileImageUrl = imageUrl)
                    
                    userRepository.updateUserProfile(updatedUser).fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                profileImageUrl = imageUrl,
                                user = updatedUser
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                saveError = "Failed to update profile image: ${error.message}"
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveError = "Failed to upload image: ${error.message}"
                    )
                }
            )
        }
    }
    
    private fun formatDate(date: java.util.Date): String {
        val formatter = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}