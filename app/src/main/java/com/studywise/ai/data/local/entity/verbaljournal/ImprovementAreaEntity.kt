package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime

@Entity(
    tableName = "improvement_areas",
    foreignKeys = [
        ForeignKey(
            entity = VerbalJournalProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["areaType", "isActive"])
    ]
)
data class ImprovementAreaEntity(
    @PrimaryKey
    val id: String,
    val profileId: String,
    val areaType: String, // PRONUNCIATION, GRAMMAR, VOCABULARY, FLUENCY, COHERENCE
    val specificArea: String, // e.g., "Past tense usage", "th sounds", "Linking words"
    val currentLevel: Float, // 0.0 to 1.0
    val targetLevel: Float, // 0.0 to 1.0
    val priority: String, // HIGH, MEDIUM, LOW
    val isActive: Boolean = true,
    val exerciseIds: String, // JSON array of recommended exercise IDs
    val progressHistory: String, // JSON array of progress snapshots
    val identifiedAt: LocalDateTime,
    val lastReviewedAt: LocalDateTime,
    val achievedAt: LocalDateTime? = null
)