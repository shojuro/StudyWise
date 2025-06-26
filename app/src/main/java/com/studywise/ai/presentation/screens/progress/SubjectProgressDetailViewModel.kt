package com.studywise.ai.presentation.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.repository.ProgressRepository
import com.studywise.ai.domain.service.AnalyticsService
import com.studywise.ai.domain.service.trackSubjectProgressViewed
import com.studywise.ai.domain.service.trackProgressExported
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SubjectProgressDetailViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val preferencesManager: PreferencesManager,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectProgressDetailUiState())
    val uiState: StateFlow<SubjectProgressDetailUiState> = _uiState.asStateFlow()

    private val userId = preferencesManager.userPreferences
        .map { it.userId ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun loadSubjectProgress(subject: String) {
        viewModelScope.launch {
            val currentUserId = userId.value
            if (currentUserId.isNotEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
                
                // Load subject progress
                progressRepository.getSubjectProgress(currentUserId, subject).fold(
                    onSuccess = { subjectProgress ->
                        _uiState.update { 
                            it.copy(
                                subjectProgress = subjectProgress,
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
                
                // Load session history
                loadSessionHistory(currentUserId, subject)
                
                // Generate recommendations
                generateRecommendations(subject)
                
                // Track analytics
                analyticsService.trackSubjectProgressViewed(subject)
            }
        }
    }

    private suspend fun loadSessionHistory(userId: String, subject: String) {
        progressRepository.getRecentSessions(userId, 20).fold(
            onSuccess = { sessions ->
                val subjectSessions = sessions
                    .filter { it.subject == subject }
                    .map { session ->
                        LearningSession(
                            date = session.startedAt,
                            questionsAnswered = session.questionsAnswered,
                            correctAnswers = session.correctAnswers,
                            accuracy = if (session.questionsAnswered > 0) {
                                session.correctAnswers.toFloat() / session.questionsAnswered
                            } else 0f,
                            minutesSpent = if (session.completedAt != null) {
                                ((session.completedAt.time - session.startedAt.time) / 60000).toInt()
                            } else 0,
                            pointsEarned = session.pointsEarned,
                            skills = emptyList() // TODO: Add skills tracking
                        )
                    }
                    .sortedByDescending { it.date }
                
                _uiState.update { it.copy(sessionHistory = subjectSessions) }
            },
            onFailure = { /* Handle error */ }
        )
    }

    private fun generateRecommendations(subject: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val recommendations = mutableListOf<String>()
            
            currentState.subjectProgress?.let { progress ->
                // Based on accuracy
                if (progress.averageAccuracy < 0.6f) {
                    recommendations.add("Focus on understanding core concepts before advancing")
                    recommendations.add("Try easier questions to build confidence")
                } else if (progress.averageAccuracy > 0.9f) {
                    recommendations.add("Challenge yourself with advanced topics")
                    recommendations.add("Consider helping others learn this subject")
                }
                
                // Based on frequency
                val daysSinceLastPractice = progress.lastPracticed?.let {
                    (Date().time - it.time) / (1000 * 60 * 60 * 24)
                } ?: Long.MAX_VALUE
                
                if (daysSinceLastPractice > 7) {
                    recommendations.add("It's been a while! Regular practice helps retention")
                }
                
                // Based on skill gaps
                progress.skillMastery.filter { it.value < 0.5f }.forEach { (skill, _) ->
                    recommendations.add("Strengthen your ${skill.replace("_", " ").lowercase()} skills")
                }
                
                // Based on trend
                when (progress.trend) {
                    com.studywise.ai.domain.model.ProgressTrend.DECLINING -> {
                        recommendations.add("Your performance is declining. Take a break and review basics")
                    }
                    com.studywise.ai.domain.model.ProgressTrend.IMPROVING -> {
                        recommendations.add("Great progress! Keep up the momentum")
                    }
                    com.studywise.ai.domain.model.ProgressTrend.STABLE -> {
                        recommendations.add("Try new learning strategies to break through the plateau")
                    }
                }
            }
            
            _uiState.update { it.copy(recommendations = recommendations.take(3)) }
        }
    }

    fun exportProgress() {
        _uiState.update { it.copy(showExportDialog = true) }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(showExportDialog = false) }
    }

    fun confirmExport(format: ExportFormat) {
        viewModelScope.launch {
            // Track analytics
            analyticsService.trackProgressExported(format.name)
            
            // TODO: Implement actual export functionality
            when (format) {
                ExportFormat.PDF -> exportAsPdf()
                ExportFormat.CSV -> exportAsCsv()
                ExportFormat.JSON -> exportAsJson()
            }
            
            _uiState.update { it.copy(showExportDialog = false) }
        }
    }

    private suspend fun exportAsPdf() {
        // TODO: Implement PDF export
        // This would generate a PDF with charts, stats, and recommendations
    }

    private suspend fun exportAsCsv() {
        // TODO: Implement CSV export
        // This would export session data in CSV format
    }

    private suspend fun exportAsJson() {
        // TODO: Implement JSON export
        // This would export all progress data in JSON format
    }
}