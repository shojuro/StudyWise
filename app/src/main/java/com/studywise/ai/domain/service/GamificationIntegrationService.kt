package com.studywise.ai.domain.service

import com.studywise.ai.domain.gamification.*
import com.studywise.ai.domain.model.Question
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.data.local.entity.SkillEntity
import com.studywise.ai.data.local.entity.StudentSkillMasteryEntity
import com.studywise.ai.presentation.screens.session.AnswerEvaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Integrates the educational content system with the Tom Sawyer gamification engine
 */
@Singleton
class GamificationIntegrationService @Inject constructor(
    private val tomSawyerEngine: TomSawyerEngine,
    private val analyticsService: AnalyticsService
) {
    
    private val _sessionGamificationState = MutableStateFlow(SessionGamificationState())
    
    /**
     * Initialize gamification for a learning session
     */
    fun initializeSession(
        userId: String,
        subject: String,
        skill: SkillEntity,
        sessionType: String
    ) {
        _sessionGamificationState.value = SessionGamificationState(
            userId = userId,
            subject = subject,
            currentSkill = skill,
            sessionType = sessionType,
            sessionStartTime = System.currentTimeMillis()
        )
        
        // Apply Tom Sawyer's fence painting strategy to make the session appealing
        val learningTask = LearningTask(
            id = skill.id.toString(),
            name = skill.name,
            description = skill.description,
            skillId = skill.code,
            difficulty = 0.5f,
            estimatedTime = 10,
            nextUnlock = "Master ${skill.name} Badge"
        )
        
        val fencePaintingResult = tomSawyerEngine.applyFencePaintingStrategy(learningTask)
        
        // Log the psychological hooks for the session
        analyticsService.logEvent(
            AnalyticsEvent.CustomEvent(
                eventName = "gamification_applied",
                parameters = mapOf(
                    "user_id" to userId,
                    "strategy" to "fence_painting",
                    "hooks" to fencePaintingResult.psychologicalHooks.joinToString(",")
                )
            )
        )
    }
    
    /**
     * Process a question presentation with gamification
     */
    fun onQuestionPresented(
        question: Question,
        questionIndex: Int,
        totalQuestions: Int
    ): QuestionGamificationData {
        val state = _sessionGamificationState.value
        
        // Check if this is a special milestone question
        val milestoneRewards = when (questionIndex) {
            totalQuestions / 2 -> Reward(
                type = RewardType.SURPRISE,
                name = "Halfway Hero!",
                description = "You're halfway through! Bonus points incoming!",
                value = 25f
            )
            totalQuestions - 1 -> Reward(
                type = RewardType.EXPERIENCE,
                name = "Final Push!",
                description = "Last question - make it count for double XP!",
                value = 2.0f
            )
            else -> null
        }
        
        // Apply time-based bonuses
        val timeSinceStart = System.currentTimeMillis() - state.sessionStartTime
        val speedBonus = when {
            timeSinceStart < 5 * 60 * 1000 -> "Lightning Fast! +20% bonus points"
            timeSinceStart < 10 * 60 * 1000 -> "Great Pace! +10% bonus points"
            else -> null
        }
        
        return QuestionGamificationData(
            milestoneReward = milestoneRewards,
            speedBonusMessage = speedBonus,
            currentCombo = state.correctAnswerStreak,
            motivationalMessage = generateMotivationalMessage(questionIndex, state)
        )
    }
    
    /**
     * Process answer submission with gamification
     */
    suspend fun processAnswerSubmission(
        question: Question,
        userAnswer: String,
        evaluation: AnswerEvaluation,
        responseTimeMs: Long,
        hintsUsed: Int,
        skillMastery: StudentSkillMasteryEntity?
    ): AnswerGamificationResult {
        val state = _sessionGamificationState.value
        
        // Create learning event for Tom Sawyer engine
        val learningEvent = LearningEvent(
            userId = state.userId,
            skillId = state.currentSkill?.code ?: "",
            skillName = state.currentSkill?.name ?: "",
            isCorrect = evaluation.isCorrect,
            accuracy = skillMastery?.accuracyRate ?: 0.5f,
            difficulty = question.difficulty,
            responseTimeMs = responseTimeMs,
            usedHint = hintsUsed > 0,
            currentStreak = if (evaluation.isCorrect) state.correctAnswerStreak + 1 else 0,
            questionsCompleted = state.questionsCompleted + 1,
            pointsEarned = 0, // Will be calculated by engine
            isFirstCorrect = state.questionsCompleted == 0 && evaluation.isCorrect,
            choseOwnPath = state.sessionType == "exploration",
            skillMasteryLevel = skillMastery?.masteryLevel ?: 0f
        )
        
        // Process through Tom Sawyer engine
        val gamificationResult = tomSawyerEngine.processLearningEvent(learningEvent)
        
        // Update session state
        _sessionGamificationState.value = state.copy(
            correctAnswerStreak = if (evaluation.isCorrect) state.correctAnswerStreak + 1 else 0,
            questionsCompleted = state.questionsCompleted + 1,
            totalPointsEarned = state.totalPointsEarned + gamificationResult.pointsEarned,
            totalExperienceGained = state.totalExperienceGained + gamificationResult.experienceGained,
            unlockedAchievements = state.unlockedAchievements + gamificationResult.unlockedAchievements,
            activeChallenges = gamificationResult.newChallenges
        )
        
        // Create celebration data if needed
        val celebration = when {
            gamificationResult.unlockedAchievements.isNotEmpty() -> 
                CelebrationData(
                    type = CelebrationType.ACHIEVEMENT,
                    title = "Achievement Unlocked!",
                    message = gamificationResult.unlockedAchievements.first().name,
                    animation = "trophy_burst"
                )
            state.correctAnswerStreak == 5 -> 
                CelebrationData(
                    type = CelebrationType.STREAK,
                    title = "5 in a Row!",
                    message = "You're on fire! 🔥",
                    animation = "fire_streak"
                )
            state.correctAnswerStreak == 10 -> 
                CelebrationData(
                    type = CelebrationType.STREAK,
                    title = "Unstoppable!",
                    message = "10 correct answers in a row!",
                    animation = "star_explosion"
                )
            gamificationResult.rewards.any { it.type == RewardType.LIMITED_TIME } ->
                CelebrationData(
                    type = CelebrationType.BONUS,
                    title = "Bonus Time!",
                    message = gamificationResult.rewards.first { it.type == RewardType.LIMITED_TIME }.description,
                    animation = "coin_shower"
                )
            else -> null
        }
        
        return AnswerGamificationResult(
            pointsEarned = gamificationResult.pointsEarned,
            experienceGained = gamificationResult.experienceGained,
            newAchievements = gamificationResult.unlockedAchievements,
            activeRewards = gamificationResult.rewards,
            celebration = celebration,
            socialProofMessage = generateSocialProofMessage(gamificationResult.socialProofUpdate),
            nextRecommendation = gamificationResult.nextRecommendation,
            updatedChallenges = gamificationResult.newChallenges
        )
    }
    
    /**
     * Complete session with final gamification summary
     */
    fun completeSession(): SessionCompletionGamification {
        val state = _sessionGamificationState.value
        val profile = tomSawyerEngine.userProfile.value
        
        // Calculate performance metrics
        val accuracy = state.questionsCompleted.let { total ->
            if (total > 0) state.correctAnswerStreak.toFloat() / total else 0f
        }
        
        val performanceRating = when {
            accuracy >= 0.9f -> PerformanceRating.EXCEPTIONAL
            accuracy >= 0.8f -> PerformanceRating.EXCELLENT
            accuracy >= 0.7f -> PerformanceRating.GOOD
            accuracy >= 0.6f -> PerformanceRating.SATISFACTORY
            else -> PerformanceRating.NEEDS_IMPROVEMENT
        }
        
        // Generate personalized summary
        val summary = PersonalizedSummary(
            greeting = generateGreeting(performanceRating),
            highlights = generateHighlights(state),
            improvements = generateImprovements(state, accuracy),
            nextSteps = generateNextSteps(state.currentSkill, profile)
        )
        
        // Calculate final rewards
        val completionBonus = when (performanceRating) {
            PerformanceRating.EXCEPTIONAL -> 100
            PerformanceRating.EXCELLENT -> 75
            PerformanceRating.GOOD -> 50
            PerformanceRating.SATISFACTORY -> 25
            PerformanceRating.NEEDS_IMPROVEMENT -> 10
        }
        
        return SessionCompletionGamification(
            totalPointsEarned = state.totalPointsEarned + completionBonus,
            totalExperienceGained = state.totalExperienceGained,
            performanceRating = performanceRating,
            unlockedAchievements = state.unlockedAchievements,
            personalizedSummary = summary,
            shareableStats = generateShareableStats(state, accuracy),
            nextSessionIncentive = generateNextSessionIncentive(profile)
        )
    }
    
    /**
     * Get real-time gamification state
     */
    fun getGamificationFlow(): Flow<GamificationUIState> {
        return combine(
            _sessionGamificationState,
            tomSawyerEngine.userProfile,
            tomSawyerEngine.currentChallenges,
            tomSawyerEngine.socialProof
        ) { sessionState, profile, challenges, socialProof ->
            GamificationUIState(
                currentPoints = sessionState.totalPointsEarned,
                currentStreak = sessionState.correctAnswerStreak,
                level = profile.level,
                levelProgress = calculateLevelProgress(profile.totalPoints),
                activeChallenges = challenges,
                socialProof = socialProof,
                nextMilestone = calculateNextMilestone(sessionState, profile)
            )
        }
    }
    
    // Helper methods
    private fun generateMotivationalMessage(questionIndex: Int, state: SessionGamificationState): String {
        return when {
            questionIndex == 0 -> "Let's start strong! 💪"
            state.correctAnswerStreak >= 3 -> "You're on a roll! Keep it up!"
            state.correctAnswerStreak == 0 && questionIndex > 3 -> "Every expert was once a beginner. You've got this!"
            questionIndex == 5 -> "Halfway there! You're doing great!"
            else -> listOf(
                "Focus and conquer!",
                "Your brain is getting stronger!",
                "Each question is a stepping stone to mastery!",
                "Believe in yourself!"
            ).random()
        }
    }
    
    private fun generateSocialProofMessage(socialProof: SocialProofData): String? {
        return when {
            socialProof.leaderboardPosition <= 10 -> 
                "You're in the Top 10! 🏆"
            socialProof.activeLearnersNow > 200 -> 
                "${socialProof.activeLearnersNow} students learning right now!"
            socialProof.peerComparisons["accuracy"]?.let { it > 1.2f } == true ->
                "Your accuracy is 20% above average!"
            else -> null
        }
    }
    
    private fun generateGreeting(rating: PerformanceRating): String {
        return when (rating) {
            PerformanceRating.EXCEPTIONAL -> "🌟 Absolutely phenomenal work!"
            PerformanceRating.EXCELLENT -> "🎉 Excellent session!"
            PerformanceRating.GOOD -> "👏 Good job today!"
            PerformanceRating.SATISFACTORY -> "✅ Nice effort!"
            PerformanceRating.NEEDS_IMPROVEMENT -> "📚 Every journey starts with a single step!"
        }
    }
    
    private fun generateHighlights(state: SessionGamificationState): List<String> {
        val highlights = mutableListOf<String>()
        
        if (state.correctAnswerStreak > 0) {
            highlights.add("Achieved a ${state.correctAnswerStreak}-answer streak!")
        }
        
        if (state.totalPointsEarned > 100) {
            highlights.add("Earned ${state.totalPointsEarned} points!")
        }
        
        if (state.unlockedAchievements.isNotEmpty()) {
            highlights.add("Unlocked ${state.unlockedAchievements.size} achievements!")
        }
        
        return highlights
    }
    
    private fun generateImprovements(state: SessionGamificationState, accuracy: Float): List<String> {
        val improvements = mutableListOf<String>()
        
        if (accuracy < 0.7f) {
            improvements.add("Try using hints strategically to build understanding")
        }
        
        if (state.questionsCompleted < 5) {
            improvements.add("Complete more questions to accelerate learning")
        }
        
        return improvements
    }
    
    private fun generateNextSteps(skill: SkillEntity?, profile: MotivationProfile): List<String> {
        return listOf(
            "Continue with ${skill?.name ?: "next skill"} tomorrow",
            "Try a challenge mode for bonus points",
            "Review today's concepts before bed for better retention"
        )
    }
    
    private fun generateShareableStats(state: SessionGamificationState, accuracy: Float): ShareableStats {
        return ShareableStats(
            headline = "I just completed a StudyWise session!",
            stats = mapOf(
                "Points Earned" to state.totalPointsEarned.toString(),
                "Accuracy" to "${(accuracy * 100).toInt()}%",
                "Streak" to state.correctAnswerStreak.toString()
            ),
            hashtags = listOf("#StudyWise", "#Learning", "#Education", "#AchievementUnlocked")
        )
    }
    
    private fun generateNextSessionIncentive(profile: MotivationProfile): String {
        return when {
            profile.currentStreak > 0 -> "Come back tomorrow to extend your ${profile.currentStreak}-day streak!"
            profile.level % 5 == 4 -> "You're one level away from a major milestone!"
            else -> "Daily bonus awaits! Extra 50 points for logging in tomorrow!"
        }
    }
    
    private fun calculateLevelProgress(totalPoints: Int): Float {
        val currentLevel = (totalPoints / 1000)
        val pointsInCurrentLevel = totalPoints % 1000
        return pointsInCurrentLevel / 1000f
    }
    
    private fun calculateNextMilestone(state: SessionGamificationState, profile: MotivationProfile): String {
        return when {
            state.correctAnswerStreak == 4 -> "1 more for a 5-streak!"
            profile.totalPoints % 1000 > 900 -> "${1000 - (profile.totalPoints % 1000)} points to next level!"
            else -> "Keep going!"
        }
    }
}

// Data classes
data class SessionGamificationState(
    val userId: String = "",
    val subject: String = "",
    val currentSkill: SkillEntity? = null,
    val sessionType: String = "",
    val sessionStartTime: Long = 0L,
    val correctAnswerStreak: Int = 0,
    val questionsCompleted: Int = 0,
    val totalPointsEarned: Int = 0,
    val totalExperienceGained: Int = 0,
    val unlockedAchievements: List<Achievement> = emptyList(),
    val activeChallenges: List<DynamicChallenge> = emptyList()
)

data class QuestionGamificationData(
    val milestoneReward: Reward? = null,
    val speedBonusMessage: String? = null,
    val currentCombo: Int = 0,
    val motivationalMessage: String = ""
)

data class AnswerGamificationResult(
    val pointsEarned: Int,
    val experienceGained: Int,
    val newAchievements: List<Achievement>,
    val activeRewards: List<Reward>,
    val celebration: CelebrationData? = null,
    val socialProofMessage: String? = null,
    val nextRecommendation: LearningRecommendation,
    val updatedChallenges: List<DynamicChallenge>
)

data class SessionCompletionGamification(
    val totalPointsEarned: Int,
    val totalExperienceGained: Int,
    val performanceRating: PerformanceRating,
    val unlockedAchievements: List<Achievement>,
    val personalizedSummary: PersonalizedSummary,
    val shareableStats: ShareableStats,
    val nextSessionIncentive: String
)

data class CelebrationData(
    val type: CelebrationType,
    val title: String,
    val message: String,
    val animation: String
)

data class PersonalizedSummary(
    val greeting: String,
    val highlights: List<String>,
    val improvements: List<String>,
    val nextSteps: List<String>
)

data class ShareableStats(
    val headline: String,
    val stats: Map<String, String>,
    val hashtags: List<String>
)

data class GamificationUIState(
    val currentPoints: Int,
    val currentStreak: Int,
    val level: Int,
    val levelProgress: Float,
    val activeChallenges: List<DynamicChallenge>,
    val socialProof: SocialProofData,
    val nextMilestone: String
)

// Enums
enum class CelebrationType {
    ACHIEVEMENT, STREAK, LEVEL_UP, BONUS, CHALLENGE_COMPLETE
}

enum class PerformanceRating {
    EXCEPTIONAL, EXCELLENT, GOOD, SATISFACTORY, NEEDS_IMPROVEMENT
}