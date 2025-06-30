package com.studywise.ai.data.repository

import com.studywise.ai.data.local.content.EducationalContentData
import com.studywise.ai.data.local.content.TemplateProcessor
import com.studywise.ai.data.local.dao.ContentTemplateDao
import com.studywise.ai.data.local.dao.QuestionDao
import com.studywise.ai.data.local.dao.SkillDao
import com.studywise.ai.data.local.dao.SkillProgressionDao
import com.studywise.ai.data.local.dao.StudentSkillMasteryDao
import com.studywise.ai.data.local.entity.ContentTemplateEntity
import com.studywise.ai.data.local.entity.QuestionEntity
import com.studywise.ai.data.local.entity.SkillCategory
import com.studywise.ai.data.local.entity.SkillEntity
import com.studywise.ai.data.local.entity.SkillProgressionEntity
import com.studywise.ai.data.local.entity.StudentSkillMasteryEntity
import com.studywise.ai.domain.model.Question
import com.studywise.ai.domain.repository.EducationalContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class EducationalContentRepositoryImpl @Inject constructor(
    private val skillDao: SkillDao,
    private val questionDao: QuestionDao,
    private val skillProgressionDao: SkillProgressionDao,
    private val contentTemplateDao: ContentTemplateDao,
    private val studentSkillMasteryDao: StudentSkillMasteryDao
) : EducationalContentRepository {

    private val templateProcessor = TemplateProcessor()

    // Skill Management
    override suspend fun getAllSkills(): Result<List<SkillEntity>> {
        return try {
            // If getAllSkills returns Flow, collect it first
            val skills = skillDao.getAllSkills()
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSkillsByCategory(category: String): Result<List<SkillEntity>> {
        return try {
            val skills = skillDao.getSkillsByCategorySuspend(category)
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSkillById(skillId: Long): Result<SkillEntity> {
        return try {
            val skill = skillDao.getSkillById(skillId)
                ?: return Result.failure(Exception("Skill not found"))
            Result.success(skill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSkillByCode(skillCode: String): Result<SkillEntity> {
        return try {
            val skill = skillDao.getSkillByCode(skillCode)
                ?: return Result.failure(Exception("Skill not found"))
            Result.success(skill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Skill Progressions
    override suspend fun getSkillProgressions(skillId: Long): Result<List<SkillProgressionEntity>> {
        return try {
            val progressions = skillProgressionDao.getProgressionsForSkill(skillId)
            Result.success(progressions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPrerequisiteSkills(skillId: Long): Result<List<SkillEntity>> {
        return try {
            val progressions = skillProgressionDao.getProgressionsForSkill(skillId)
            val prerequisiteIds = progressions.mapNotNull { it.prerequisiteSkillId }
            val skills = prerequisiteIds.mapNotNull { skillDao.getSkillById(it) }
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNextSkills(skillId: Long, studentId: String): Result<List<SkillEntity>> {
        return try {
            val nextProgressions = skillProgressionDao.getSkillsWithPrerequisite(skillId)
            val nextSkillIds = nextProgressions.map { it.skillId }
            val skills = nextSkillIds.mapNotNull { skillDao.getSkillById(it) }
            
            // Filter based on student mastery
            val masteredSkills = studentSkillMasteryDao.getMasteredSkills(studentId)
            val masteredSkillIds = masteredSkills.map { it.skillId }.toSet()
            
            val availableSkills = skills.filter { skill ->
                // Check if all prerequisites are mastered
                val skillProgressions = skillProgressionDao.getProgressionsForSkill(skill.id)
                val prerequisites = skillProgressions.mapNotNull { it.prerequisiteSkillId }
                prerequisites.all { prereqId -> prereqId in masteredSkillIds }
            }
            
            Result.success(availableSkills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSkillsForGrade(gradeLevel: Int): Result<List<SkillEntity>> {
        return try {
            val progressions = skillProgressionDao.getProgressionsForGrade(gradeLevel)
            val skillIds = progressions.map { it.skillId }.distinct()
            val skills = skillIds.mapNotNull { skillDao.getSkillById(it) }
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Question Management
    override suspend fun getQuestionsForSkill(skillId: Long, gradeLevel: Int): Result<List<Question>> {
        return try {
            val questions = questionDao.getQuestionsForSkillAndGrade(skillId, gradeLevel)
            val domainQuestions = questions.map { it.toDomainModel() }
            Result.success(domainQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getQuestionById(questionId: Long): Result<Question> {
        return try {
            val question = questionDao.getQuestionById(questionId)
                ?: return Result.failure(Exception("Question not found"))
            Result.success(question.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateQuestionsFromTemplates(
        skillId: Long,
        gradeLevel: Int,
        count: Int,
        difficultyLevel: String?
    ): Result<List<Question>> {
        return try {
            val templates = if (difficultyLevel != null) {
                contentTemplateDao.getRandomTemplates(skillId, gradeLevel, difficultyLevel, count)
            } else {
                contentTemplateDao.getTemplatesForSkillAndGrade(skillId, gradeLevel).take(count)
            }
            
            val questions = templates.map { template ->
                generateQuestionFromTemplate(template)
            }
            
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Content Templates
    override suspend fun getContentTemplates(
        skillId: Long,
        gradeLevel: Int,
        templateType: String?
    ): Result<List<ContentTemplateEntity>> {
        return try {
            val templates = if (templateType != null) {
                contentTemplateDao.getTemplatesByType(skillId, gradeLevel, templateType)
            } else {
                contentTemplateDao.getTemplatesForSkillAndGrade(skillId, gradeLevel)
            }
            Result.success(templates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createContentFromTemplate(
        templateId: Long,
        variables: Map<String, String>
    ): Result<Question> {
        return try {
            val template = contentTemplateDao.getTemplateById(templateId)
                ?: return Result.failure(Exception("Template not found"))
            
            val processedPrompt = templateProcessor.processTemplate(template, variables)
            val skill = skillDao.getSkillById(template.skillId)
                ?: return Result.failure(Exception("Skill not found"))
            
            // Generate hints based on scaffolding level
            val hints = generateHints(template.scaffoldingLevel, skill.code)
            
            // Generate follow-up questions for Socratic method
            val followUpQuestions = generateFollowUpQuestions(skill.code, template.gradeLevel)
            
            val question = Question(
                id = UUID.randomUUID().toString(),
                skillId = template.skillId.toString(),
                gradeLevel = template.gradeLevel,
                prompt = processedPrompt,
                hints = hints,
                followUpQuestions = followUpQuestions,
                difficulty = when (template.difficultyLevel) {
                    "low" -> 0.3f
                    "medium" -> 0.5f
                    "high" -> 0.7f
                    "advanced" -> 0.9f
                    else -> 0.5f
                },
                metadata = JSONObject().apply {
                    put("templateId", templateId.toString())
                    put("templateType", template.templateType)
                    put("contextType", template.contextType ?: "general")
                    put("scaffoldingLevel", template.scaffoldingLevel ?: "independent")
                }.toString()
            )
            
            Result.success(question)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Student Progress
    override suspend fun getStudentMastery(
        studentId: String,
        skillId: Long
    ): Result<StudentSkillMasteryEntity?> {
        return try {
            val mastery = studentSkillMasteryDao.getMastery(studentId, skillId)
            Result.success(mastery)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStudentMastery(mastery: StudentSkillMasteryEntity): Result<Unit> {
        return try {
            studentSkillMasteryDao.upsertMastery(mastery)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStudentProgress(
        studentId: String,
        gradeLevel: Int?
    ): Result<List<StudentSkillMasteryEntity>> {
        return try {
            val progress = if (gradeLevel != null) {
                studentSkillMasteryDao.getMasteriesForGrade(studentId, gradeLevel)
            } else {
                studentSkillMasteryDao.observeStudentMasteries(studentId).map { it }.let {
                    // Convert Flow to List - simplified for now
                    emptyList()
                }
            }
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecommendedSkills(
        studentId: String,
        limit: Int
    ): Result<List<SkillEntity>> {
        return try {
            val nextProgressions = skillProgressionDao.getNextAvailableSkills(studentId, limit)
            val skillIds = nextProgressions.map { it.skillId }
            val skills = skillIds.mapNotNull { skillDao.getSkillById(it) }
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Content Data Access
    override suspend fun getSkillPrompt(skillCode: String, gradeLevel: Int): Result<String> {
        return try {
            val skillData = EducationalContentData.getSkillById(skillCode)
                ?: return Result.failure(Exception("Skill not found"))
            val prompt = skillData.prompts[gradeLevel]
                ?: return Result.failure(Exception("No prompt for grade $gradeLevel"))
            Result.success(prompt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllPromptsForGrade(gradeLevel: Int): Result<Map<String, String>> {
        return try {
            val prompts = EducationalContentData.getPromptsForGrade(gradeLevel)
            Result.success(prompts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSkillDescription(skillCode: String): Result<String> {
        return try {
            val skillData = EducationalContentData.getSkillById(skillCode)
                ?: return Result.failure(Exception("Skill not found"))
            Result.success(skillData.description)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Analytics
    override suspend fun trackQuestionAttempt(
        studentId: String,
        questionId: Long,
        wasCorrect: Boolean,
        responseTime: Int
    ): Result<Unit> {
        return try {
            val question = questionDao.getQuestionById(questionId)
                ?: return Result.failure(Exception("Question not found"))
            
            // Update student mastery
            val mastery = studentSkillMasteryDao.getMastery(studentId, question.skillId)
                ?: StudentSkillMasteryEntity(
                    studentId = studentId,
                    skillId = question.skillId,
                    gradeLevel = question.gradeLevel,
                    firstPracticed = Date()
                )
            
            studentSkillMasteryDao.updatePracticeStats(
                studentId = studentId,
                skillId = question.skillId,
                wasSuccessful = if (wasCorrect) 1 else 0,
                timestamp = Date(),
                practiceTimeMinutes = responseTime / 60
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSkillEffectiveness(skillId: Long): Result<Float> {
        return try {
            // Calculate based on student success rates
            // Simplified implementation
            Result.success(0.75f)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMostEffectiveTemplates(limit: Int): Result<List<ContentTemplateEntity>> {
        return try {
            val templates = contentTemplateDao.getTopEffectiveTemplates(0.7f, limit)
            Result.success(templates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Initialization
    override suspend fun initializeEducationalContent(): Result<Unit> {
        return try {
            // Initialize skills from EducationalContentData
            val skills = EducationalContentData.allSkills.map { skillData ->
                SkillEntity(
                    name = skillData.name,
                    category = mapToSkillCategory(skillData.category),
                    description = skillData.description,
                    code = skillData.skillId,
                    orderIndex = 0,
                    iconUrl = null,
                    isActive = true
                )
            }
            
            skills.forEach { skill ->
                skillDao.insertSkill(skill)
            }
            
            // Initialize skill progressions
            val progressions = createSkillProgressions()
            skillProgressionDao.insertProgressions(progressions)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isContentInitialized(): Result<Boolean> {
        return try {
            val skillCount = skillDao.getSkillCount()
            Result.success(skillCount >= 80) // We expect at least 80 skills
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Observables
    override fun observeStudentProgress(studentId: String): Flow<List<StudentSkillMasteryEntity>> {
        return studentSkillMasteryDao.observeStudentMasteries(studentId)
    }

    override fun observeSkillMastery(
        studentId: String,
        skillId: Long
    ): Flow<StudentSkillMasteryEntity?> {
        return studentSkillMasteryDao.observeStudentMasteries(studentId)
            .map { masteries ->
                masteries.find { it.skillId == skillId }
            }
    }

    // Helper methods
    private fun generateQuestionFromTemplate(template: ContentTemplateEntity): Question {
        val processedPrompt = templateProcessor.processTemplate(template)
        
        return Question(
            id = "template_${template.id}_${System.currentTimeMillis()}",
            skillId = template.skillId.toString(),
            gradeLevel = template.gradeLevel,
            prompt = processedPrompt,
            hints = generateHints(template.scaffoldingLevel, template.skillId.toString()),
            followUpQuestions = generateFollowUpQuestions(template.skillId.toString(), template.gradeLevel),
            difficulty = when (template.difficultyLevel) {
                "low" -> 0.3f
                "medium" -> 0.5f
                "high" -> 0.7f
                "advanced" -> 0.9f
                else -> 0.5f
            },
            metadata = JSONObject().apply {
                put("templateId", template.id.toString())
                put("templateType", template.templateType)
                put("generatedAt", System.currentTimeMillis().toString())
            }.toString()
        )
    }
    
    private fun generateHints(scaffoldingLevel: String?, skillCode: String): List<String> {
        return when (scaffoldingLevel) {
            "high_support" -> listOf(
                "Start by identifying the key elements in the text",
                "Make a list of what you find",
                "Think about how these elements connect",
                "Check your answer against the text"
            )
            "medium_support" -> listOf(
                "Look for patterns in the text",
                "Consider the author's purpose",
                "Think about cause and effect"
            )
            "low_support" -> listOf(
                "Re-read the relevant section",
                "Think about what the question is really asking"
            )
            else -> listOf(
                "Take your time to think through this carefully"
            )
        }
    }
    
    private fun generateFollowUpQuestions(skillCode: String, gradeLevel: Int): List<String> {
        // Socratic follow-up questions based on skill and grade
        val baseQuestions = listOf(
            "What evidence from the text supports your answer?",
            "Can you explain your thinking?",
            "What makes you say that?",
            "How did you arrive at that conclusion?"
        )
        
        val advancedQuestions = if (gradeLevel >= 6) {
            listOf(
                "What other perspectives might exist?",
                "How does this connect to what you already know?",
                "What patterns do you notice?",
                "What questions does this raise for you?"
            )
        } else {
            listOf(
                "Can you give an example?",
                "What else did you notice?",
                "How do you know?"
            )
        }
        
        return (baseQuestions.shuffled().take(2) + advancedQuestions.shuffled().take(1))
    }
    
    private fun createSkillProgressions(): List<SkillProgressionEntity> {
        // Convert the progression map from EducationalContentData
        val progressions = mutableListOf<SkillProgressionEntity>()
        val progressionMap = EducationalContentData.getSkillProgressions()
        
        // This would need to map skill codes to actual skill IDs from the database
        // Simplified for now
        return progressions
    }
    
    private fun QuestionEntity.toDomainModel(): Question {
        return Question(
            id = id.toString(),
            skillId = skillId.toString(),
            gradeLevel = gradeLevel,
            prompt = prompt,
            hints = hints.split("|").filter { it.isNotBlank() },
            followUpQuestions = followUpQuestions?.split("|")?.filter { it.isNotBlank() },
            difficulty = difficulty
        )
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
}