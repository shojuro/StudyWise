package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Long): QuestionEntity?

    @Query("SELECT * FROM questions WHERE skillId = :skillId AND gradeLevel = :gradeLevel")
    suspend fun getQuestionsBySkillAndGrade(skillId: Long, gradeLevel: Int): List<QuestionEntity>
    
    @Query("SELECT * FROM questions WHERE skillId = :skillId AND gradeLevel = :gradeLevel")
    suspend fun getQuestionsForSkillAndGrade(skillId: Long, gradeLevel: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE gradeLevel = :gradeLevel")
    fun getQuestionsByGrade(gradeLevel: Int): Flow<List<QuestionEntity>>

    @Query("""
        SELECT q.* FROM questions q
        WHERE q.skillId = :skillId 
        AND q.gradeLevel = :gradeLevel
        AND q.id NOT IN (
            SELECT sq.questionId FROM session_questions sq
            WHERE sq.sessionId IN (
                SELECT id FROM learning_sessions 
                WHERE userId = :userId 
                AND completedAt IS NOT NULL
            )
        )
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getUnaskedQuestions(
        userId: String,
        skillId: Long,
        gradeLevel: Int,
        limit: Int
    ): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
}