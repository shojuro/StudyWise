package com.studywise.ai.presentation.screens.verbaljournal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.repository.VerbalJournalRepository
import com.studywise.ai.domain.usecase.verbaljournal.EndSessionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerbalJournalResultsViewModel @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()
    
    fun loadSessionResults(sessionId: String) {
        viewModelScope.launch {
            try {
                // Get session details
                val session = verbalJournalRepository.getJournalEntry(sessionId)
                if (session == null) {
                    _uiState.value = ResultsUiState.Error("Session not found")
                    return@launch
                }
                
                // Get analysis
                val analysis = verbalJournalRepository.getSessionAnalysis(sessionId)
                if (analysis == null) {
                    _uiState.value = ResultsUiState.Error("Analysis not found")
                    return@launch
                }
                
                // Get profile
                val profile = verbalJournalRepository.getOrCreateProfile(
                    userId = session.userId,
                    nativeLanguage = "English"
                )
                
                // Get metrics
                val metrics = verbalJournalRepository.getUserMetrics(
                    userId = session.userId,
                    period = "monthly"
                )
                
                // Get achievements for this session
                val achievements = analysis.achievements
                
                // Create session summary
                val summary = SessionSummary(
                    duration = session.actualDurationMinutes,
                    wordsSpoken = analysis.overallStats.wordsSpoken,
                    accuracy = analysis.overallStats.overallAccuracy,
                    fluencyScore = analysis.overallStats.fluencyScore,
                    topErrors = analysis.improvementAreas.take(3).map { it.errorType },
                    mainTopics = session.topics,
                    engagement = session.engagementLevel
                )
                
                // Generate next steps
                val nextSteps = generateNextSteps(analysis, profile)
                
                // Create result
                val result = EndSessionResult.Success(
                    sessionAnalysis = analysis,
                    newAchievements = achievements,
                    updatedProfile = profile,
                    progressMetrics = metrics,
                    nextSteps = nextSteps,
                    sessionSummary = summary
                )
                
                _uiState.value = ResultsUiState.Success(result)
                
            } catch (e: Exception) {
                _uiState.value = ResultsUiState.Error(
                    e.message ?: "Failed to load results"
                )
            }
        }
    }
    
    private fun generateNextSteps(
        analysis: SessionAnalysis,
        profile: VerbalJournalProfile
    ): List<String> {
        val steps = mutableListOf<String>()
        
        // Based on accuracy
        when {
            analysis.overallStats.overallAccuracy < 0.7f -> {
                steps.add("Focus on communication-critical errors first")
            }
            analysis.overallStats.overallAccuracy < 0.85f -> {
                steps.add("Practice the most frequent error types")
            }
            else -> {
                steps.add("Challenge yourself with more complex topics")
            }
        }
        
        // Based on fluency
        if (analysis.overallStats.fluencyScore < 0.7f) {
            steps.add("Work on speaking more continuously")
        }
        
        // Based on improvement areas
        if (analysis.improvementAreas.isNotEmpty()) {
            val topError = analysis.improvementAreas.first()
            steps.add("Practice ${topError.errorType.replace("_", " ")} exercises")
        }
        
        // Encouragement based on progress
        if (analysis.errorBreakdown.improvementFromLastSession > 0) {
            steps.add("Keep up the great progress!")
        }
        
        // Break-in period guidance
        if (profile.breakInWeek < 12) {
            steps.add("Continue daily practice to build your speaking habit")
        }
        
        return steps
    }
}

sealed class ResultsUiState {
    object Loading : ResultsUiState()
    data class Success(val result: EndSessionResult.Success) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
}