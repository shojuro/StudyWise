package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "session_questions",
    foreignKeys = [
        ForeignKey(
            entity = LearningSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("questionId")]
)
data class SessionQuestionEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val questionId: String,
    val presentedAt: Date,
    val answeredAt: Date? = null,
    val studentResponse: String? = null,
    val hintsUsed: Int = 0,
    val pointsEarned: Int = 0,
    val timeSpentSeconds: Int = 0,
    val isCorrect: Boolean? = null // null means not yet evaluated
)