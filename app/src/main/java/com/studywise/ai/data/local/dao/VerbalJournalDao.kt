package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Objects for the Verbal Journal system
 */

@Dao
interface VerbalJournalDao {
    
    // ===== Verbal Journal Entries =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: VerbalJournalEntryEntity)
    
    @Update
    suspend fun updateJournalEntry(entry: VerbalJournalEntryEntity)
    
    @Delete
    suspend fun deleteJournalEntry(entry: VerbalJournalEntryEntity)
    
    @Query("SELECT * FROM verbal_journal_entries WHERE id = :entryId")
    suspend fun getJournalEntry(entryId: String): VerbalJournalEntryEntity?
    
    @Query("SELECT * FROM verbal_journal_entries WHERE userId = :userId ORDER BY sessionDate DESC")
    fun getUserJournalEntries(userId: String): Flow<List<VerbalJournalEntryEntity>>
    
    @Query("SELECT * FROM verbal_journal_entries WHERE userId = :userId AND sessionDate >= :startDate AND sessionDate <= :endDate ORDER BY sessionDate DESC")
    fun getUserJournalEntriesInDateRange(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<VerbalJournalEntryEntity>>
    
    @Query("SELECT * FROM verbal_journal_entries WHERE userId = :userId AND status = :status ORDER BY sessionDate DESC")
    fun getUserJournalEntriesByStatus(
        userId: String,
        status: String
    ): Flow<List<VerbalJournalEntryEntity>>
    
    @Query("SELECT COUNT(*) FROM verbal_journal_entries WHERE userId = :userId AND sessionDate >= :startDate")
    suspend fun getSessionCountSince(userId: String, startDate: Date): Int
    
    @Query("SELECT SUM(actualDurationMinutes) FROM verbal_journal_entries WHERE userId = :userId AND sessionDate >= :startDate")
    suspend fun getTotalMinutesSince(userId: String, startDate: Date): Int?
    
    // ===== Conversation Turns =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationTurn(turn: ConversationTurnEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationTurns(turns: List<ConversationTurnEntity>)
    
    @Query("SELECT * FROM conversation_turns WHERE journalEntryId = :entryId ORDER BY sequenceNumber")
    suspend fun getConversationTurns(entryId: String): List<ConversationTurnEntity>
    
    @Query("DELETE FROM conversation_turns WHERE journalEntryId = :entryId")
    suspend fun deleteConversationTurns(entryId: String)
    
    // ===== Speech Errors =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeechError(error: SpeechErrorEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeechErrors(errors: List<SpeechErrorEntity>)
    
    @Query("SELECT * FROM speech_errors WHERE conversationTurnId = :turnId")
    suspend fun getSpeechErrors(turnId: String): List<SpeechErrorEntity>
    
    @Query("""
        SELECT se.* FROM speech_errors se
        INNER JOIN conversation_turns ct ON se.conversationTurnId = ct.id
        WHERE ct.journalEntryId = :entryId
        ORDER BY ct.sequenceNumber, se.timestampMillis
    """)
    suspend fun getAllErrorsForEntry(entryId: String): List<SpeechErrorEntity>
    
    @Query("""
        SELECT errorType, COUNT(*) as count FROM speech_errors se
        INNER JOIN conversation_turns ct ON se.conversationTurnId = ct.id
        INNER JOIN verbal_journal_entries vje ON ct.journalEntryId = vje.id
        WHERE vje.userId = :userId AND vje.sessionDate >= :startDate
        GROUP BY errorType
        ORDER BY count DESC
    """)
    suspend fun getErrorFrequencySince(userId: String, startDate: Date): List<ErrorFrequency>
    
    // ===== Session Analysis =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionAnalysis(analysis: SessionAnalysisEntity)
    
    @Query("SELECT * FROM session_analyses WHERE sessionId = :sessionId")
    suspend fun getSessionAnalysis(sessionId: String): SessionAnalysisEntity?
    
    @Query("""
        SELECT sa.* FROM session_analyses sa
        INNER JOIN verbal_journal_entries vje ON sa.sessionId = vje.id
        WHERE vje.userId = :userId
        ORDER BY sa.analyzedAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentAnalyses(userId: String, limit: Int): List<SessionAnalysisEntity>
    
    // ===== Verbal Journal Profile =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: VerbalJournalProfileEntity)
    
    @Query("SELECT * FROM verbal_journal_profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): VerbalJournalProfileEntity?
    
    @Query("SELECT * FROM verbal_journal_profiles WHERE userId = :userId")
    fun observeProfile(userId: String): Flow<VerbalJournalProfileEntity?>
    
    @Query("UPDATE verbal_journal_profiles SET streakDays = :streakDays, lastSessionDate = :lastSessionDate WHERE userId = :userId")
    suspend fun updateStreak(userId: String, streakDays: Int, lastSessionDate: Date)
    
    @Query("UPDATE verbal_journal_profiles SET totalSpeakingMinutes = totalSpeakingMinutes + :minutes WHERE userId = :userId")
    suspend fun addSpeakingMinutes(userId: String, minutes: Int)
    
    @Query("UPDATE verbal_journal_profiles SET currentTier = :tier, currentLevel = :level WHERE userId = :userId")
    suspend fun updateUserLevel(userId: String, tier: String, level: String)
    
    // ===== Improvement Areas =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImprovementAreas(areas: List<ImprovementAreaEntity>)
    
    @Query("SELECT * FROM improvement_areas WHERE analysisId = :analysisId ORDER BY frequency DESC")
    suspend fun getImprovementAreas(analysisId: String): List<ImprovementAreaEntity>
    
    // ===== Recommendations =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<SessionRecommendationEntity>)
    
    @Query("SELECT * FROM session_recommendations WHERE analysisId = :analysisId ORDER BY priority DESC")
    suspend fun getRecommendations(analysisId: String): List<SessionRecommendationEntity>
    
    // ===== Daily Prompts =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyPrompt(prompt: DailyJournalPromptEntity)
    
    @Query("SELECT * FROM daily_journal_prompts WHERE date = :date")
    suspend fun getDailyPrompt(date: Date): DailyJournalPromptEntity?
    
    @Query("SELECT * FROM daily_journal_prompts WHERE date >= :startDate AND date <= :endDate ORDER BY date")
    fun getPromptsInRange(startDate: Date, endDate: Date): Flow<List<DailyJournalPromptEntity>>
    
    // ===== Achievements =====
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: VerbalJournalAchievementEntity)
    
    @Query("SELECT * FROM verbal_journal_achievements WHERE userId = :userId ORDER BY earnedAt DESC")
    fun getUserAchievements(userId: String): Flow<List<VerbalJournalAchievementEntity>>
    
    @Query("SELECT COUNT(*) FROM verbal_journal_achievements WHERE userId = :userId")
    suspend fun getAchievementCount(userId: String): Int
    
    // ===== Progress Snapshots =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressSnapshot(snapshot: SpeechProgressSnapshotEntity)
    
    @Query("SELECT * FROM speech_progress_snapshots WHERE userId = :userId AND date >= :startDate ORDER BY date")
    fun getProgressSnapshots(userId: String, startDate: Date): Flow<List<SpeechProgressSnapshotEntity>>
    
    @Query("SELECT * FROM speech_progress_snapshots WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestSnapshot(userId: String): SpeechProgressSnapshotEntity?
    
    // ===== Metrics and Analytics =====
    
    @Query("""
        SELECT 
            AVG(overallAccuracy) as averageAccuracy,
            COUNT(*) as sessionCount,
            SUM(actualDurationMinutes) as totalMinutes,
            AVG(wordsSpoken) as averageWords
        FROM verbal_journal_entries 
        WHERE userId = :userId AND sessionDate >= :startDate AND status = 'ANALYZED'
    """)
    suspend fun getBasicMetrics(userId: String, startDate: Date): BasicMetrics?
    
    @Query("""
        SELECT 
            COUNT(DISTINCT DATE(sessionDate)) as activeDays,
            MAX(actualDurationMinutes) as longestSession,
            MIN(overallAccuracy) as lowestAccuracy,
            MAX(overallAccuracy) as highestAccuracy
        FROM verbal_journal_entries 
        WHERE userId = :userId AND sessionDate >= :startDate AND status = 'ANALYZED'
    """)
    suspend fun getAdvancedMetrics(userId: String, startDate: Date): AdvancedMetrics?
    
    // ===== Streak Calculation =====
    
    @Query("""
        WITH RECURSIVE dates(date) AS (
            SELECT DATE('now', '-30 days')
            UNION ALL
            SELECT DATE(date, '+1 day')
            FROM dates
            WHERE date < DATE('now')
        ),
        session_dates AS (
            SELECT DISTINCT DATE(sessionDate) as session_date
            FROM verbal_journal_entries
            WHERE userId = :userId AND status != 'ACTIVE'
        )
        SELECT COUNT(*) as streak
        FROM (
            SELECT date,
                   CASE WHEN session_date IS NOT NULL THEN 1 ELSE 0 END as has_session,
                   ROW_NUMBER() OVER (ORDER BY date DESC) as rn
            FROM dates
            LEFT JOIN session_dates ON dates.date = session_dates.session_date
            ORDER BY date DESC
        )
        WHERE has_session = 1 AND rn <= (
            SELECT MIN(rn) 
            FROM (
                SELECT ROW_NUMBER() OVER (ORDER BY date DESC) as rn,
                       CASE WHEN session_date IS NOT NULL THEN 1 ELSE 0 END as has_session
                FROM dates
                LEFT JOIN session_dates ON dates.date = session_dates.session_date
            )
            WHERE has_session = 0
        )
    """)
    suspend fun calculateCurrentStreak(userId: String): Int
}

// Data classes for complex queries
data class ErrorFrequency(
    val errorType: String,
    val count: Int
)

data class BasicMetrics(
    val averageAccuracy: Float?,
    val sessionCount: Int,
    val totalMinutes: Int?,
    val averageWords: Float?
)

data class AdvancedMetrics(
    val activeDays: Int,
    val longestSession: Int?,
    val lowestAccuracy: Float?,
    val highestAccuracy: Float?
)