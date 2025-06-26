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
            id = id,
            skillId = skillIds.firstOrNull() ?: subject, // Use first skill or subject as fallback
            gradeLevel = gradeLevel,
            prompt = content,
            hints = hints?.joinToString("|") ?: "",
            type = type,
            difficulty = difficulty.uppercase(),
            correctAnswer = correctAnswer,
            explanation = explanation,
            options = options?.joinToString("|"),
            followUpQuestions = null,
            skillSubCategory = null,
            createdAt = Date()
        )
    }
}