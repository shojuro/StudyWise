package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime

@Entity(
    tableName = "session_recommendations",
    foreignKeys = [
        ForeignKey(
            entity = SessionAnalysisEntity::class,
            parentColumns = ["id"],
            childColumns = ["analysisId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["analysisId"])
    ]
)
data class SessionRecommendationEntity(
    @PrimaryKey
    val id: String,
    val analysisId: String,
    val recommendationType: String, // EXERCISE, TOPIC, TECHNIQUE, RESOURCE
    val title: String,
    val description: String,
    val reason: String,
    val priority: String, // HIGH, MEDIUM, LOW
    val targetArea: String, // Which skill area this targets
    val estimatedDurationMinutes: Int,
    val resourceUrl: String? = null,
    val exerciseData: String? = null, // JSON for exercise details
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime
)