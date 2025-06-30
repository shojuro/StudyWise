package com.studywise.ai.domain.repository

import com.studywise.ai.domain.model.verbaljournal.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.Date

/**
 * Repository interface for Verbal Journal functionality
 */
interface VerbalJournalRepository {
    
    // ===== Journal Entry Management =====
    
    /**
     * Create a new journal entry session
     */
    suspend fun createJournalEntry(
        userId: String,
        targetDurationMinutes: Int,
        correctionTier: CorrectionTier
    ): VerbalJournalEntry
    
    /**
     * Update an existing journal entry
     */
    suspend fun updateJournalEntry(entry: VerbalJournalEntry)
    
    /**
     * Get a specific journal entry
     */
    suspend fun getJournalEntry(entryId: String): VerbalJournalEntry?
    
    /**
     * Get all journal entries for a user
     */
    fun getUserJournalEntries(userId: String): Flow<List<VerbalJournalEntry>>
    
    /**
     * Get journal entries within a date range
     */
    fun getJournalEntriesInDateRange(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<VerbalJournalEntry>>
    
    /**
     * Delete a journal entry and all associated data
     */
    suspend fun deleteJournalEntry(entryId: String)
    
    // ===== Conversation Management =====
    
    /**
     * Add a conversation turn to an entry
     */
    suspend fun addConversationTurn(
        entryId: String,
        turn: ConversationTurn
    )
    
    /**
     * Get all conversation turns for an entry
     */
    suspend fun getConversationTurns(entryId: String): List<ConversationTurn>
    
    /**
     * Save audio file for a conversation turn
     */
    suspend fun saveAudioSegment(
        turnId: String,
        audioFile: File
    ): String // Returns file path
    
    // ===== Error Detection and Analysis =====
    
    /**
     * Add detected errors to a conversation turn
     */
    suspend fun addSpeechErrors(
        turnId: String,
        errors: List<ErrorInstance>
    )
    
    /**
     * Get all errors for a journal entry
     */
    suspend fun getEntryErrors(entryId: String): List<ErrorInstance>
    
    /**
     * Get error frequency analysis for a user
     */
    suspend fun getErrorFrequencyAnalysis(
        userId: String,
        startDate: Date
    ): Map<String, Int>
    
    // ===== Session Analysis =====
    
    /**
     * Save session analysis results
     */
    suspend fun saveSessionAnalysis(
        entryId: String,
        analysis: SessionAnalysis
    )
    
    /**
     * Get session analysis for an entry
     */
    suspend fun getSessionAnalysis(entryId: String): SessionAnalysis?
    
    /**
     * Get recent analyses for progress tracking
     */
    suspend fun getRecentAnalyses(
        userId: String,
        limit: Int = 10
    ): List<SessionAnalysis>
    
    // ===== User Profile Management =====
    
    /**
     * Get or create user's verbal journal profile
     */
    suspend fun getOrCreateProfile(
        userId: String,
        nativeLanguage: String
    ): VerbalJournalProfile
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(profile: VerbalJournalProfile)
    
    /**
     * Observe profile changes
     */
    fun observeProfile(userId: String): Flow<VerbalJournalProfile?>
    
    /**
     * Update user's current tier and level
     */
    suspend fun updateUserLevel(
        userId: String,
        tier: CorrectionTier,
        level: ProficiencyLevel
    )
    
    // ===== Progress Tracking =====
    
    /**
     * Update streak information
     */
    suspend fun updateStreak(
        userId: String,
        streakDays: Int,
        lastSessionDate: Date
    )
    
    /**
     * Add speaking minutes to total
     */
    suspend fun addSpeakingMinutes(
        userId: String,
        minutes: Int
    )
    
    /**
     * Save progress snapshot
     */
    suspend fun saveProgressSnapshot(
        userId: String,
        snapshot: ProgressSnapshot
    )
    
    /**
     * Get progress snapshots for a user
     */
    fun getProgressSnapshots(
        userId: String,
        startDate: Date
    ): Flow<List<ProgressSnapshot>>
    
    /**
     * Calculate current streak
     */
    suspend fun calculateCurrentStreak(userId: String): Int
    
    // ===== Daily Prompts =====
    
    /**
     * Get or generate daily prompt
     */
    suspend fun getDailyPrompt(
        date: Date,
        userLevel: ProficiencyLevel
    ): JournalPrompt
    
    /**
     * Get prompts for date range
     */
    fun getPromptsInRange(
        startDate: Date,
        endDate: Date
    ): Flow<List<JournalPrompt>>
    
    // ===== Achievements =====
    
    /**
     * Award achievement to user
     */
    suspend fun awardAchievement(
        userId: String,
        achievement: Achievement
    )
    
    /**
     * Get user's achievements
     */
    fun getUserAchievements(userId: String): Flow<List<Achievement>>
    
    /**
     * Check and award eligible achievements
     */
    suspend fun checkAndAwardAchievements(
        userId: String,
        sessionAnalysis: SessionAnalysis
    ): List<Achievement>
    
    // ===== Metrics and Analytics =====
    
    /**
     * Get comprehensive metrics for a user
     */
    suspend fun getUserMetrics(
        userId: String,
        period: String // "daily", "weekly", "monthly"
    ): VerbalJournalMetrics
    
    /**
     * Get session statistics
     */
    suspend fun getSessionStats(
        userId: String,
        startDate: Date
    ): SessionStatistics
    
    /**
     * Get improvement trends
     */
    suspend fun getImprovementTrends(
        userId: String,
        weeks: Int = 4
    ): ImprovementTrends
    
    // ===== Recommendations =====
    
    /**
     * Get personalized recommendations
     */
    suspend fun getRecommendations(
        userId: String,
        basedOnSessions: Int = 5
    ): List<Recommendation>
    
    /**
     * Save session recommendations
     */
    suspend fun saveRecommendations(
        analysisId: String,
        recommendations: List<Recommendation>
    )
    
    // ===== Audio Management =====
    
    /**
     * Clean up old audio files
     */
    suspend fun cleanupOldAudioFiles(olderThanDays: Int = 30)
    
    /**
     * Get total audio storage size for user
     */
    suspend fun getAudioStorageSize(userId: String): Long
}

// Additional data classes for repository operations

data class SessionStatistics(
    val totalSessions: Int,
    val totalMinutes: Int,
    val averageSessionLength: Float,
    val completionRate: Float,
    val activeDays: Int
)

data class ImprovementTrends(
    val accuracyTrend: List<AccuracyPoint>,
    val fluencyTrend: List<FluencyPoint>,
    val errorReductionTrend: Map<String, List<ErrorPoint>>,
    val overallImprovement: Float
)

data class AccuracyPoint(
    val date: Date,
    val accuracy: Float,
    val tier: CorrectionTier
)

data class FluencyPoint(
    val date: Date,
    val wordsPerMinute: Float,
    val pauseRatio: Float,
    val fillerCount: Int
)

data class ErrorPoint(
    val date: Date,
    val count: Int,
    val severity: ErrorSeverity
)