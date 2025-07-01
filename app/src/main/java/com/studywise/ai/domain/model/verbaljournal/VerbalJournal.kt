package com.studywise.ai.domain.model.verbaljournal

import java.time.LocalDateTime

data class VerbalJournalEntry(
    val id: String,
    val userId: Int,
    val sessionType: SessionType,
    val topic: String? = null,
    val promptId: String? = null,
    val targetDurationMinutes: Int,
    val actualDurationSeconds: Int = 0,
    val totalTurns: Int = 0,
    val overallScore: Float = 0f,
    val status: SessionStatus,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime? = null,
    val language: String = "en",
    val difficulty: DifficultyLevel = DifficultyLevel.INTERMEDIATE
)

data class ConversationTurn(
    val id: String,
    val entryId: String,
    val turnNumber: Int,
    val role: ConversationRole,
    val audioFilePath: String? = null,
    val transcription: String,
    val durationSeconds: Float,
    val metrics: SpeechMetrics? = null,
    val timestamp: LocalDateTime
)

data class SpeechMetrics(
    val wordsPerMinute: Float,
    val fluencyScore: Float,
    val pronunciationScore: Float,
    val grammarScore: Float,
    val vocabularyScore: Float,
    val coherenceScore: Float
)

data class SpeechError(
    val id: String,
    val turnId: String,
    val errorType: ErrorType,
    val errorCategory: String,
    val originalText: String,
    val correctedText: String,
    val explanation: String,
    val severity: ErrorSeverity,
    val position: ErrorPosition? = null,
    val audioTimestamp: Float? = null
)

data class ErrorPosition(
    val start: Int,
    val end: Int
)

data class SessionAnalysis(
    val id: String,
    val entryId: String,
    val metrics: AggregateMetrics,
    val speechStats: SpeechStatistics,
    val errorAnalysis: ErrorAnalysis,
    val performanceInsights: PerformanceInsights,
    val generatedAt: LocalDateTime
)

data class AggregateMetrics(
    val averageFluencyScore: Float,
    val averagePronunciationScore: Float,
    val averageGrammarScore: Float,
    val averageVocabularyScore: Float,
    val averageCoherenceScore: Float
)

data class SpeechStatistics(
    val totalWords: Int,
    val uniqueWords: Int,
    val averageWordsPerMinute: Float,
    val totalPauses: Int,
    val averagePauseDuration: Float,
    val longestUtterance: Int,
    val totalFillerWords: Int,
    val totalSelfCorrections: Int
)

data class ErrorAnalysis(
    val mostCommonErrors: List<ErrorPattern>,
    val errorDistribution: Map<ErrorType, Int>
)

data class ErrorPattern(
    val type: ErrorType,
    val category: String,
    val frequency: Int,
    val examples: List<String>
)

data class PerformanceInsights(
    val vocabularyLevel: VocabularyLevel,
    val topicRelevanceScore: Float,
    val conversationFlowScore: Float,
    val strengths: List<String>,
    val weaknesses: List<String>
)

data class VerbalJournalProfile(
    val id: String,
    val userId: Int,
    val proficiencyLevel: ProficiencyLevel,
    val nativeLanguage: String,
    val targetLanguage: String = "en",
    val preferences: UserPreferences,
    val goals: UserGoals,
    val statistics: UserStatistics,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class UserPreferences(
    val preferredTopics: List<String>,
    val preferredSessionTime: String? = null,
    val enableReminders: Boolean = true,
    val enableBreakInPeriod: Boolean = true
)

data class UserGoals(
    val dailyGoalMinutes: Int = 10,
    val weeklyGoalSessions: Int = 5
)

data class UserStatistics(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalSessions: Int = 0,
    val totalMinutesSpoken: Int = 0,
    val lastSessionDate: LocalDateTime? = null
)

data class ImprovementArea(
    val id: String,
    val profileId: String,
    val areaType: ImprovementAreaType,
    val specificArea: String,
    val currentLevel: Float,
    val targetLevel: Float,
    val priority: Priority,
    val isActive: Boolean = true,
    val recommendedExercises: List<String>,
    val progressHistory: List<ProgressSnapshot>,
    val identifiedAt: LocalDateTime,
    val lastReviewedAt: LocalDateTime,
    val achievedAt: LocalDateTime? = null
)

data class ProgressSnapshot(
    val date: LocalDateTime,
    val level: Float,
    val note: String? = null
)

data class SessionRecommendation(
    val id: String,
    val analysisId: String,
    val type: RecommendationType,
    val title: String,
    val description: String,
    val reason: String,
    val priority: Priority,
    val targetArea: String,
    val estimatedDurationMinutes: Int,
    val resourceUrl: String? = null,
    val exerciseData: ExerciseData? = null,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime
)

data class ExerciseData(
    val instructions: String,
    val examples: List<String>,
    val targetSkills: List<String>
)

data class DailyPrompt(
    val id: String,
    val date: java.time.LocalDate,
    val promptText: String,
    val category: PromptCategory,
    val difficulty: DifficultyLevel,
    val followUpQuestions: List<String>,
    val vocabularySuggestions: List<String>,
    val grammarFocus: String? = null,
    val culturalNotes: String? = null,
    val estimatedDurationMinutes: Int = 5
)

data class Achievement(
    val id: String,
    val profileId: String,
    val type: AchievementType,
    val name: String,
    val description: String,
    val iconName: String,
    val requirement: AchievementRequirement,
    val progress: Float = 0f,
    val isUnlocked: Boolean = false,
    val unlockedAt: LocalDateTime? = null,
    val points: Int = 0,
    val tier: AchievementTier? = null
)

data class AchievementRequirement(
    val metric: String,
    val targetValue: Int,
    val currentValue: Int = 0
)

data class SpeechProgressSnapshot(
    val id: String,
    val profileId: String,
    val snapshotDate: LocalDateTime,
    val periodType: PeriodType,
    val metrics: AggregateMetrics,
    val statistics: ProgressStatistics,
    val comparison: ProgressComparison? = null
)

data class ProgressStatistics(
    val totalSessionsCount: Int,
    val totalMinutesSpoken: Int,
    val averageSessionDuration: Float,
    val wordsPerMinute: Float,
    val uniqueWordsCount: Int,
    val errorRatePerMinute: Float,
    val mostImprovedArea: String? = null,
    val needsWorkArea: String? = null
)

data class ProgressComparison(
    val previousPeriodMetrics: AggregateMetrics,
    val improvementPercentage: Map<String, Float>,
    val trend: TrendDirection
)

// Enums
enum class SessionType {
    FREE_CONVERSATION,
    TOPIC_BASED,
    DAILY_PROMPT
}

enum class SessionStatus {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}

enum class ConversationRole {
    USER,
    AI
}

enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class ErrorType {
    PRONUNCIATION,
    GRAMMAR,
    VOCABULARY,
    FLUENCY,
    COHERENCE
}

enum class ErrorSeverity {
    LOW,
    MEDIUM,
    HIGH
}

enum class VocabularyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class ProficiencyLevel {
    BEGINNER,
    ELEMENTARY,
    INTERMEDIATE,
    UPPER_INTERMEDIATE,
    ADVANCED,
    PROFICIENT
}

enum class ImprovementAreaType {
    PRONUNCIATION,
    GRAMMAR,
    VOCABULARY,
    FLUENCY,
    COHERENCE
}

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

enum class RecommendationType {
    EXERCISE,
    TOPIC,
    TECHNIQUE,
    RESOURCE
}

enum class PromptCategory {
    PERSONAL,
    OPINION,
    STORYTELLING,
    HYPOTHETICAL,
    DESCRIPTIVE
}

enum class AchievementType {
    STREAK,
    MILESTONE,
    SKILL,
    SPECIAL
}

enum class AchievementTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM
}

enum class PeriodType {
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING
}