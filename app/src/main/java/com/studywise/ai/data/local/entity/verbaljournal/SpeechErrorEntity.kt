package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "speech_errors",
    foreignKeys = [
        ForeignKey(
            entity = ConversationTurnEntity::class,
            parentColumns = ["id"],
            childColumns = ["turnId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["turnId"]),
        Index(value = ["errorType"])
    ]
)
data class SpeechErrorEntity(
    @PrimaryKey
    val id: String,
    val turnId: String,
    val errorType: String, // PRONUNCIATION, GRAMMAR, VOCABULARY, FLUENCY
    val errorCategory: String, // More specific categorization
    val originalText: String,
    val correctedText: String,
    val explanation: String,
    val severity: String, // LOW, MEDIUM, HIGH
    val startPosition: Int? = null,
    val endPosition: Int? = null,
    val audioTimestamp: Float? = null
)