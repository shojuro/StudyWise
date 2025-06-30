package com.studywise.ai.domain.model

import java.util.Date

/**
 * Comprehensive language error detection and correction models
 * Based on ESL pedagogy principles and L1 interference patterns
 */

// Error Hierarchy Levels
enum class ErrorLevel {
    CRITICAL,      // Impedes comprehension
    SIGNIFICANT,   // Affects clarity but message recoverable
    MINOR,         // Stylistic or non-critical
    ENHANCEMENT    // Opportunities for improvement
}

// Error Categories aligned with ESL pedagogy
enum class ErrorCategory {
    // Grammatical Errors
    VERB_TENSE,
    SUBJECT_VERB_AGREEMENT,
    ARTICLE_USAGE,
    PREPOSITION,
    PRONOUN_REFERENCE,
    WORD_ORDER,
    
    // Lexical Errors
    WORD_CHOICE,
    COLLOCATION,
    REGISTER_MISMATCH,
    FALSE_COGNATE,
    
    // Phonological/Spelling
    SPELLING,
    HOMOPHONE_CONFUSION,
    
    // Discourse Level
    COHERENCE,
    COHESION,
    PRAGMATIC,
    
    // L1 Interference Specific
    L1_SYNTAX_TRANSFER,
    L1_LEXICAL_TRANSFER,
    L1_PHONOLOGICAL_TRANSFER,
    L1_PRAGMATIC_TRANSFER
}

// Correction Tier System
enum class CorrectionTier {
    AWARENESS,      // Highlight the issue without correction
    GUIDED,         // Provide hints or partial correction
    SCAFFOLDED,     // Step-by-step guidance
    DIRECT,         // Direct correction with explanation
    METALINGUISTIC  // Explanation of the rule/pattern
}

// Base error model
data class LanguageError(
    val id: String,
    val text: String,
    val startIndex: Int,
    val endIndex: Int,
    val category: ErrorCategory,
    val level: ErrorLevel,
    val description: String,
    val l1Language: String? = null,
    val confidence: Float,
    val detectedAt: Date = Date()
)

// Error with correction options
data class ErrorWithCorrections(
    val error: LanguageError,
    val corrections: List<CorrectionOption>,
    val recommendedTier: CorrectionTier,
    val pedagogicalNote: String? = null
)

// Correction option
data class CorrectionOption(
    val suggestion: String,
    val explanation: String,
    val tier: CorrectionTier,
    val examples: List<String> = emptyList(),
    val ruleReference: String? = null,
    val confidence: Float
)

// L1 Interference Pattern
data class L1InterferencePattern(
    val id: String,
    val l1Language: String,
    val l2Language: String = "English",
    val category: ErrorCategory,
    val pattern: String,
    val description: String,
    val commonErrors: List<String>,
    val correctionStrategy: String,
    val linguisticExplanation: String
)

// Student error profile
data class StudentErrorProfile(
    val studentId: String,
    val dominantL1: String,
    val commonErrorPatterns: Map<ErrorCategory, Int>,
    val progressionHistory: List<ErrorProgressionPoint>,
    val currentProficiencyLevel: ProficiencyLevel,
    val recommendedFocus: List<ErrorCategory>
)

// Proficiency levels aligned with CEFR
enum class ProficiencyLevel {
    A1_BEGINNER,
    A2_ELEMENTARY,
    B1_INTERMEDIATE,
    B2_UPPER_INTERMEDIATE,
    C1_ADVANCED,
    C2_PROFICIENT
}

// Error progression tracking
data class ErrorProgressionPoint(
    val date: Date,
    val category: ErrorCategory,
    val errorRate: Float,
    val improvementRate: Float
)

// Writing sample analysis
data class WritingSampleAnalysis(
    val sampleId: String,
    val studentId: String,
    val text: String,
    val errors: List<ErrorWithCorrections>,
    val overallScore: WritingScore,
    val strengthAreas: List<String>,
    val improvementAreas: List<ErrorCategory>,
    val socraticQuestions: List<SocraticPrompt>
)

// Writing score components
data class WritingScore(
    val grammar: Float,
    val vocabulary: Float,
    val coherence: Float,
    val taskAchievement: Float,
    val overall: Float
)

// Socratic prompt for error reflection
data class SocraticPrompt(
    val question: String,
    val errorCategory: ErrorCategory,
    val scaffoldingLevel: Int,
    val expectedInsight: String
)

// Error detection rule
data class ErrorDetectionRule(
    val id: String,
    val category: ErrorCategory,
    val pattern: Regex,
    val contextRequired: Boolean,
    val minimumConfidence: Float,
    val l1Specific: List<String> = emptyList()
)

// Correction feedback
data class CorrectionFeedback(
    val errorId: String,
    val studentId: String,
    val wasHelpful: Boolean,
    val studentChoice: String?,
    val timeSpent: Long,
    val additionalComments: String?
)