package com.studywise.ai.domain.usecase.verbaljournal

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.VerbalJournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use case for getting Verbal Journal progress and statistics
 */
class GetVerbalJournalProgressUseCase @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository
) {
    
    /**
     * Get comprehensive progress data for a user
     */
    operator fun invoke(userId: String): Flow<VerbalJournalProgress> {
        return combine(
            verbalJournalRepository.observeProfile(userId),
            verbalJournalRepository.getUserAchievements(userId),
            verbalJournalRepository.getUserJournalEntries(userId),
            verbalJournalRepository.getProgressSnapshots(
                userId,
                java.util.Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
            )
        ) { profile, achievements, entries, snapshots ->
            
            val userProfile = profile ?: throw IllegalStateException("Profile not found")
            
            // Calculate statistics
            val totalSessions = entries.size
            val completedSessions = entries.count { it.status == SessionStatus.ANALYZED }
            val totalMinutes = entries.sumOf { it.actualDurationMinutes }
            val averageAccuracy = entries.mapNotNull { it.sessionAnalysis }
                .map { it.overallStats.overallAccuracy }
                .average()
                .takeIf { !it.isNaN() } ?: 0.0
            
            // Get recent performance
            val recentPerformance = snapshots.takeLast(7).map { snapshot ->
                DailyPerformance(
                    date = snapshot.date,
                    accuracy = snapshot.accuracy,
                    fluencyScore = snapshot.fluencyScore,
                    minutesSpoken = snapshot.minutesSpoken,
                    errorsFound = snapshot.errorsFound
                )
            }
            
            // Get improvement trends
            val improvementTrends = calculateImprovementTrends(snapshots)
            
            // Get next milestones
            val nextMilestones = getNextMilestones(userProfile, totalSessions, totalMinutes)
            
            // Get skill breakdown
            val skillBreakdown = calculateSkillBreakdown(entries)
            
            VerbalJournalProgress(
                profile = userProfile,
                totalSessions = totalSessions,
                completedSessions = completedSessions,
                totalMinutesSpoken = totalMinutes,
                currentStreak = userProfile.streakDays,
                longestStreak = calculateLongestStreak(entries),
                averageAccuracy = averageAccuracy.toFloat(),
                achievements = achievements,
                recentPerformance = recentPerformance,
                improvementTrends = improvementTrends,
                nextMilestones = nextMilestones,
                skillBreakdown = skillBreakdown,
                weeklyGoalProgress = calculateWeeklyGoalProgress(userProfile, entries)
            )
        }
    }
    
    /**
     * Calculate improvement trends from snapshots
     */
    private fun calculateImprovementTrends(
        snapshots: List<ProgressSnapshot>
    ): ImprovementTrends {
        if (snapshots.size < 2) {
            return ImprovementTrends(
                accuracyTrend = emptyList(),
                fluencyTrend = emptyList(),
                errorReductionTrend = emptyMap(),
                overallImprovement = 0f
            )
        }
        
        val accuracyTrend = snapshots.map { 
            TrendPoint(it.date, it.accuracy)
        }
        
        val fluencyTrend = snapshots.map {
            TrendPoint(it.date, it.fluencyScore)
        }
        
        // Calculate error reduction by tracking total errors over time
        val errorTrend = mutableMapOf<String, Float>()
        val firstSnapshot = snapshots.first()
        val lastSnapshot = snapshots.last()
        
        val errorReduction = if (firstSnapshot.errorsFound > 0) {
            (firstSnapshot.errorsFound - lastSnapshot.errorsFound).toFloat() / firstSnapshot.errorsFound
        } else 0f
        
        val overallImprovement = (
            (lastSnapshot.accuracy - firstSnapshot.accuracy) +
            (lastSnapshot.fluencyScore - firstSnapshot.fluencyScore)
        ) / 2
        
        return ImprovementTrends(
            accuracyTrend = accuracyTrend,
            fluencyTrend = fluencyTrend,
            errorReductionTrend = mapOf("overall" to errorReduction),
            overallImprovement = overallImprovement
        )
    }
    
    /**
     * Calculate longest streak from entries
     */
    private fun calculateLongestStreak(entries: List<VerbalJournalEntry>): Int {
        if (entries.isEmpty()) return 0
        
        val sortedDates = entries
            .map { it.sessionDate }
            .sortedBy { it.time }
            .map { it.toCalendarDay() }
            .distinct()
        
        var longestStreak = 1
        var currentStreak = 1
        
        for (i in 1 until sortedDates.size) {
            val daysDiff = sortedDates[i] - sortedDates[i - 1]
            if (daysDiff == 1L) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
        
        return longestStreak
    }
    
    /**
     * Get next milestones to achieve
     */
    private fun getNextMilestones(
        profile: VerbalJournalProfile,
        totalSessions: Int,
        totalMinutes: Int
    ): List<Milestone> {
        val milestones = mutableListOf<Milestone>()
        
        // Streak milestones
        val streakMilestones = listOf(7, 14, 30, 60, 100)
        streakMilestones.firstOrNull { it > profile.streakDays }?.let { target ->
            milestones.add(
                Milestone(
                    name = "$target Day Streak",
                    description = "Practice for $target days in a row",
                    progress = profile.streakDays.toFloat() / target,
                    target = target,
                    current = profile.streakDays,
                    reward = "Unlock '${getStreakRewardName(target)}' achievement"
                )
            )
        }
        
        // Session milestones
        val sessionMilestones = listOf(10, 25, 50, 100, 200)
        sessionMilestones.firstOrNull { it > totalSessions }?.let { target ->
            milestones.add(
                Milestone(
                    name = "$target Sessions",
                    description = "Complete $target speaking sessions",
                    progress = totalSessions.toFloat() / target,
                    target = target,
                    current = totalSessions,
                    reward = "Unlock new conversation topics"
                )
            )
        }
        
        // Speaking time milestones (in hours)
        val hourMilestones = listOf(5, 10, 25, 50, 100)
        val totalHours = totalMinutes / 60
        hourMilestones.firstOrNull { it > totalHours }?.let { target ->
            milestones.add(
                Milestone(
                    name = "$target Hours Speaking",
                    description = "Accumulate $target hours of speaking practice",
                    progress = totalHours.toFloat() / target,
                    target = target * 60, // Convert to minutes
                    current = totalMinutes,
                    reward = "Unlock advanced conversation features"
                )
            )
        }
        
        // Tier progression milestone
        if (profile.currentTier != CorrectionTier.COMPREHENSIVE) {
            milestones.add(
                Milestone(
                    name = "Next Correction Tier",
                    description = "Progress to ${getNextTier(profile.currentTier)} corrections",
                    progress = 0.7f, // Would calculate based on recent accuracy
                    target = 1,
                    current = 0,
                    reward = "Access more detailed error corrections"
                )
            )
        }
        
        return milestones.sortedBy { it.progress }.reversed().take(3)
    }
    
    /**
     * Calculate skill breakdown from recent sessions
     */
    private fun calculateSkillBreakdown(
        entries: List<VerbalJournalEntry>
    ): Map<String, SkillLevel> {
        val recentAnalyses = entries
            .mapNotNull { it.sessionAnalysis }
            .takeLast(10)
        
        if (recentAnalyses.isEmpty()) {
            return mapOf(
                "Grammar" to SkillLevel(0.5f, "Developing"),
                "Vocabulary" to SkillLevel(0.5f, "Developing"),
                "Fluency" to SkillLevel(0.5f, "Developing"),
                "Pronunciation" to SkillLevel(0.5f, "Developing")
            )
        }
        
        // Calculate average scores
        val grammarScore = recentAnalyses
            .map { it.overallStats.overallAccuracy }
            .average()
            .toFloat()
        
        val vocabularyScore = recentAnalyses
            .map { it.overallStats.vocabularyDiversity }
            .average()
            .toFloat()
        
        val fluencyScore = recentAnalyses
            .map { it.overallStats.fluencyScore }
            .average()
            .toFloat()
        
        val pronunciationScore = recentAnalyses
            .map { it.overallStats.pronunciationScore }
            .average()
            .toFloat()
        
        return mapOf(
            "Grammar" to SkillLevel(grammarScore, getSkillLabel(grammarScore)),
            "Vocabulary" to SkillLevel(vocabularyScore, getSkillLabel(vocabularyScore)),
            "Fluency" to SkillLevel(fluencyScore, getSkillLabel(fluencyScore)),
            "Pronunciation" to SkillLevel(pronunciationScore, getSkillLabel(pronunciationScore))
        )
    }
    
    /**
     * Calculate weekly goal progress
     */
    private fun calculateWeeklyGoalProgress(
        profile: VerbalJournalProfile,
        entries: List<VerbalJournalEntry>
    ): WeeklyGoalProgress {
        val weekStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        
        val thisWeekEntries = entries.filter { it.sessionDate >= weekStart }
        val minutesThisWeek = thisWeekEntries.sumOf { it.actualDurationMinutes }
        val daysActive = thisWeekEntries.map { it.sessionDate.toCalendarDay() }.distinct().size
        
        val weeklyGoalMinutes = profile.dailyTargetMinutes * 7
        
        return WeeklyGoalProgress(
            targetMinutes = weeklyGoalMinutes,
            completedMinutes = minutesThisWeek,
            daysActive = daysActive,
            targetDays = 5, // Aim for 5 days a week
            progress = minOf(1f, minutesThisWeek.toFloat() / weeklyGoalMinutes)
        )
    }
    
    /**
     * Helper function to get streak reward name
     */
    private fun getStreakRewardName(days: Int): String {
        return when (days) {
            7 -> "Week Warrior"
            14 -> "Fortnight Fighter"
            30 -> "Monthly Master"
            60 -> "Consistency Champion"
            100 -> "Century Speaker"
            else -> "Streak Master"
        }
    }
    
    /**
     * Helper function to get skill label
     */
    private fun getSkillLabel(score: Float): String {
        return when {
            score >= 0.9f -> "Excellent"
            score >= 0.8f -> "Proficient"
            score >= 0.7f -> "Good"
            score >= 0.6f -> "Developing"
            else -> "Needs Practice"
        }
    }
    
    /**
     * Helper function to get next tier
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
     * Extension function to convert Date to calendar day
     */
    private fun java.util.Date.toCalendarDay(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.time = this
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis / (1000 * 60 * 60 * 24)
    }
}

/**
 * Comprehensive progress data
 */
data class VerbalJournalProgress(
    val profile: VerbalJournalProfile,
    val totalSessions: Int,
    val completedSessions: Int,
    val totalMinutesSpoken: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val averageAccuracy: Float,
    val achievements: List<Achievement>,
    val recentPerformance: List<DailyPerformance>,
    val improvementTrends: ImprovementTrends,
    val nextMilestones: List<Milestone>,
    val skillBreakdown: Map<String, SkillLevel>,
    val weeklyGoalProgress: WeeklyGoalProgress
)

/**
 * Daily performance data
 */
data class DailyPerformance(
    val date: java.util.Date,
    val accuracy: Float,
    val fluencyScore: Float,
    val minutesSpoken: Int,
    val errorsFound: Int
)

/**
 * Trend point for charts
 */
data class TrendPoint(
    val date: java.util.Date,
    val value: Float
)

/**
 * Milestone to achieve
 */
data class Milestone(
    val name: String,
    val description: String,
    val progress: Float,
    val target: Int,
    val current: Int,
    val reward: String
)

/**
 * Skill level assessment
 */
data class SkillLevel(
    val score: Float,
    val label: String
)

/**
 * Weekly goal progress
 */
data class WeeklyGoalProgress(
    val targetMinutes: Int,
    val completedMinutes: Int,
    val daysActive: Int,
    val targetDays: Int,
    val progress: Float
)