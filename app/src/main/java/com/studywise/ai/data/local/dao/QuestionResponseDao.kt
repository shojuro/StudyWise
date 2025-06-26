package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.QuestionResponseEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface QuestionResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: QuestionResponseEntity)
    
    @Update
    suspend fun updateResponse(response: QuestionResponseEntity)
    
    @Query("SELECT * FROM question_responses WHERE userId = :userId AND questionId = :questionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestResponse(userId: String, questionId: String): QuestionResponseEntity?
    
    @Query("SELECT * FROM question_responses WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserResponses(userId: String): Flow<List<QuestionResponseEntity>>
    
    @Query("SELECT * FROM question_responses WHERE userId = :userId AND syncStatus = 'PENDING'")
    suspend fun getPendingSyncResponses(userId: String): List<QuestionResponseEntity>
    
    @Query("SELECT COUNT(*) FROM question_responses WHERE userId = :userId AND isCorrect = 1")
    suspend fun getCorrectAnswerCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM question_responses WHERE userId = :userId")
    suspend fun getTotalAnswerCount(userId: String): Int
    
    @Query("SELECT * FROM question_responses WHERE userId = :userId AND timestamp >= :startDate AND timestamp <= :endDate")
    suspend fun getResponsesBetweenDates(userId: String, startDate: Date, endDate: Date): List<QuestionResponseEntity>
}