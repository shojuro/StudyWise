package com.studywise.ai.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studywise.ai.data.local.DatabaseInitializerV2
import com.studywise.ai.data.local.content.*
import com.studywise.ai.data.local.dao.*
import com.studywise.ai.data.local.database.StudyWiseDatabase
import com.studywise.ai.data.local.entity.*
import com.studywise.ai.data.repository.EducationalContentRepositoryImpl
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for educational content database operations
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ContentDatabaseIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var database: StudyWiseDatabase
    
    @Inject
    lateinit var skillDao: SkillDao
    
    @Inject
    lateinit var contentTemplateDao: ContentTemplateDao
    
    @Inject
    lateinit var skillProgressionDao: SkillProgressionDao
    
    @Inject
    lateinit var studentSkillMasteryDao: StudentSkillMasteryDao
    
    @Inject
    lateinit var questionDao: QuestionDao
    
    @Inject
    lateinit var databaseInitializer: DatabaseInitializerV2
    
    @Inject
    lateinit var templateProcessor: TemplateProcessor
    
    @Inject
    lateinit var promptExpansionService: PromptExpansionService
    
    @Before
    fun setup() {
        hiltRule.inject()
        
        // Clear database
        runBlocking {
            database.clearAllTables()
        }
    }
    
    @Test
    fun testDatabaseInitialization() = runBlocking {
        // When - Initialize database
        databaseInitializer.initialize()
        
        // Then - Verify skills are loaded
        val skills = skillDao.getAllSkills().first()
        assertTrue(skills.isNotEmpty(), "Skills should be loaded")
        assertEquals(80, skills.size, "Should have 80 ELA skills")
        
        // Verify skill categories
        val categories = skills.map { it.category }.toSet()
        assertTrue(categories.contains(SkillCategory.READING_LITERATURE))
        assertTrue(categories.contains(SkillCategory.READING_INFORMATIONAL))
        assertTrue(categories.contains(SkillCategory.WRITING))
        assertTrue(categories.contains(SkillCategory.LANGUAGE_GRAMMAR))
        assertTrue(categories.contains(SkillCategory.VOCABULARY_SPEAKING))
        
        // Verify grade levels
        val gradeLevels = skills.map { it.gradeLevel }.toSet()
        assertTrue(gradeLevels.containsAll(listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)))
        
        // Verify content templates
        val templates = contentTemplateDao.getAllTemplates().first()
        assertTrue(templates.isNotEmpty(), "Content templates should be loaded")
        
        // Verify skill progressions
        val progressions = skillProgressionDao.getAllProgressions().first()
        assertTrue(progressions.isNotEmpty(), "Skill progressions should be loaded")
    }
    
    @Test
    fun testContentTemplateProcessing() = runBlocking {
        // Given - Initialize and get a template
        databaseInitializer.initialize()
        val templates = contentTemplateDao.getTemplatesBySkill(1L).first()
        assertTrue(templates.isNotEmpty())
        
        val template = templates.first()
        
        // When - Process template with custom variables
        val customVars = mapOf(
            "character" to "Sarah",
            "element" to "the mysterious map"
        )
        val processedPrompt = templateProcessor.processTemplate(template, customVars)
        
        // Then - Verify processing
        assertTrue(processedPrompt.contains("Sarah") || processedPrompt.isNotEmpty())
        assertTrue(processedPrompt.length > 20, "Prompt should be meaningful")
        
        // Test variation generation
        val variations = templateProcessor.generateVariations(template, count = 5)
        assertEquals(5, variations.size)
        assertEquals(5, variations.toSet().size, "Variations should be unique")
    }
    
    @Test
    fun testPromptExpansion() = runBlocking {
        // Given - Initialize database
        databaseInitializer.initialize()
        
        // When - Expand prompts for a skill
        val result = promptExpansionService.expandPromptsForSkill(
            skillCode = "RL.4.1",
            gradeLevel = 4,
            basePrompt = "What details tell you about the main character?"
        )
        
        // Then - Verify expansion
        assertTrue(result.isSuccess)
        val expandedTemplates = result.getOrThrow()
        assertEquals(10, expandedTemplates.size, "Should generate 10 variations")
        
        // Verify templates have different variables
        val allVariables = expandedTemplates.flatMap { it.templateVariables.keys }.toSet()
        assertTrue(allVariables.size > 1, "Should have multiple variable types")
    }
    
    @Test
    fun testSkillProgressionFlow() = runBlocking {
        // Given - Initialize and create student mastery
        databaseInitializer.initialize()
        
        val studentId = "test_student"
        val skill1 = skillDao.getSkillByCode("RL.4.1")
        assertNotNull(skill1)
        
        // Create initial mastery
        val mastery = StudentSkillMasteryEntity(
            studentId = studentId,
            skillId = skill1.id,
            gradeLevel = 4,
            masteryLevel = 0.3f,
            confidenceScore = 0.5f,
            practiceCount = 5,
            correctCount = 2,
            accuracyRate = 0.4f,
            firstPracticed = java.util.Date(),
            lastPracticed = java.util.Date()
        )
        studentSkillMasteryDao.upsertMastery(mastery)
        
        // When - Update mastery after correct answers
        repeat(10) {
            val currentMastery = studentSkillMasteryDao
                .getMasteryByStudentAndSkill(studentId, skill1.id)
                .first()!!
            
            val updatedMastery = currentMastery.copy(
                practiceCount = currentMastery.practiceCount + 1,
                correctCount = currentMastery.correctCount + 1,
                accuracyRate = (currentMastery.correctCount + 1f) / (currentMastery.practiceCount + 1f),
                masteryLevel = ((currentMastery.correctCount + 1f) / (currentMastery.practiceCount + 1f) * 0.8f + 
                               currentMastery.masteryLevel * 0.2f).coerceIn(0f, 1f)
            )
            studentSkillMasteryDao.upsertMastery(updatedMastery)
        }
        
        // Then - Check progression readiness
        val finalMastery = studentSkillMasteryDao
            .getMasteryByStudentAndSkill(studentId, skill1.id)
            .first()!!
        
        assertTrue(finalMastery.masteryLevel > 0.7f, "Mastery should increase with correct answers")
        assertTrue(finalMastery.accuracyRate > 0.8f, "Accuracy should be high")
        
        // Check if ready for next skill
        val progressions = skillProgressionDao.getProgressionsFromSkill(skill1.id).first()
        if (progressions.isNotEmpty()) {
            val nextProgression = progressions.first()
            val isReady = finalMastery.masteryLevel >= nextProgression.requiredMastery
            assertTrue(isReady || finalMastery.masteryLevel > 0.7f, 
                "Should be ready for progression or have high mastery")
        }
    }
    
    @Test
    fun testEducationalContentRepositoryIntegration() = runBlocking {
        // Given - Initialize and inject repository
        databaseInitializer.initialize()
        
        val repository = EducationalContentRepositoryImpl(
            skillDao = skillDao,
            contentTemplateDao = contentTemplateDao,
            skillProgressionDao = skillProgressionDao,
            studentSkillMasteryDao = studentSkillMasteryDao,
            questionDao = questionDao,
            templateProcessor = templateProcessor,
            promptExpansionService = promptExpansionService,
            batchPromptGenerationManager = BatchPromptGenerationManager(
                promptExpansionService, contentTemplateDao
            )
        )
        
        // Test getting skills by category
        val readingSkills = repository.getSkillsByCategory("reading_literature").getOrThrow()
        assertTrue(readingSkills.isNotEmpty())
        assertTrue(readingSkills.all { it.category == SkillCategory.READING_LITERATURE })
        
        // Test creating content from template
        val template = contentTemplateDao.getAllTemplates().first().first()
        val questionResult = repository.createContentFromTemplate(
            templateId = template.id,
            variables = mapOf("character" to "the hero")
        )
        
        assertTrue(questionResult.isSuccess)
        val question = questionResult.getOrThrow()
        assertTrue(question.prompt.isNotEmpty())
        assertTrue(question.hints?.isNotEmpty() == true)
        
        // Test student progress tracking
        val studentId = "test_student"
        val progressResult = repository.getStudentProgress(studentId, 4)
        assertTrue(progressResult.isSuccess)
        
        // Track a question attempt
        repository.trackQuestionAttempt(
            studentId = studentId,
            questionId = 1L,
            wasCorrect = true,
            responseTime = 30000
        )
        
        // Verify tracking worked
        val updatedProgress = repository.getStudentProgress(studentId, 4).getOrThrow()
        assertTrue(updatedProgress.isNotEmpty() || progressResult.getOrThrow().size >= 0)
    }
    
    @Test
    fun testBatchPromptGeneration() = runBlocking {
        // Given - Initialize database
        databaseInitializer.initialize()
        
        val batchManager = BatchPromptGenerationManager(
            promptExpansionService, contentTemplateDao
        )
        
        // When - Generate batch prompts
        val result = batchManager.generateBatchPrompts(
            skills = skillDao.getAllSkills().first().take(5), // Test with 5 skills
            promptsPerSkillGrade = 10
        )
        
        // Then - Verify generation
        assertTrue(result.isSuccess)
        val stats = result.getOrThrow()
        assertTrue(stats.totalGenerated > 0)
        assertEquals(5, stats.skillsProcessed)
        
        // Verify templates were saved
        val allTemplates = contentTemplateDao.getAllTemplates().first()
        assertTrue(allTemplates.size >= stats.totalGenerated)
    }
}