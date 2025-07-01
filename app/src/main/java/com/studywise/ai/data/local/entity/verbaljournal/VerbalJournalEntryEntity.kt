package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.studywise.ai.data.local.entity.UserEntity
import java.time.LocalDateTime

@Entity(
    tableName = "verbal_journal_entries",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"])
    ]
)
data class VerbalJournalEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: Int,
    val sessionType: String, // FREE_CONVERSATION, TOPIC_BASED, DAILY_PROMPT
    val topic: String? = null,
    val promptId: String? = null,
    val targetDurationMinutes: Int,
    val actualDurationSeconds: Int = 0,
    val totalTurns: Int = 0,
    val overallScore: Float = 0f,
    val status: String, // IN_PROGRESS, COMPLETED, ABANDONED
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime? = null,
    val language: String = "en",
    val difficulty: String = "INTERMEDIATE"
)