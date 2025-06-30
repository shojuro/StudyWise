package com.studywise.ai.data.local.content

import com.studywise.ai.data.local.dao.QuestionDao
import com.studywise.ai.data.local.dao.StudentSkillMasteryDao
import com.studywise.ai.data.local.entity.QuestionEntity
import com.studywise.ai.test.fixtures.EducationalContentFixtures
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for QuestionSelector
 */
class QuestionSelectorTest {
    
    private lateinit var questionDao: QuestionDao
    private lateinit var studentSkillMasteryDao: StudentSkillMasteryDao
    private lateinit var questionSelector: QuestionSelector
    
    @Before
    fun setup() {
        questionDao = mockk()
        studentSkillMasteryDao = mockk()
        questionSelector = QuestionSelector(questionDao, studentSkillMasteryDao)
    }
    
    @Test
    fun `selectQuestions for DIAGNOSTIC session should return varied difficulty questions`() = runTest {
        // Given
        val skill = EducationalContentFixtures.createTestSkill()
        val questions = createTestQuestionEntities(skill.id)
        
        every { questionDao.getQuestionsBySkill(skill.id) } returns flowOf(questions)
        every { studentSkillMasteryDao.getMasteryByStudentAndSkill(any(), skill.id) } returns flowOf(null)
        
        // When
        val result = questionSelector.selectQuestions(
            skill = skill,
            gradeLevel = 4,
            sessionType = QuestionSelector.SessionType.DIAGNOSTIC,
            count = 5,
            studentId = "student123"
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedQuestions = result.getOrThrow()
        assertEquals(5, selectedQuestions.size)
        
        // Should have varied difficulties for diagnostic
        val difficulties = selectedQuestions.map { it.difficulty }.toSet()
        assertTrue(difficulties.size > 1, "Diagnostic should include varied difficulties")
    }
    
    @Test
    fun `selectQuestions for PRACTICE session should adapt to student mastery`() = runTest {
        // Given
        val skill = EducationalContentFixtures.createTestSkill()
        val mastery = EducationalContentFixtures.createTestMastery(
            studentId = "student123",
            skillId = skill.id,
            masteryLevel = 0.7f
        )
        val questions = createTestQuestionEntities(skill.id)
        
        every { questionDao.getQuestionsBySkill(skill.id) } returns flowOf(questions)
        every { studentSkillMasteryDao.getMasteryByStudentAndSkill("student123", skill.id) } returns flowOf(mastery)
        
        // When
        val result = questionSelector.selectQuestions(
            skill = skill,
            gradeLevel = 4,
            sessionType = QuestionSelector.SessionType.PRACTICE,
            count = 5,
            studentId = "student123"
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedQuestions = result.getOrThrow()
        assertEquals(5, selectedQuestions.size)
        
        // Should select questions around mastery level (0.7)
        val avgDifficulty = selectedQuestions.map { it.difficulty }.average()
        assertTrue(avgDifficulty >= 0.6 && avgDifficulty <= 0.8, 
            "Practice questions should match student mastery level")
    }
    
    @Test
    fun `selectQuestions for REVIEW session should focus on previously incorrect`() = runTest {
        // Given
        val skill = EducationalContentFixtures.createTestSkill()
        val mastery = EducationalContentFixtures.createTestMastery(
            studentId = "student123",
            skillId = skill.id,
            incorrectAnswers = 5
        )
        val questions = createTestQuestionEntities(skill.id)
        
        every { questionDao.getQuestionsBySkill(skill.id) } returns flowOf(questions)
        every { studentSkillMasteryDao.getMasteryByStudentAndSkill("student123", skill.id) } returns flowOf(mastery)
        
        // When
        val result = questionSelector.selectQuestions(
            skill = skill,
            gradeLevel = 4,
            sessionType = QuestionSelector.SessionType.REVIEW,
            count = 3,
            studentId = "student123"
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedQuestions = result.getOrThrow()
        assertEquals(3, selectedQuestions.size)
        
        // For review, should include easier questions for reinforcement
        val avgDifficulty = selectedQuestions.map { it.difficulty }.average()
        assertTrue(avgDifficulty <= 0.6, "Review questions should be easier for reinforcement")
    }
    
    @Test
    fun `selectQuestions for CHALLENGE session should select harder questions`() = runTest {
        // Given
        val skill = EducationalContentFixtures.createTestSkill()
        val mastery = EducationalContentFixtures.createTestMastery(
            studentId = "student123",
            skillId = skill.id,
            masteryLevel = 0.85f
        )
        val questions = createTestQuestionEntities(skill.id)
        
        every { questionDao.getQuestionsBySkill(skill.id) } returns flowOf(questions)
        every { studentSkillMasteryDao.getMasteryByStudentAndSkill("student123", skill.id) } returns flowOf(mastery)
        
        // When
        val result = questionSelector.selectQuestions(
            skill = skill,
            gradeLevel = 4,
            sessionType = QuestionSelector.SessionType.CHALLENGE,
            count = 5,
            studentId = "student123"
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedQuestions = result.getOrThrow()
        assertEquals(5, selectedQuestions.size)
        
        // Challenge questions should be harder
        val avgDifficulty = selectedQuestions.map { it.difficulty }.average()
        assertTrue(avgDifficulty >= 0.7, "Challenge questions should be harder")
    }
    
    @Test
    fun `selectQuestions should handle insufficient questions gracefully`() = runTest {
        // Given
        val skill = EducationalContentFixtures.createTestSkill()
        val questions = listOf(
            createQuestionEntity(1L, skill.id, 0.5f),
            createQuestionEntity(2L, skill.id, 0.6f)
        )
        
        every { questionDao.getQuestionsBySkill(skill.id) } returns flowOf(questions)
        every { studentSkillMasteryDao.getMasteryByStudentAndSkill(any(), skill.id) } returns flowOf(null)
        
        // When
        val result = questionSelector.selectQuestions(
            skill = skill,
            gradeLevel = 4,
            sessionType = QuestionSelector.SessionType.PRACTICE,
            count = 5,
            studentId = "student123"
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedQuestions = result.getOrThrow()
        assertEquals(2, selectedQuestions.size) // Only 2 available
    }
    
    @Test
    fun `selectQuestions should avoid recently used questions`() = runTest {
        // Given
        val skill = EducationalContentFixtures.createTestSkill()
        val recentTime = System.currentTimeMillis() - 60000 // 1 minute ago
        val oldTime = System.currentTimeMillis() - 86400000 // 1 day ago
        
        val questions = listOf(
            createQuestionEntity(1L, skill.id, 0.5f, lastUsed = recentTime),
            createQuestionEntity(2L, skill.id, 0.5f, lastUsed = recentTime),
            createQuestionEntity(3L, skill.id, 0.5f, lastUsed = oldTime),
            createQuestionEntity(4L, skill.id, 0.5f, lastUsed = oldTime),
            createQuestionEntity(5L, skill.id, 0.5f, lastUsed = null)
        )
        
        every { questionDao.getQuestionsBySkill(skill.id) } returns flowOf(questions)
        every { studentSkillMasteryDao.getMasteryByStudentAndSkill(any(), skill.id) } returns flowOf(null)
        
        // When
        val result = questionSelector.selectQuestions(
            skill = skill,
            gradeLevel = 4,
            sessionType = QuestionSelector.SessionType.PRACTICE,
            count = 3,
            studentId = "student123"
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedQuestions = result.getOrThrow()
        assertEquals(3, selectedQuestions.size)
        
        // Should prefer questions that haven't been used recently
        val selectedIds = selectedQuestions.map { it.id }
        assertTrue(selectedIds.contains("3") || selectedIds.contains("4") || selectedIds.contains("5"))
    }
    
    @Test
    fun `selectQuestions should handle empty question list`() = runTest {
        // Given
        val skill = EducationalContentFixtures.createTestSkill()
        
        every { questionDao.getQuestionsBySkill(skill.id) } returns flowOf(emptyList())
        every { studentSkillMasteryDao.getMasteryByStudentAndSkill(any(), skill.id) } returns flowOf(null)
        
        // When
        val result = questionSelector.selectQuestions(
            skill = skill,
            gradeLevel = 4,
            sessionType = QuestionSelector.SessionType.PRACTICE,
            count = 5,
            studentId = "student123"
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedQuestions = result.getOrThrow()
        assertTrue(selectedQuestions.isEmpty())
    }
    
    // Helper functions
    private fun createTestQuestionEntities(skillId: Long): List<QuestionEntity> {
        return listOf(
            createQuestionEntity(1L, skillId, 0.2f),
            createQuestionEntity(2L, skillId, 0.3f),
            createQuestionEntity(3L, skillId, 0.4f),
            createQuestionEntity(4L, skillId, 0.5f),
            createQuestionEntity(5L, skillId, 0.6f),
            createQuestionEntity(6L, skillId, 0.7f),
            createQuestionEntity(7L, skillId, 0.8f),
            createQuestionEntity(8L, skillId, 0.9f)
        )
    }
    
    private fun createQuestionEntity(
        id: Long,
        skillId: Long,
        difficulty: Float,
        lastUsed: Long? = null
    ) = QuestionEntity(
        id = id,
        skillId = skillId,
        gradeLevel = 4,
        prompt = "Test question $id",
        correctAnswer = "Answer $id",
        incorrectAnswers = listOf("Wrong 1", "Wrong 2", "Wrong 3"),
        hints = listOf("Hint 1", "Hint 2"),
        explanation = "Explanation for question $id",
        difficulty = difficulty,
        tags = listOf("test"),
        metadata = emptyMap(),
        createdAt = java.util.Date(),
        lastUsed = lastUsed?.let { java.util.Date(it) }
    )
}