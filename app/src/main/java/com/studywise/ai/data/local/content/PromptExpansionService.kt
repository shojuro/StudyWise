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
     * Generate variations using AI assistance (placeholder for future implementation)
     */
    suspend fun generateAIVariations(
        skill: SkillEntity,
        gradeLevel: Int,
        count: Int
    ): Result<List<ContentTemplateEntity>> {
        // TODO: Implement AI-based generation
        // This would connect to GPT or similar service
        return Result.success(emptyList())
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
    
    // Constants for variable options
    companion object Options {
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
}