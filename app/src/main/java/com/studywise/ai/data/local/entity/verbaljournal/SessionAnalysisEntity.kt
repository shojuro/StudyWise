package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime

@Entity(
    tableName = "session_analyses",
    foreignKeys = [
        ForeignKey(
            entity = VerbalJournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["entryId"], unique = true)
    ]
)
data class SessionAnalysisEntity(
    @PrimaryKey
    val id: String,
    val entryId: String,
    val averageFluencyScore: Float,
    val averagePronunciationScore: Float,
    val averageGrammarScore: Float,
    val averageVocabularyScore: Float,
    val averageCoherenceScore: Float,
    val totalWords: Int,
    val uniqueWords: Int,
    val averageWordsPerMinute: Float,
    val totalPauses: Int,
    val averagePauseDuration: Float,
    val longestUtterance: Int,
    val totalFillerWords: Int,
    val totalSelfCorrections: Int,
    val mostCommonErrors: String, // JSON array of error types
    val vocabularyLevel: String, // BEGINNER, INTERMEDIATE, ADVANCED
    val topicRelevanceScore: Float,
    val conversationFlowScore: Float,
    val strengths: String, // JSON array
    val weaknesses: String, // JSON array
    val generatedAt: LocalDateTime
)