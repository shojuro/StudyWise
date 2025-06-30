package com.studywise.ai.domain.usecase.verbaljournal

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.VerbalJournalRepository
import com.studywise.ai.domain.service.SpeechAnalysisService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date
import javax.inject.Inject

/**
 * Use case for ending a Verbal Journal session and generating analysis
 */
class EndVerbalJournalSessionUseCase @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository,
    private val speechAnalysisService: SpeechAnalysisService
) {
    
    /**
     * End a Verbal Journal session and generate analysis
     */
    suspend operator fun invoke(
        sessionId: String,
        userFeedback: String? = null
    ): Flow<EndSessionResult> = flow {
        try {
            // Get session
            val session = verbalJournalRepository.getJournalEntry(sessionId)
                ?: throw IllegalStateException("Session not found")
            
            // Get user profile
            val profile = verbalJournalRepository.getOrCreateProfile(
                userId = session.userId,
                nativeLanguage = "English" // Should come from user settings
            )
            
            // Update session end time
            val endTime = Date()
            val actualDuration = ((endTime.time - session.startTime.time) / 1000 / 60).toInt()
            
            val updatedSession = session.copy(
                endTime = endTime,
                actualDurationMinutes = actualDuration,
                status = SessionStatus.COMPLETED
            )
            verbalJournalRepository.updateJournalEntry(updatedSession)
            
            // Analyze session
            val analysis = speechAnalysisService.analyzeSession(
                journalEntry = updatedSession,
                userProfile = profile
            )
            
            // Save analysis
            verbalJournalRepository.saveSessionAnalysis(sessionId, analysis)
            
            // Update user profile
            val updatedProfile = updateUserProfile(profile, analysis, actualDuration)
            verbalJournalRepository.updateProfile(updatedProfile)
            
            // Check and award achievements
            val newAchievements = verbalJournalRepository.checkAndAwardAchievements(
                userId = session.userId,
                sessionAnalysis = analysis
            )
            
            // Calculate progress metrics
            val progressMetrics = speechAnalysisService.calculateProgressMetrics(
                userId = session.userId,
                period = 30 // Last 30 days
            )
            
            // Generate personalized next steps
            val nextSteps = generateNextSteps(analysis, profile)
            
            emit(
                EndSessionResult.Success(
                    sessionAnalysis = analysis,
                    newAchievements = newAchievements,
                    updatedProfile = updatedProfile,
                    progressMetrics = progressMetrics,
                    nextSteps = nextSteps,
                    sessionSummary = generateSessionSummary(updatedSession, analysis)
                )
            )
            
        } catch (e: Exception) {
            emit(EndSessionResult.Error(e.message ?: "Failed to end session"))
        }
    }
    
    /**
     * Update user profile based on session results
     */
    private suspend fun updateUserProfile(
        profile: VerbalJournalProfile,
        analysis: SessionAnalysis,
        sessionMinutes: Int
    ): VerbalJournalProfile {
        // Update streak
        val today = Date()
        val isConsecutiveDay = profile.lastSessionDate?.let { lastDate ->
            val daysSince = ((today.time - lastDate.time) / (1000 * 60 * 60 * 24)).toInt()
            daysSince == 1
        } ?: false
        
        val newStreak = if (isConsecutiveDay) profile.streakDays + 1 else 1
        
        // Update total minutes
        val totalMinutes = profile.totalSpeakingMinutes + sessionMinutes
        
        // Check if ready to progress tier
        val shouldProgressTier = checkTierProgression(profile, analysis)
        val newTier = if (shouldProgressTier) {
            getNextTier(profile.currentTier)
        } else {
            profile.currentTier
        }
        
        // Check if ready to progress level
        val shouldProgressLevel = checkLevelProgression(profile, analysis)
        val newLevel = if (shouldProgressLevel) {
            getNextLevel(profile.currentLevel)
        } else {
            profile.currentLevel
        }
        
        // Update break-in week if needed
        val weeksSinceStart = profile.progressHistory.size / 7
        val newBreakInWeek = minOf(weeksSinceStart + 1, 12)
        
        // Save streak update
        verbalJournalRepository.updateStreak(profile.userId, newStreak, today)
        
        // Save speaking minutes
        verbalJournalRepository.addSpeakingMinutes(profile.userId, sessionMinutes)
        
        // Save progress snapshot
        val snapshot = ProgressSnapshot(
            date = today,
            tier = profile.currentTier,
            accuracy = analysis.overallStats.overallAccuracy,
            minutesSpoken = sessionMinutes,
            errorsFound = analysis.errorBreakdown.errorsByType.values.sum(),
            fluencyScore = analysis.overallStats.fluencyScore,
            milestone = if (shouldProgressTier || shouldProgressLevel) {
                "Progressed to $newTier tier / $newLevel level"
            } else null
        )
        verbalJournalRepository.saveProgressSnapshot(profile.userId, snapshot)
        
        return profile.copy(
            currentTier = newTier,
            currentLevel = newLevel,
            streakDays = newStreak,
            totalSpeakingMinutes = totalMinutes,
            lastSessionDate = today,
            breakInWeek = newBreakInWeek,
            progressHistory = profile.progressHistory + snapshot
        )
    }
    
    /**
     * Check if user should progress to next tier
     */
    private fun checkTierProgression(
        profile: VerbalJournalProfile,
        analysis: SessionAnalysis
    ): Boolean {
        val tierAccuracy = analysis.overallStats.tierSpecificAccuracy[profile.currentTier] ?: 0f
        
        return when (profile.currentTier) {
            CorrectionTier.CRITICAL -> tierAccuracy >= 0.90f
            CorrectionTier.IMPORTANT -> tierAccuracy >= 0.85f
            CorrectionTier.HELPFUL -> tierAccuracy >= 0.80f
            CorrectionTier.COMPREHENSIVE -> false // Already at highest
        }
    }
    
    /**
     * Check if user should progress to next level
     */
    private fun checkLevelProgression(
        profile: VerbalJournalProfile,
        analysis: SessionAnalysis
    ): Boolean {
        // Requires consistent high performance
        val recentSnapshots = profile.progressHistory.takeLast(10)
        if (recentSnapshots.size < 10) return false
        
        val avgAccuracy = recentSnapshots.map { it.accuracy }.average()
        val avgFluency = recentSnapshots.map { it.fluencyScore }.average()
        
        return when (profile.currentLevel) {
            ProficiencyLevel.BEGINNER -> avgAccuracy >= 0.85 && avgFluency >= 0.70
            ProficiencyLevel.INTERMEDIATE -> avgAccuracy >= 0.90 && avgFluency >= 0.80
            ProficiencyLevel.ADVANCED -> avgAccuracy >= 0.95 && avgFluency >= 0.90
            ProficiencyLevel.NATIVE_LEVEL -> false // Already at highest
        }
    }
    
    /**
     * Get next tier in progression
     */
    private fun getNextTier(current: CorrectionTier): CorrectionTier {
        return when (current) {
            CorrectionTier.CRITICAL -> CorrectionTier.IMPORTANT
            CorrectionTier.IMPORTANT -> CorrectionTier.HELPFUL
            CorrectionTier.HELPFUL -> CorrectionTier.COMPREHENSIVE
            CorrectionTier.COMPREHENSIVE -> CorrectionTier.COMPREHENSIVE
        }
    }
    
    /**
     * Get next level in progression
     */
    private fun getNextLevel(current: ProficiencyLevel): ProficiencyLevel {
        return when (current) {
            ProficiencyLevel.BEGINNER -> ProficiencyLevel.INTERMEDIATE
            ProficiencyLevel.INTERMEDIATE -> ProficiencyLevel.ADVANCED
            ProficiencyLevel.ADVANCED -> ProficiencyLevel.NATIVE_LEVEL
            ProficiencyLevel.NATIVE_LEVEL -> ProficiencyLevel.NATIVE_LEVEL
        }
    }
    
    /**
     * Generate personalized next steps
     */
    private fun generateNextSteps(
        analysis: SessionAnalysis,
        profile: VerbalJournalProfile
    ): List<String> {
        val steps = mutableListOf<String>()
        
        // Based on errors
        analysis.improvementAreas.take(2).forEach { area ->
            steps.add("Practice ${area.errorType.replace("_", " ")} with focused exercises")
        }
        
        // Based on fluency
        if (analysis.overallStats.fluencyScore < 0.7f) {
            steps.add("Work on speaking more continuously without long pauses")
        }
        
        // Based on vocabulary
        if (analysis.overallStats.vocabularyDiversity < 0.5f) {
            steps.add("Try using more varied vocabulary in your responses")
        }
        
        // Encouragement
        if (analysis.errorBreakdown.improvementFromLastSession > 0) {
            steps.add("Keep up the great progress - you're improving!")
        }
        
        return steps
    }
    
    /**
     * Generate session summary
     */
    private fun generateSessionSummary(
        session: VerbalJournalEntry,
        analysis: SessionAnalysis
    ): SessionSummary {
        return SessionSummary(
            duration = session.actualDurationMinutes,
            wordsSpoken = analysis.overallStats.wordsSpoken,
            accuracy = analysis.overallStats.overallAccuracy,
            fluencyScore = analysis.overallStats.fluencyScore,
            topErrors = analysis.improvementAreas.take(3).map { it.errorType },
            mainTopics = session.topics,
            engagement = session.engagementLevel
        )
    }
}

/**
 * Result of ending a session
 */
sealed class EndSessionResult {
    data class Success(
        val sessionAnalysis: SessionAnalysis,
        val newAchievements: List<Achievement>,
        val updatedProfile: VerbalJournalProfile,
        val progressMetrics: VerbalJournalMetrics,
        val nextSteps: List<String>,
        val sessionSummary: SessionSummary
    ) : EndSessionResult()
    
    data class Error(val message: String) : EndSessionResult()
}

/**
 * Summary of completed session
 */
data class SessionSummary(
    val duration: Int,
    val wordsSpoken: Int,
    val accuracy: Float,
    val fluencyScore: Float,
    val topErrors: List<String>,
    val mainTopics: List<String>,
    val engagement: EngagementLevel
)