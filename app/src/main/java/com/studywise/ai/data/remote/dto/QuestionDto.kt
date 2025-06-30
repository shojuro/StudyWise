package com.studywise.ai.data.remote.dto

import com.studywise.ai.data.local.entity.QuestionEntity
import java.util.Date

data class QuestionDto(
    val id: String,
    val subject: String,
    val gradeLevel: Int,
    val difficulty: String,
    val type: String,
    val content: String,
    val imageUrl: String?,
    val options: List<String>?,
    val correctAnswer: String,
    val explanation: String?,
    val hints: List<String>?,
    val points: Int,
    val skillIds: List<String>
) {
    fun toEntity(): QuestionEntity {
        return QuestionEntity(
            id = id.toLongOrNull() ?: 0L,
            skillId = skillIds.firstOrNull()?.toLongOrNull() ?: 1L, // Use first skill or default
            gradeLevel = gradeLevel,
            prompt = content,
            hints = hints?.joinToString("|") ?: "",
            type = type,
            difficulty = when(difficulty.uppercase()) {
                "EASY" -> 0.3f
                "MEDIUM" -> 0.5f
                "HARD" -> 0.7f
                else -> 0.5f
            },
            correctAnswer = correctAnswer,
            explanation = explanation,
            options = options?.joinToString("|"),
            followUpQuestions = null,
            skillSubCategory = null,
            createdAt = Date()
        )
    }
}