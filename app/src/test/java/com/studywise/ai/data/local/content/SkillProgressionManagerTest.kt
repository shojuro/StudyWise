package com.studywise.ai.data.local.content

import com.studywise.ai.data.local.dao.SkillProgressionDao
import com.studywise.ai.data.local.dao.StudentSkillMasteryDao
import com.studywise.ai.data.local.entity.ProgressionType
import com.studywise.ai.test.fixtures.EducationalContentFixtures
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for SkillProgressionManager
 */
class SkillProgressionManagerTest {
    
    private lateinit var skillProgressionDao: SkillProgressionDao
    private lateinit var studentSkillMasteryDao: StudentSkillMasteryDao
    private lateinit var skillProgressionManager: SkillProgressionManager
    
    @Before
    fun setup() {
        skillProgressionDao = mockk()
        studentSkillMasteryDao = mockk()
        skillProgressionManager = SkillProgressionManager(skillProgressionDao, studentSkillMasteryDao)
    }
    
    @Test
    fun `selectNextSkill for new student should return foundational skill`() = runTest {
        // Given
        val skills = EducationalContentFixtures.createTestSkills()
        val progressions = EducationalContentFixtures.createTestProgressions()
        
        every { studentSkillMasteryDao.getMasteryByStudent("student123") } returns flowOf(emptyList())
        every { skillProgressionDao.getAllProgressions() } returns flowOf(progressions)
        
        // When
        val result = skillProgressionManager.selectNextSkill(
            studentId = "student123",
            gradeLevel = 4,
            availableSkills = skills,
            sessionType = QuestionSelector.SessionType.DIAGNOSTIC
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedSkill = result.getOrThrow()
        assertNotNull(selectedSkill)
        assertEquals("RL.4.1", selectedSkill.code) // Should start with first skill
    }
    
    @Test
    fun `selectNextSkill should progress to next skill when mastery achieved`() = runTest {
        // Given
        val skills = EducationalContentFixtures.createTestSkills()
        val progressions = EducationalContentFixtures.createTestProgressions()
        val masteryData = listOf(
            EducationalContentFixtures.createTestMastery("student123", 1L, 4, 0.85f) // High mastery on skill 1
        )
        
        every { studentSkillMasteryDao.getMasteryByStudent("student123") } returns flowOf(masteryData)
        every { skillProgressionDao.getAllProgressions() } returns flowOf(progressions)
        
        // When
        val result = skillProgressionManager.selectNextSkill(
            studentId = "student123",
            gradeLevel = 4,
            availableSkills = skills,
            sessionType = QuestionSelector.SessionType.PRACTICE
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedSkill = result.getOrThrow()
        assertNotNull(selectedSkill)
        assertEquals("RL.4.2", selectedSkill.code) // Should progress to skill 2
    }
    
    @Test
    fun `selectNextSkill should not progress if mastery below threshold`() = runTest {
        // Given
        val skills = EducationalContentFixtures.createTestSkills()
        val progressions = EducationalContentFixtures.createTestProgressions()
        val masteryData = listOf(
            EducationalContentFixtures.createTestMastery("student123", 1L, 4, 0.6f) // Below 0.8 threshold
        )
        
        every { studentSkillMasteryDao.getMasteryByStudent("student123") } returns flowOf(masteryData)
        every { skillProgressionDao.getAllProgressions() } returns flowOf(progressions)
        
        // When
        val result = skillProgressionManager.selectNextSkill(
            studentId = "student123",
            gradeLevel = 4,
            availableSkills = skills,
            sessionType = QuestionSelector.SessionType.PRACTICE
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedSkill = result.getOrThrow()
        assertNotNull(selectedSkill)
        assertEquals("RL.4.1", selectedSkill.code) // Should stay on skill 1
    }
    
    @Test
    fun `selectNextSkill for REVIEW should select recently practiced skill`() = runTest {
        // Given
        val skills = EducationalContentFixtures.createTestSkills()
        val recentDate = java.util.Date()
        val oldDate = java.util.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) // 7 days ago
        
        val masteryData = listOf(
            EducationalContentFixtures.createTestMastery("student123", 1L, 4, 0.7f).copy(lastPracticed = recentDate),
            EducationalContentFixtures.createTestMastery("student123", 2L, 4, 0.6f).copy(lastPracticed = oldDate),
            EducationalContentFixtures.createTestMastery("student123", 3L, 4, 0.5f).copy(lastPracticed = oldDate)
        )
        
        every { studentSkillMasteryDao.getMasteryByStudent("student123") } returns flowOf(masteryData)
        every { skillProgressionDao.getAllProgressions() } returns flowOf(emptyList())
        
        // When
        val result = skillProgressionManager.selectNextSkill(
            studentId = "student123",
            gradeLevel = 4,
            availableSkills = skills,
            sessionType = QuestionSelector.SessionType.REVIEW
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedSkill = result.getOrThrow()
        assertNotNull(selectedSkill)
        // Should select one of the older skills for review
        assertTrue(selectedSkill.code in listOf("RL.4.2", "RI.4.1"))
    }
    
    @Test
    fun `selectNextSkill for CHALLENGE should select high mastery skill`() = runTest {
        // Given
        val skills = EducationalContentFixtures.createTestSkills()
        val masteryData = listOf(
            EducationalContentFixtures.createTestMastery("student123", 1L, 4, 0.9f),
            EducationalContentFixtures.createTestMastery("student123", 2L, 4, 0.5f),
            EducationalContentFixtures.createTestMastery("student123", 3L, 4, 0.7f)
        )
        
        every { studentSkillMasteryDao.getMasteryByStudent("student123") } returns flowOf(masteryData)
        every { skillProgressionDao.getAllProgressions() } returns flowOf(emptyList())
        
        // When
        val result = skillProgressionManager.selectNextSkill(
            studentId = "student123",
            gradeLevel = 4,
            availableSkills = skills,
            sessionType = QuestionSelector.SessionType.CHALLENGE
        )
        
        // Then
        assertTrue(result.isSuccess)
        val selectedSkill = result.getOrThrow()
        assertNotNull(selectedSkill)
        assertEquals("RL.4.1", selectedSkill.code) // Should select highest mastery skill
    }
    
    @Test
    fun `getSkillReadiness should calculate correct readiness levels`() = runTest {
        // Given
        val skills = EducationalContentFixtures.createTestSkills()
        val progressions = EducationalContentFixtures.createTestProgressions()
        val masteryData = listOf(
            EducationalContentFixtures.createTestMastery("student123", 1L, 4, 0.85f),
            EducationalContentFixtures.createTestMastery("student123", 2L, 4, 0.3f)
        )
        
        every { studentSkillMasteryDao.getMasteryByStudent("student123") } returns flowOf(masteryData)
        every { skillProgressionDao.getAllProgressions() } returns flowOf(progressions)
        
        // When
        val result = skillProgressionManager.getSkillReadiness("student123", skills)
        
        // Then
        assertTrue(result.isSuccess)
        val readinessMap = result.getOrThrow()
        
        // Skill 1 should be mastered
        assertTrue(readinessMap[1L]!! >= 0.8f)
        
        // Skill 2 should have low readiness (being practiced)
        assertTrue(readinessMap[2L]!! < 0.5f)
        
        // Skill 3 should be ready since skill 1 is mastered
        assertTrue(readinessMap[3L]!! > 0.5f)
    }
    
    @Test
    fun `suggestSkillPath should create learning path based on progressions`() = runTest {
        // Given
        val skills = EducationalContentFixtures.createTestSkills()
        val progressions = EducationalContentFixtures.createTestProgressions()
        val masteryData = listOf(
            EducationalContentFixtures.createTestMastery("student123", 1L, 4, 0.85f)
        )
        
        every { studentSkillMasteryDao.getMasteryByStudent("student123") } returns flowOf(masteryData)
        every { skillProgressionDao.getAllProgressions() } returns flowOf(progressions)
        
        // When
        val result = skillProgressionManager.suggestSkillPath(
            studentId = "student123",
            targetSkillId = 5L,
            currentGradeLevel = 4
        )
        
        // Then
        assertTrue(result.isSuccess)
        val path = result.getOrThrow()
        
        // Path should include skills 2, 3, 4, 5 (skill 1 already mastered)
        assertTrue(path.size >= 4)
        assertEquals(2L, path[0].id) // Next skill should be 2
        assertEquals(5L, path.last().id) // Target skill should be last
    }
    
    @Test
    fun `updateMastery should create new mastery if not exists`() = runTest {
        // Given
        val skill = EducationalContentFixtures.createTestSkills().first()
        
        every { 
            studentSkillMasteryDao.getMasteryByStudentAndSkill("student123", skill.id) 
        } returns flowOf(null)
        
        coEvery { 
            studentSkillMasteryDao.upsertMastery(any()) 
        } just Runs
        
        // When
        val result = skillProgressionManager.updateMastery(
            studentId = "student123",
            skillId = skill.id,
            wasCorrect = true,
            responseTimeMs = 30000,
            hintsUsed = 0
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify {
            studentSkillMasteryDao.upsertMastery(match { mastery ->
                mastery.studentId == "student123" &&
                mastery.skillId == skill.id &&
                mastery.practiceCount == 1 &&
                mastery.correctCount == 1
            })
        }
    }
    
    @Test
    fun `updateMastery should update existing mastery correctly`() = runTest {
        // Given
        val existingMastery = EducationalContentFixtures.createTestMastery(
            studentId = "student123",
            skillId = 1L,
            practiceCount = 10,
            correctCount = 7
        )
        
        every { 
            studentSkillMasteryDao.getMasteryByStudentAndSkill("student123", 1L) 
        } returns flowOf(existingMastery)
        
        coEvery { 
            studentSkillMasteryDao.upsertMastery(any()) 
        } just Runs
        
        // When
        val result = skillProgressionManager.updateMastery(
            studentId = "student123",
            skillId = 1L,
            wasCorrect = true,
            responseTimeMs = 25000,
            hintsUsed = 1
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify {
            studentSkillMasteryDao.upsertMastery(match { mastery ->
                mastery.practiceCount == 11 &&
                mastery.correctCount == 8 &&
                mastery.hintsUsed == 4 // 3 + 1
            })
        }
    }
}