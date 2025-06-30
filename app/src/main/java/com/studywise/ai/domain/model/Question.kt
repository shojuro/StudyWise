package com.studywise.ai.domain.model

import java.util.Date

data class Question(
    val id: String,
    val skillId: String,
    val gradeLevel: Int,
    val prompt: String, // The Socratic question text
    val type: String = "practice", // diagnostic, practice, challenge, assessment
    val difficulty: Float = 0.5f, // 0.0 to 1.0
    val hints: List<String> = emptyList(),
    val correctAnswer: String? = null,
    val explanation: String? = null,
    val options: List<String>? = null, // For multiple choice questions
    val followUpQuestions: List<String>? = null,
    val skillSubCategory: String? = null,
    val metadata: String? = null,
    val createdAt: Date = Date()
) {
    // Legacy text property for backward compatibility
    val text: String get() = prompt
    
    // Convert float difficulty to enum for backward compatibility
    val difficultyEnum: QuestionDifficulty get() = when {
        difficulty <= 0.33f -> QuestionDifficulty.EASY
        difficulty <= 0.66f -> QuestionDifficulty.MEDIUM
        else -> QuestionDifficulty.HARD
    }
}

enum class QuestionDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class QuestionSession(
    val currentQuestion: Question,
    val hintsUsed: Int = 0,
    val startTime: Long = System.currentTimeMillis()
)