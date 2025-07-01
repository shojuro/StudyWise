package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime

@Entity(
    tableName = "conversation_turns",
    foreignKeys = [
        ForeignKey(
            entity = VerbalJournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["entryId"]),
        Index(value = ["turnNumber"])
    ]
)
data class ConversationTurnEntity(
    @PrimaryKey
    val id: String,
    val entryId: String,
    val turnNumber: Int,
    val role: String, // USER, AI
    val audioFilePath: String? = null,
    val transcription: String,
    val durationSeconds: Float,
    val wordsPerMinute: Float? = null,
    val fluencyScore: Float? = null,
    val pronunciationScore: Float? = null,
    val grammarScore: Float? = null,
    val vocabularyScore: Float? = null,
    val coherenceScore: Float? = null,
    val timestamp: LocalDateTime
)