package com.studywise.ai.data.remote.dto

import com.studywise.ai.data.local.entity.LearningSessionEntity
import com.studywise.ai.data.local.entity.SessionStatus
import com.studywise.ai.data.local.entity.SyncStatus
import java.util.Date

data class LearningSessionDto(
    val id: String,
    val userId: String,
    val subject: String,
    val bookTitle: String?,
    val startedAt: Date,
    val completedAt: Date?,
    val durationMinutes: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val pointsEarned: Int,
    val status: String
) {
    fun toEntity(): LearningSessionEntity {
        return LearningSessionEntity(
            id = id,
            userId = userId,
            subject = subject,
            bookTitle = bookTitle,
            startedAt = startedAt,
            completedAt = completedAt,
            durationMinutes = durationMinutes,
            questionsAnswered = questionsAnswered,
            correctAnswers = correctAnswers,
            pointsEarned = pointsEarned,
            status = SessionStatus.valueOf(status),
            syncStatus = SyncStatus.SYNCED
        )
    }
}