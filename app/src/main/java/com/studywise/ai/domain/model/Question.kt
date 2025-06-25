package com.studywise.ai.domain.model

data class Question(
    val id: String,
    val text: String,
    val type: String, // multiple_choice, true_false, comprehension, reflection
    val difficulty: QuestionDifficulty,
    val hints: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val options: List<String>? = null, // For multiple choice questions
    val skillId: String? = null,
    val skillName: String? = null,
    val gradeLevel: Int? = null,
    val followUpQuestions: List<String> = emptyList()
)

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