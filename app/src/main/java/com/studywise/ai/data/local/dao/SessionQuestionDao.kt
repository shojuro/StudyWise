package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.SessionQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionQuestion(sessionQuestion: SessionQuestionEntity)

    @Update
    suspend fun updateSessionQuestion(sessionQuestion: SessionQuestionEntity)

    @Query("SELECT * FROM session_questions WHERE sessionId = :sessionId ORDER BY presentedAt")
    fun getSessionQuestions(sessionId: String): Flow<List<SessionQuestionEntity>>

    @Query("SELECT * FROM session_questions WHERE id = :id")
    suspend fun getSessionQuestionById(id: String): SessionQuestionEntity?

    @Query("""
        SELECT COUNT(*) FROM session_questions 
        WHERE sessionId = :sessionId AND isCorrect = 1
    """)
    suspend fun getCorrectAnswerCount(sessionId: String): Int

    @Query("""
        SELECT AVG(timeSpentSeconds) FROM session_questions 
        WHERE sessionId = :sessionId AND answeredAt IS NOT NULL
    """)
    suspend fun getAverageTimePerQuestion(sessionId: String): Float?

    @Query("SELECT SUM(hintsUsed) FROM session_questions WHERE sessionId = :sessionId")
    suspend fun getTotalHintsUsed(sessionId: String): Int?
}