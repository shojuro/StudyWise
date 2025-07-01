package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime

@Entity(
    tableName = "speech_progress_snapshots",
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
        Index(value = ["snapshotDate"])
    ]
)
data class SpeechProgressSnapshotEntity(
    @PrimaryKey
    val id: String,
    val profileId: String,
    val snapshotDate: LocalDateTime,
    val periodType: String, // DAILY, WEEKLY, MONTHLY
    val fluencyScore: Float,
    val pronunciationScore: Float,
    val grammarScore: Float,
    val vocabularyScore: Float,
    val coherenceScore: Float,
    val overallScore: Float,
    val totalSessionsCount: Int,
    val totalMinutesSpoken: Int,
    val averageSessionDuration: Float,
    val wordsPerMinute: Float,
    val uniqueWordsCount: Int,
    val errorRatePerMinute: Float,
    val mostImprovedArea: String? = null,
    val needsWorkArea: String? = null,
    val comparisonToPrevious: String? = null // JSON with comparison data
)