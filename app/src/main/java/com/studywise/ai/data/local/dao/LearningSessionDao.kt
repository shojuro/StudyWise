package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.LearningSessionEntity
import com.studywise.ai.data.local.entity.SessionStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface LearningSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LearningSessionEntity)

    @Update
    suspend fun updateSession(session: LearningSessionEntity)

    @Query("SELECT * FROM learning_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): LearningSessionEntity?

    @Query("SELECT * FROM learning_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getUserSessions(userId: String): Flow<List<LearningSessionEntity>>

    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND status = :status ORDER BY startedAt DESC")
    fun getUserSessionsByStatus(userId: String, status: SessionStatus): Flow<List<LearningSessionEntity>>

    @Query("""
        SELECT * FROM learning_sessions 
        WHERE userId = :userId 
        AND DATE(startedAt / 1000, 'unixepoch') = DATE(:date / 1000, 'unixepoch')
        ORDER BY startedAt DESC
    """)
    suspend fun getUserSessionsForDate(userId: String, date: Date): List<LearningSessionEntity>

    @Query("""
        UPDATE learning_sessions 
        SET completedAt = :completedAt, 
            status = :status,
            durationMinutes = :duration,
            questionsAnswered = :questionsAnswered,
            pointsEarned = :pointsEarned
        WHERE id = :sessionId
    """)
    suspend fun completeSession(
        sessionId: String,
        completedAt: Date,
        status: SessionStatus,
        duration: Int,
        questionsAnswered: Int,
        pointsEarned: Int
    )

    @Query("SELECT COUNT(*) FROM learning_sessions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getCompletedSessionCount(userId: String): Int

    @Query("SELECT SUM(pointsEarned) FROM learning_sessions WHERE userId = :userId")
    suspend fun getTotalPointsEarned(userId: String): Int?
    
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    suspend fun getAllSessions(userId: String): List<LearningSessionEntity>
    
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND subject = :subject ORDER BY startedAt DESC")
    suspend fun getSessionsBySubject(userId: String, subject: String): List<LearningSessionEntity>
    
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND startedAt >= :startDate AND startedAt <= :endDate ORDER BY startedAt DESC")
    suspend fun getSessionsBetweenDates(userId: String, startDate: Date, endDate: Date): List<LearningSessionEntity>
    
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND startedAt >= :date ORDER BY startedAt DESC")
    suspend fun getSessionsAfterDate(userId: String, date: Date): List<LearningSessionEntity>
    
    @Query("SELECT DISTINCT subject FROM learning_sessions WHERE userId = :userId")
    suspend fun getAllSubjects(userId: String): List<String>
    
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId AND syncStatus = 'PENDING'")
    suspend fun getPendingSyncSessions(userId: String): List<LearningSessionEntity>
}