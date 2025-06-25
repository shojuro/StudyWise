package com.studywise.ai.domain.model

data class Question(
    val id: String,
    val skillId: String,
    val skillName: String,
    val gradeLevel: Int,
    val prompt: String,
    val hints: List<String>,
    val followUpQuestions: List<String> = emptyList(),
    val skillSubCategory: String? = null
)

data class QuestionSession(
    val currentQuestion: Question,
    val hintsUsed: Int = 0,
    val startTime: Long = System.currentTimeMillis()
)