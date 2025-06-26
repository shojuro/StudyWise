package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "question_responses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("questionId"),
        Index("timestamp")
    ]
)
data class QuestionResponseEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val questionId: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int,
    val timestamp: Date,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)