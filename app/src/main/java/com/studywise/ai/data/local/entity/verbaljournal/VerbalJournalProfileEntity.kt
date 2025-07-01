package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.studywise.ai.data.local.entity.UserEntity
import java.time.LocalDateTime

@Entity(
    tableName = "verbal_journal_profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"], unique = true)
    ]
)
data class VerbalJournalProfileEntity(
    @PrimaryKey
    val id: String,
    val userId: Int,
    val proficiencyLevel: String, // BEGINNER, INTERMEDIATE, ADVANCED
    val nativeLanguage: String,
    val targetLanguage: String = "en",
    val preferredTopics: String, // JSON array
    val dailyGoalMinutes: Int = 10,
    val weeklyGoalSessions: Int = 5,
    val preferredSessionTime: String? = null, // HH:mm format
    val enableReminders: Boolean = true,
    val enableBreakInPeriod: Boolean = true,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalSessions: Int = 0,
    val totalMinutesSpoken: Int = 0,
    val lastSessionDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)