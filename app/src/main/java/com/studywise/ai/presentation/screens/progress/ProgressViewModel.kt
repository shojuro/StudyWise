package com.studywise.ai.presentation.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.*
import com.studywise.ai.domain.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val isLoading: Boolean = true,
    val weeklyProgress: WeeklyProgress? = null,
    val subjectProgressList: List<SubjectProgress> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val currentStreak: Int = 0,
    val totalPoints: Int = 0,
    val selectedSubject: String? = null,
    val selectedSubjectProgress: SubjectProgress? = null,
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val userId = preferencesManager.userPreferences
        .map { it.userId ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            userId.collect { currentUserId ->
                if (currentUserId.isNotEmpty()) {
                    loadWeeklyProgress(currentUserId)
                    loadSubjectProgress(currentUserId)
                    loadAchievements(currentUserId)
                    loadStats(currentUserId)
                }
            }
        }
    }

    private suspend fun loadWeeklyProgress(userId: String) {
        progressRepository.getWeeklyProgress(userId).fold(
            onSuccess = { weekly ->
                _uiState.update { it.copy(weeklyProgress = weekly) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        )
    }

    private suspend fun loadSubjectProgress(userId: String) {
        progressRepository.getAllSubjectsProgress(userId).fold(
            onSuccess = { subjects ->
                _uiState.update { 
                    it.copy(
                        subjectProgressList = subjects.sortedByDescending { s -> s.totalSessions },
                        isLoading = false
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { 
                    it.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
            }
        )
    }

    private suspend fun loadAchievements(userId: String) {
        progressRepository.getAchievements(userId).fold(
            onSuccess = { achievements ->
                _uiState.update { it.copy(achievements = achievements) }
            },
            onFailure = { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        )
    }

    private suspend fun loadStats(userId: String) {
        // Load streak
        progressRepository.getDailyStreak(userId).fold(
            onSuccess = { streak ->
                _uiState.update { it.copy(currentStreak = streak) }
            },
            onFailure = { /* Handle error */ }
        )

        // Load total points
        progressRepository.getTotalPoints(userId).fold(
            onSuccess = { points ->
                _uiState.update { it.copy(totalPoints = points) }
            },
            onFailure = { /* Handle error */ }
        )
    }

    fun onSubjectSelected(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
        
        viewModelScope.launch {
            val currentUserId = userId.value
            if (currentUserId.isNotEmpty()) {
                progressRepository.getSubjectProgress(currentUserId, subject).fold(
                    onSuccess = { progress ->
                        _uiState.update { it.copy(selectedSubjectProgress = progress) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadProgressData()
        }
    }
}