package com.studywise.ai.domain.usecase.language

import com.studywise.ai.domain.model.*
import com.studywise.ai.domain.repository.EducationalContentRepository
import com.studywise.ai.domain.repository.ProgressRepository
import com.studywise.ai.domain.service.LanguageErrorDetectionService
import com.studywise.ai.domain.service.ProgressiveCorrectionService
import com.studywise.ai.domain.service.AIEvaluationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for analyzing student writing with error detection and Socratic guidance
 * Integrates with existing StudyWise educational framework
 */
class AnalyzeWritingUseCase @Inject constructor(
    private val errorDetectionService: LanguageErrorDetectionService,
    private val progressiveCorrectionService: ProgressiveCorrectionService,
    private val aiEvaluationService: AIEvaluationService,
    private val progressRepository: ProgressRepository,
    private val contentRepository: EducationalContentRepository
) {
    
    /**
     * Analyze student writing and provide progressive feedback
     */
    suspend operator fun invoke(
        text: String,
        studentId: String,
        gradeLevel: Int,
        assignmentContext: String? = null
    ): Flow<WritingAnalysisResult> = flow {
        
        // Emit initial loading state
        emit(WritingAnalysisResult.Loading)
        
        try {
            // Get student profile
            val studentProfile = getOrCreateStudentProfile(studentId, gradeLevel)
            
            // Perform comprehensive analysis
            val analysis = errorDetectionService.analyzeText(
                text = text,
                studentId = studentId,
                studentL1 = studentProfile.dominantL1,
                gradeLevel = gradeLevel
            )
            
            // Generate progressive corrections for each error
            val correctionsWithFeedback = analysis.errors.map { errorWithCorrection ->
                val feedback = progressiveCorrectionService.generateProgressiveCorrection(
                    errorWithCorrections = errorWithCorrection,
                    studentProfile = studentProfile,
                    gradeLevel = gradeLevel
                )
                ErrorCorrectionWithFeedback(errorWithCorrection, feedback)
            }
            
            // Generate Socratic follow-ups using existing AI evaluation service
            val socraticFollowUps = generateSocraticFollowUps(
                analysis = analysis,
                gradeLevel = gradeLevel,
                context = assignmentContext
            )
            
            // Create skill connections to existing ELA skills
            val relatedSkills = mapErrorsToELASkills(analysis.errors)
            
            // Build comprehensive result
            val result = WritingAnalysisResult.Success(
                analysis = analysis,
                correctionsWithFeedback = correctionsWithFeedback,
                socraticFollowUps = socraticFollowUps,
                relatedELASkills = relatedSkills,
                nextSteps = generateNextSteps(analysis, studentProfile)
            )
            
            // Update student progress
            updateStudentProgress(studentId, analysis)
            
            emit(result)
            
        } catch (e: Exception) {
            emit(WritingAnalysisResult.Error(e.message ?: "Analysis failed"))
        }
    }
    
    /**
     * Get or create student error profile
     */
    private suspend fun getOrCreateStudentProfile(
        studentId: String,
        gradeLevel: Int
    ): StudentErrorProfile {
        // In a real implementation, this would fetch from repository
        // For now, create a default profile
        return StudentErrorProfile(
            studentId = studentId,
            dominantL1 = "Unknown", // Would be set during user profile creation
            commonErrorPatterns = emptyMap(),
            progressionHistory = emptyList(),
            currentProficiencyLevel = when (gradeLevel) {
                in 2..3 -> ProficiencyLevel.A1_BEGINNER
                in 4..5 -> ProficiencyLevel.A2_ELEMENTARY
                in 6..7 -> ProficiencyLevel.B1_INTERMEDIATE
                in 8..9 -> ProficiencyLevel.B2_UPPER_INTERMEDIATE
                in 10..11 -> ProficiencyLevel.C1_ADVANCED
                else -> ProficiencyLevel.C2_PROFICIENT
            },
            recommendedFocus = emptyList()
        )
    }
    
    /**
     * Generate Socratic follow-ups using AI evaluation service
     */
    private suspend fun generateSocraticFollowUps(
        analysis: WritingSampleAnalysis,
        gradeLevel: Int,
        context: String?
    ): List<SocraticFollowUp> {
        // Group errors by category for targeted follow-ups
        val primaryErrorCategories = analysis.errors
            .groupBy { it.error.category }
            .entries
            .sortedByDescending { it.value.size }
            .take(2)
            .map { it.key }
        
        val followUps = mutableListOf<SocraticFollowUp>()
        
        // Generate category-specific follow-ups
        primaryErrorCategories.forEach { category ->
            val followUp = when (category) {
                ErrorCategory.VERB_TENSE -> SocraticFollowUp(
                    question = when (gradeLevel) {
                        in 2..5 -> "Look at your action words. How can you tell when something happened?"
                        in 6..8 -> "What patterns do you notice in how verbs change with time?"
                        else -> "How does tense consistency affect the clarity of your narrative?"
                    },
                    purpose = "Develop awareness of temporal consistency",
                    type = FollowUpType.REFLECTION
                )
                
                ErrorCategory.ARTICLE_USAGE -> SocraticFollowUp(
                    question = when (gradeLevel) {
                        in 2..5 -> "When do we use 'a' and when do we use 'the'?"
                        in 6..8 -> "How do articles help readers know if something is specific or general?"
                        else -> "What role do articles play in establishing reference and cohesion?"
                    },
                    purpose = "Understand article system functions",
                    type = FollowUpType.CLARIFICATION
                )
                
                ErrorCategory.WORD_ORDER -> SocraticFollowUp(
                    question = when (gradeLevel) {
                        in 2..5 -> "What order do words usually come in English sentences?"
                        in 6..8 -> "How does word order change meaning in English?"
                        else -> "How does English word order differ from other languages you know?"
                    },
                    purpose = "Recognize syntactic patterns",
                    type = FollowUpType.EXTENSION
                )
                
                else -> SocraticFollowUp(
                    question = "What pattern do you notice in this type of correction?",
                    purpose = "Develop pattern recognition",
                    type = FollowUpType.REFLECTION
                )
            }
            followUps.add(followUp)
        }
        
        // Add metacognitive follow-up
        followUps.add(
            SocraticFollowUp(
                question = when (gradeLevel) {
                    in 2..5 -> "What's one thing you'll check in your next writing?"
                    in 6..8 -> "What strategy could help you catch these errors yourself?"
                    else -> "How might systematic self-editing improve your writing?"
                },
                purpose = "Develop self-monitoring strategies",
                type = FollowUpType.REFLECTION
            )
        )
        
        return followUps
    }
    
    /**
     * Map language errors to related ELA skills
     */
    private suspend fun mapErrorsToELASkills(
        errors: List<ErrorWithCorrections>
    ): List<RelatedELASkill> {
        val relatedSkills = mutableListOf<RelatedELASkill>()
        
        val errorCategories = errors.map { it.error.category }.distinct()
        
        errorCategories.forEach { category ->
            when (category) {
                ErrorCategory.VERB_TENSE, ErrorCategory.SUBJECT_VERB_AGREEMENT -> {
                    relatedSkills.add(
                        RelatedELASkill(
                            skillId = "verb_tenses",
                            skillName = "Verb Tenses",
                            relevance = "Mastering verb tenses will help avoid these errors",
                            category = "Language & Grammar"
                        )
                    )
                    relatedSkills.add(
                        RelatedELASkill(
                            skillId = "subject_verb_agreement",
                            skillName = "Subject-Verb Agreement",
                            relevance = "Understanding agreement rules prevents common mistakes",
                            category = "Language & Grammar"
                        )
                    )
                }
                
                ErrorCategory.ARTICLE_USAGE -> {
                    relatedSkills.add(
                        RelatedELASkill(
                            skillId = "parts_of_speech",
                            skillName = "Parts of Speech",
                            relevance = "Understanding determiners helps with article usage",
                            category = "Language & Grammar"
                        )
                    )
                }
                
                ErrorCategory.WORD_ORDER -> {
                    relatedSkills.add(
                        RelatedELASkill(
                            skillId = "sentence_types",
                            skillName = "Sentence Types",
                            relevance = "Sentence structure knowledge improves word order",
                            category = "Language & Grammar"
                        )
                    )
                }
                
                ErrorCategory.COHERENCE, ErrorCategory.COHESION -> {
                    relatedSkills.add(
                        RelatedELASkill(
                            skillId = "transition_usage",
                            skillName = "Transition Usage",
                            relevance = "Transitions improve text coherence and flow",
                            category = "Writing"
                        )
                    )
                    relatedSkills.add(
                        RelatedELASkill(
                            skillId = "paragraph_structure",
                            skillName = "Paragraph Structure",
                            relevance = "Good structure enhances coherence",
                            category = "Writing"
                        )
                    )
                }
                
                ErrorCategory.WORD_CHOICE, ErrorCategory.REGISTER_MISMATCH -> {
                    relatedSkills.add(
                        RelatedELASkill(
                            skillId = "academic_vocabulary",
                            skillName = "Academic Vocabulary",
                            relevance = "Appropriate word choice for formal writing",
                            category = "Vocabulary & Speaking"
                        )
                    )
                    relatedSkills.add(
                        RelatedELASkill(
                            skillId = "context_clues",
                            skillName = "Context Clues",
                            relevance = "Understanding word usage in context",
                            category = "Vocabulary & Speaking"
                        )
                    )
                }
                
                else -> {
                    // Generic language skill recommendation
                    relatedSkills.add(
                        RelatedELASkill(
                            skillId = "conventions_in_dialogue",
                            skillName = "Usage Conventions",
                            relevance = "Understanding language conventions",
                            category = "Language & Grammar"
                        )
                    )
                }
            }
        }
        
        return relatedSkills.distinctBy { it.skillId }
    }
    
    /**
     * Generate next steps based on analysis
     */
    private fun generateNextSteps(
        analysis: WritingSampleAnalysis,
        studentProfile: StudentErrorProfile
    ): List<NextStep> {
        val steps = mutableListOf<NextStep>()
        
        // Priority 1: Address critical errors
        val criticalErrors = analysis.errors.filter { it.error.level == ErrorLevel.CRITICAL }
        if (criticalErrors.isNotEmpty()) {
            steps.add(
                NextStep(
                    priority = 1,
                    action = "Focus on critical grammar errors",
                    description = "Work on errors that affect understanding",
                    estimatedSessions = 3
                )
            )
        }
        
        // Priority 2: Pattern-based improvement
        analysis.improvementAreas.take(2).forEach { category ->
            steps.add(
                NextStep(
                    priority = 2,
                    action = "Practice ${category.name.replace('_', ' ').toLowerCase()}",
                    description = "Targeted exercises for frequent error patterns",
                    estimatedSessions = 2
                )
            )
        }
        
        // Priority 3: Strengthen good areas
        if (analysis.strengthAreas.isNotEmpty()) {
            steps.add(
                NextStep(
                    priority = 3,
                    action = "Build on strengths",
                    description = "Advanced practice in: ${analysis.strengthAreas.first()}",
                    estimatedSessions = 1
                )
            )
        }
        
        return steps.sortedBy { it.priority }
    }
    
    /**
     * Update student progress with analysis results
     */
    private suspend fun updateStudentProgress(
        studentId: String,
        analysis: WritingSampleAnalysis
    ) {
        // Track error patterns
        val errorCounts = analysis.errors
            .groupingBy { it.error.category }
            .eachCount()
        
        // Update progress repository
        // This would integrate with existing progress tracking
    }
}

// Result classes
sealed class WritingAnalysisResult {
    object Loading : WritingAnalysisResult()
    
    data class Success(
        val analysis: WritingSampleAnalysis,
        val correctionsWithFeedback: List<ErrorCorrectionWithFeedback>,
        val socraticFollowUps: List<SocraticFollowUp>,
        val relatedELASkills: List<RelatedELASkill>,
        val nextSteps: List<NextStep>
    ) : WritingAnalysisResult()
    
    data class Error(val message: String) : WritingAnalysisResult()
}

data class ErrorCorrectionWithFeedback(
    val errorWithCorrection: ErrorWithCorrections,
    val progressiveFeedback: ProgressiveCorrectionFeedback
)

data class RelatedELASkill(
    val skillId: String,
    val skillName: String,
    val relevance: String,
    val category: String
)

data class NextStep(
    val priority: Int,
    val action: String,
    val description: String,
    val estimatedSessions: Int
)