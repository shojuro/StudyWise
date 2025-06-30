package com.studywise.ai.domain.service

import com.studywise.ai.domain.gamification.*
import com.studywise.ai.presentation.screens.session.AnswerEvaluation
import com.studywise.ai.test.fixtures.EducationalContentFixtures
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for GamificationIntegrationService
 */
class GamificationIntegrationServiceTest {
    
    private lateinit var tomSawyerEngine: TomSawyerEngine
    private lateinit var analyticsService: AnalyticsService
    private lateinit var gamificationService: GamificationIntegrationService
    
    @Before
    fun setup() {
        tomSawyerEngine = mockk(relaxed = true)
        analyticsService = mockk(relaxed = true)
        gamificationService = GamificationIntegrationService(tomSawyerEngine, analyticsService)
    }
    
    @Test
    fun `initializeSession should apply fence painting strategy`() = runTest {
        // Given
        val skill = EducationalContentFixtures.createTestSkill()
        val fencePaintingResult = FencePaintingResult(
            reframedTask = LearningTask(
                id = skill.id.toString(),
                name = skill.name,
                description = "Enhanced: ${skill.description}",
                skillId = skill.code,
                difficulty = 0.5f,
                estimatedTime = 10,
                appealFactors = listOf(
                    AppealFactor("exclusivity", "Special opportunity", 0.8f)
                )
            ),
            estimatedEngagementBoost = 1.5f,
            psychologicalHooks = listOf("Only top students get this", "Limited time bonus")
        )
        
        every { 
            tomSawyerEngine.applyFencePaintingStrategy(any()) 
        } returns fencePaintingResult
        
        // When
        gamificationService.initializeSession(
            userId = "student123",
            subject = "Reading",
            skill = skill,
            sessionType = "practice"
        )
        
        // Then
        verify {
            tomSawyerEngine.applyFencePaintingStrategy(match { task ->
                task.name == skill.name &&
                task.skillId == skill.code
            })
        }
        
        verify {
            analyticsService.logEvent(match<AnalyticsEvent.GamificationApplied> { event ->
                event.userId == "student123" &&
                event.strategy == "fence_painting" &&
                event.hooks == fencePaintingResult.psychologicalHooks
            })
        }
    }
    
    @Test
    fun `onQuestionPresented should detect milestone questions`() = runTest {
        // Given
        val question = EducationalContentFixtures.createTestQuestion()
        
        // When - Halfway milestone
        val halfwayResult = gamificationService.onQuestionPresented(
            question = question,
            questionIndex = 5, // Halfway through 10 questions
            totalQuestions = 10
        )
        
        // Then
        assertNotNull(halfwayResult.milestoneReward)
        assertEquals(RewardType.SURPRISE, halfwayResult.milestoneReward?.type)
        assertEquals("Halfway Hero!", halfwayResult.milestoneReward?.name)
        assertEquals(25f, halfwayResult.milestoneReward?.value)
    }
    
    @Test
    fun `onQuestionPresented should apply speed bonuses`() = runTest {
        // Given
        gamificationService.initializeSession(
            userId = "student123",
            subject = "Reading",
            skill = EducationalContentFixtures.createTestSkill(),
            sessionType = "practice"
        )
        
        val question = EducationalContentFixtures.createTestQuestion()
        
        // When - Fast session (< 5 minutes)
        val result = gamificationService.onQuestionPresented(
            question = question,
            questionIndex = 2,
            totalQuestions = 10
        )
        
        // Then
        assertNotNull(result.speedBonusMessage)
        assertTrue(result.speedBonusMessage!!.contains("Lightning Fast"))
        assertTrue(result.speedBonusMessage!!.contains("20%"))
    }
    
    @Test
    fun `processAnswerSubmission should handle correct answer with rewards`() = runTest {
        // Given
        gamificationService.initializeSession(
            userId = "student123",
            subject = "Reading",
            skill = EducationalContentFixtures.createTestSkill(),
            sessionType = "practice"
        )
        
        val question = EducationalContentFixtures.createTestQuestion()
        val evaluation = AnswerEvaluation(
            isCorrect = true,
            feedback = "Great job!",
            suggestedFollowUp = "Think deeper"
        )
        
        val gamificationResult = GamificationResult(
            pointsEarned = 15,
            experienceGained = 50,
            unlockedAchievements = listOf(
                EducationalContentFixtures.createTestAchievement()
            ),
            newChallenges = emptyList(),
            rewards = listOf(
                Reward(
                    type = RewardType.POINTS,
                    name = "Correct Answer",
                    description = "Well done!",
                    value = 10f
                )
            ),
            socialProofUpdate = SocialProofData(activeLearnersNow = 150),
            engagementMultiplier = 1.2f,
            nextRecommendation = LearningRecommendation(
                skillId = "next_skill",
                reason = "You're ready",
                estimatedTime = 10,
                potentialPoints = 100,
                unlockables = emptyList()
            )
        )
        
        every { 
            tomSawyerEngine.processLearningEvent(any()) 
        } returns gamificationResult
        
        // When
        val result = gamificationService.processAnswerSubmission(
            question = question,
            userAnswer = "Good answer",
            evaluation = evaluation,
            responseTimeMs = 25000,
            hintsUsed = 0,
            skillMastery = null
        )
        
        // Then
        assertEquals(15, result.pointsEarned)
        assertEquals(50, result.experienceGained)
        assertTrue(result.newAchievements.isNotEmpty())
        assertNotNull(result.celebration) // Should celebrate achievement
        assertEquals(CelebrationType.ACHIEVEMENT, result.celebration?.type)
    }
    
    @Test
    fun `processAnswerSubmission should celebrate streaks`() = runTest {
        // Given - Initialize with existing streak
        gamificationService.initializeSession(
            userId = "student123",
            subject = "Reading",
            skill = EducationalContentFixtures.createTestSkill(),
            sessionType = "practice"
        )
        
        // Simulate 4 correct answers to set up for 5-streak
        repeat(4) {
            gamificationService.processAnswerSubmission(
                question = EducationalContentFixtures.createTestQuestion(),
                userAnswer = "answer",
                evaluation = AnswerEvaluation(true, "Good"),
                responseTimeMs = 30000,
                hintsUsed = 0,
                skillMastery = null
            )
        }
        
        val gamificationResult = GamificationResult(
            pointsEarned = 10,
            experienceGained = 30,
            unlockedAchievements = emptyList(),
            newChallenges = emptyList(),
            rewards = emptyList(),
            socialProofUpdate = SocialProofData(),
            engagementMultiplier = 1.0f,
            nextRecommendation = LearningRecommendation(
                skillId = "skill",
                reason = "Continue",
                estimatedTime = 10,
                potentialPoints = 50,
                unlockables = emptyList()
            )
        )
        
        every { 
            tomSawyerEngine.processLearningEvent(any()) 
        } returns gamificationResult
        
        // When - 5th correct answer
        val result = gamificationService.processAnswerSubmission(
            question = EducationalContentFixtures.createTestQuestion(),
            userAnswer = "answer",
            evaluation = AnswerEvaluation(true, "Good"),
            responseTimeMs = 30000,
            hintsUsed = 0,
            skillMastery = null
        )
        
        // Then
        assertNotNull(result.celebration)
        assertEquals(CelebrationType.STREAK, result.celebration?.type)
        assertEquals("5 in a Row!", result.celebration?.title)
        assertEquals("fire_streak", result.celebration?.animation)
    }
    
    @Test
    fun `completeSession should calculate performance rating`() = runTest {
        // Given - Session with good performance
        gamificationService.initializeSession(
            userId = "student123",
            subject = "Reading",
            skill = EducationalContentFixtures.createTestSkill(),
            sessionType = "practice"
        )
        
        // Simulate 8/10 correct answers
        repeat(8) {
            gamificationService.processAnswerSubmission(
                question = EducationalContentFixtures.createTestQuestion(),
                userAnswer = "answer",
                evaluation = AnswerEvaluation(true, "Good"),
                responseTimeMs = 30000,
                hintsUsed = 0,
                skillMastery = null
            )
        }
        repeat(2) {
            gamificationService.processAnswerSubmission(
                question = EducationalContentFixtures.createTestQuestion(),
                userAnswer = "answer",
                evaluation = AnswerEvaluation(false, "Try again"),
                responseTimeMs = 30000,
                hintsUsed = 0,
                skillMastery = null
            )
        }
        
        every { 
            tomSawyerEngine.userProfile 
        } returns MutableStateFlow(
            MotivationProfile(
                userId = "student123",
                level = 5,
                totalPoints = 500
            )
        )
        
        // When
        val result = gamificationService.completeSession()
        
        // Then
        assertEquals(PerformanceRating.EXCELLENT, result.performanceRating) // 80% accuracy
        assertTrue(result.personalizedSummary.greeting.contains("Excellent"))
        assertTrue(result.totalPointsEarned > 0)
        assertTrue(result.shareableStats.stats.containsKey("Accuracy"))
        assertEquals("80%", result.shareableStats.stats["Accuracy"])
    }
    
    @Test
    fun `getGamificationFlow should combine all gamification states`() = runTest {
        // Given
        val mockProfile = MutableStateFlow(
            MotivationProfile(
                userId = "student123",
                level = 3,
                totalPoints = 250
            )
        )
        val mockChallenges = MutableStateFlow(
            listOf(
                DynamicChallenge(
                    id = "c1",
                    type = ChallengeType.STREAK,
                    title = "Streak Master",
                    description = "Get 10 in a row",
                    requirements = mapOf("streak" to 10),
                    rewards = ChallengeRewards(points = 100, experienceMultiplier = 2f),
                    progress = 0.5f
                )
            )
        )
        val mockSocialProof = MutableStateFlow(
            SocialProofData(
                activeLearnersNow = 200,
                leaderboardPosition = 15
            )
        )
        
        every { tomSawyerEngine.userProfile } returns mockProfile
        every { tomSawyerEngine.currentChallenges } returns mockChallenges
        every { tomSawyerEngine.socialProof } returns mockSocialProof
        
        gamificationService.initializeSession(
            userId = "student123",
            subject = "Reading",
            skill = EducationalContentFixtures.createTestSkill(),
            sessionType = "practice"
        )
        
        // When
        val flow = gamificationService.getGamificationFlow()
        
        // Then
        flow.collect { state ->
            assertEquals(3, state.level)
            assertEquals(0.25f, state.levelProgress) // 250/1000
            assertTrue(state.activeChallenges.isNotEmpty())
            assertEquals(200, state.socialProof.activeLearnersNow)
            assertTrue(state.nextMilestone.contains("points to next level"))
        }
    }
}