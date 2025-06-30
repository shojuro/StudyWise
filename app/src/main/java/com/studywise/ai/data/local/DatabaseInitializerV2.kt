package com.studywise.ai.data.local

import android.util.Log
import com.studywise.ai.data.local.content.BatchPromptGenerationManager
import com.studywise.ai.data.local.content.EducationalContentData
import com.studywise.ai.data.local.content.QuestionBankData
import com.studywise.ai.data.local.dao.*
import com.studywise.ai.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced database initializer that includes all 80 ELA skills,
 * skill progressions, and comprehensive educational content
 */
@Singleton
class DatabaseInitializerV2 @Inject constructor(
    private val skillDao: SkillDao,
    private val questionDao: QuestionDao,
    private val skillProgressionDao: SkillProgressionDao,
    private val contentTemplateDao: ContentTemplateDao,
    private val batchPromptGenerationManager: BatchPromptGenerationManager
) {
    
    companion object {
        private const val TAG = "DatabaseInitializerV2"
        private const val ENABLE_10X_EXPANSION = true // Toggle for 10x prompt expansion
    }
    
    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        // Check if database is already initialized
        val skillCount = skillDao.getSkillCount()
        if (skillCount >= 80) { // We expect 80 skills
            return@withContext
        }
        
        // Clear existing data for fresh initialization
        if (skillCount > 0) {
            clearExistingData()
        }
        
        // Initialize all components
        initializeSkills()
        initializeSkillProgressions()
        initializeQuestionTemplates()
        initializeSampleQuestions()
        
        // Generate 10x expanded prompts if enabled
        if (ENABLE_10X_EXPANSION) {
            generate10xPrompts()
        }
    }
    
    private suspend fun clearExistingData() {
        // Clear in correct order to respect foreign key constraints
        contentTemplateDao.deleteAllTemplates()
        skillProgressionDao.deleteAllProgressions()
        questionDao.deleteAllQuestions()
        skillDao.deleteAllSkills()
    }
    
    private suspend fun initializeSkills() {
        val skills = mutableListOf<SkillEntity>()
        
        EducationalContentData.allSkills.forEach { skillData ->
            skills.add(
                SkillEntity(
                    name = skillData.name,
                    category = mapToSkillCategory(skillData.category),
                    description = skillData.description,
                    code = skillData.skillId,
                    orderIndex = skills.size,
                    iconUrl = null,
                    isActive = true
                )
            )
        }
        
        skillDao.insertSkills(skills)
    }
    
    private suspend fun initializeSkillProgressions() {
        val progressions = mutableListOf<SkillProgressionEntity>()
        val skillMap = skillDao.getAllSkills().associateBy { it.code }
        val progressionMap = EducationalContentData.getSkillProgressions()
        
        // Create progressions for each grade level
        for (grade in 2..12) {
            progressionMap.forEach { (skillCode, prerequisites) ->
                val skill = skillMap[skillCode] ?: return@forEach
                
                if (prerequisites.isEmpty()) {
                    // Foundation skill - no prerequisites
                    progressions.add(
                        SkillProgressionEntity(
                            skillId = skill.id,
                            prerequisiteSkillId = null,
                            gradeLevel = grade,
                            progressionType = "foundation",
                            minimumMasteryLevel = 0.0f,
                            recommendedOrder = getRecommendedOrder(skillCode, grade),
                            cognitiveLevel = getCognitiveLevel(grade),
                            transitionGuidance = "Begin with basic concepts and build understanding gradually",
                            scaffoldingSuggestions = "Provide examples and guided practice"
                        )
                    )
                } else {
                    // Skills with prerequisites
                    prerequisites.forEach { prereqCode ->
                        val prereqSkill = skillMap[prereqCode] ?: return@forEach
                        progressions.add(
                            SkillProgressionEntity(
                                skillId = skill.id,
                                prerequisiteSkillId = prereqSkill.id,
                                gradeLevel = grade,
                                progressionType = "prerequisite",
                                minimumMasteryLevel = 0.7f,
                                recommendedOrder = getRecommendedOrder(skillCode, grade),
                                cognitiveLevel = getCognitiveLevel(grade),
                                transitionGuidance = "Ensure mastery of ${prereqSkill.name} before advancing",
                                scaffoldingSuggestions = "Review prerequisite concepts if student struggles"
                            )
                        )
                    }
                }
            }
        }
        
        skillProgressionDao.insertProgressions(progressions)
    }
    
    private suspend fun initializeQuestionTemplates() {
        val templates = mutableListOf<ContentTemplateEntity>()
        val skillMap = skillDao.getAllSkills().associateBy { it.code }
        
        val templateTypes = listOf(
            "diagnostic", "foundation", "practice", "application", 
            "challenge", "test_aligned", "extension"
        )
        
        val difficultyLevels = mapOf(
            2..3 to "low",
            4..6 to "medium",
            7..9 to "high",
            10..12 to "advanced"
        )
        
        EducationalContentData.allSkills.forEach { skillData ->
            val skill = skillMap[skillData.skillId] ?: return@forEach
            
            skillData.prompts.forEach { (grade, prompt) ->
                val difficulty = difficultyLevels.entries
                    .find { grade in it.key }?.value ?: "medium"
                
                // Create multiple template variations for each skill/grade
                templateTypes.forEach { templateType ->
                    templates.add(
                        ContentTemplateEntity(
                            skillId = skill.id,
                            templateType = templateType,
                            gradeLevel = grade,
                            templatePattern = createTemplatePattern(prompt, templateType),
                            variables = createTemplateVariables(templateType),
                            constraints = null,
                            difficultyLevel = difficulty,
                            contextType = getContextType(skillData.category),
                            scaffoldingLevel = getScaffoldingLevel(grade, templateType),
                            timingVariation = getTimingVariation(templateType),
                            usageCount = 0,
                            effectivenessScore = null,
                            lastUsed = null
                        )
                    )
                }
            }
        }
        
        contentTemplateDao.insertTemplates(templates)
    }
    
    private suspend fun initializeSampleQuestions() {
        val questions = mutableListOf<QuestionEntity>()
        val skillMap = skillDao.getAllSkills().associateBy { it.code }
        
        // Use comprehensive question bank data
        QuestionBankData.allQuestions.forEach { template ->
            val skill = skillMap[template.skillId] ?: return@forEach
            
            questions.add(
                QuestionEntity(
                    skillId = skill.id,
                    gradeLevel = template.gradeLevel,
                    prompt = template.prompt,
                    hints = template.hints.joinToString("|"),
                    followUpQuestions = template.followUpQuestions.joinToString("|"),
                    difficulty = template.difficulty,
                    isActive = true,
                    type = template.questionType,
                    correctAnswer = null, // Open-ended questions
                    explanation = "Good readers think deeply about what they read and can explain their thinking.",
                    metadata = """
                        {
                            "scaffoldingPrompts": ${template.scaffoldingPrompts.map { "\"$it\"" }},
                            "expectedResponseElements": ${template.expectedResponseElements.map { "\"$it\"" }},
                            "questionType": "${template.questionType}"
                        }
                    """.trimIndent()
                )
            )
        }
        
        // Also add default questions from EducationalContentData for any missing skills
        EducationalContentData.allSkills.forEach { skillData ->
            val skill = skillMap[skillData.skillId] ?: return@forEach
            
            // Check if we already have questions for this skill
            val existingGrades = questions
                .filter { it.skillId == skill.id }
                .map { it.gradeLevel }
                .toSet()
            
            // Add questions for grades not covered by QuestionBankData
            skillData.prompts.forEach { (grade, prompt) ->
                if (grade !in existingGrades) {
                    questions.add(
                        QuestionEntity(
                            skillId = skill.id,
                            gradeLevel = grade,
                            prompt = prompt,
                            hints = generateHints(skillData.skillId, grade),
                            followUpQuestions = generateFollowUps(skillData.skillId, grade),
                            difficulty = getDifficultyForGrade(grade),
                            isActive = true,
                            type = "practice"
                        )
                    )
                }
            }
        }
        
        questionDao.insertQuestions(questions)
    }
    
    // Helper methods
    
    private fun getRecommendedOrder(skillCode: String, grade: Int): Int {
        // Define learning sequence priorities
        val categoryOrder = mapOf(
            EducationalContentData.READING_LITERATURE to 0,
            EducationalContentData.READING_INFORMATIONAL to 100,
            EducationalContentData.WRITING to 200,
            EducationalContentData.LANGUAGE_GRAMMAR to 300,
            EducationalContentData.VOCABULARY_SPEAKING to 400
        )
        
        val skill = EducationalContentData.getSkillById(skillCode)
        val baseOrder = categoryOrder[skill?.category] ?: 0
        
        // Add skill-specific order within category
        val skillOrder = when (skillCode) {
            // Foundation skills come first
            "character_traits", "main_idea_informational", "paragraph_structure", 
            "parts_of_speech", "context_clues" -> 0
            // Advanced skills come later
            "symbolism", "synthesizing_info", "reflection_writing",
            "usage_conventions", "discussion_skills" -> 50
            else -> 25
        }
        
        return baseOrder + skillOrder
    }
    
    private fun getCognitiveLevel(grade: Int): String {
        return when (grade) {
            in 2..3 -> "remember"
            in 4..5 -> "understand"
            in 6..7 -> "apply"
            in 8..9 -> "analyze"
            in 10..11 -> "evaluate"
            12 -> "create"
            else -> "understand"
        }
    }
    
    private fun createTemplatePattern(basePrompt: String, templateType: String): String {
        return when (templateType) {
            "diagnostic" -> "Let's see what you already know. $basePrompt"
            "foundation" -> basePrompt
            "practice" -> "Practice time! $basePrompt"
            "application" -> "Apply your knowledge: $basePrompt"
            "challenge" -> "Challenge yourself: $basePrompt Can you go deeper?"
            "test_aligned" -> "$basePrompt Be sure to use evidence from the text."
            "extension" -> "$basePrompt Now connect this to another text or experience."
            else -> basePrompt
        }
    }
    
    private fun createTemplateVariables(templateType: String): String {
        // JSON representation of available variables
        return """
        {
            "text_references": ["your text", "the passage", "this section", "the article", "your reading"],
            "action_verbs": ["identify", "analyze", "examine", "explore", "investigate"],
            "thinking_prompts": ["Think about", "Consider", "Reflect on", "Notice how"],
            "evidence_prompts": ["Find evidence", "Support with examples", "Use specific details"]
        }
        """.trimIndent()
    }
    
    private fun getContextType(category: String): String {
        return when (category) {
            EducationalContentData.READING_LITERATURE -> "narrative"
            EducationalContentData.READING_INFORMATIONAL -> "informational"
            EducationalContentData.WRITING -> "composition"
            else -> "general"
        }
    }
    
    private fun getScaffoldingLevel(grade: Int, templateType: String): String {
        return when {
            grade <= 3 || templateType == "foundation" -> "high_support"
            grade <= 6 || templateType == "practice" -> "medium_support"
            grade <= 9 || templateType == "application" -> "low_support"
            else -> "independent"
        }
    }
    
    private fun getTimingVariation(templateType: String): String {
        return when (templateType) {
            "diagnostic" -> "untimed"
            "foundation", "practice" -> "generous"
            "application", "test_aligned" -> "standard"
            "challenge" -> "standard"
            "extension" -> "untimed"
            else -> "standard"
        }
    }
    
    private fun generateHints(skillCode: String, grade: Int): String {
        val baseHints = listOf(
            "Look for clues in the text",
            "Think about what you already know",
            "Re-read if you need to"
        )
        
        val gradeSpecificHints = when {
            grade <= 3 -> listOf(
                "Take your time",
                "Use your finger to follow along",
                "Picture it in your mind"
            )
            grade <= 6 -> listOf(
                "Look for patterns",
                "Connect ideas together",
                "Ask yourself why"
            )
            grade <= 9 -> listOf(
                "Consider multiple perspectives",
                "Analyze the author's choices",
                "Look for deeper meaning"
            )
            else -> listOf(
                "Think critically",
                "Question assumptions",
                "Synthesize information"
            )
        }
        
        return (baseHints + gradeSpecificHints).shuffled().take(3).joinToString("|")
    }
    
    private fun generateFollowUps(skillCode: String, grade: Int): String {
        val followUps = when {
            grade <= 3 -> listOf(
                "Can you tell me more?",
                "What makes you think that?",
                "Can you show me where you found that?"
            )
            grade <= 6 -> listOf(
                "What evidence supports your answer?",
                "How does this connect to the main idea?",
                "Can you find another example?"
            )
            grade <= 9 -> listOf(
                "How does this relate to the theme?",
                "What would happen if this changed?",
                "Why do you think the author chose this?"
            )
            else -> listOf(
                "What are the implications of this?",
                "How does this challenge or confirm your understanding?",
                "What questions does this raise for you?"
            )
        }
        
        return followUps.shuffled().take(3).joinToString("|")
    }
    
    private fun getDifficultyForGrade(grade: Int): Float {
        return when (grade) {
            in 2..3 -> 0.2f
            in 4..5 -> 0.3f
            in 6..7 -> 0.5f
            in 8..9 -> 0.7f
            in 10..11 -> 0.8f
            12 -> 0.9f
            else -> 0.5f
        }
    }
    
    private fun mapToSkillCategory(category: String): SkillCategory {
        return when (category) {
            EducationalContentData.READING_LITERATURE -> SkillCategory.READING_LITERATURE
            EducationalContentData.READING_INFORMATIONAL -> SkillCategory.READING_INFORMATIONAL
            EducationalContentData.WRITING -> SkillCategory.WRITING
            EducationalContentData.LANGUAGE_GRAMMAR -> SkillCategory.LANGUAGE_GRAMMAR
            EducationalContentData.VOCABULARY_SPEAKING -> SkillCategory.VOCABULARY_SPEAKING
            else -> SkillCategory.READING_LITERATURE
        }
    }
    
    /**
     * Generate 10x expanded prompts using the batch generation system
     */
    private suspend fun generate10xPrompts() {
        Log.d(TAG, "Starting 10x prompt expansion...")
        
        try {
            // Generate prompts for all grades (2-12)
            val result = batchPromptGenerationManager.generateAllPrompts(
                startGrade = 2,
                endGrade = 12,
                clearExisting = false // Keep existing templates
            )
            
            if (result.success) {
                Log.d(TAG, "Successfully generated ${result.totalGenerated} prompt templates")
                Log.d(TAG, "Statistics: ${result.statistics}")
            } else {
                Log.e(TAG, "Failed to generate prompts. Errors: ${result.errors}")
            }
            
            // Validate the generation
            val validation = batchPromptGenerationManager.validateTemplates()
            if (!validation.isValid) {
                Log.w(TAG, "Validation issues found: ${validation.issues}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during 10x prompt generation", e)
        }
    }
}