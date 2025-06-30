package com.studywise.ai.data.local.content

import android.util.Log
import com.studywise.ai.data.local.dao.ContentTemplateDao
import com.studywise.ai.data.local.dao.SkillDao
import com.studywise.ai.data.local.entity.ContentTemplateEntity
import com.studywise.ai.data.local.entity.SkillEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages batch generation of prompts for the 10X expansion
 * Coordinates template generation, AI assistance, and quality control
 */
@Singleton
class BatchPromptGenerationManager @Inject constructor(
    private val skillDao: SkillDao,
    private val contentTemplateDao: ContentTemplateDao,
    private val promptExpansionService: PromptExpansionService
) {
    
    companion object {
        private const val TAG = "BatchPromptGen"
        private const val PROMPTS_PER_SKILL_PER_GRADE = 10
        private const val BATCH_SIZE = 20 // Process 20 skills at a time
    }
    
    // Generation progress tracking
    private val _generationProgress = MutableStateFlow(GenerationProgress())
    val generationProgress: StateFlow<GenerationProgress> = _generationProgress.asStateFlow()
    
    // Generation statistics
    private val _generationStats = MutableStateFlow(GenerationStatistics())
    val generationStats: StateFlow<GenerationStatistics> = _generationStats.asStateFlow()
    
    /**
     * Generate all prompts for all grades and skills
     * This is the main entry point for the 10X expansion
     */
    suspend fun generateAllPrompts(
        startGrade: Int = 2,
        endGrade: Int = 12,
        clearExisting: Boolean = false
    ): GenerationResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting batch prompt generation for grades $startGrade-$endGrade")
        
        try {
            // Clear existing templates if requested
            if (clearExisting) {
                contentTemplateDao.deleteAllTemplates()
                Log.d(TAG, "Cleared existing templates")
            }
            
            val allSkills = skillDao.getAllSkills()
            val totalOperations = allSkills.size * (endGrade - startGrade + 1)
            var completedOperations = 0
            
            updateProgress(
                total = totalOperations,
                completed = 0,
                currentTask = "Initializing prompt generation..."
            )
            
            val allGeneratedTemplates = mutableListOf<ContentTemplateEntity>()
            val errors = mutableListOf<GenerationError>()
            
            // Process each grade level
            for (grade in startGrade..endGrade) {
                Log.d(TAG, "Processing grade $grade")
                updateProgress(
                    currentTask = "Generating prompts for grade $grade"
                )
                
                // Process skills in batches
                allSkills.chunked(BATCH_SIZE).forEach { skillBatch ->
                    val batchResults = coroutineScope {
                        skillBatch.map { skill ->
                            async {
                                generatePromptsForSkillAndGrade(skill, grade)
                            }
                        }.awaitAll()
                    }
                    
                    // Collect results and errors
                    batchResults.forEach { result ->
                        result.fold(
                            onSuccess = { templates ->
                                allGeneratedTemplates.addAll(templates)
                            },
                            onFailure = { error ->
                                errors.add(
                                    GenerationError(
                                        skillId = result.getOrNull()?.firstOrNull()?.skillId.toString(),
                                        grade = grade,
                                        error = error.message ?: "Unknown error"
                                    )
                                )
                            }
                        )
                    }
                    
                    completedOperations += skillBatch.size
                    updateProgress(
                        completed = completedOperations,
                        percentComplete = (completedOperations.toFloat() / totalOperations) * 100
                    )
                }
            }
            
            // Save all generated templates
            if (allGeneratedTemplates.isNotEmpty()) {
                contentTemplateDao.insertTemplates(allGeneratedTemplates)
                Log.d(TAG, "Saved ${allGeneratedTemplates.size} templates to database")
            }
            
            // Update final statistics
            updateStatistics(
                totalGenerated = allGeneratedTemplates.size,
                errors = errors,
                skillsCovered = allGeneratedTemplates.map { it.skillId }.distinct().size,
                gradesCovered = endGrade - startGrade + 1
            )
            
            updateProgress(
                completed = totalOperations,
                percentComplete = 100f,
                currentTask = "Generation complete!",
                isComplete = true
            )
            
            GenerationResult(
                success = true,
                totalGenerated = allGeneratedTemplates.size,
                errors = errors,
                statistics = _generationStats.value
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during batch generation", e)
            updateProgress(
                currentTask = "Generation failed: ${e.message}",
                isComplete = true,
                hasError = true
            )
            
            GenerationResult(
                success = false,
                totalGenerated = 0,
                errors = listOf(
                    GenerationError(
                        skillId = "system",
                        grade = 0,
                        error = e.message ?: "Unknown system error"
                    )
                ),
                statistics = _generationStats.value
            )
        }
    }
    
    /**
     * Generate prompts for a specific skill and grade
     */
    private suspend fun generatePromptsForSkillAndGrade(
        skill: SkillEntity,
        gradeLevel: Int
    ): Result<List<ContentTemplateEntity>> = withContext(Dispatchers.Default) {
        try {
            val basePrompt = EducationalContentData.getSkillPromptForGrade(
                skill.code,
                gradeLevel
            ) ?: return@withContext Result.failure(
                Exception("No base prompt found for ${skill.code} grade $gradeLevel")
            )
            
            // Use the expansion service to generate variations
            val expandedPrompts = promptExpansionService.expandPromptsForSkill(
                skillCode = skill.code,
                gradeLevel = gradeLevel,
                basePrompt = basePrompt
            )
            
            expandedPrompts.fold(
                onSuccess = { templates ->
                    Log.d(TAG, "Generated ${templates.size} prompts for ${skill.code} grade $gradeLevel")
                    Result.success(templates)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to generate prompts for ${skill.code}: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating prompts for ${skill.code}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Generate prompts for a specific grade only
     */
    suspend fun generatePromptsForGrade(
        gradeLevel: Int,
        clearExisting: Boolean = false
    ): GenerationResult = withContext(Dispatchers.IO) {
        generateAllPrompts(
            startGrade = gradeLevel,
            endGrade = gradeLevel,
            clearExisting = clearExisting
        )
    }
    
    /**
     * Generate prompts for specific skills
     */
    suspend fun generatePromptsForSkills(
        skillCodes: List<String>,
        grades: List<Int>,
        clearExisting: Boolean = false
    ): GenerationResult = withContext(Dispatchers.IO) {
        try {
            if (clearExisting) {
                // Clear only templates for specified skills
                skillCodes.forEach { skillCode ->
                    val skill = skillDao.getSkillByCode(skillCode)
                    skill?.let { 
                        // Would need to add a DAO method to delete by skill ID
                        // For now, we'll just proceed with generation
                    }
                }
            }
            
            val allGeneratedTemplates = mutableListOf<ContentTemplateEntity>()
            val errors = mutableListOf<GenerationError>()
            val totalOperations = skillCodes.size * grades.size
            var completedOperations = 0
            
            updateProgress(
                total = totalOperations,
                completed = 0,
                currentTask = "Generating prompts for selected skills..."
            )
            
            skillCodes.forEach { skillCode ->
                val skill = skillDao.getSkillByCode(skillCode)
                if (skill != null) {
                    grades.forEach { grade ->
                        val result = generatePromptsForSkillAndGrade(skill, grade)
                        result.fold(
                            onSuccess = { templates ->
                                allGeneratedTemplates.addAll(templates)
                            },
                            onFailure = { error ->
                                errors.add(
                                    GenerationError(
                                        skillId = skillCode,
                                        grade = grade,
                                        error = error.message ?: "Unknown error"
                                    )
                                )
                            }
                        )
                        
                        completedOperations++
                        updateProgress(
                            completed = completedOperations,
                            percentComplete = (completedOperations.toFloat() / totalOperations) * 100
                        )
                    }
                }
            }
            
            // Save generated templates
            if (allGeneratedTemplates.isNotEmpty()) {
                contentTemplateDao.insertTemplates(allGeneratedTemplates)
            }
            
            updateProgress(
                completed = totalOperations,
                percentComplete = 100f,
                currentTask = "Generation complete!",
                isComplete = true
            )
            
            GenerationResult(
                success = true,
                totalGenerated = allGeneratedTemplates.size,
                errors = errors,
                statistics = _generationStats.value
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during skill-specific generation", e)
            GenerationResult(
                success = false,
                totalGenerated = 0,
                errors = listOf(
                    GenerationError(
                        skillId = "system",
                        grade = 0,
                        error = e.message ?: "Unknown system error"
                    )
                ),
                statistics = _generationStats.value
            )
        }
    }
    
    /**
     * Validate generated templates
     */
    suspend fun validateTemplates(): ValidationResult = withContext(Dispatchers.IO) {
        try {
            val allTemplates = contentTemplateDao.getAllTemplates() // Would need to add this method
            val issues = mutableListOf<ValidationIssue>()
            
            // Check for missing combinations
            val expectedCombinations = mutableSetOf<String>()
            val actualCombinations = mutableSetOf<String>()
            
            val skills = skillDao.getAllSkills()
            for (skill in skills) {
                for (grade in 2..12) {
                    expectedCombinations.add("${skill.code}_$grade")
                }
            }
            
            allTemplates.forEach { template ->
                val skill = skillDao.getSkillById(template.skillId)
                skill?.let {
                    actualCombinations.add("${it.code}_${template.gradeLevel}")
                }
            }
            
            val missing = expectedCombinations - actualCombinations
            missing.forEach { combination ->
                val parts = combination.split("_")
                issues.add(
                    ValidationIssue(
                        type = "missing_template",
                        description = "Missing templates for skill ${parts[0]} grade ${parts[1]}"
                    )
                )
            }
            
            // Check template distribution
            val templateCounts = allTemplates.groupBy { "${it.skillId}_${it.gradeLevel}" }
                .mapValues { it.value.size }
            
            templateCounts.forEach { (key, count) ->
                if (count < PROMPTS_PER_SKILL_PER_GRADE) {
                    issues.add(
                        ValidationIssue(
                            type = "insufficient_templates",
                            description = "Only $count templates for $key (expected $PROMPTS_PER_SKILL_PER_GRADE)"
                        )
                    )
                }
            }
            
            ValidationResult(
                isValid = issues.isEmpty(),
                totalTemplates = allTemplates.size,
                issues = issues
            )
            
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                totalTemplates = 0,
                issues = listOf(
                    ValidationIssue(
                        type = "error",
                        description = "Validation failed: ${e.message}"
                    )
                )
            )
        }
    }
    
    // Helper methods for progress tracking
    private fun updateProgress(
        total: Int? = null,
        completed: Int? = null,
        currentTask: String? = null,
        percentComplete: Float? = null,
        isComplete: Boolean = false,
        hasError: Boolean = false
    ) {
        _generationProgress.value = _generationProgress.value.copy(
            totalTasks = total ?: _generationProgress.value.totalTasks,
            completedTasks = completed ?: _generationProgress.value.completedTasks,
            currentTask = currentTask ?: _generationProgress.value.currentTask,
            percentComplete = percentComplete ?: _generationProgress.value.percentComplete,
            isComplete = isComplete,
            hasError = hasError
        )
    }
    
    private fun updateStatistics(
        totalGenerated: Int,
        errors: List<GenerationError>,
        skillsCovered: Int,
        gradesCovered: Int
    ) {
        _generationStats.value = GenerationStatistics(
            totalTemplatesGenerated = totalGenerated,
            totalErrors = errors.size,
            skillsCovered = skillsCovered,
            gradesCovered = gradesCovered,
            averageTemplatesPerSkill = if (skillsCovered > 0) {
                totalGenerated.toFloat() / skillsCovered
            } else 0f,
            generationTimestamp = System.currentTimeMillis()
        )
    }
}

// Data classes for tracking
data class GenerationProgress(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val currentTask: String = "",
    val percentComplete: Float = 0f,
    val isComplete: Boolean = false,
    val hasError: Boolean = false
)

data class GenerationStatistics(
    val totalTemplatesGenerated: Int = 0,
    val totalErrors: Int = 0,
    val skillsCovered: Int = 0,
    val gradesCovered: Int = 0,
    val averageTemplatesPerSkill: Float = 0f,
    val generationTimestamp: Long = 0L
)

data class GenerationResult(
    val success: Boolean,
    val totalGenerated: Int,
    val errors: List<GenerationError>,
    val statistics: GenerationStatistics
)

data class GenerationError(
    val skillId: String,
    val grade: Int,
    val error: String
)

data class ValidationResult(
    val isValid: Boolean,
    val totalTemplates: Int,
    val issues: List<ValidationIssue>
)

data class ValidationIssue(
    val type: String,
    val description: String
)