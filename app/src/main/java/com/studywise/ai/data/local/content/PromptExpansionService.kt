package com.studywise.ai.data.local.content

import com.studywise.ai.data.local.dao.ContentTemplateDao
import com.studywise.ai.data.local.dao.SkillDao
import com.studywise.ai.data.local.entity.ContentTemplateEntity
import com.studywise.ai.data.local.entity.SkillEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Service for expanding prompts from 1 per skill/grade to 10 variations
 * Following the 10X expansion strategy from the specification
 */
@Singleton
class PromptExpansionService @Inject constructor(
    private val skillDao: SkillDao,
    private val contentTemplateDao: ContentTemplateDao
) {
    
    companion object {
        // Template types distribution (10 total)
        private val TEMPLATE_DISTRIBUTION = mapOf(
            "diagnostic" to 1,
            "foundation" to 2,
            "practice" to 2,
            "application" to 2,
            "challenge" to 1,
            "test_aligned" to 1,
            "extension" to 1
        )
        
        // Difficulty levels for each template type
        private val TEMPLATE_DIFFICULTY = mapOf(
            "diagnostic" to "medium",
            "foundation" to listOf("low", "medium"),
            "practice" to listOf("medium", "medium"),
            "application" to listOf("medium", "high"),
            "challenge" to "high",
            "test_aligned" to "medium",
            "extension" to "advanced"
        )
        
        // Context variations for different prompt types
        private val CONTEXT_VARIATIONS = listOf(
            "narrative_fiction",
            "informational_science", 
            "informational_history",
            "persuasive_argument",
            "technical_manual",
            "poetry_literary",
            "multimedia_mixed"
        )
        
        // Scaffolding variations
        private val SCAFFOLDING_VARIATIONS = listOf(
            "high_support",
            "medium_support", 
            "low_support",
            "independent"
        )
        
        // Timing variations
        private val TIMING_VARIATIONS = listOf(
            "untimed_exploration",
            "generous_time",
            "standard_time",
            "speed_challenge",
            "rapid_fire"
        )
        
        // Constants for variable options
        private val TEXT_TYPE_OPTIONS = listOf(
            "your text", "this passage", "the article", "this section",
            "your reading", "the story", "the information", "what you've read"
        )
        
        private val ACTION_VERBS = listOf(
            "identify", "find", "locate", "discover", "analyze", "examine",
            "evaluate", "assess", "compare", "contrast", "explain", "describe"
        )
        
        private val NUMBER_OPTIONS = listOf(
            "two", "three", "four", "several", "multiple"
        )
        
        private val COGNITIVE_DESCRIPTORS = listOf(
            "main", "central", "key", "primary", "important", "significant",
            "essential", "fundamental", "critical"
        )
    }
    
    private val templateProcessor = TemplateProcessor()
    
    /**
     * Expand prompts for a specific skill and grade
     * Generates 10 variations from the base prompt
     */
    suspend fun expandPromptsForSkill(
        skillCode: String,
        gradeLevel: Int,
        basePrompt: String
    ): Result<List<ContentTemplateEntity>> = withContext(Dispatchers.IO) {
        try {
            val skill = skillDao.getSkillByCode(skillCode)
                ?: return@withContext Result.failure(Exception("Skill not found"))
            
            val templates = mutableListOf<ContentTemplateEntity>()
            
            // Generate templates according to distribution
            TEMPLATE_DISTRIBUTION.forEach { (templateType, count) ->
                val difficulties = when (val diff = TEMPLATE_DIFFICULTY[templateType]) {
                    is String -> List(count) { diff }
                    is List<*> -> diff.take(count).map { it as String }
                    else -> List(count) { "medium" }
                }
                
                repeat(count) { index ->
                    val template = createTemplateVariation(
                        skill = skill,
                        gradeLevel = gradeLevel,
                        basePrompt = basePrompt,
                        templateType = templateType,
                        difficulty = difficulties.getOrElse(index) { "medium" },
                        variationIndex = index
                    )
                    templates.add(template)
                }
            }
            
            Result.success(templates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate all prompt variations for a grade level
     */
    suspend fun generateAllPromptsForGrade(gradeLevel: Int): Result<Map<String, List<ContentTemplateEntity>>> = 
        withContext(Dispatchers.IO) {
            try {
                val allPrompts = mutableMapOf<String, List<ContentTemplateEntity>>()
                val skills = skillDao.getAllSkills()
                
                skills.forEach { skill ->
                    val basePrompt = EducationalContentData.getSkillPromptForGrade(
                        skill.code,
                        gradeLevel
                    )
                    
                    if (basePrompt != null) {
                        val expanded = expandPromptsForSkill(
                            skill.code,
                            gradeLevel,
                            basePrompt
                        ).getOrNull()
                        
                        if (expanded != null) {
                            allPrompts[skill.code] = expanded
                        }
                    }
                }
                
                Result.success(allPrompts)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * Create a template variation based on type and parameters
     */
    private fun createTemplateVariation(
        skill: SkillEntity,
        gradeLevel: Int,
        basePrompt: String,
        templateType: String,
        difficulty: String,
        variationIndex: Int
    ): ContentTemplateEntity {
        
        // Analyze base prompt and create template pattern
        val templateData = analyzeAndCreateTemplate(basePrompt, templateType, gradeLevel)
        
        // Select variation parameters
        val contextType = selectContextType(templateType, skill.category.name)
        val scaffoldingLevel = selectScaffoldingLevel(templateType, difficulty, gradeLevel)
        val timingVariation = selectTimingVariation(templateType, difficulty)
        
        // Add template-specific modifications
        val modifiedTemplate = applyTemplateTypeModifications(
            templateData,
            templateType,
            gradeLevel
        )
        
        return ContentTemplateEntity(
            skillId = skill.id,
            templateType = templateType,
            gradeLevel = gradeLevel,
            templatePattern = modifiedTemplate.pattern,
            variables = modifiedTemplate.variables,
            constraints = modifiedTemplate.constraints,
            difficultyLevel = difficulty,
            contextType = contextType,
            scaffoldingLevel = scaffoldingLevel,
            timingVariation = timingVariation
        )
    }
    
    /**
     * Analyze base prompt and create template structure
     */
    private fun analyzeAndCreateTemplate(
        basePrompt: String,
        templateType: String,
        gradeLevel: Int
    ): TemplateData {
        val variables = mutableListOf<Variable>()
        var pattern = basePrompt
        
        // Replace common patterns with variables
        val replacements = mapOf(
            // Text references
            Regex("(your text|this passage|the article|this section|your reading|the story)", RegexOption.IGNORE_CASE) 
                to Variable("text_type", "text_type", TEXT_TYPE_OPTIONS),
            
            // Action verbs
            Regex("(identify|find|locate|analyze|examine|evaluate|compare|explain|describe)", RegexOption.IGNORE_CASE)
                to Variable("action", "action_verb", ACTION_VERBS),
            
            // Quantity references
            Regex("(two|three|four|five|several|multiple)", RegexOption.IGNORE_CASE)
                to Variable("count", "number", NUMBER_OPTIONS),
            
            // Cognitive descriptors
            Regex("(main|central|key|primary|important|significant)", RegexOption.IGNORE_CASE)
                to Variable("focus", "cognitive_level", COGNITIVE_DESCRIPTORS)
        )
        
        replacements.forEach { (regex, variable) ->
            if (pattern.contains(regex)) {
                pattern = pattern.replace(regex) { "{${variable.name}}" }
                if (variables.none { it.name == variable.name }) {
                    variables.add(variable)
                }
            }
        }
        
        // Add template-specific variables
        when (templateType) {
            "diagnostic" -> {
                variables.add(Variable("diagnostic_focus", "custom", 
                    listOf("current understanding", "prior knowledge", "initial thoughts")))
            }
            "foundation" -> {
                variables.add(Variable("foundation_step", "custom",
                    listOf("Start by", "Begin with", "First, let's")))
            }
            "practice" -> {
                variables.add(Variable("practice_instruction", "custom",
                    listOf("Try this:", "Practice by:", "Work through:")))
            }
            "application" -> {
                variables.add(Variable("real_world", "custom",
                    listOf("in real life", "in everyday situations", "in practical contexts")))
            }
            "challenge" -> {
                variables.add(Variable("challenge_level", "custom",
                    listOf("advanced", "complex", "sophisticated")))
            }
            "test_aligned" -> {
                variables.add(Variable("test_format", "custom",
                    listOf("Select evidence", "Choose the best answer", "Support your response")))
            }
            "extension" -> {
                variables.add(Variable("extension_type", "custom",
                    listOf("creative", "cross-curricular", "project-based")))
            }
        }
        
        val variablesJson = JSONArray(variables.map { variable ->
            JSONObject().apply {
                put("name", variable.name)
                put("type", variable.type)
                put("options", JSONArray(variable.options))
            }
        }).toString()
        
        return TemplateData(pattern, variablesJson, null)
    }
    
    /**
     * Apply template-type specific modifications
     */
    private fun applyTemplateTypeModifications(
        templateData: TemplateData,
        templateType: String,
        gradeLevel: Int
    ): TemplateData {
        var pattern = templateData.pattern
        
        when (templateType) {
            "diagnostic" -> {
                pattern = "{diagnostic_focus} about {text_type}? $pattern"
            }
            "foundation" -> {
                pattern = "{foundation_step} $pattern Remember to look for clear evidence."
            }
            "practice" -> {
                pattern = "{practice_instruction} $pattern"
            }
            "application" -> {
                pattern = "$pattern How might this apply {real_world}?"
            }
            "challenge" -> {
                pattern = "Challenge: $pattern Consider {challenge_level} aspects."
            }
            "test_aligned" -> {
                pattern = "$pattern {test_format} to demonstrate your understanding."
            }
            "extension" -> {
                pattern = "Extension activity: $pattern Take a {extension_type} approach."
            }
        }
        
        return templateData.copy(pattern = pattern)
    }
    
    /**
     * Select appropriate context type based on template and skill
     */
    private fun selectContextType(templateType: String, skillCategory: String): String {
        return when {
            templateType == "extension" -> CONTEXT_VARIATIONS.random()
            skillCategory.contains("LITERATURE") -> listOf(
                "narrative_fiction", 
                "poetry_literary"
            ).random()
            skillCategory.contains("INFORMATIONAL") -> listOf(
                "informational_science",
                "informational_history", 
                "technical_manual"
            ).random()
            skillCategory.contains("WRITING") -> listOf(
                "persuasive_argument",
                "narrative_fiction",
                "informational_science"
            ).random()
            else -> CONTEXT_VARIATIONS.random()
        }
    }
    
    /**
     * Select scaffolding level based on template type and difficulty
     */
    private fun selectScaffoldingLevel(
        templateType: String,
        difficulty: String,
        gradeLevel: Int
    ): String {
        return when {
            templateType == "foundation" -> "high_support"
            templateType == "challenge" -> "independent"
            difficulty == "low" -> "high_support"
            difficulty == "high" || difficulty == "advanced" -> "low_support"
            gradeLevel <= 4 -> "medium_support"
            else -> "low_support"
        }
    }
    
    /**
     * Select timing variation based on template type
     */
    private fun selectTimingVariation(templateType: String, difficulty: String): String {
        return when (templateType) {
            "diagnostic" -> "untimed_exploration"
            "foundation" -> "generous_time"
            "practice" -> "standard_time"
            "challenge" -> if (difficulty == "advanced") "rapid_fire" else "speed_challenge"
            "test_aligned" -> "standard_time"
            "extension" -> "untimed_exploration"
            else -> "standard_time"
        }
    }
    
    /**
     * Save generated templates to database
     */
    suspend fun saveGeneratedTemplates(templates: List<ContentTemplateEntity>): Result<Unit> = 
        withContext(Dispatchers.IO) {
            try {
                contentTemplateDao.insertTemplates(templates)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * Generate variations using AI-inspired structured generation
     * This uses educational best practices to create meaningful variations
     */
    suspend fun generateAIVariations(
        skill: SkillEntity,
        gradeLevel: Int,
        count: Int
    ): Result<List<ContentTemplateEntity>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val basePrompts = getSkillBasePrompts(skill)
            val variations = mutableListOf<ContentTemplateEntity>()
            
            repeat(count) { index ->
                val basePrompt = basePrompts.random()
                val variation = generateEducationalVariation(
                    basePrompt = basePrompt,
                    skill = skill,
                    gradeLevel = gradeLevel,
                    variationIndex = index,
                    totalCount = count
                )
                variations.add(variation)
            }
            
            Result.success(variations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate an educationally sound variation of a prompt
     */
    private fun generateEducationalVariation(
        basePrompt: String,
        skill: SkillEntity,
        gradeLevel: Int,
        variationIndex: Int,
        totalCount: Int
    ): ContentTemplateEntity {
        val variationStrategies = listOf(
            "perspective_change",
            "complexity_adjustment",
            "context_variation",
            "question_type_shift",
            "metacognitive_focus",
            "scaffolding_variation"
        )
        
        val strategy = variationStrategies[variationIndex % variationStrategies.size]
        val adjustedPrompt = applyVariationStrategy(basePrompt, strategy, gradeLevel)
        
        return ContentTemplateEntity(
            id = 0, // Will be auto-generated
            skillId = skill.id,
            templatePattern = adjustedPrompt,
            templateType = mapStrategyToTemplateType(strategy),
            gradeLevel = gradeLevel,
            variables = "[]", // Empty variables for now
            constraints = generateConstraintsForStrategy(strategy, gradeLevel),
            difficultyLevel = when (calculateDifficultyForVariation(strategy, gradeLevel)) {
                in 0f..0.33f -> "low"
                in 0.33f..0.66f -> "medium"
                in 0.66f..0.9f -> "high"
                else -> "advanced"
            },
            contextType = "educational",
            scaffoldingLevel = when (strategy) {
                "scaffolding_variation" -> "high_support"
                "metacognitive_focus" -> "low_support"
                else -> "medium_support"
            }
        )
    }
    
    /**
     * Apply educational variation strategies to create meaningful prompt differences
     */
    private fun applyVariationStrategy(basePrompt: String, strategy: String, gradeLevel: Int): String {
        return when (strategy) {
            "perspective_change" -> {
                // Change the viewpoint or perspective in the question
                basePrompt.replace("What do you think", "From the character's perspective, what might")
                    .replace("How does", "If you were in this situation, how would")
                    .replace("Why might", "What reasons could explain why")
            }
            "complexity_adjustment" -> {
                // Adjust cognitive complexity appropriately for grade
                if (gradeLevel <= 6) {
                    basePrompt.replace("analyze", "look at")
                        .replace("evaluate", "decide if")
                        .replace("synthesize", "put together")
                } else {
                    basePrompt.replace("look at", "analyze carefully")
                        .replace("decide", "evaluate")
                        .replace("put together", "synthesize")
                }
            }
            "context_variation" -> {
                // Change the context while maintaining the skill focus
                val contexts = listOf("in your story", "in the text you're reading", "in this passage", "in the book")
                val currentContext = contexts.find { basePrompt.contains(it) }
                val newContext = contexts.filter { it != currentContext }.random()
                basePrompt.replace(currentContext ?: "in your text", newContext)
            }
            "question_type_shift" -> {
                // Shift between different question types (what, how, why, when)
                when {
                    basePrompt.startsWith("What") -> basePrompt.replace("What", "How")
                    basePrompt.startsWith("How") -> basePrompt.replace("How", "Why")
                    basePrompt.startsWith("Why") -> basePrompt.replace("Why", "What")
                    else -> "What details in your text help you understand $basePrompt"
                }
            }
            "metacognitive_focus" -> {
                // Add metacognitive elements (thinking about thinking)
                "Before answering, think about your reading strategy. Then: $basePrompt What thinking steps did you use?"
            }
            "scaffolding_variation" -> {
                // Provide different levels of support
                if (gradeLevel <= 5) {
                    "First, read your text carefully. Then: $basePrompt (Hint: Look for specific words or phrases that give you clues.)"
                } else {
                    "$basePrompt Support your answer with specific evidence from the text."
                }
            }
            else -> basePrompt
        }
    }
    
    /**
     * Get base educational prompts for a skill
     */
    private fun getSkillBasePrompts(skill: SkillEntity): List<String> {
        return when (skill.category.name.lowercase()) {
            "reading_literature" -> listOf(
                "What details in your story help you understand the main character?",
                "How does the setting affect what happens in your story?",
                "What lesson or message does your story teach?",
                "How do the character's actions show what they're like inside?",
                "What problem does the main character face in your story?"
            )
            "reading_informational" -> listOf(
                "What is the main idea of your text?",
                "How are the ideas in your text organized?",
                "What evidence does the author use to support their point?",
                "How does this text connect to what you already know?",
                "What new information did you learn from this text?"
            )
            "writing" -> listOf(
                "How can you organize your ideas clearly?",
                "What details will help your reader understand your message?",
                "How can you connect your ideas smoothly?",
                "What words will make your writing more interesting?",
                "How can you end your writing in a strong way?"
            )
            "language_grammar" -> listOf(
                "How do the words in this sentence work together?",
                "What does this word mean in your text?",
                "How does changing this word change the meaning?",
                "What pattern do you notice in these sentences?",
                "How can you say this in a different way?"
            )
            "vocabulary_speaking" -> listOf(
                "What does this word tell you about the character or situation?",
                "How would you explain this idea to someone else?",
                "What other words could you use instead?",
                "How does the author's word choice affect the meaning?",
                "What words help you picture what's happening?"
            )
            else -> listOf(
                "What do you notice in your text that helps answer this question?",
                "How does this connect to what you're learning?",
                "What evidence can you find to support your thinking?"
            )
        }
    }
    
    private fun calculateDifficultyForVariation(strategy: String, gradeLevel: Int): Float {
        val baseDifficulty = when (gradeLevel) {
            2, 3 -> 0.3f
            4, 5 -> 0.4f
            6, 7 -> 0.5f
            8, 9 -> 0.6f
            else -> 0.7f
        }
        
        val strategyModifier = when (strategy) {
            "scaffolding_variation" -> -0.1f
            "complexity_adjustment" -> 0.0f
            "metacognitive_focus" -> 0.1f
            "question_type_shift" -> 0.05f
            else -> 0.0f
        }
        
        return (baseDifficulty + strategyModifier).coerceIn(0.1f, 1.0f)
    }
    
    private fun mapStrategyToTemplateType(strategy: String): String {
        return when (strategy) {
            "scaffolding_variation" -> "foundation"
            "complexity_adjustment" -> "practice"
            "metacognitive_focus" -> "application"
            "question_type_shift" -> "diagnostic"
            "perspective_change" -> "challenge"
            "context_variation" -> "test_aligned"
            else -> "practice"
        }
    }
    
    private fun generateConstraintsForStrategy(strategy: String, gradeLevel: Int): String {
        return when (strategy) {
            "scaffolding_variation" -> "Provide clear guidance and support"
            "complexity_adjustment" -> "Match cognitive level to grade $gradeLevel"
            "metacognitive_focus" -> "Include thinking process reflection"
            "question_type_shift" -> "Use varied question formats"
            "perspective_change" -> "Encourage multiple viewpoints"
            "context_variation" -> "Adapt to different text types"
            else -> "Follow educational best practices"
        }
    }
    
    private fun generateVariationMetadata(strategy: String, skill: SkillEntity, gradeLevel: Int): String {
        return JSONObject().apply {
            put("strategy", strategy)
            put("skill_name", skill.name)
            put("skill_category", skill.category.name)
            put("grade_level", gradeLevel)
            put("generated_by", "ai_variation_system")
            put("creation_timestamp", System.currentTimeMillis())
            put("educational_approach", "socratic_method")
            put("content_agnostic", true)
        }.toString()
    }
    
    // Data classes
    private data class TemplateData(
        val pattern: String,
        val variables: String,
        val constraints: String?
    )
    
    private data class Variable(
        val name: String,
        val type: String,
        val options: List<String>
    )
}