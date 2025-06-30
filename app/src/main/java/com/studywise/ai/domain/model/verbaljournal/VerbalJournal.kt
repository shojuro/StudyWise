package com.studywise.ai.domain.model.verbaljournal

import java.util.Date

/**
 * Domain models for the Verbal Journal system
 * Based on ESL pedagogy principles for progressive error correction
 */

// Core proficiency levels aligned with CEFR standards
enum class ProficiencyLevel(val cefrLevel: String, val description: String) {
    BEGINNER("A1-A2", "Basic user with limited vocabulary and simple structures"),
    INTERMEDIATE("B1-B2", "Independent user with good communication ability"),
    ADVANCED("C1-C2", "Proficient user with near-native fluency"),
    NATIVE_LEVEL("Native", "Post-fluency maintenance and refinement")
}

// Correction tiers for progressive error feedback
enum class CorrectionTier(
    val level: Int,
    val description: String,
    val focusAreas: List<String>
) {
    CRITICAL(
        level = 1,
        description = "Only communication-breaking errors",
        focusAreas = listOf("word_order", "verb_tense_core", "word_form_critical")
    ),
    IMPORTANT(
        level = 2,
        description = "Plus confusion-causing errors",
        focusAreas = listOf("prepositions_core", "verb_tense_consistency")
    ),
    HELPFUL(
        level = 3,
        description = "Plus noticeable but clear errors",
        focusAreas = listOf("articles", "plural_markers")
    ),
    COMPREHENSIVE(
        level = 4,
        description = "All errors including style and idiom",
        focusAreas = listOf("subject_verb_agreement", "idioms_collocations")
    )
}

// Error severity classification
enum class ErrorSeverity(val weight: Int, val impact: String) {
    CRITICAL(5, "Impedes communication completely"),
    IMPORTANT(3, "Causes confusion or misunderstanding"),
    HELPFUL(2, "Noticeable to natives, doesn't impede understanding"),
    POLISH(1, "Minor style or idiom improvements")
}

// Engagement level tracking
enum class EngagementLevel {
    HIGH,       // User actively participating
    MEDIUM,     // Standard responses
    LOW,        // Needs encouragement/topic change
    STRUGGLING  // Simplify language
}

// Session status
enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    ANALYZING,
    ANALYZED
}

// Main Verbal Journal entry model
data class VerbalJournalEntry(
    val id: String,
    val userId: String,
    val sessionDate: Date,
    val startTime: Date,
    val endTime: Date?,
    val targetDurationMinutes: Int,
    val actualDurationMinutes: Int = 0,
    val audioFilePath: String?,
    val transcript: String = "",
    val correctionTier: CorrectionTier,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val conversationTurns: List<ConversationTurn> = emptyList(),
    val sessionAnalysis: SessionAnalysis? = null,
    val engagementLevel: EngagementLevel = EngagementLevel.MEDIUM,
    val topics: List<String> = emptyList(),
    val mood: String? = null
)

// Conversation turn in a journal session
data class ConversationTurn(
    val id: String,
    val speaker: Speaker,
    val timestamp: Date,
    val audioSegmentPath: String?,
    val transcript: String,
    val transcriptionConfidence: Float,
    val errors: List<ErrorInstance> = emptyList(),
    val aiResponse: String? = null
)

enum class Speaker {
    USER,
    AI
}

// Error instance detected in speech
data class ErrorInstance(
    val id: String,
    val errorType: ErrorType,
    val severity: ErrorSeverity,
    val originalText: String,
    val correctedText: String,
    val explanation: String,
    val timestamp: Long,
    val confidence: Float,
    val l1AdjustedWeight: Float,
    val contextWindow: String // Surrounding text for context
)

// Error type definition
data class ErrorType(
    val id: String,
    val name: String,
    val category: ErrorCategory,
    val severity: ErrorSeverity,
    val description: String,
    val examples: List<String>,
    val correctionTier: CorrectionTier
)

enum class ErrorCategory {
    GRAMMAR,
    VOCABULARY,
    PRONUNCIATION,
    FLUENCY,
    COHERENCE,
    PRAGMATICS
}

// Session analysis results
data class SessionAnalysis(
    val sessionId: String,
    val analyzedAt: Date,
    val overallStats: SessionStats,
    val errorBreakdown: ErrorBreakdown,
    val improvementAreas: List<ImprovementArea>,
    val achievements: List<Achievement>,
    val recommendations: List<Recommendation>
)

// Session statistics
data class SessionStats(
    val totalSpeakingMinutes: Int,
    val wordsSpoken: Int,
    val averageWordsPerMinute: Float,
    val pausePattern: PauseAnalysis,
    val overallAccuracy: Float,
    val tierSpecificAccuracy: Map<CorrectionTier, Float>,
    val fluencyScore: Float,
    val pronunciationScore: Float,
    val vocabularyDiversity: Float
)

// Pause pattern analysis
data class PauseAnalysis(
    val averagePauseDuration: Float,
    val pauseFrequency: Float,
    val fillerWordCount: Int,
    val hesitationCount: Int,
    val naturalPauseRatio: Float
)

// Error breakdown by type and tier
data class ErrorBreakdown(
    val errorsByType: Map<String, Int>,
    val errorsByCategory: Map<ErrorCategory, Int>,
    val errorsBySeverity: Map<ErrorSeverity, Int>,
    val errorsByTier: Map<CorrectionTier, List<ErrorInstance>>,
    val improvementFromLastSession: Float,
    val persistentErrors: List<String>,
    val newErrors: List<String>
)

// Areas for improvement
data class ImprovementArea(
    val errorType: String,
    val frequency: Int,
    val impact: ErrorSeverity,
    val practiceExercises: List<String>,
    val estimatedPracticeTime: Int,
    val resourceLinks: List<String>
)

// Achievement earned
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val earnedAt: Date,
    val category: AchievementCategory,
    val points: Int
)

enum class AchievementCategory {
    STREAK,
    ACCURACY,
    FLUENCY,
    VOCABULARY,
    MILESTONE,
    CHALLENGE
}

// Recommendation for next steps
data class Recommendation(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: Int,
    val estimatedTime: Int,
    val relatedSkills: List<String>
)

enum class RecommendationType {
    PRACTICE_EXERCISE,
    TOPIC_SUGGESTION,
    DIFFICULTY_ADJUSTMENT,
    FOCUS_AREA,
    LEARNING_RESOURCE
}

// L1 (Native Language) interference pattern
data class L1InterferencePattern(
    val nativeLanguage: String,
    val languageFamily: String,
    val commonErrors: List<String>,
    val pronunciationChallenges: List<String>,
    val grammarInterference: List<String>,
    val falseFreinds: List<FalseFriend>,
    val weightingAdjustments: Map<String, Float>
)

// False friend (false cognate) between languages
data class FalseFriend(
    val l1Word: String,
    val l2Word: String,
    val l1Meaning: String,
    val l2Meaning: String,
    val example: String
)

// User's verbal journal profile
data class VerbalJournalProfile(
    val userId: String,
    val nativeLanguage: String,
    val currentLevel: ProficiencyLevel,
    val currentTier: CorrectionTier,
    val targetAccent: String = "General American",
    val dailyTargetMinutes: Int,
    val preferredTopics: List<String>,
    val correctionStyle: CorrectionStyle,
    val streakDays: Int,
    val totalSpeakingMinutes: Int,
    val lastSessionDate: Date?,
    val breakInWeek: Int = 1,
    val achievements: List<String>,
    val progressHistory: List<ProgressSnapshot>
)

enum class CorrectionStyle {
    GENTLE,    // Minimal correction, focus on encouragement
    BALANCED,  // Standard correction approach
    DIRECT,    // Direct error correction
    DETAILED   // Extensive explanations
}

// Progress snapshot for tracking
data class ProgressSnapshot(
    val date: Date,
    val tier: CorrectionTier,
    val accuracy: Float,
    val minutesSpoken: Int,
    val errorsFound: Int,
    val fluencyScore: Float,
    val milestone: String?
)

// Break-in period schedule for new users
data class BreakInSchedule(
    val week: Int,
    val sessionLengthMinutes: Int,
    val correctionFocus: CorrectionTier,
    val accuracyTarget: Float,
    val description: String
)

// Daily journal prompt
data class JournalPrompt(
    val id: String,
    val date: Date,
    val promptText: String,
    val category: PromptCategory,
    val difficulty: ProficiencyLevel,
    val estimatedMinutes: Int,
    val followUpQuestions: List<String>,
    val vocabularyHints: List<String>
)

enum class PromptCategory {
    REFLECTION,
    STORYTELLING,
    OPINION,
    DESCRIPTION,
    PLANNING,
    PROBLEM_SOLVING,
    CREATIVE,
    PROFESSIONAL
}

// Audio features for analysis
data class AudioFeatures(
    val pronunciationScore: Float,
    val fluencyScore: Float,
    val pace: Float, // Words per minute
    val pausePatterns: List<Float>,
    val volumeVariation: Float,
    val pitchVariation: Float,
    val clarity: Float
)

// Transcript segment with timing
data class TranscriptSegment(
    val text: String,
    val startTime: Long,
    val endTime: Long,
    val confidence: Float,
    val speaker: Speaker,
    val wordTimings: List<WordTiming>?
)

// Word-level timing data
data class WordTiming(
    val word: String,
    val startTime: Long,
    val endTime: Long,
    val confidence: Float
)

// Success metrics for analytics
data class VerbalJournalMetrics(
    val userId: String,
    val period: String, // "daily", "weekly", "monthly"
    val accuracyImprovement: Float,
    val tierProgressionDays: Int?,
    val speakingFluencyGains: Float,
    val errorReductionRate: Float,
    val streakRetention: Int,
    val sessionCompletionRate: Float,
    val averageSessionLength: Float,
    val totalMinutesSpoken: Int,
    val vocabularyGrowth: Int,
    val mostImprovedAreas: List<String>,
    val challengingAreas: List<String>
)