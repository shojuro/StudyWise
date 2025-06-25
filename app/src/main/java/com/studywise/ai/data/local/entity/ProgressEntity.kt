package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "progress",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skillId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("skillId"),
        Index(value = ["userId", "skillId"], unique = true)
    ]
)
data class ProgressEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val skillId: String,
    val gradeLevel: Int,
    val masteryLevel: Float = 0f, // 0.0 to 1.0
    val questionsAttempted: Int = 0,
    val questionsCorrect: Int = 0,
    val lastPracticedAt: Date? = null,
    val streakDays: Int = 0,
    val totalPointsEarned: Int = 0
)