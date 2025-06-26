package com.studywise.ai.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.dao.ProgressDao
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.SubjectProgress
import com.studywise.ai.domain.model.ProgressTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class StudentDashboardViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val progressDao: ProgressDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentDashboardUiState())
    val uiState: StateFlow<StudentDashboardUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadSubjectProgress()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            preferencesManager.userPreferences.collect { preferences ->
                _uiState.value = _uiState.value.copy(
                    userName = preferences.userName ?: "Student"
                )
            }
        }
    }

    private fun loadSubjectProgress() {
        viewModelScope.launch {
            // For MVP, we'll show static subjects with simulated progress
            val subjects = listOf(
                SubjectProgress(
                    subject = "English",
                    totalSessions = 15,
                    totalQuestions = 150,
                    correctAnswers = 98,
                    averageAccuracy = 0.65f,
                    totalTimeMinutes = 450,
                    lastPracticed = Date(),
                    skillMastery = mapOf("Grammar" to 0.7f, "Vocabulary" to 0.6f),
                    trend = ProgressTrend.IMPROVING
                ),
                SubjectProgress(
                    subject = "Mathematics",
                    totalSessions = 12,
                    totalQuestions = 120,
                    correctAnswers = 54,
                    averageAccuracy = 0.45f,
                    totalTimeMinutes = 320,
                    lastPracticed = Date(System.currentTimeMillis() - 86400000), // Yesterday
                    skillMastery = mapOf("Algebra" to 0.5f, "Geometry" to 0.4f),
                    trend = ProgressTrend.STABLE
                ),
                SubjectProgress(
                    subject = "Science",
                    totalSessions = 18,
                    totalQuestions = 180,
                    correctAnswers = 144,
                    averageAccuracy = 0.80f,
                    totalTimeMinutes = 580,
                    lastPracticed = Date(System.currentTimeMillis() - 172800000), // 2 days ago
                    skillMastery = mapOf("Physics" to 0.85f, "Chemistry" to 0.75f),
                    trend = ProgressTrend.IMPROVING
                ),
                SubjectProgress(
                    subject = "History",
                    totalSessions = 5,
                    totalQuestions = 50,
                    correctAnswers = 15,
                    averageAccuracy = 0.30f,
                    totalTimeMinutes = 150,
                    lastPracticed = null,
                    skillMastery = mapOf("Ancient History" to 0.3f),
                    trend = ProgressTrend.DECLINING
                )
            )

            _uiState.value = _uiState.value.copy(
                subjects = subjects,
                isLoading = false
            )
        }
    }

}

data class StudentDashboardUiState(
    val userName: String = "",
    val subjects: List<SubjectProgress> = emptyList(),
    val isLoading: Boolean = true
)