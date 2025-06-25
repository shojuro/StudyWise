package com.studywise.ai.domain.repository

import com.studywise.ai.domain.model.Question
import com.studywise.ai.domain.model.QuestionDifficulty

interface QuestionRepository {
    suspend fun generateQuestion(
        subject: String,
        gradeLevel: Int,
        difficulty: QuestionDifficulty,
        bookContext: String
    ): Result<Question>
    
    suspend fun getQuestionById(questionId: String): Result<Question>
    
    suspend fun saveQuestion(question: Question): Result<Unit>
}