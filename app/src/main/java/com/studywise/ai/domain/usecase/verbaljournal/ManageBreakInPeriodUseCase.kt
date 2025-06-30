package com.studywise.ai.domain.usecase.verbaljournal

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.VerbalJournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for managing the break-in period progression
 */
class ManageBreakInPeriodUseCase @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository
) {
    
    /**
     * Get current break-in period status and recommendations
     */
    suspend operator fun invoke(userId: String): Flow<BreakInPeriodStatus> = flow {
        try {
            // Get user profile
            val profile = verbalJournalRepository.getOrCreateProfile(
                userId = userId,
                nativeLanguage = "English" // Should come from user settings
            )
            
            // Get current break-in schedule
            val currentSchedule = BreakInSchedules.getStandardSchedule()
                .find { it.week == profile.breakInWeek }
                ?: BreakInSchedules.getStandardSchedule().last()
            
            // Get next week's schedule if applicable
            val nextSchedule = if (profile.breakInWeek < 12) {
                BreakInSchedules.getStandardSchedule()
                    .find { it.week == profile.breakInWeek + 1 }
            } else null
            
            // Calculate progression readiness
            val progressionReadiness = calculateProgressionReadiness(userId, profile)
            
            // Get schedule type recommendation
            val recommendedScheduleType = recommendScheduleType(profile, progressionReadiness)
            
            // Generate tips for current week
            val weeklyTips = generateWeeklyTips(profile.breakInWeek, profile.currentLevel)
            
            // Check if user can skip ahead
            val canSkipAhead = checkSkipEligibility(profile, progressionReadiness)
            
            emit(
                BreakInPeriodStatus.Success(
                    currentWeek = profile.breakInWeek,
                    currentSchedule = currentSchedule,
                    nextSchedule = nextSchedule,
                    progressionReadiness = progressionReadiness,
                    recommendedScheduleType = recommendedScheduleType,
                    weeklyTips = weeklyTips,
                    canSkipAhead = canSkipAhead,
                    isComplete = profile.breakInWeek >= 12
                )
            )
            
        } catch (e: Exception) {
            emit(BreakInPeriodStatus.Error(e.message ?: "Failed to get break-in status"))
        }
    }
    
    /**
     * Update break-in schedule type
     */
    suspend fun updateScheduleType(
        userId: String,
        scheduleType: BreakInScheduleType
    ): Flow<UpdateScheduleResult> = flow {
        try {
            val profile = verbalJournalRepository.getOrCreateProfile(userId, "English")
            
            // Get appropriate schedule
            val newSchedule = when (scheduleType) {
                BreakInScheduleType.STANDARD -> BreakInSchedules.getStandardSchedule()
                BreakInScheduleType.ACCELERATED -> BreakInSchedules.getAcceleratedSchedule()
                BreakInScheduleType.GENTLE -> BreakInSchedules.getGentleSchedule()
            }
            
            // Update profile with new schedule info
            // Note: This would require adding schedule type to profile
            emit(
                UpdateScheduleResult.Success(
                    newScheduleType = scheduleType,
                    updatedSchedule = newSchedule.find { it.week == profile.breakInWeek }!!
                )
            )
            
        } catch (e: Exception) {
            emit(UpdateScheduleResult.Error(e.message ?: "Failed to update schedule"))
        }
    }
    
    /**
     * Skip to a specific week if eligible
     */
    suspend fun skipToWeek(
        userId: String,
        targetWeek: Int
    ): Flow<SkipWeekResult> = flow {
        try {
            val profile = verbalJournalRepository.getOrCreateProfile(userId, "English")
            
            // Validate skip eligibility
            val progressionReadiness = calculateProgressionReadiness(userId, profile)
            if (progressionReadiness.overallReadiness < 0.8f) {
                emit(SkipWeekResult.Error("Not ready to skip ahead. Continue practicing!"))
                return@flow
            }
            
            // Update break-in week
            val updatedProfile = profile.copy(breakInWeek = targetWeek)
            verbalJournalRepository.updateProfile(updatedProfile)
            
            // Get new schedule
            val newSchedule = BreakInSchedules.getStandardSchedule()
                .find { it.week == targetWeek }!!
            
            emit(
                SkipWeekResult.Success(
                    newWeek = targetWeek,
                    newSchedule = newSchedule
                )
            )
            
        } catch (e: Exception) {
            emit(SkipWeekResult.Error(e.message ?: "Failed to skip week"))
        }
    }
    
    /**
     * Calculate readiness to progress to next week
     */
    private suspend fun calculateProgressionReadiness(
        userId: String,
        profile: VerbalJournalProfile
    ): ProgressionReadiness {
        // Get recent sessions
        val recentEntries = verbalJournalRepository.getUserJournalEntries(userId)
            .let { flow ->
                val entries = mutableListOf<VerbalJournalEntry>()
                flow.collect { entries.addAll(it) }
                entries
            }
            .filter { 
                it.sessionDate.time > System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
            }
        
        // Calculate metrics
        val sessionsThisWeek = recentEntries.size
        val targetSessions = 5 // Aim for 5 sessions per week
        val sessionCompletion = minOf(1f, sessionsThisWeek.toFloat() / targetSessions)
        
        val avgAccuracy = recentEntries
            .mapNotNull { it.sessionAnalysis?.overallStats?.overallAccuracy }
            .average()
            .takeIf { !it.isNaN() }
            ?.toFloat() ?: 0.7f
        
        val avgDuration = recentEntries
            .map { it.actualDurationMinutes }
            .average()
            .takeIf { !it.isNaN() }
            ?.toFloat() ?: 0f
        
        val targetDuration = BreakInSchedules.getStandardSchedule()
            .find { it.week == profile.breakInWeek }
            ?.sessionLengthMinutes?.toFloat() ?: 30f
        
        val durationCompletion = minOf(1f, avgDuration / targetDuration)
        
        // Check tier-specific accuracy
        val tierAccuracy = recentEntries
            .mapNotNull { 
                it.sessionAnalysis?.overallStats?.tierSpecificAccuracy?.get(profile.currentTier)
            }
            .average()
            .takeIf { !it.isNaN() }
            ?.toFloat() ?: 0.7f
        
        val overallReadiness = (
            sessionCompletion * 0.3f +
            avgAccuracy * 0.3f +
            durationCompletion * 0.2f +
            tierAccuracy * 0.2f
        )
        
        return ProgressionReadiness(
            sessionsCompleted = sessionsThisWeek,
            targetSessions = targetSessions,
            averageAccuracy = avgAccuracy,
            averageDuration = avgDuration.toInt(),
            targetDuration = targetDuration.toInt(),
            tierAccuracy = tierAccuracy,
            overallReadiness = overallReadiness,
            readyToProgress = overallReadiness >= 0.75f
        )
    }
    
    /**
     * Recommend schedule type based on performance
     */
    private fun recommendScheduleType(
        profile: VerbalJournalProfile,
        readiness: ProgressionReadiness
    ): BreakInScheduleType {
        return when {
            readiness.overallReadiness >= 0.9f && 
            profile.currentLevel != ProficiencyLevel.BEGINNER -> {
                BreakInScheduleType.ACCELERATED
            }
            readiness.overallReadiness < 0.6f || 
            readiness.averageAccuracy < 0.7f -> {
                BreakInScheduleType.GENTLE
            }
            else -> BreakInScheduleType.STANDARD
        }
    }
    
    /**
     * Generate tips for current week
     */
    private fun generateWeeklyTips(week: Int, level: ProficiencyLevel): List<String> {
        val tips = mutableListOf<String>()
        
        // Week-specific tips
        when (week) {
            1 -> {
                tips.add("Start with short 2-minute sessions to build the habit")
                tips.add("Don't worry about mistakes - focus on speaking")
                tips.add("Choose topics you're excited to talk about")
            }
            in 2..4 -> {
                tips.add("Try to speak a bit longer each day")
                tips.add("Notice which errors are corrected - these are critical")
                tips.add("Use the follow-up questions to extend conversations")
            }
            in 5..8 -> {
                tips.add("You're seeing more corrections now - this is progress!")
                tips.add("Try using new vocabulary from your corrections")
                tips.add("Record yourself to hear your improvement")
            }
            in 9..12 -> {
                tips.add("Challenge yourself with more complex topics")
                tips.add("Focus on fluency - speak without long pauses")
                tips.add("Review your progress charts for motivation")
            }
            else -> {
                tips.add("You've completed the break-in period!")
                tips.add("Keep practicing daily to maintain progress")
                tips.add("Explore advanced topics and vocabulary")
            }
        }
        
        // Level-specific tips
        when (level) {
            ProficiencyLevel.BEGINNER -> {
                tips.add("Use simple sentences - clarity over complexity")
                tips.add("Practice common phrases until they feel natural")
            }
            ProficiencyLevel.INTERMEDIATE -> {
                tips.add("Try connecting ideas with transition words")
                tips.add("Experiment with different tenses")
            }
            ProficiencyLevel.ADVANCED -> {
                tips.add("Work on idiomatic expressions")
                tips.add("Practice expressing nuanced opinions")
            }
            ProficiencyLevel.NATIVE_LEVEL -> {
                tips.add("Focus on style and eloquence")
                tips.add("Practice specialized vocabulary")
            }
        }
        
        return tips
    }
    
    /**
     * Check if user can skip ahead in break-in period
     */
    private fun checkSkipEligibility(
        profile: VerbalJournalProfile,
        readiness: ProgressionReadiness
    ): Boolean {
        return profile.currentLevel != ProficiencyLevel.BEGINNER &&
               readiness.overallReadiness >= 0.85f &&
               profile.totalSpeakingMinutes >= 60 // At least 1 hour total
    }
}

/**
 * Break-in period status result
 */
sealed class BreakInPeriodStatus {
    data class Success(
        val currentWeek: Int,
        val currentSchedule: BreakInSchedule,
        val nextSchedule: BreakInSchedule?,
        val progressionReadiness: ProgressionReadiness,
        val recommendedScheduleType: BreakInScheduleType,
        val weeklyTips: List<String>,
        val canSkipAhead: Boolean,
        val isComplete: Boolean
    ) : BreakInPeriodStatus()
    
    data class Error(val message: String) : BreakInPeriodStatus()
}

/**
 * Update schedule result
 */
sealed class UpdateScheduleResult {
    data class Success(
        val newScheduleType: BreakInScheduleType,
        val updatedSchedule: BreakInSchedule
    ) : UpdateScheduleResult()
    
    data class Error(val message: String) : UpdateScheduleResult()
}

/**
 * Skip week result
 */
sealed class SkipWeekResult {
    data class Success(
        val newWeek: Int,
        val newSchedule: BreakInSchedule
    ) : SkipWeekResult()
    
    data class Error(val message: String) : SkipWeekResult()
}

/**
 * Progression readiness metrics
 */
data class ProgressionReadiness(
    val sessionsCompleted: Int,
    val targetSessions: Int,
    val averageAccuracy: Float,
    val averageDuration: Int,
    val targetDuration: Int,
    val tierAccuracy: Float,
    val overallReadiness: Float,
    val readyToProgress: Boolean
)

/**
 * Break-in schedule types
 */
enum class BreakInScheduleType {
    STANDARD,
    ACCELERATED,
    GENTLE
}