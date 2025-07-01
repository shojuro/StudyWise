package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.verbaljournal.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface VerbalJournalDao {
    
    // VerbalJournalEntry operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: VerbalJournalEntryEntity)
    
    @Update
    suspend fun updateEntry(entry: VerbalJournalEntryEntity)
    
    @Query("SELECT * FROM verbal_journal_entries WHERE id = :entryId")
    suspend fun getEntry(entryId: String): VerbalJournalEntryEntity?
    
    @Query("SELECT * FROM verbal_journal_entries WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserEntries(userId: Int): Flow<List<VerbalJournalEntryEntity>>
    
    @Query("SELECT * FROM verbal_journal_entries WHERE userId = :userId AND status = 'COMPLETED' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentCompletedEntries(userId: Int, limit: Int): List<VerbalJournalEntryEntity>
    
    // ConversationTurn operations
    @Insert
    suspend fun insertTurn(turn: ConversationTurnEntity)
    
    @Query("SELECT * FROM conversation_turns WHERE entryId = :entryId ORDER BY turnNumber")
    suspend fun getEntryTurns(entryId: String): List<ConversationTurnEntity>
    
    // SpeechError operations
    @Insert
    suspend fun insertErrors(errors: List<SpeechErrorEntity>)
    
    @Query("SELECT * FROM speech_errors WHERE turnId = :turnId")
    suspend fun getTurnErrors(turnId: String): List<SpeechErrorEntity>
    
    @Query("""
        SELECT se.* FROM speech_errors se
        INNER JOIN conversation_turns ct ON se.turnId = ct.id
        WHERE ct.entryId = :entryId
    """)
    suspend fun getEntryErrors(entryId: String): List<SpeechErrorEntity>
    
    // SessionAnalysis operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: SessionAnalysisEntity)
    
    @Query("SELECT * FROM session_analyses WHERE entryId = :entryId")
    suspend fun getAnalysis(entryId: String): SessionAnalysisEntity?
    
    // VerbalJournalProfile operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VerbalJournalProfileEntity)
    
    @Update
    suspend fun updateProfile(profile: VerbalJournalProfileEntity)
    
    @Query("SELECT * FROM verbal_journal_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: Int): VerbalJournalProfileEntity?
    
    @Query("SELECT * FROM verbal_journal_profiles WHERE userId = :userId")
    fun observeUserProfile(userId: Int): Flow<VerbalJournalProfileEntity?>
    
    // ImprovementArea operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImprovementAreas(areas: List<ImprovementAreaEntity>)
    
    @Update
    suspend fun updateImprovementArea(area: ImprovementAreaEntity)
    
    @Query("SELECT * FROM improvement_areas WHERE profileId = :profileId AND isActive = 1 ORDER BY priority")
    suspend fun getActiveImprovementAreas(profileId: String): List<ImprovementAreaEntity>
    
    // SessionRecommendation operations
    @Insert
    suspend fun insertRecommendations(recommendations: List<SessionRecommendationEntity>)
    
    @Query("""
        SELECT sr.* FROM session_recommendations sr
        INNER JOIN session_analyses sa ON sr.analysisId = sa.id
        WHERE sa.entryId = :entryId AND sr.isCompleted = 0
        ORDER BY sr.priority
    """)
    suspend fun getEntryRecommendations(entryId: String): List<SessionRecommendationEntity>
    
    // DailyJournalPrompt operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: DailyJournalPromptEntity)
    
    @Query("SELECT * FROM daily_journal_prompts WHERE date = :date AND isActive = 1")
    suspend fun getDailyPrompt(date: LocalDate): DailyJournalPromptEntity?
    
    @Query("SELECT * FROM daily_journal_prompts WHERE date >= :startDate AND date <= :endDate AND isActive = 1 ORDER BY date")
    suspend fun getPromptsInRange(startDate: LocalDate, endDate: LocalDate): List<DailyJournalPromptEntity>
    
    // VerbalJournalAchievement operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: VerbalJournalAchievementEntity)
    
    @Update
    suspend fun updateAchievement(achievement: VerbalJournalAchievementEntity)
    
    @Query("SELECT * FROM verbal_journal_achievements WHERE profileId = :profileId")
    fun observeAchievements(profileId: String): Flow<List<VerbalJournalAchievementEntity>>
    
    @Query("SELECT * FROM verbal_journal_achievements WHERE profileId = :profileId AND isUnlocked = 1 ORDER BY unlockedAt DESC")
    suspend fun getUnlockedAchievements(profileId: String): List<VerbalJournalAchievementEntity>
    
    // SpeechProgressSnapshot operations
    @Insert
    suspend fun insertSnapshot(snapshot: SpeechProgressSnapshotEntity)
    
    @Query("""
        SELECT * FROM speech_progress_snapshots 
        WHERE profileId = :profileId AND periodType = :periodType 
        ORDER BY snapshotDate DESC 
        LIMIT :limit
    """)
    suspend fun getRecentSnapshots(profileId: String, periodType: String, limit: Int): List<SpeechProgressSnapshotEntity>
    
    @Query("""
        SELECT * FROM speech_progress_snapshots 
        WHERE profileId = :profileId 
        AND snapshotDate >= :startDate 
        AND snapshotDate <= :endDate
        ORDER BY snapshotDate
    """)
    suspend fun getSnapshotsInRange(
        profileId: String, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): List<SpeechProgressSnapshotEntity>
    
    // Composite queries
    @Transaction
    @Query("""
        SELECT COUNT(*) FROM verbal_journal_entries 
        WHERE userId = :userId 
        AND status = 'COMPLETED' 
        AND DATE(createdAt) = DATE(:date)
    """)
    suspend fun getCompletedSessionsOnDate(userId: Int, date: LocalDateTime): Int
    
    @Transaction
    suspend fun completeSession(entryId: String, completedAt: LocalDateTime) {
        val entry = getEntry(entryId)
        entry?.let {
            updateEntry(it.copy(status = "COMPLETED", completedAt = completedAt))
        }
    }
}