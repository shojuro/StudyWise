package com.studywise.ai.domain.gamification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.random.Random

/**
 * Tom Sawyer Gamification Engine
 * 
 * Inspired by Tom Sawyer's fence-painting genius, this engine makes learning
 * irresistibly engaging through psychological patterns that transform 
 * educational tasks into coveted achievements.
 */
@Singleton
class TomSawyerEngine @Inject constructor() {

    private val _userProfile = MutableStateFlow(MotivationProfile())
    val userProfile: StateFlow<MotivationProfile> = _userProfile.asStateFlow()

    private val _currentChallenges = MutableStateFlow<List<DynamicChallenge>>(emptyList())
    val currentChallenges: StateFlow<List<DynamicChallenge>> = _currentChallenges.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _socialProof = MutableStateFlow(SocialProofData())
    val socialProof: StateFlow<SocialProofData> = _socialProof.asStateFlow()

    // Psychological patterns from Tom Sawyer
    private val psychologicalPatterns = listOf(
        PsychologicalPattern.SCARCITY,
        PsychologicalPattern.SOCIAL_PROOF,
        PsychologicalPattern.AUTONOMY,
        PsychologicalPattern.MASTERY,
        PsychologicalPattern.PURPOSE,
        PsychologicalPattern.EXCLUSIVITY,
        PsychologicalPattern.PROGRESSION,
        PsychologicalPattern.SURPRISE_DELIGHT
    )

    /**
     * Process a learning event and apply gamification
     */
    fun processLearningEvent(event: LearningEvent): GamificationResult {
        // Update motivation profile based on event
        updateMotivationProfile(event)
        
        // Generate dynamic challenges
        val newChallenges = generateDynamicChallenges(event)
        
        // Check for achievements
        val unlockedAchievements = checkAchievements(event)
        
        // Apply psychological patterns
        val rewards = applyPsychologicalPatterns(event)
        
        // Update social proof
        updateSocialProof(event)
        
        // Calculate engagement boost
        val engagementMultiplier = calculateEngagementMultiplier()
        
        return GamificationResult(
            pointsEarned = calculatePoints(event, engagementMultiplier),
            experienceGained = calculateExperience(event),
            unlockedAchievements = unlockedAchievements,
            newChallenges = newChallenges,
            rewards = rewards,
            socialProofUpdate = _socialProof.value,
            engagementMultiplier = engagementMultiplier,
            nextRecommendation = generateNextRecommendation(event)
        )
    }

    /**
     * Apply Tom Sawyer's fence-painting strategy
     */
    fun applyFencePaintingStrategy(task: LearningTask): FencePaintingResult {
        // Make the task appear exclusive and desirable
        val exclusivityFrame = createExclusivityFrame(task)
        
        // Add artificial scarcity
        val scarcityElement = addScarcityElement(task)
        
        // Create social proof
        val socialProofElement = createSocialProofElement(task)
        
        // Add progression visibility
        val progressionElement = createProgressionElement(task)
        
        return FencePaintingResult(
            reframedTask = task.copy(
                description = exclusivityFrame.description,
                appealFactors = listOf(
                    exclusivityFrame,
                    scarcityElement,
                    socialProofElement,
                    progressionElement
                )
            ),
            estimatedEngagementBoost = calculateEngagementBoost(task),
            psychologicalHooks = listOf(
                "Only top students get to try this challenge",
                "Limited time: Special bonus points available",
                "Join 87% of successful students who mastered this",
                "You're just 3 steps away from the next level"
            )
        )
    }

    /**
     * Generate personalized challenges based on user profile
     */
    private fun generateDynamicChallenges(event: LearningEvent): List<DynamicChallenge> {
        val profile = _userProfile.value
        val challenges = mutableListOf<DynamicChallenge>()
        
        // Time-based challenge
        if (profile.preferredLearningTime.contains(getCurrentHour())) {
            challenges.add(
                DynamicChallenge(
                    id = UUID.randomUUID().toString(),
                    type = ChallengeType.TIME_BASED,
                    title = "Prime Time Scholar",
                    description = "Complete 5 questions during your peak hours",
                    requirements = mapOf("questions" to 5, "timeWindow" to 2),
                    rewards = ChallengeRewards(
                        points = 50,
                        experienceMultiplier = 1.5f,
                        exclusiveBadge = "prime_time_scholar"
                    ),
                    expiresAt = Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000),
                    difficulty = adaptiveDifficulty(profile)
                )
            )
        }
        
        // Streak challenge
        if (event.currentStreak > 0) {
            challenges.add(
                DynamicChallenge(
                    id = UUID.randomUUID().toString(),
                    type = ChallengeType.STREAK,
                    title = "Unstoppable Force",
                    description = "Extend your streak to ${event.currentStreak + 3}",
                    requirements = mapOf("targetStreak" to event.currentStreak + 3),
                    rewards = ChallengeRewards(
                        points = 30 * (event.currentStreak + 3),
                        experienceMultiplier = 2.0f,
                        exclusiveBadge = "streak_master_${event.currentStreak + 3}"
                    ),
                    difficulty = adaptiveDifficulty(profile)
                )
            )
        }
        
        // Skill mastery challenge
        challenges.add(
            DynamicChallenge(
                id = UUID.randomUUID().toString(),
                type = ChallengeType.MASTERY,
                title = "Skill Perfectionist",
                description = "Achieve 90% accuracy on ${event.skillName}",
                requirements = mapOf("accuracy" to 90, "skillId" to event.skillId),
                rewards = ChallengeRewards(
                    points = 100,
                    experienceMultiplier = 1.8f,
                    exclusiveBadge = "master_${event.skillId}",
                    unlockableContent = "Advanced ${event.skillName} techniques"
                ),
                difficulty = adaptiveDifficulty(profile)
            )
        )
        
        return challenges
    }

    /**
     * Check and unlock achievements
     */
    private fun checkAchievements(event: LearningEvent): List<Achievement> {
        val unlocked = mutableListOf<Achievement>()
        
        // First correct answer
        if (event.isFirstCorrect) {
            unlocked.add(
                Achievement(
                    id = "first_steps",
                    name = "First Steps",
                    description = "Your journey begins!",
                    iconUrl = "achievement_first_steps",
                    rarity = AchievementRarity.COMMON,
                    unlockedAt = Date()
                )
            )
        }
        
        // Streak achievements
        when (event.currentStreak) {
            5 -> unlocked.add(createStreakAchievement(5, AchievementRarity.COMMON))
            10 -> unlocked.add(createStreakAchievement(10, AchievementRarity.RARE))
            25 -> unlocked.add(createStreakAchievement(25, AchievementRarity.EPIC))
            50 -> unlocked.add(createStreakAchievement(50, AchievementRarity.LEGENDARY))
            100 -> unlocked.add(createStreakAchievement(100, AchievementRarity.MYTHIC))
        }
        
        // Speed achievements
        if (event.responseTimeMs < 30000 && event.isCorrect) {
            unlocked.add(
                Achievement(
                    id = "speed_demon",
                    name = "Speed Demon",
                    description = "Answered correctly in under 30 seconds!",
                    iconUrl = "achievement_speed",
                    rarity = AchievementRarity.RARE,
                    unlockedAt = Date()
                )
            )
        }
        
        // Skill mastery achievements
        if (event.skillMasteryLevel >= 0.8f) {
            unlocked.add(
                Achievement(
                    id = "skill_master_${event.skillId}",
                    name = "${event.skillName} Master",
                    description = "Achieved 80% mastery in ${event.skillName}",
                    iconUrl = "achievement_mastery",
                    rarity = AchievementRarity.EPIC,
                    unlockedAt = Date(),
                    metadata = mapOf("skillId" to event.skillId, "mastery" to event.skillMasteryLevel)
                )
            )
        }
        
        _achievements.value = _achievements.value + unlocked
        return unlocked
    }

    /**
     * Apply psychological patterns for engagement
     */
    private fun applyPsychologicalPatterns(event: LearningEvent): List<Reward> {
        val rewards = mutableListOf<Reward>()
        
        // Scarcity pattern
        if (Random.nextFloat() < 0.2f) { // 20% chance
            rewards.add(
                Reward(
                    type = RewardType.LIMITED_TIME,
                    name = "Lightning Bonus",
                    description = "2X points for the next 10 minutes!",
                    value = 2.0f,
                    expiresAt = Date(System.currentTimeMillis() + 10 * 60 * 1000)
                )
            )
        }
        
        // Social proof pattern
        val percentile = calculatePercentile(event)
        if (percentile > 80) {
            rewards.add(
                Reward(
                    type = RewardType.SOCIAL_RECOGNITION,
                    name = "Top 20% Club",
                    description = "You're performing better than 80% of students!",
                    value = 1.5f
                )
            )
        }
        
        // Surprise and delight
        if (event.questionsCompleted % 7 == 0) { // Every 7th question
            rewards.add(
                Reward(
                    type = RewardType.SURPRISE,
                    name = "Lucky 7!",
                    description = "Bonus reward for your dedication!",
                    value = Random.nextInt(50, 150).toFloat()
                )
            )
        }
        
        // Autonomy reward
        if (event.choseOwnPath) {
            rewards.add(
                Reward(
                    type = RewardType.AUTONOMY,
                    name = "Trailblazer",
                    description = "Extra points for choosing your own learning path!",
                    value = 1.3f
                )
            )
        }
        
        return rewards
    }

    /**
     * Update social proof elements
     */
    private fun updateSocialProof(event: LearningEvent) {
        _socialProof.value = _socialProof.value.copy(
            activeLearnersNow = Random.nextInt(150, 300),
            recentAchievements = generateRecentAchievements(),
            leaderboardPosition = calculateLeaderboardPosition(event),
            peerComparisons = generatePeerComparisons(event),
            trendingSkills = identifyTrendingSkills()
        )
    }

    /**
     * Calculate dynamic engagement multiplier
     */
    private fun calculateEngagementMultiplier(): Float {
        val profile = _userProfile.value
        var multiplier = 1.0f
        
        // Time-based multiplier
        if (isOptimalLearningTime(profile)) {
            multiplier *= 1.2f
        }
        
        // Streak multiplier
        val streakBonus = 1.0f + (profile.currentStreak * 0.05f).coerceAtMost(2.0f)
        multiplier *= streakBonus
        
        // Challenge participation
        if (_currentChallenges.value.isNotEmpty()) {
            multiplier *= 1.15f
        }
        
        // Social engagement
        if (_socialProof.value.friendsActive > 0) {
            multiplier *= 1.1f
        }
        
        return multiplier
    }

    /**
     * Update user's motivation profile
     */
    private fun updateMotivationProfile(event: LearningEvent) {
        val profile = _userProfile.value
        
        // Update primary motivators based on behavior
        val updatedMotivators = profile.primaryMotivators.toMutableMap()
        
        if (event.choseOwnPath) {
            updatedMotivators[Motivator.AUTONOMY] = 
                (updatedMotivators[Motivator.AUTONOMY] ?: 0.5f) * 1.1f
        }
        
        if (event.isCorrect) {
            updatedMotivators[Motivator.MASTERY] = 
                (updatedMotivators[Motivator.MASTERY] ?: 0.5f) * 1.05f
        }
        
        if (event.sharedProgress) {
            updatedMotivators[Motivator.SOCIAL] = 
                (updatedMotivators[Motivator.SOCIAL] ?: 0.5f) * 1.15f
        }
        
        // Update profile
        _userProfile.value = profile.copy(
            primaryMotivators = updatedMotivators.mapValues { it.value.coerceIn(0f, 1f) },
            currentStreak = if (event.isCorrect) profile.currentStreak + 1 else 0,
            totalPoints = profile.totalPoints + event.pointsEarned,
            level = calculateLevel(profile.totalPoints + event.pointsEarned),
            lastActiveTime = Date(),
            preferredChallengeTypes = updatePreferredChallenges(profile, event)
        )
    }

    // Helper methods
    private fun createExclusivityFrame(task: LearningTask): AppealFactor {
        return AppealFactor(
            type = "exclusivity",
            description = "Special opportunity: ${task.name} (Usually reserved for advanced students)",
            appealScore = 0.85f
        )
    }

    private fun addScarcityElement(task: LearningTask): AppealFactor {
        val timeLimit = Random.nextInt(10, 30)
        return AppealFactor(
            type = "scarcity",
            description = "Limited time: $timeLimit minutes remaining for bonus rewards!",
            appealScore = 0.75f
        )
    }

    private fun createSocialProofElement(task: LearningTask): AppealFactor {
        val percentage = Random.nextInt(75, 95)
        return AppealFactor(
            type = "social_proof",
            description = "$percentage% of top students completed this challenge",
            appealScore = 0.80f
        )
    }

    private fun createProgressionElement(task: LearningTask): AppealFactor {
        return AppealFactor(
            type = "progression",
            description = "Complete this to unlock: '${task.nextUnlock}'",
            appealScore = 0.70f
        )
    }

    private fun calculateEngagementBoost(task: LearningTask): Float {
        return task.appealFactors.map { it.appealScore }.average().toFloat() * 2.0f
    }

    private fun getCurrentHour(): Int {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    private fun adaptiveDifficulty(profile: MotivationProfile): ChallengeDifficulty {
        return when {
            profile.level < 5 -> ChallengeDifficulty.EASY
            profile.level < 10 -> ChallengeDifficulty.MEDIUM
            profile.level < 20 -> ChallengeDifficulty.HARD
            else -> ChallengeDifficulty.EXPERT
        }
    }

    private fun createStreakAchievement(streak: Int, rarity: AchievementRarity): Achievement {
        return Achievement(
            id = "streak_$streak",
            name = "Unstoppable $streak",
            description = "Achieved a $streak day learning streak!",
            iconUrl = "achievement_streak_$streak",
            rarity = rarity,
            unlockedAt = Date()
        )
    }

    private fun calculatePercentile(event: LearningEvent): Int {
        // Simplified calculation - in production, this would query actual data
        return when {
            event.accuracy > 0.9f -> 95
            event.accuracy > 0.8f -> 85
            event.accuracy > 0.7f -> 70
            event.accuracy > 0.6f -> 50
            else -> 30
        }
    }

    private fun generateRecentAchievements(): List<String> {
        return listOf(
            "Sarah just earned 'Speed Reader' badge!",
            "Mike completed a 10-day streak!",
            "Emma mastered Character Analysis!",
            "James joined the Top 10% Club!"
        ).shuffled().take(3)
    }

    private fun calculateLeaderboardPosition(event: LearningEvent): Int {
        // Simplified - would query actual rankings
        return Random.nextInt(1, 100)
    }

    private fun generatePeerComparisons(event: LearningEvent): Map<String, Float> {
        return mapOf(
            "accuracy" to event.accuracy / 0.75f, // Compared to average
            "speed" to 45000f / event.responseTimeMs.toFloat(), // Compared to average time
            "consistency" to event.currentStreak / 3f // Compared to average streak
        )
    }

    private fun identifyTrendingSkills(): List<String> {
        return listOf(
            "Theme Identification",
            "Character Analysis",
            "Main Idea",
            "Vocabulary in Context",
            "Making Inferences"
        ).shuffled().take(3)
    }

    private fun isOptimalLearningTime(profile: MotivationProfile): Boolean {
        val currentHour = getCurrentHour()
        return profile.preferredLearningTime.contains(currentHour)
    }

    private fun calculateLevel(totalPoints: Int): Int {
        // Exponential leveling curve
        return (totalPoints.toFloat().pow(0.5f) / 10).toInt() + 1
    }

    private fun updatePreferredChallenges(
        profile: MotivationProfile,
        event: LearningEvent
    ): Set<ChallengeType> {
        val preferred = profile.preferredChallengeTypes.toMutableSet()
        
        if (event.completedChallenge != null) {
            preferred.add(event.completedChallenge.type)
        }
        
        return preferred
    }

    private fun calculatePoints(event: LearningEvent, multiplier: Float): Int {
        val basePoints = when {
            event.isCorrect && !event.usedHint -> 10
            event.isCorrect && event.usedHint -> 5
            else -> 2
        }
        
        val difficultyMultiplier = event.difficulty
        val speedBonus = if (event.responseTimeMs < 30000) 2 else 0
        
        return ((basePoints * difficultyMultiplier + speedBonus) * multiplier).toInt()
    }

    private fun calculateExperience(event: LearningEvent): Int {
        return (event.difficulty * 100).toInt()
    }

    private fun generateNextRecommendation(event: LearningEvent): LearningRecommendation {
        val profile = _userProfile.value
        
        return LearningRecommendation(
            skillId = suggestNextSkill(event, profile),
            reason = "Based on your excellent performance in ${event.skillName}",
            estimatedTime = 10,
            potentialPoints = 150,
            unlockables = listOf("Advanced ${event.skillName} Badge", "New Challenge Type")
        )
    }

    private fun suggestNextSkill(event: LearningEvent, profile: MotivationProfile): String {
        // Simplified - would use actual skill progression logic
        return "next_skill_${event.skillId}"
    }
}

// Data classes
data class MotivationProfile(
    val userId: String = "",
    val primaryMotivators: Map<Motivator, Float> = mapOf(
        Motivator.AUTONOMY to 0.5f,
        Motivator.MASTERY to 0.5f,
        Motivator.PURPOSE to 0.5f,
        Motivator.SOCIAL to 0.5f,
        Motivator.ACHIEVEMENT to 0.5f
    ),
    val learningStyle: LearningStyle = LearningStyle.BALANCED,
    val preferredChallengeTypes: Set<ChallengeType> = emptySet(),
    val currentStreak: Int = 0,
    val totalPoints: Int = 0,
    val level: Int = 1,
    val preferredLearningTime: Set<Int> = setOf(15, 16, 17, 18, 19), // 3-7 PM
    val lastActiveTime: Date = Date()
)

data class LearningEvent(
    val userId: String,
    val skillId: String,
    val skillName: String,
    val isCorrect: Boolean,
    val accuracy: Float,
    val difficulty: Float,
    val responseTimeMs: Long,
    val usedHint: Boolean,
    val currentStreak: Int,
    val questionsCompleted: Int,
    val pointsEarned: Int,
    val isFirstCorrect: Boolean = false,
    val choseOwnPath: Boolean = false,
    val sharedProgress: Boolean = false,
    val completedChallenge: DynamicChallenge? = null,
    val skillMasteryLevel: Float = 0f
)

data class GamificationResult(
    val pointsEarned: Int,
    val experienceGained: Int,
    val unlockedAchievements: List<Achievement>,
    val newChallenges: List<DynamicChallenge>,
    val rewards: List<Reward>,
    val socialProofUpdate: SocialProofData,
    val engagementMultiplier: Float,
    val nextRecommendation: LearningRecommendation
)

data class DynamicChallenge(
    val id: String,
    val type: ChallengeType,
    val title: String,
    val description: String,
    val requirements: Map<String, Any>,
    val rewards: ChallengeRewards,
    val difficulty: ChallengeDifficulty = ChallengeDifficulty.MEDIUM,
    val expiresAt: Date? = null,
    val progress: Float = 0f
)

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val rarity: AchievementRarity,
    val unlockedAt: Date,
    val metadata: Map<String, Any> = emptyMap()
)

data class Reward(
    val type: RewardType,
    val name: String,
    val description: String,
    val value: Float,
    val expiresAt: Date? = null
)

data class SocialProofData(
    val activeLearnersNow: Int = 0,
    val recentAchievements: List<String> = emptyList(),
    val leaderboardPosition: Int = 0,
    val peerComparisons: Map<String, Float> = emptyMap(),
    val friendsActive: Int = 0,
    val trendingSkills: List<String> = emptyList()
)

data class LearningTask(
    val id: String,
    val name: String,
    val description: String,
    val skillId: String,
    val difficulty: Float,
    val estimatedTime: Int,
    val appealFactors: List<AppealFactor> = emptyList(),
    val nextUnlock: String = ""
)

data class FencePaintingResult(
    val reframedTask: LearningTask,
    val estimatedEngagementBoost: Float,
    val psychologicalHooks: List<String>
)

data class AppealFactor(
    val type: String,
    val description: String,
    val appealScore: Float
)

data class ChallengeRewards(
    val points: Int,
    val experienceMultiplier: Float,
    val exclusiveBadge: String? = null,
    val unlockableContent: String? = null
)

data class LearningRecommendation(
    val skillId: String,
    val reason: String,
    val estimatedTime: Int,
    val potentialPoints: Int,
    val unlockables: List<String>
)

// Enums
enum class Motivator {
    AUTONOMY, MASTERY, PURPOSE, SOCIAL, ACHIEVEMENT
}

enum class LearningStyle {
    VISUAL, AUDITORY, KINESTHETIC, READING_WRITING, BALANCED
}

enum class ChallengeType {
    TIME_BASED, STREAK, MASTERY, SPEED, ACCURACY, EXPLORATION, SOCIAL, CREATIVE
}

enum class ChallengeDifficulty {
    EASY, MEDIUM, HARD, EXPERT
}

enum class AchievementRarity {
    COMMON, RARE, EPIC, LEGENDARY, MYTHIC
}

enum class RewardType {
    POINTS, EXPERIENCE, BADGE, UNLOCK, LIMITED_TIME, SOCIAL_RECOGNITION, SURPRISE, AUTONOMY
}

enum class PsychologicalPattern {
    SCARCITY, SOCIAL_PROOF, AUTONOMY, MASTERY, PURPOSE, EXCLUSIVITY, PROGRESSION, SURPRISE_DELIGHT
}