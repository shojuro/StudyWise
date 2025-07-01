package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime

@Entity(
    tableName = "verbal_journal_achievements",
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
        Index(value = ["achievementType", "unlockedAt"])
    ]
)
data class VerbalJournalAchievementEntity(
    @PrimaryKey
    val id: String,
    val profileId: String,
    val achievementType: String, // STREAK, MILESTONE, SKILL, SPECIAL
    val name: String,
    val description: String,
    val iconName: String,
    val requirement: String, // JSON describing the requirement
    val progress: Float = 0f, // 0.0 to 1.0
    val isUnlocked: Boolean = false,
    val unlockedAt: LocalDateTime? = null,
    val points: Int = 0,
    val tier: String? = null // BRONZE, SILVER, GOLD, PLATINUM
)