package com.studywise.ai.domain.model

data class SubjectProgress(
    val subject: String,
    val progress: Float, // 0.0 to 1.0
    val lastPracticed: String?,
    val totalPoints: Int
)