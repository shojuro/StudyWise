package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "learning_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("startedAt")]
)
data class LearningSessionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val subject: String,
    val bookTitle: String? = null, // Optional, student can specify what they're reading
    val startedAt: Date,
    val completedAt: Date? = null,
    val durationMinutes: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val pointsEarned: Int = 0,
    val status: SessionStatus = SessionStatus.IN_PROGRESS,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

enum class SessionStatus {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}