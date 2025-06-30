package com.studywise.ai.domain.gamification

import com.studywise.ai.test.fixtures.EducationalContentFixtures
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for TomSawyerEngine
 */
class TomSawyerEngineTest {
    
    private lateinit var tomSawyerEngine: TomSawyerEngine
    
    @Before
    fun setup() {
        tomSawyerEngine = TomSawyerEngine()
    }
    
    @Test
    fun `processLearningEvent should award points for correct answer`() = runTest {
        // Given
        val event = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true,
            responseTimeMs = 20000 // Fast response
        )
        
        // When
        val result = tomSawyerEngine.processLearningEvent(event)
        
        // Then
        assertTrue(result.pointsEarned > 0)
        assertTrue(result.experienceGained > 0)
        assertTrue(result.engagementMultiplier >= 1.0f)
    }
    
    @Test
    fun `processLearningEvent should award bonus points for fast response`() = runTest {
        // Given
        val fastEvent = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true,
            responseTimeMs = 15000 // Very fast
        )
        val slowEvent = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true,
            responseTimeMs = 60000 // Slow
        )
        
        // When
        val fastResult = tomSawyerEngine.processLearningEvent(fastEvent)
        val slowResult = tomSawyerEngine.processLearningEvent(slowEvent)
        
        // Then
        assertTrue(fastResult.pointsEarned > slowResult.pointsEarned)
    }
    
    @Test
    fun `processLearningEvent should unlock streak achievements`() = runTest {
        // Given
        val event = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true,
            currentStreak = 5 // Streak of 5
        )
        
        // When
        val result = tomSawyerEngine.processLearningEvent(event)
        
        // Then
        assertTrue(result.unlockedAchievements.isNotEmpty())
        val streakAchievement = result.unlockedAchievements.find { it.id == "streak_5" }
        assertNotNull(streakAchievement)
        assertEquals("Unstoppable 5", streakAchievement.name)
    }
    
    @Test
    fun `applyFencePaintingStrategy should make task appealing`() = runTest {
        // Given
        val task = LearningTask(
            id = "task1",
            name = "Character Analysis",
            description = "Analyze character motivations",
            skillId = "RL.4.2",
            difficulty = 0.6f,
            estimatedTime = 10,
            nextUnlock = "Advanced Character Analysis Badge"
        )
        
        // When
        val result = tomSawyerEngine.applyFencePaintingStrategy(task)
        
        // Then
        assertTrue(result.estimatedEngagementBoost > 1.0f)
        assertTrue(result.psychologicalHooks.isNotEmpty())
        assertTrue(result.reframedTask.appealFactors.isNotEmpty())
        
        // Should have exclusivity frame
        val exclusivityFactor = result.reframedTask.appealFactors.find { it.type == "exclusivity" }
        assertNotNull(exclusivityFactor)
        assertTrue(exclusivityFactor.description.contains("Special opportunity"))
    }
    
    @Test
    fun `processLearningEvent should apply psychological rewards`() = runTest {
        // Given
        val event = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true,
            choseOwnPath = true // Autonomy
        )
        
        // When
        val result = tomSawyerEngine.processLearningEvent(event)
        
        // Then
        val autonomyReward = result.rewards.find { it.type == RewardType.AUTONOMY }
        assertNotNull(autonomyReward)
        assertTrue(autonomyReward.value > 1.0f)
    }
    
    @Test
    fun `processLearningEvent should generate dynamic challenges`() = runTest {
        // Given
        val event = EducationalContentFixtures.createTestLearningEvent(
            currentStreak = 2,
            questionsCompleted = 5
        )
        
        // When
        val result = tomSawyerEngine.processLearningEvent(event)
        
        // Then
        assertTrue(result.newChallenges.isNotEmpty())
        
        // Should have streak challenge
        val streakChallenge = result.newChallenges.find { it.type == ChallengeType.STREAK }
        assertNotNull(streakChallenge)
        assertTrue(streakChallenge.description.contains("Extend your streak"))
    }
    
    @Test
    fun `processLearningEvent should update social proof`() = runTest {
        // Given
        val event = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true,
            accuracy = 0.9f
        )
        
        // When
        val result = tomSawyerEngine.processLearningEvent(event)
        
        // Then
        assertTrue(result.socialProofUpdate.activeLearnersNow > 0)
        assertTrue(result.socialProofUpdate.recentAchievements.isNotEmpty())
        assertTrue(result.socialProofUpdate.peerComparisons.isNotEmpty())
    }
    
    @Test
    fun `userProfile should update after processing events`() = runTest {
        // Given
        val initialProfile = tomSawyerEngine.userProfile.first()
        val event = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true,
            pointsEarned = 50
        )
        
        // When
        tomSawyerEngine.processLearningEvent(event)
        val updatedProfile = tomSawyerEngine.userProfile.first()
        
        // Then
        assertEquals(initialProfile.totalPoints + 50, updatedProfile.totalPoints)
        assertTrue(updatedProfile.currentStreak >= 0)
    }
    
    @Test
    fun `processLearningEvent should handle first correct answer achievement`() = runTest {
        // Given
        val event = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true,
            isFirstCorrect = true
        )
        
        // When
        val result = tomSawyerEngine.processLearningEvent(event)
        
        // Then
        val firstStepsAchievement = result.unlockedAchievements.find { it.id == "first_steps" }
        assertNotNull(firstStepsAchievement)
        assertEquals("First Steps", firstStepsAchievement.name)
    }
    
    @Test
    fun `processLearningEvent should apply engagement multipliers`() = runTest {
        // Given
        val event = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true
        )
        
        // When - Process multiple events to build engagement
        tomSawyerEngine.processLearningEvent(event)
        tomSawyerEngine.processLearningEvent(event.copy(currentStreak = 3))
        val result = tomSawyerEngine.processLearningEvent(event.copy(currentStreak = 5))
        
        // Then
        assertTrue(result.engagementMultiplier > 1.0f)
        assertTrue(result.pointsEarned > 10) // Base points with multiplier
    }
    
    @Test
    fun `processLearningEvent should handle skill mastery achievements`() = runTest {
        // Given
        val event = EducationalContentFixtures.createTestLearningEvent(
            isCorrect = true,
            skillMasteryLevel = 0.85f
        )
        
        // When
        val result = tomSawyerEngine.processLearningEvent(event)
        
        // Then
        val masteryAchievement = result.unlockedAchievements.find { 
            it.id.startsWith("skill_master_")
        }
        assertNotNull(masteryAchievement)
        assertTrue(masteryAchievement.description.contains("80% mastery"))
    }
    
    @Test
    fun `applyFencePaintingStrategy should add multiple appeal factors`() = runTest {
        // Given
        val task = LearningTask(
            id = "task1",
            name = "Story Analysis",
            description = "Analyze story elements",
            skillId = "RL.4.1",
            difficulty = 0.5f,
            estimatedTime = 15,
            nextUnlock = "Story Expert Badge"
        )
        
        // When
        val result = tomSawyerEngine.applyFencePaintingStrategy(task)
        
        // Then
        val appealFactors = result.reframedTask.appealFactors
        assertEquals(4, appealFactors.size)
        
        // Verify all psychological patterns are applied
        assertTrue(appealFactors.any { it.type == "exclusivity" })
        assertTrue(appealFactors.any { it.type == "scarcity" })
        assertTrue(appealFactors.any { it.type == "social_proof" })
        assertTrue(appealFactors.any { it.type == "progression" })
    }
    
    @Test
    fun `surprise rewards should be awarded periodically`() = runTest {
        // Given - Process exactly 7 questions to trigger surprise reward
        val baseEvent = EducationalContentFixtures.createTestLearningEvent(isCorrect = true)
        
        // When
        repeat(6) { index ->
            tomSawyerEngine.processLearningEvent(baseEvent.copy(questionsCompleted = index))
        }
        val result = tomSawyerEngine.processLearningEvent(baseEvent.copy(questionsCompleted = 6))
        
        // Then
        val surpriseReward = result.rewards.find { it.type == RewardType.SURPRISE }
        assertNotNull(surpriseReward)
        assertEquals("Lucky 7!", surpriseReward.name)
        assertTrue(surpriseReward.value >= 50f)
    }
}