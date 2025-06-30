package com.studywise.ai.data.local.content

import com.studywise.ai.data.local.entity.ContentTemplateEntity
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

/**
 * Processes content templates to generate dynamic educational prompts
 */
class TemplateProcessor {
    
    companion object {
        // Common variable types used across templates
        private val TEXT_TYPE_OPTIONS = listOf(
            "your text", 
            "this passage", 
            "the article", 
            "this section", 
            "your reading",
            "the story",
            "the information",
            "what you've read"
        )
        
        private val ACTION_VERBS = mapOf(
            "identify" to listOf("identify", "find", "locate", "discover", "point out"),
            "analyze" to listOf("analyze", "examine", "investigate", "explore", "study"),
            "evaluate" to listOf("evaluate", "assess", "judge", "critique", "review"),
            "create" to listOf("create", "develop", "design", "construct", "build"),
            "compare" to listOf("compare", "contrast", "differentiate", "distinguish", "relate"),
            "explain" to listOf("explain", "describe", "clarify", "illustrate", "demonstrate")
        )
        
        private val COGNITIVE_LEVELS = mapOf(
            "basic" to listOf("main", "basic", "primary", "key", "important"),
            "intermediate" to listOf("central", "significant", "essential", "core", "fundamental"),
            "advanced" to listOf("complex", "nuanced", "sophisticated", "intricate", "multifaceted")
        )
        
        private val TIME_CONSTRAINTS = mapOf(
            "untimed" to "Take your time to analyze thoroughly.",
            "generous" to "Try to complete this in {minutes} minutes.",
            "standard" to "Challenge yourself to finish in {minutes} minutes.",
            "speed_challenge" to "Speed challenge: Complete in under {seconds} seconds!",
            "rapid_fire" to "Quick response: 30 seconds or less!"
        )
        
        private val EXAMPLE_COUNTS = listOf("two", "three", "four", "five")
        
        private val SUPPORT_INSTRUCTIONS = mapOf(
            "high_support" to listOf(
                "Start by {initial_step}. Then {next_step}.",
                "First, {initial_step}. Next, {next_step}. Finally, {final_step}.",
                "Let's break this down: 1) {step_one} 2) {step_two} 3) {step_three}"
            ),
            "medium_support" to listOf(
                "Think about {consideration}. What {question}?",
                "Consider {aspect}. How does this {relation}?",
                "Look for {element}. Why might this {significance}?"
            ),
            "low_support" to listOf(
                "What evidence supports this?",
                "How do you know?",
                "Explain your reasoning.",
                "What makes you think that?"
            )
        )
    }
    
    /**
     * Process a template and generate a complete prompt
     */
    fun processTemplate(
        template: ContentTemplateEntity,
        customVariables: Map<String, String> = emptyMap()
    ): String {
        val variables = parseVariables(template.variables)
        val processedPattern = template.templatePattern
        
        // Process all variables in the template
        var result = processedPattern
        variables.forEach { variable ->
            val placeholder = "{${variable.name}}"
            if (result.contains(placeholder)) {
                val value = customVariables[variable.name] 
                    ?: generateVariableValue(variable, template)
                result = result.replace(placeholder, value)
            }
        }
        
        // Add timing constraint if specified
        if (template.timingVariation != null && template.timingVariation != "untimed") {
            val timeConstraint = generateTimeConstraint(template.timingVariation)
            result = "$result\n\n$timeConstraint"
        }
        
        // Add scaffolding if specified
        if (template.scaffoldingLevel != null && template.scaffoldingLevel != "independent") {
            val scaffolding = generateScaffolding(template.scaffoldingLevel, template.skillId)
            result = "$result\n\n$scaffolding"
        }
        
        return result.trim()
    }
    
    /**
     * Generate multiple variations from a template
     */
    fun generateVariations(
        template: ContentTemplateEntity,
        count: Int,
        ensureUnique: Boolean = true
    ): List<String> {
        val variations = mutableSetOf<String>()
        var attempts = 0
        val maxAttempts = count * 10
        
        while (variations.size < count && attempts < maxAttempts) {
            val variation = processTemplate(template)
            if (!ensureUnique || variation !in variations) {
                variations.add(variation)
            }
            attempts++
        }
        
        return variations.toList()
    }
    
    /**
     * Parse variables from JSON string
     */
    private fun parseVariables(variablesJson: String): List<TemplateVariable> {
        return try {
            val jsonArray = JSONArray(variablesJson)
            (0 until jsonArray.length()).map { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                TemplateVariable(
                    name = jsonObject.getString("name"),
                    type = jsonObject.getString("type"),
                    options = jsonObject.optJSONArray("options")?.let { optArray ->
                        (0 until optArray.length()).map { optArray.getString(it) }
                    } ?: emptyList(),
                    constraints = jsonObject.optJSONObject("constraints")?.let { constraints ->
                        mapOf(
                            "minLength" to constraints.optInt("minLength", 0),
                            "maxLength" to constraints.optInt("maxLength", Int.MAX_VALUE)
                        )
                    } ?: emptyMap()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Generate value for a variable based on its type and constraints
     */
    private fun generateVariableValue(
        variable: TemplateVariable,
        template: ContentTemplateEntity
    ): String {
        return when (variable.type) {
            "text_type" -> TEXT_TYPE_OPTIONS.random()
            "action_verb" -> selectActionVerb(template.difficultyLevel)
            "cognitive_level" -> selectCognitiveLevel(template.gradeLevel, template.difficultyLevel)
            "example_count" -> EXAMPLE_COUNTS.random()
            "time_limit" -> generateTimeLimit(template.timingVariation ?: "standard")
            "support_instruction" -> generateSupportInstruction(template.scaffoldingLevel ?: "low_support")
            "custom" -> variable.options.randomOrNull() ?: "your response"
            else -> variable.options.randomOrNull() ?: variable.name
        }
    }
    
    private fun selectActionVerb(difficultyLevel: String): String {
        val verbCategory = when (difficultyLevel) {
            "low" -> "identify"
            "medium" -> listOf("analyze", "compare", "explain").random()
            "high", "advanced" -> listOf("evaluate", "create", "analyze").random()
            else -> "identify"
        }
        return ACTION_VERBS[verbCategory]?.random() ?: verbCategory
    }
    
    private fun selectCognitiveLevel(gradeLevel: Int, difficultyLevel: String): String {
        val level = when {
            gradeLevel <= 5 -> "basic"
            gradeLevel <= 8 -> if (difficultyLevel in listOf("high", "advanced")) "intermediate" else "basic"
            else -> when (difficultyLevel) {
                "low" -> "intermediate"
                "medium" -> "intermediate"
                else -> "advanced"
            }
        }
        return COGNITIVE_LEVELS[level]?.random() ?: "main"
    }
    
    private fun generateTimeLimit(timingVariation: String): String {
        return when (timingVariation) {
            "generous" -> "${Random.nextInt(5, 8)} minutes"
            "standard" -> "${Random.nextInt(3, 5)} minutes"
            "speed_challenge" -> "${Random.nextInt(60, 120)} seconds"
            "rapid_fire" -> "30 seconds"
            else -> "your own pace"
        }
    }
    
    private fun generateTimeConstraint(timingVariation: String): String {
        val template = TIME_CONSTRAINTS[timingVariation] ?: TIME_CONSTRAINTS["untimed"]!!
        return template
            .replace("{minutes}", Random.nextInt(3, 8).toString())
            .replace("{seconds}", Random.nextInt(60, 120).toString())
    }
    
    private fun generateSupportInstruction(scaffoldingLevel: String): String {
        val templates = SUPPORT_INSTRUCTIONS[scaffoldingLevel] ?: SUPPORT_INSTRUCTIONS["low_support"]!!
        val template = templates.random()
        
        // Generate step instructions based on scaffolding level
        val steps = when (scaffoldingLevel) {
            "high_support" -> mapOf(
                "initial_step" to "read through the text carefully",
                "next_step" to "identify key information",
                "final_step" to "organize your thoughts",
                "step_one" to "Find the main topic",
                "step_two" to "Locate supporting details",
                "step_three" to "Connect the ideas"
            )
            "medium_support" -> mapOf(
                "consideration" to "the author's purpose",
                "question" to "does this reveal",
                "aspect" to "different perspectives",
                "relation" to "connect to the main idea",
                "element" to "patterns or connections",
                "significance" to "be important"
            )
            else -> emptyMap()
        }
        
        var result = template
        steps.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        
        return result
    }
    
    private fun generateScaffolding(scaffoldingLevel: String, skillId: Long): String {
        // Add helpful prompts based on scaffolding level
        return when (scaffoldingLevel) {
            "high_support" -> {
                """
                💡 Helpful tips:
                • Read the question carefully
                • Look for clue words in your text
                • Take notes as you read
                • Check your answer makes sense
                """.trimIndent()
            }
            "medium_support" -> {
                """
                💭 Remember to:
                • Support your answer with evidence
                • Consider multiple perspectives
                """.trimIndent()
            }
            else -> ""
        }
    }
    
    data class TemplateVariable(
        val name: String,
        val type: String,
        val options: List<String> = emptyList(),
        val constraints: Map<String, Any> = emptyMap()
    )
    
    /**
     * Create a template pattern from skill prompt
     */
    fun createTemplateFromPrompt(
        prompt: String,
        templateType: String,
        difficultyLevel: String
    ): ContentTemplateEntity {
        // Analyze prompt to identify variable opportunities
        val variables = mutableListOf<TemplateVariable>()
        var templatePattern = prompt
        
        // Identify and replace text references
        val textPatterns = listOf(
            "your text", "this passage", "the article", "this section",
            "your reading", "the story", "the information"
        )
        textPatterns.forEach { pattern ->
            if (templatePattern.contains(pattern, ignoreCase = true)) {
                templatePattern = templatePattern.replace(pattern, "{text_type}", ignoreCase = true)
                if (variables.none { it.name == "text_type" }) {
                    variables.add(TemplateVariable("text_type", "text_type"))
                }
            }
        }
        
        // Identify and replace action verbs
        ACTION_VERBS.forEach { (_, verbs) ->
            verbs.forEach { verb ->
                if (templatePattern.contains(verb, ignoreCase = true)) {
                    templatePattern = templatePattern.replace(verb, "{action}", ignoreCase = true)
                    if (variables.none { it.name == "action" }) {
                        variables.add(TemplateVariable("action", "action_verb"))
                    }
                }
            }
        }
        
        // Convert variables to JSON
        val variablesJson = JSONArray(variables.map { variable ->
            JSONObject().apply {
                put("name", variable.name)
                put("type", variable.type)
                if (variable.options.isNotEmpty()) {
                    put("options", JSONArray(variable.options))
                }
            }
        }).toString()
        
        return ContentTemplateEntity(
            skillId = 0, // Will be set when saving
            templateType = templateType,
            gradeLevel = 0, // Will be set when saving
            templatePattern = templatePattern,
            variables = variablesJson,
            difficultyLevel = difficultyLevel
        )
    }
}