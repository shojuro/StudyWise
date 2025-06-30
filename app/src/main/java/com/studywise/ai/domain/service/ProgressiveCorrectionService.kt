package com.studywise.ai.domain.service

import com.studywise.ai.domain.model.*
import com.studywise.ai.domain.repository.ProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Service for implementing progressive correction tiers based on:
 * - Student proficiency level
 * - Error frequency
 * - Previous correction effectiveness
 * - Pedagogical best practices
 */
@Singleton
class ProgressiveCorrectionService @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val aiEvaluationService: AIEvaluationService
) {
    
    companion object {
        // Tier selection thresholds
        private const val HIGH_PROFICIENCY_THRESHOLD = 0.7f
        private const val MEDIUM_PROFICIENCY_THRESHOLD = 0.5f
        
        // Error frequency thresholds
        private const val FREQUENT_ERROR_THRESHOLD = 5
        private const val OCCASIONAL_ERROR_THRESHOLD = 2
    }
    
    /**
     * Determine appropriate correction tier for an error
     */
    suspend fun determineCorrectionTier(
        error: LanguageError,
        studentProfile: StudentErrorProfile,
        contextLength: Int = 100
    ): CorrectionTier = withContext(Dispatchers.Default) {
        
        // Get error frequency for this category
        val errorFrequency = studentProfile.commonErrorPatterns[error.category] ?: 0
        
        // Get student's current proficiency
        val proficiencyScore = getProficiencyScore(studentProfile.currentProficiencyLevel)
        
        // Get error severity score
        val severityScore = getErrorSeverityScore(error.level)
        
        // Calculate tier based on multiple factors
        when {
            // New error type or low frequency - start with awareness
            errorFrequency < OCCASIONAL_ERROR_THRESHOLD -> CorrectionTier.AWARENESS
            
            // High proficiency students - use metalinguistic for deeper understanding
            proficiencyScore > HIGH_PROFICIENCY_THRESHOLD && 
            error.level != ErrorLevel.CRITICAL -> CorrectionTier.METALINGUISTIC
            
            // Medium proficiency with occasional errors - guided correction
            proficiencyScore > MEDIUM_PROFICIENCY_THRESHOLD && 
            errorFrequency < FREQUENT_ERROR_THRESHOLD -> CorrectionTier.GUIDED
            
            // Low proficiency or frequent errors - scaffolded support
            proficiencyScore <= MEDIUM_PROFICIENCY_THRESHOLD || 
            errorFrequency >= FREQUENT_ERROR_THRESHOLD -> CorrectionTier.SCAFFOLDED
            
            // Critical errors always get direct correction
            error.level == ErrorLevel.CRITICAL -> CorrectionTier.DIRECT
            
            else -> CorrectionTier.GUIDED
        }
    }
    
    /**
     * Generate progressive correction feedback
     */
    suspend fun generateProgressiveCorrection(
        errorWithCorrections: ErrorWithCorrections,
        studentProfile: StudentErrorProfile,
        gradeLevel: Int
    ): ProgressiveCorrectionFeedback = withContext(Dispatchers.Default) {
        
        val tier = errorWithCorrections.recommendedTier
        val error = errorWithCorrections.error
        
        val feedback = when (tier) {
            CorrectionTier.AWARENESS -> generateAwarenessFeedback(error, gradeLevel)
            CorrectionTier.GUIDED -> generateGuidedFeedback(errorWithCorrections, gradeLevel)
            CorrectionTier.SCAFFOLDED -> generateScaffoldedFeedback(errorWithCorrections, gradeLevel)
            CorrectionTier.DIRECT -> generateDirectFeedback(errorWithCorrections, gradeLevel)
            CorrectionTier.METALINGUISTIC -> generateMetalinguisticFeedback(errorWithCorrections, gradeLevel)
        }
        
        // Add follow-up activities based on error pattern
        val activities = generateFollowUpActivities(error, studentProfile, gradeLevel)
        
        feedback.copy(followUpActivities = activities)
    }
    
    /**
     * Generate awareness-level feedback
     */
    private fun generateAwarenessFeedback(
        error: LanguageError,
        gradeLevel: Int
    ): ProgressiveCorrectionFeedback {
        val highlighting = when (gradeLevel) {
            in 2..5 -> "Look at this part: '${error.text}'. Does it sound right?"
            in 6..8 -> "There's something to notice here: '${error.text}'. Can you spot what might need attention?"
            else -> "Consider this segment: '${error.text}'. Reflect on its grammatical accuracy."
        }
        
        return ProgressiveCorrectionFeedback(
            tier = CorrectionTier.AWARENESS,
            mainFeedback = highlighting,
            hints = listOf(
                "Read it out loud",
                "Think about the rules you know",
                "Compare it to similar sentences"
            ),
            explanation = null,
            examples = emptyList(),
            followUpActivities = emptyList()
        )
    }
    
    /**
     * Generate guided feedback
     */
    private fun generateGuidedFeedback(
        errorWithCorrections: ErrorWithCorrections,
        gradeLevel: Int
    ): ProgressiveCorrectionFeedback {
        val error = errorWithCorrections.error
        val correction = errorWithCorrections.corrections.firstOrNull()
        
        val guidance = when (error.category) {
            ErrorCategory.VERB_TENSE -> when (gradeLevel) {
                in 2..5 -> "This action word needs to match when it happened. What time clues do you see?"
                in 6..8 -> "Check if your verb tense matches the time markers in your sentence."
                else -> "Consider the temporal consistency between your verb form and contextual time indicators."
            }
            ErrorCategory.ARTICLE_USAGE -> when (gradeLevel) {
                in 2..5 -> "Do we need 'a', 'an', or 'the' here? Or maybe nothing?"
                in 6..8 -> "Think about whether this noun is specific or general. What article fits?"
                else -> "Analyze whether this noun reference requires definite, indefinite, or zero article."
            }
            else -> "Think about what rule applies here."
        }
        
        return ProgressiveCorrectionFeedback(
            tier = CorrectionTier.GUIDED,
            mainFeedback = guidance,
            hints = listOf(
                "Focus on: ${error.category.name.replace('_', ' ').toLowerCase()}",
                correction?.let { "The correct form has ${it.suggestion.split(' ').size} words" } ?: "",
                "This is a common pattern to master"
            ).filter { it.isNotEmpty() },
            explanation = null,
            examples = correction?.examples ?: emptyList(),
            followUpActivities = emptyList()
        )
    }
    
    /**
     * Generate scaffolded feedback
     */
    private fun generateScaffoldedFeedback(
        errorWithCorrections: ErrorWithCorrections,
        gradeLevel: Int
    ): ProgressiveCorrectionFeedback {
        val error = errorWithCorrections.error
        val correction = errorWithCorrections.corrections.firstOrNull()
        
        val steps = when (error.category) {
            ErrorCategory.SUBJECT_VERB_AGREEMENT -> listOf(
                "Step 1: Find the subject (who/what is doing the action)",
                "Step 2: Is the subject singular (one) or plural (more than one)?",
                "Step 3: Match your verb - add 's' for he/she/it in present tense",
                "Step 4: Check: ${correction?.suggestion ?: "Does it sound right now?"}"
            )
            ErrorCategory.VERB_TENSE -> listOf(
                "Step 1: When does this happen? (past, present, future)",
                "Step 2: Find time words like 'yesterday', 'now', 'tomorrow'",
                "Step 3: Change the verb to match the time",
                "Step 4: Result: ${correction?.suggestion ?: "Try the correct form"}"
            )
            else -> listOf(
                "Step 1: Identify what type of error this is",
                "Step 2: Remember the rule that applies",
                "Step 3: Apply the rule to fix it",
                "Step 4: Check if it sounds natural"
            )
        }
        
        return ProgressiveCorrectionFeedback(
            tier = CorrectionTier.SCAFFOLDED,
            mainFeedback = "Let's fix '${error.text}' step by step:",
            hints = steps,
            explanation = correction?.explanation,
            examples = correction?.examples ?: emptyList(),
            followUpActivities = emptyList()
        )
    }
    
    /**
     * Generate direct feedback
     */
    private fun generateDirectFeedback(
        errorWithCorrections: ErrorWithCorrections,
        gradeLevel: Int
    ): ProgressiveCorrectionFeedback {
        val error = errorWithCorrections.error
        val correction = errorWithCorrections.corrections.firstOrNull()
            ?: return ProgressiveCorrectionFeedback(
                tier = CorrectionTier.DIRECT,
                mainFeedback = "Error in '${error.text}'",
                hints = emptyList(),
                explanation = null,
                examples = emptyList(),
                followUpActivities = emptyList()
            )
        
        return ProgressiveCorrectionFeedback(
            tier = CorrectionTier.DIRECT,
            mainFeedback = "Change '${error.text}' to '${correction.suggestion}'",
            hints = emptyList(),
            explanation = correction.explanation,
            examples = correction.examples,
            followUpActivities = emptyList()
        )
    }
    
    /**
     * Generate metalinguistic feedback
     */
    private fun generateMetalinguisticFeedback(
        errorWithCorrections: ErrorWithCorrections,
        gradeLevel: Int
    ): ProgressiveCorrectionFeedback {
        val error = errorWithCorrections.error
        val correction = errorWithCorrections.corrections.firstOrNull()
        
        val linguisticExplanation = when (error.category) {
            ErrorCategory.L1_SYNTAX_TRANSFER -> {
                "This error reflects ${error.l1Language ?: "your first language"}'s " +
                "syntactic structure. In English, ${correction?.explanation ?: "different rules apply"}."
            }
            ErrorCategory.VERB_TENSE -> {
                "English verb morphology requires tense marking through inflection. " +
                "Unlike languages that use context alone, English verbs must change form."
            }
            ErrorCategory.ARTICLE_USAGE -> {
                "The English article system distinguishes between specific (the) and " +
                "non-specific (a/an) reference, plus zero article for generics."
            }
            else -> correction?.explanation ?: "Consider the underlying grammatical principle here."
        }
        
        return ProgressiveCorrectionFeedback(
            tier = CorrectionTier.METALINGUISTIC,
            mainFeedback = linguisticExplanation,
            hints = listOf(
                "Rule: ${correction?.ruleReference ?: "Research this grammatical pattern"}",
                "This relates to ${error.category.name.replace('_', ' ').toLowerCase()}",
                "Understanding this pattern will help with similar cases"
            ),
            explanation = "Linguistic insight: ${errorWithCorrections.pedagogicalNote ?: "Pattern recognition is key"}",
            examples = correction?.examples ?: emptyList(),
            followUpActivities = emptyList()
        )
    }
    
    /**
     * Generate follow-up activities
     */
    private fun generateFollowUpActivities(
        error: LanguageError,
        studentProfile: StudentErrorProfile,
        gradeLevel: Int
    ): List<FollowUpActivity> {
        val activities = mutableListOf<FollowUpActivity>()
        
        // Pattern recognition activity
        activities.add(
            FollowUpActivity(
                type = ActivityType.PATTERN_RECOGNITION,
                title = "Find the Pattern",
                description = when (gradeLevel) {
                    in 2..5 -> "Can you find 3 more examples of ${error.category.name.replace('_', ' ').toLowerCase()} in your reading?"
                    in 6..8 -> "Identify 5 sentences that use this grammar rule correctly."
                    else -> "Analyze how professional writers handle this grammatical structure."
                },
                errorCategory = error.category,
                estimatedMinutes = 10
            )
        )
        
        // Practice activity
        if (studentProfile.commonErrorPatterns[error.category] ?: 0 >= 3) {
            activities.add(
                FollowUpActivity(
                    type = ActivityType.FOCUSED_PRACTICE,
                    title = "Targeted Practice",
                    description = "Complete exercises focusing on ${error.category.name.replace('_', ' ').toLowerCase()}",
                    errorCategory = error.category,
                    estimatedMinutes = 15
                )
            )
        }
        
        // Self-monitoring activity
        activities.add(
            FollowUpActivity(
                type = ActivityType.SELF_MONITORING,
                title = "Check Your Writing",
                description = "Before submitting your next writing, specifically check for this type of error",
                errorCategory = error.category,
                estimatedMinutes = 5
            )
        )
        
        return activities
    }
    
    /**
     * Convert proficiency level to score
     */
    private fun getProficiencyScore(level: ProficiencyLevel): Float {
        return when (level) {
            ProficiencyLevel.A1_BEGINNER -> 0.1f
            ProficiencyLevel.A2_ELEMENTARY -> 0.3f
            ProficiencyLevel.B1_INTERMEDIATE -> 0.5f
            ProficiencyLevel.B2_UPPER_INTERMEDIATE -> 0.7f
            ProficiencyLevel.C1_ADVANCED -> 0.85f
            ProficiencyLevel.C2_PROFICIENT -> 0.95f
        }
    }
    
    /**
     * Convert error level to severity score
     */
    private fun getErrorSeverityScore(level: ErrorLevel): Float {
        return when (level) {
            ErrorLevel.CRITICAL -> 1.0f
            ErrorLevel.SIGNIFICANT -> 0.7f
            ErrorLevel.MINOR -> 0.4f
            ErrorLevel.ENHANCEMENT -> 0.2f
        }
    }
    
    /**
     * Update student profile based on correction interaction
     */
    suspend fun updateStudentProfile(
        studentProfile: StudentErrorProfile,
        errorCategory: ErrorCategory,
        correctionEffective: Boolean
    ): StudentErrorProfile = withContext(Dispatchers.Default) {
        
        val currentCount = studentProfile.commonErrorPatterns[errorCategory] ?: 0
        val newCount = if (correctionEffective) {
            max(0, currentCount - 1)
        } else {
            currentCount + 1
        }
        
        val updatedPatterns = studentProfile.commonErrorPatterns.toMutableMap()
        updatedPatterns[errorCategory] = newCount
        
        // Update progression history
        val progressionPoint = ErrorProgressionPoint(
            date = java.util.Date(),
            category = errorCategory,
            errorRate = newCount.toFloat() / (studentProfile.commonErrorPatterns.values.sum() + 1),
            improvementRate = if (correctionEffective) 0.1f else -0.05f
        )
        
        studentProfile.copy(
            commonErrorPatterns = updatedPatterns,
            progressionHistory = studentProfile.progressionHistory + progressionPoint,
            recommendedFocus = updatedPatterns
                .toList()
                .sortedByDescending { it.second }
                .take(3)
                .map { it.first }
        )
    }
}

// Supporting data classes
data class ProgressiveCorrectionFeedback(
    val tier: CorrectionTier,
    val mainFeedback: String,
    val hints: List<String>,
    val explanation: String?,
    val examples: List<String>,
    val followUpActivities: List<FollowUpActivity>
)

data class FollowUpActivity(
    val type: ActivityType,
    val title: String,
    val description: String,
    val errorCategory: ErrorCategory,
    val estimatedMinutes: Int
)

enum class ActivityType {
    PATTERN_RECOGNITION,
    FOCUSED_PRACTICE,
    SELF_MONITORING,
    PEER_REVIEW,
    REFLECTION
}