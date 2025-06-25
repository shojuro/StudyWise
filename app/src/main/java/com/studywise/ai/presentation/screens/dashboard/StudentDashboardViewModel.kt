package com.studywise.ai.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.dao.ProgressDao
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.SubjectProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
                    progress = 0.65f,
                    lastPracticed = "Today",
                    totalPoints = 450
                ),
                SubjectProgress(
                    subject = "Mathematics",
                    progress = 0.45f,
                    lastPracticed = "Yesterday",
                    totalPoints = 320
                ),
                SubjectProgress(
                    subject = "Science",
                    progress = 0.80f,
                    lastPracticed = "2 days ago",
                    totalPoints = 580
                ),
                SubjectProgress(
                    subject = "History",
                    progress = 0.30f,
                    lastPracticed = null,
                    totalPoints = 150
                )
            )

            _uiState.value = _uiState.value.copy(
                subjects = subjects,
                isLoading = false
            )
        }
    }

    fun onSubjectSelected(subject: String) {
        _uiState.value = _uiState.value.copy(selectedSubject = subject)
    }
}

data class StudentDashboardUiState(
    val userName: String = "",
    val subjects: List<SubjectProgress> = emptyList(),
    val isLoading: Boolean = true,
    val selectedSubject: String? = null
)