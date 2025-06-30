package com.studywise.ai.data.local.entity

import androidx.room.*
import java.util.Date

/**
 * Database entities for the Verbal Journal system
 */

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
        Index(value = ["sessionDate"]),
        Index(value = ["status"])
    ]
)
data class VerbalJournalEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val sessionDate: Date,
    val startTime: Date,
    val endTime: Date?,
    val targetDurationMinutes: Int,
    val actualDurationMinutes: Int = 0,
    val audioFilePath: String?,
    val transcript: String = "",
    val correctionTier: String, // CorrectionTier enum name
    val status: String, // SessionStatus enum name
    val engagementLevel: String = "MEDIUM", // EngagementLevel enum name
    val topics: String = "", // JSON array of topics
    val mood: String? = null,
    val overallAccuracy: Float? = null,
    val wordsSpoken: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

@Entity(
    tableName = "conversation_turns",
    foreignKeys = [
        ForeignKey(
            entity = VerbalJournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["journalEntryId"]),
        Index(value = ["timestamp"])
    ]
)
data class ConversationTurnEntity(
    @PrimaryKey
    val id: String,
    val journalEntryId: String,
    val speaker: String, // "USER" or "AI"
    val timestamp: Date,
    val audioSegmentPath: String?,
    val transcript: String,
    val transcriptionConfidence: Float,
    val aiResponse: String? = null,
    val sequenceNumber: Int // Order within the session
)

@Entity(
    tableName = "speech_errors",
    foreignKeys = [
        ForeignKey(
            entity = ConversationTurnEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationTurnId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationTurnId"]),
        Index(value = ["errorType"]),
        Index(value = ["severity"])
    ]
)
data class SpeechErrorEntity(
    @PrimaryKey
    val id: String,
    val conversationTurnId: String,
    val errorType: String,
    val errorCategory: String, // ErrorCategory enum name
    val severity: String, // ErrorSeverity enum name
    val originalText: String,
    val correctedText: String,
    val explanation: String,
    val timestampMillis: Long,
    val confidence: Float,
    val l1AdjustedWeight: Float,
    val contextWindow: String
)

@Entity(
    tableName = "session_analyses",
    foreignKeys = [
        ForeignKey(
            entity = VerbalJournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"], unique = true),
        Index(value = ["analyzedAt"])
    ]
)
data class SessionAnalysisEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val analyzedAt: Date,
    // Session stats
    val totalSpeakingMinutes: Int,
    val wordsSpoken: Int,
    val averageWordsPerMinute: Float,
    val overallAccuracy: Float,
    val fluencyScore: Float,
    val pronunciationScore: Float,
    val vocabularyDiversity: Float,
    // Pause analysis
    val averagePauseDuration: Float,
    val pauseFrequency: Float,
    val fillerWordCount: Int,
    val hesitationCount: Int,
    val naturalPauseRatio: Float,
    // Error breakdown (JSON)
    val errorsByType: String, // JSON map
    val errorsByCategory: String, // JSON map
    val errorsBySeverity: String, // JSON map
    val persistentErrors: String, // JSON array
    val newErrors: String, // JSON array
    val improvementFromLastSession: Float,
    // Achievements (JSON array of achievement IDs)
    val achievementsEarned: String
)

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
    val userId: String,
    val nativeLanguage: String,
    val currentLevel: String, // ProficiencyLevel enum name
    val currentTier: String, // CorrectionTier enum name
    val targetAccent: String = "General American",
    val dailyTargetMinutes: Int,
    val preferredTopics: String, // JSON array
    val correctionStyle: String, // CorrectionStyle enum name
    val streakDays: Int = 0,
    val totalSpeakingMinutes: Int = 0,
    val lastSessionDate: Date? = null,
    val breakInWeek: Int = 1,
    val breakInScheduleType: String = "standard", // "standard", "accelerated", "gentle"
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

@Entity(
    tableName = "improvement_areas",
    foreignKeys = [
        ForeignKey(
            entity = SessionAnalysisEntity::class,
            parentColumns = ["id"],
            childColumns = ["analysisId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["analysisId"]),
        Index(value = ["errorType"])
    ]
)
data class ImprovementAreaEntity(
    @PrimaryKey
    val id: String,
    val analysisId: String,
    val errorType: String,
    val frequency: Int,
    val impact: String, // ErrorSeverity enum name
    val practiceExercises: String, // JSON array
    val estimatedPracticeTime: Int,
    val resourceLinks: String // JSON array
)

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
        Index(value = ["analysisId"]),
        Index(value = ["priority"])
    ]
)
data class SessionRecommendationEntity(
    @PrimaryKey
    val id: String,
    val analysisId: String,
    val type: String, // RecommendationType enum name
    val title: String,
    val description: String,
    val priority: Int,
    val estimatedTime: Int,
    val relatedSkills: String // JSON array
)

@Entity(
    tableName = "daily_journal_prompts",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["category"]),
        Index(value = ["difficulty"])
    ]
)
data class DailyJournalPromptEntity(
    @PrimaryKey
    val id: String,
    val date: Date,
    val promptText: String,
    val category: String, // PromptCategory enum name
    val difficulty: String, // ProficiencyLevel enum name
    val estimatedMinutes: Int,
    val followUpQuestions: String, // JSON array
    val vocabularyHints: String // JSON array
)

@Entity(
    tableName = "verbal_journal_achievements",
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
        Index(value = ["earnedAt"])
    ]
)
data class VerbalJournalAchievementEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val achievementId: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val earnedAt: Date,
    val category: String, // AchievementCategory enum name
    val points: Int
)

@Entity(
    tableName = "speech_progress_snapshots",
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
        Index(value = ["date"])
    ]
)
data class SpeechProgressSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val date: Date,
    val tier: String, // CorrectionTier enum name
    val accuracy: Float,
    val minutesSpoken: Int,
    val errorsFound: Int,
    val fluencyScore: Float,
    val milestone: String? = null
)

// Data class for aggregated metrics queries
data class VerbalJournalMetricsEntity(
    val userId: String,
    val period: String,
    val accuracyImprovement: Float,
    val tierProgressionDays: Int?,
    val speakingFluencyGains: Float,
    val errorReductionRate: Float,
    val streakRetention: Int,
    val sessionCompletionRate: Float,
    val averageSessionLength: Float,
    val totalMinutesSpoken: Int,
    val vocabularyGrowth: Int
)