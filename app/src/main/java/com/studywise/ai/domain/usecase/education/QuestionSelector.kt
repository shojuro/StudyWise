package com.studywise.ai.domain.usecase.education

import com.studywise.ai.data.local.entity.StudentSkillMasteryEntity
import com.studywise.ai.domain.model.Question
import com.studywise.ai.domain.repository.EducationalContentRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class QuestionSelector @Inject constructor(
    private val educationalContentRepository: EducationalContentRepository,
    private val skillProgressionManager: SkillProgressionManager
) {
    
    data class QuestionSelectionCriteria(
        val studentId: String,
        val skillId: Long,
        val gradeLevel: Int,
        val sessionType: SessionType,
        val timeAvailable: Int? = null, // in minutes
        val avoidRecentQuestions: Boolean = true,
        val focusOnWeakAreas: Boolean = true
    )
    
    enum class SessionType {
        DIAGNOSTIC,      // Initial assessment
        PRACTICE,        // Regular practice
        REVIEW,          // Spaced repetition review
        CHALLENGE,       // Advanced/stretch questions
        ASSESSMENT,      // Formal evaluation
        MIXED           // Combination of types
    }
    
    data class SelectedQuestion(
        val question: Question,
        val reasoning: String,
        val estimatedTime: Int, // in seconds
        val targetDifficulty: Float,
        val alternativeQuestions: List<Question> = emptyList()
    )
    
    data class QuestionSession(
        val questions: List<SelectedQuestion>,
        val totalEstimatedTime: Int, // in minutes
        val skillsCovered: Set<Long>,
        val difficultyProgression: List<Float>,
        val sessionObjectives: List<String>
    )
    
    /**
     * Select a single question based on criteria
     */
    suspend fun selectNextQuestion(criteria: QuestionSelectionCriteria): Result<SelectedQuestion> {
        return try {
            val mastery = educationalContentRepository
                .getStudentMastery(criteria.studentId, criteria.skillId)
                .getOrThrow()
            
            val availableQuestions = educationalContentRepository
                .getQuestionsForSkill(criteria.skillId, criteria.gradeLevel)
                .getOrThrow()
            
            if (availableQuestions.isEmpty()) {
                // Generate questions from templates if none exist
                val generated = educationalContentRepository
                    .generateQuestionsFromTemplates(
                        skillId = criteria.skillId,
                        gradeLevel = criteria.gradeLevel,
                        count = 5,
                        difficultyLevel = determineDifficultyLevel(mastery, criteria.sessionType)
                    )
                    .getOrThrow()
                
                if (generated.isEmpty()) {
                    return Result.failure(Exception("No questions available for this skill"))
                }
                
                return selectFromQuestions(generated, mastery, criteria)
            }
            
            selectFromQuestions(availableQuestions, mastery, criteria)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a complete question session
     */
    suspend fun createQuestionSession(
        studentId: String,
        gradeLevel: Int,
        sessionType: SessionType,
        timeAvailable: Int = 30, // minutes
        skillIds: List<Long>? = null
    ): Result<QuestionSession> {
        return try {
            val targetSkills = skillIds ?: selectSkillsForSession(studentId, gradeLevel, sessionType)
            val questions = mutableListOf<SelectedQuestion>()
            val skillsCovered = mutableSetOf<Long>()
            var totalTime = 0
            
            for (skillId in targetSkills) {
                if (totalTime >= timeAvailable * 60) break // Convert to seconds
                
                val criteria = QuestionSelectionCriteria(
                    studentId = studentId,
                    skillId = skillId,
                    gradeLevel = gradeLevel,
                    sessionType = sessionType,
                    timeAvailable = timeAvailable - (totalTime / 60)
                )
                
                val questionCount = determineQuestionCount(sessionType, timeAvailable - (totalTime / 60))
                
                repeat(questionCount) {
                    if (totalTime < timeAvailable * 60) {
                        selectNextQuestion(criteria).getOrNull()?.let { selected ->
                            questions.add(selected)
                            skillsCovered.add(skillId)
                            totalTime += selected.estimatedTime
                        }
                    }
                }
            }
            
            val session = QuestionSession(
                questions = questions,
                totalEstimatedTime = (totalTime / 60),
                skillsCovered = skillsCovered,
                difficultyProgression = questions.map { it.targetDifficulty },
                sessionObjectives = generateSessionObjectives(sessionType, skillsCovered)
            )
            
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get adaptive questions that adjust to student performance
     */
    suspend fun getAdaptiveQuestions(
        studentId: String,
        skillId: Long,
        gradeLevel: Int,
        previousPerformance: Float? = null
    ): Result<List<SelectedQuestion>> {
        return try {
            val mastery = educationalContentRepository
                .getStudentMastery(studentId, skillId)
                .getOrThrow()
            
            val targetDifficulty = calculateAdaptiveDifficulty(mastery, previousPerformance)
            
            val questions = educationalContentRepository
                .generateQuestionsFromTemplates(
                    skillId = skillId,
                    gradeLevel = gradeLevel,
                    count = 3,
                    difficultyLevel = difficultyLevelFromFloat(targetDifficulty)
                )
                .getOrThrow()
            
            val selected = questions.map { question ->
                SelectedQuestion(
                    question = question,
                    reasoning = "Adaptive selection based on ${previousPerformance?.let { "recent performance: ${(it * 100).toInt()}%" } ?: "current mastery: ${(mastery?.masteryLevel ?: 0f * 100).toInt()}%"}",
                    estimatedTime = estimateQuestionTime(question),
                    targetDifficulty = targetDifficulty
                )
            }
            
            Result.success(selected)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get questions for spaced repetition review
     */
    suspend fun getReviewQuestions(
        studentId: String,
        gradeLevel: Int,
        maxQuestions: Int = 10
    ): Result<List<SelectedQuestion>> {
        return try {
            val skillsNeedingReview = skillProgressionManager
                .getSkillsNeedingReview(studentId, maxQuestions)
                .getOrThrow()
            
            val reviewQuestions = mutableListOf<SelectedQuestion>()
            
            for (skill in skillsNeedingReview) {
                val mastery = educationalContentRepository
                    .getStudentMastery(studentId, skill.id)
                    .getOrThrow()
                
                val questions = educationalContentRepository
                    .getQuestionsForSkill(skill.id, gradeLevel)
                    .getOrThrow()
                    .take(2) // 2 questions per skill for review
                
                questions.forEach { question ->
                    reviewQuestions.add(
                        SelectedQuestion(
                            question = question,
                            reasoning = "Spaced repetition review - last practiced ${mastery?.lastPracticed}",
                            estimatedTime = estimateQuestionTime(question) * 0.8.toInt(), // Faster for review
                            targetDifficulty = mastery?.masteryLevel ?: 0.5f
                        )
                    )
                }
            }
            
            Result.success(reviewQuestions.take(maxQuestions))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper methods
    
    private suspend fun selectFromQuestions(
        questions: List<Question>,
        mastery: StudentSkillMasteryEntity?,
        criteria: QuestionSelectionCriteria
    ): Result<SelectedQuestion> {
        val targetDifficulty = calculateTargetDifficulty(mastery, criteria.sessionType)
        
        // Sort questions by how close they are to target difficulty
        val sortedQuestions = questions.sortedBy { 
            kotlin.math.abs(it.difficulty - targetDifficulty) 
        }
        
        val selected = sortedQuestions.firstOrNull()
            ?: return Result.failure(Exception("No suitable question found"))
        
        val alternatives = sortedQuestions.drop(1).take(3)
        
        return Result.success(
            SelectedQuestion(
                question = selected,
                reasoning = generateSelectionReasoning(mastery, criteria, targetDifficulty),
                estimatedTime = estimateQuestionTime(selected),
                targetDifficulty = targetDifficulty,
                alternativeQuestions = alternatives
            )
        )
    }
    
    private fun calculateTargetDifficulty(
        mastery: StudentSkillMasteryEntity?,
        sessionType: SessionType
    ): Float {
        val baseDifficulty = mastery?.masteryLevel ?: 0.3f
        
        return when (sessionType) {
            SessionType.DIAGNOSTIC -> 0.5f // Medium difficulty for assessment
            SessionType.PRACTICE -> (baseDifficulty + 0.1f).coerceIn(0.2f, 0.8f)
            SessionType.REVIEW -> baseDifficulty
            SessionType.CHALLENGE -> (baseDifficulty + 0.3f).coerceIn(0.5f, 1.0f)
            SessionType.ASSESSMENT -> baseDifficulty
            SessionType.MIXED -> baseDifficulty + Random.nextFloat() * 0.2f - 0.1f
        }
    }
    
    private fun calculateAdaptiveDifficulty(
        mastery: StudentSkillMasteryEntity?,
        previousPerformance: Float?
    ): Float {
        val baseDifficulty = mastery?.masteryLevel ?: 0.3f
        
        return when {
            previousPerformance == null -> baseDifficulty
            previousPerformance >= 0.9f -> (baseDifficulty + 0.15f).coerceIn(0.0f, 1.0f)
            previousPerformance >= 0.7f -> (baseDifficulty + 0.05f).coerceIn(0.0f, 1.0f)
            previousPerformance >= 0.5f -> baseDifficulty
            previousPerformance >= 0.3f -> (baseDifficulty - 0.05f).coerceIn(0.0f, 1.0f)
            else -> (baseDifficulty - 0.15f).coerceIn(0.0f, 1.0f)
        }
    }
    
    private fun determineDifficultyLevel(
        mastery: StudentSkillMasteryEntity?,
        sessionType: SessionType
    ): String {
        val difficulty = calculateTargetDifficulty(mastery, sessionType)
        return difficultyLevelFromFloat(difficulty)
    }
    
    private fun difficultyLevelFromFloat(difficulty: Float): String {
        return when {
            difficulty >= 0.8f -> "advanced"
            difficulty >= 0.6f -> "high"
            difficulty >= 0.4f -> "medium"
            else -> "low"
        }
    }
    
    private fun estimateQuestionTime(question: Question): Int {
        // Base time on difficulty and prompt length
        val baseTime = 60 // seconds
        val difficultyMultiplier = 1 + question.difficulty
        val lengthMultiplier = 1 + (question.prompt.length / 200f).coerceIn(0f, 1f)
        
        return (baseTime * difficultyMultiplier * lengthMultiplier).toInt()
    }
    
    private fun generateSelectionReasoning(
        mastery: StudentSkillMasteryEntity?,
        criteria: QuestionSelectionCriteria,
        targetDifficulty: Float
    ): String {
        val masteryLevel = mastery?.masteryLevel ?: 0f
        val masteryPercent = (masteryLevel * 100).toInt()
        
        return when (criteria.sessionType) {
            SessionType.DIAGNOSTIC -> "Diagnostic question to assess current understanding"
            SessionType.PRACTICE -> "Practice question at ${(targetDifficulty * 100).toInt()}% difficulty based on $masteryPercent% mastery"
            SessionType.REVIEW -> "Review question to reinforce previous learning"
            SessionType.CHALLENGE -> "Challenge question to stretch beyond current $masteryPercent% mastery"
            SessionType.ASSESSMENT -> "Assessment question matching current skill level"
            SessionType.MIXED -> "Mixed session question for varied practice"
        }
    }
    
    private suspend fun selectSkillsForSession(
        studentId: String,
        gradeLevel: Int,
        sessionType: SessionType
    ): List<Long> {
        return when (sessionType) {
            SessionType.DIAGNOSTIC -> {
                // Select a variety of skills for initial assessment
                educationalContentRepository.getSkillsForGrade(gradeLevel)
                    .getOrThrow()
                    .shuffled()
                    .take(5)
                    .map { it.id }
            }
            SessionType.PRACTICE, SessionType.MIXED -> {
                // Get recommended skills
                skillProgressionManager.getNextRecommendedSkills(studentId, 3)
                    .getOrThrow()
                    .map { it.id }
            }
            SessionType.REVIEW -> {
                // Get skills needing review
                skillProgressionManager.getSkillsNeedingReview(studentId, 3)
                    .getOrThrow()
                    .map { it.id }
            }
            SessionType.CHALLENGE -> {
                // Get skills where student has high mastery for stretch
                educationalContentRepository.getStudentProgress(studentId, gradeLevel)
                    .getOrThrow()
                    .filter { it.masteryLevel >= 0.7f }
                    .sortedByDescending { it.masteryLevel }
                    .take(3)
                    .map { it.skillId }
            }
            SessionType.ASSESSMENT -> {
                // Representative sample of grade-level skills
                educationalContentRepository.getSkillsForGrade(gradeLevel)
                    .getOrThrow()
                    .shuffled()
                    .take(5)
                    .map { it.id }
            }
        }
    }
    
    private fun determineQuestionCount(sessionType: SessionType, timeAvailable: Int): Int {
        val questionsPerMinute = when (sessionType) {
            SessionType.DIAGNOSTIC -> 0.5f
            SessionType.PRACTICE -> 1f
            SessionType.REVIEW -> 1.5f
            SessionType.CHALLENGE -> 0.3f
            SessionType.ASSESSMENT -> 0.5f
            SessionType.MIXED -> 0.8f
        }
        
        return (timeAvailable * questionsPerMinute).toInt().coerceAtLeast(1)
    }
    
    private fun generateSessionObjectives(
        sessionType: SessionType,
        skillsCovered: Set<Long>
    ): List<String> {
        val objectives = mutableListOf<String>()
        
        when (sessionType) {
            SessionType.DIAGNOSTIC -> {
                objectives.add("Assess current understanding across ${skillsCovered.size} skills")
                objectives.add("Identify strengths and areas for improvement")
            }
            SessionType.PRACTICE -> {
                objectives.add("Build mastery in ${skillsCovered.size} skills")
                objectives.add("Apply learning through varied practice")
            }
            SessionType.REVIEW -> {
                objectives.add("Reinforce previous learning")
                objectives.add("Prevent skill decay through spaced repetition")
            }
            SessionType.CHALLENGE -> {
                objectives.add("Stretch beyond comfort zone")
                objectives.add("Explore advanced applications")
            }
            SessionType.ASSESSMENT -> {
                objectives.add("Evaluate progress formally")
                objectives.add("Demonstrate skill mastery")
            }
            SessionType.MIXED -> {
                objectives.add("Balanced skill development")
                objectives.add("Maintain engagement through variety")
            }
        }
        
        return objectives
    }
}