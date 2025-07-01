package com.studywise.ai.domain.repository

import com.studywise.ai.domain.model.verbaljournal.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface VerbalJournalRepository {
    
    // Entry operations
    suspend fun createEntry(
        userId: Int,
        sessionType: SessionType,
        topic: String? = null,
        promptId: String? = null,
        targetDurationMinutes: Int,
        language: String = "en",
        difficulty: DifficultyLevel = DifficultyLevel.INTERMEDIATE
    ): VerbalJournalEntry
    
    suspend fun updateEntry(entry: VerbalJournalEntry)
    
    suspend fun getEntry(entryId: String): VerbalJournalEntry?
    
    fun getUserEntries(userId: Int): Flow<List<VerbalJournalEntry>>
    
    suspend fun getRecentCompletedEntries(userId: Int, limit: Int = 10): List<VerbalJournalEntry>
    
    suspend fun completeSession(entryId: String, completedAt: LocalDateTime = LocalDateTime.now())
    
    // Conversation operations
    suspend fun addConversationTurn(
        entryId: String,
        role: ConversationRole,
        transcription: String,
        audioFilePath: String? = null,
        durationSeconds: Float,
        metrics: SpeechMetrics? = null
    ): ConversationTurn
    
    suspend fun getConversationTurns(entryId: String): List<ConversationTurn>
    
    // Error operations
    suspend fun addSpeechErrors(turnId: String, errors: List<SpeechError>)
    
    suspend fun getSpeechErrors(turnId: String): List<SpeechError>
    
    suspend fun getSessionErrors(entryId: String): List<SpeechError>
    
    // Analysis operations
    suspend fun saveSessionAnalysis(analysis: SessionAnalysis)
    
    suspend fun getSessionAnalysis(entryId: String): SessionAnalysis?
    
    // Profile operations
    suspend fun createOrUpdateProfile(profile: VerbalJournalProfile)
    
    suspend fun getUserProfile(userId: Int): VerbalJournalProfile?
    
    fun observeUserProfile(userId: Int): Flow<VerbalJournalProfile?>
    
    suspend fun updateProfileStatistics(
        userId: Int,
        sessionCompleted: Boolean,
        minutesSpoken: Int,
        lastSessionDate: LocalDateTime
    )
    
    suspend fun updateStreak(userId: Int, currentStreak: Int, longestStreak: Int)
    
    // Improvement areas
    suspend fun addImprovementAreas(profileId: String, areas: List<ImprovementArea>)
    
    suspend fun updateImprovementArea(area: ImprovementArea)
    
    suspend fun getActiveImprovementAreas(profileId: String): List<ImprovementArea>
    
    suspend fun markImprovementAreaAchieved(areaId: String, achievedAt: LocalDateTime = LocalDateTime.now())
    
    // Recommendations
    suspend fun addRecommendations(analysisId: String, recommendations: List<SessionRecommendation>)
    
    suspend fun getSessionRecommendations(entryId: String): List<SessionRecommendation>
    
    suspend fun markRecommendationCompleted(recommendationId: String, completedAt: LocalDateTime = LocalDateTime.now())
    
    // Daily prompts
    suspend fun getDailyPrompt(date: LocalDate = LocalDate.now()): DailyPrompt?
    
    suspend fun getPromptsForDateRange(startDate: LocalDate, endDate: LocalDate): List<DailyPrompt>
    
    suspend fun saveDailyPrompt(prompt: DailyPrompt)
    
    // Achievements
    suspend fun createAchievement(achievement: Achievement)
    
    suspend fun updateAchievementProgress(achievementId: String, progress: Float)
    
    suspend fun unlockAchievement(achievementId: String, unlockedAt: LocalDateTime = LocalDateTime.now())
    
    fun observeAchievements(profileId: String): Flow<List<Achievement>>
    
    suspend fun getUnlockedAchievements(profileId: String): List<Achievement>
    
    suspend fun checkAndUnlockAchievements(profileId: String): List<Achievement>
    
    // Progress tracking
    suspend fun saveProgressSnapshot(snapshot: SpeechProgressSnapshot)
    
    suspend fun getRecentSnapshots(
        profileId: String,
        periodType: PeriodType,
        limit: Int = 10
    ): List<SpeechProgressSnapshot>
    
    suspend fun getProgressInDateRange(
        profileId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<SpeechProgressSnapshot>
    
    suspend fun generateProgressSnapshot(
        profileId: String,
        periodType: PeriodType
    ): SpeechProgressSnapshot
    
    // Statistics and analytics
    suspend fun getTotalSessionTime(userId: Int): Int
    
    suspend fun getAverageSessionDuration(userId: Int): Float
    
    suspend fun getSessionCountByType(userId: Int): Map<SessionType, Int>
    
    suspend fun getMostCommonErrors(userId: Int, limit: Int = 10): List<ErrorPattern>
    
    suspend fun getSkillProgress(userId: Int): Map<ImprovementAreaType, Float>
    
    suspend fun getCompletedSessionsOnDate(userId: Int, date: LocalDateTime): Int
    
    // Break-in period
    suspend fun getBreakInSchedule(userId: Int): BreakInSchedule?
    
    suspend fun isUserInBreakInPeriod(userId: Int): Boolean
    
    suspend fun getWeekInProgram(userId: Int): Int?
    
    // Cleanup
    suspend fun deleteOldAudioFiles(olderThan: LocalDateTime)
    
    suspend fun cleanupIncompleteSession(entryId: String)
}