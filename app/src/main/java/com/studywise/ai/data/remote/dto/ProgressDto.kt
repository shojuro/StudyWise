package com.studywise.ai.data.remote.dto

import com.studywise.ai.data.local.entity.ProgressEntity
import com.studywise.ai.data.local.entity.SyncStatus
import java.util.Date

data class ProgressDto(
    val id: String,
    val userId: String,
    val skillId: String,
    val gradeLevel: Int,
    val masteryLevel: Float,
    val questionsAttempted: Int,
    val questionsCorrect: Int,
    val lastPracticedAt: Date?,
    val streakDays: Int,
    val totalPointsEarned: Int,
    val subject: String,
    val updatedAt: Date
) {
    fun toEntity(): ProgressEntity {
        return ProgressEntity(
            id = id,
            userId = userId,
            skillId = skillId,
            gradeLevel = gradeLevel,
            masteryLevel = masteryLevel,
            questionsAttempted = questionsAttempted,
            questionsCorrect = questionsCorrect,
            lastPracticedAt = lastPracticedAt,
            streakDays = streakDays,
            totalPointsEarned = totalPointsEarned,
            subject = subject,
            questionsAnswered = questionsAttempted,
            correctAnswers = questionsCorrect,
            updatedAt = updatedAt,
            syncStatus = SyncStatus.SYNCED
        )
    }
}