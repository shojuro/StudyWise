package com.studywise.ai.data.repository

import com.studywise.ai.data.local.dao.LearningSessionDao
import com.studywise.ai.data.local.dao.ProgressDao
import com.studywise.ai.data.local.dao.SkillDao
import com.studywise.ai.data.local.dao.StudentSkillMasteryDao
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.entity.SessionStatus
import com.studywise.ai.data.local.entity.StudentSkillMasteryEntity
import com.studywise.ai.domain.model.*
import com.studywise.ai.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao,
    private val sessionDao: LearningSessionDao,
    private val userDao: UserDao,
    private val skillDao: SkillDao,
    private val studentSkillMasteryDao: StudentSkillMasteryDao
) : ProgressRepository {

    override suspend fun getWeeklyProgress(userId: String, weekOffset: Int): Result<WeeklyProgress> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_YEAR, -weekOffset)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val weekStart = calendar.time
            
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val weekEnd = calendar.time
            
            val sessions = sessionDao.getSessionsBetweenDates(userId, weekStart, weekEnd)
            val dailyProgressList = mutableListOf<DailyProgress>()
            
            // Group sessions by day
            val sessionsByDay = sessions.groupBy { session ->
                Calendar.getInstance().apply {
                    time = session.startedAt
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time
            }
            
            // Create daily progress for each day
            for (day in 0..6) {
                calendar.time = weekStart
                calendar.add(Calendar.DAY_OF_YEAR, day)
                val dayDate = calendar.time
                
                val daySessions = sessionsByDay[dayDate] ?: emptyList()
                val questionsAnswered = daySessions.sumOf { it.questionsAnswered }
                val correctAnswers = daySessions.sumOf { it.correctAnswers }
                val timeSpent = daySessions.sumOf { 
                    if (it.completedAt != null) {
                        TimeUnit.MILLISECONDS.toMinutes(it.completedAt.time - it.startedAt.time).toInt()
                    } else 0
                }
                
                dailyProgressList.add(
                    DailyProgress(
                        date = dayDate,
                        questionsAnswered = questionsAnswered,
                        correctAnswers = correctAnswers,
                        timeSpentMinutes = timeSpent,
                        subjects = daySessions.map { it.subject }.distinct(),
                        accuracy = if (questionsAnswered > 0) correctAnswers.toFloat() / questionsAnswered else 0f,
                        sessionsCompleted = daySessions.filter { it.status == SessionStatus.COMPLETED }.size,
                        minutesStudied = timeSpent,
                        pointsEarned = daySessions.sumOf { it.pointsEarned }
                    )
                )
            }
            
            val totalQuestions = dailyProgressList.sumOf { it.questionsAnswered }
            val totalCorrect = dailyProgressList.sumOf { it.correctAnswers }
            
            Result.success(
                WeeklyProgress(
                    weekStartDate = weekStart,
                    dailyProgress = dailyProgressList,
                    totalQuestions = totalQuestions,
                    totalCorrect = totalCorrect,
                    averageAccuracy = if (totalQuestions > 0) totalCorrect.toFloat() / totalQuestions else 0f,
                    totalTimeMinutes = dailyProgressList.sumOf { it.timeSpentMinutes },
                    subjectsStudied = dailyProgressList.flatMap { it.subjects }.toSet(),
                    dailyStats = dailyProgressList,
                    totalMinutes = dailyProgressList.sumOf { it.timeSpentMinutes }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSubjectProgress(userId: String, subject: String): Result<SubjectProgress> {
        return try {
            val sessions = sessionDao.getSessionsBySubject(userId, subject)
            val progress = progressDao.getProgressBySubject(userId, subject)
            
            val totalQuestions = sessions.sumOf { it.questionsAnswered }
            val correctAnswers = sessions.sumOf { it.correctAnswers }
            val totalTime = sessions.sumOf { session ->
                if (session.completedAt != null) {
                    TimeUnit.MILLISECONDS.toMinutes(session.completedAt.time - session.startedAt.time).toInt()
                } else 0
            }
            
            val skillMastery = progress.groupBy { it.skillId }
                .mapValues { (_, progressList) ->
                    progressList.map { it.masteryLevel }.average().toFloat()
                }
            
            // Calculate trend based on recent sessions
            val recentSessions = sessions.sortedByDescending { it.startedAt }.take(5)
            val trend = calculateTrend(recentSessions)
            
            Result.success(
                SubjectProgress(
                    subject = subject,
                    totalSessions = sessions.size,
                    totalQuestions = totalQuestions,
                    correctAnswers = correctAnswers,
                    averageAccuracy = if (totalQuestions > 0) correctAnswers.toFloat() / totalQuestions else 0f,
                    totalTimeMinutes = totalTime,
                    lastPracticed = sessions.maxByOrNull { it.startedAt }?.startedAt,
                    skillMastery = skillMastery,
                    trend = trend
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllSubjectsProgress(userId: String): Result<List<SubjectProgress>> {
        return try {
            val subjects = sessionDao.getAllSubjects(userId)
            val progressList = subjects.mapNotNull { subject ->
                getSubjectProgress(userId, subject).getOrNull()
            }
            Result.success(progressList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSkillProgress(userId: String, skillId: String): Result<SkillProgress> {
        return try {
            val progressList = progressDao.getProgressBySkill(userId, skillId)
            
            if (progressList.isEmpty()) {
                return Result.success(
                    SkillProgress(
                        skillId = skillId,
                        skillName = skillId.replace("_", " ").capitalize(),
                        currentLevel = 0,
                        progress = 0f,
                        questionsAnswered = 0,
                        correctAnswers = 0,
                        lastPracticed = null,
                        milestones = createDefaultMilestones()
                    )
                )
            }
            
            val latestProgress = progressList.maxByOrNull { it.updatedAt }!!
            val totalQuestions = progressList.sumOf { it.questionsAnswered }
            val totalCorrect = progressList.sumOf { it.correctAnswers }
            
            val milestones = createMilestones(latestProgress.masteryLevel, progressList)
            
            Result.success(
                SkillProgress(
                    skillId = skillId,
                    skillName = skillDao.getSkillById(skillId.toLongOrNull() ?: 0L)?.name ?: "Unknown Skill",
                    currentLevel = (latestProgress.masteryLevel * 10).roundToInt(),
                    progress = latestProgress.masteryLevel,
                    questionsAnswered = totalQuestions,
                    correctAnswers = totalCorrect,
                    lastPracticed = latestProgress.updatedAt,
                    milestones = milestones
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAchievements(userId: String): Result<List<Achievement>> {
        return try {
            val achievements = createAllAchievements()
            val unlockedIds = getUnlockedAchievementIds(userId)
            val userStats = getUserStats(userId)
            
            val achievementsWithProgress = achievements.map { achievement ->
                val progress = calculateAchievementProgress(achievement, userStats)
                achievement.copy(
                    progress = progress,
                    isUnlocked = unlockedIds.contains(achievement.id),
                    unlockedDate = if (unlockedIds.contains(achievement.id)) Date() else null
                )
            }
            
            Result.success(achievementsWithProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnlockedAchievements(userId: String): Result<List<Achievement>> {
        return try {
            val allAchievements = getAchievements(userId).getOrThrow()
            Result.success(allAchievements.filter { it.isUnlocked })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailyStreak(userId: String): Result<Int> {
        return try {
            val sessions = sessionDao.getAllSessions(userId)
                .filter { it.status == SessionStatus.COMPLETED }
                .sortedByDescending { it.startedAt }
            
            if (sessions.isEmpty()) return Result.success(0)
            
            var streak = 0
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            
            val today = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            var checkDate = calendar.time
            
            // Check if there's a session today
            val hasSessionToday = sessions.any { session ->
                val sessionDate = Calendar.getInstance().apply {
                    time = session.startedAt
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time
                sessionDate == today
            }
            
            if (hasSessionToday) streak = 1
            
            // Count consecutive days
            for (i in 1..365) {
                val hasSession = sessions.any { session ->
                    val sessionDate = Calendar.getInstance().apply {
                        time = session.startedAt
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.time
                    sessionDate == checkDate
                }
                
                if (hasSession) {
                    streak++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    checkDate = calendar.time
                } else {
                    break
                }
            }
            
            Result.success(streak)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTotalPoints(userId: String): Result<Int> {
        return try {
            val sessions = sessionDao.getAllSessions(userId)
            val points = sessions.sumOf { it.pointsEarned }
            Result.success(points)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProgressTrend(userId: String, daysBack: Int): Result<ProgressTrend> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -daysBack)
            val startDate = calendar.time
            
            val sessions = sessionDao.getSessionsAfterDate(userId, startDate)
                .filter { it.status == SessionStatus.COMPLETED }
                .sortedBy { it.startedAt }
            
            if (sessions.size < 3) return Result.success(ProgressTrend.STABLE)
            
            // Calculate accuracy for first half and second half
            val midPoint = sessions.size / 2
            val firstHalf = sessions.take(midPoint)
            val secondHalf = sessions.drop(midPoint)
            
            val firstAccuracy = if (firstHalf.sumOf { it.questionsAnswered } > 0) {
                firstHalf.sumOf { it.correctAnswers }.toFloat() / firstHalf.sumOf { it.questionsAnswered }
            } else 0f
            
            val secondAccuracy = if (secondHalf.sumOf { it.questionsAnswered } > 0) {
                secondHalf.sumOf { it.correctAnswers }.toFloat() / secondHalf.sumOf { it.questionsAnswered }
            } else 0f
            
            val trend = when {
                secondAccuracy > firstAccuracy + 0.1f -> ProgressTrend.IMPROVING
                secondAccuracy < firstAccuracy - 0.1f -> ProgressTrend.DECLINING
                else -> ProgressTrend.STABLE
            }
            
            Result.success(trend)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeWeeklyProgress(userId: String): Flow<WeeklyProgress> = flow {
        getWeeklyProgress(userId).getOrNull()?.let { emit(it) }
    }

    override fun observeAchievements(userId: String): Flow<List<Achievement>> = flow {
        getAchievements(userId).getOrNull()?.let { emit(it) }
    }

    override fun observeStreak(userId: String): Flow<Int> = flow {
        getDailyStreak(userId).getOrNull()?.let { emit(it) }
    }

    override suspend fun recordProgress(progressData: ProgressData): Result<Unit> {
        // This would be called by learning sessions to record progress
        return Result.success(Unit)
    }

    override suspend fun unlockAchievement(userId: String, achievementId: String): Result<Unit> {
        // Store unlocked achievement in database
        return Result.success(Unit)
    }

    override suspend fun getDailyProgress(userId: String, date: Date): Result<DailyProgress> {
        return try {
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val dayStart = calendar.time
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val dayEnd = calendar.time
            
            val sessions = sessionDao.getSessionsBetweenDates(userId, dayStart, dayEnd)
            val questionsAnswered = sessions.sumOf { it.questionsAnswered }
            val correctAnswers = sessions.sumOf { it.correctAnswers }
            val minutesStudied = sessions.sumOf { session ->
                if (session.completedAt != null) {
                    TimeUnit.MILLISECONDS.toMinutes(session.completedAt.time - session.startedAt.time).toInt()
                } else 0
            }
            val pointsEarned = sessions.sumOf { it.pointsEarned }
            
            Result.success(
                DailyProgress(
                    date = date,
                    questionsAnswered = questionsAnswered,
                    correctAnswers = correctAnswers,
                    timeSpentMinutes = minutesStudied,
                    subjects = sessions.map { it.subject }.distinct(),
                    accuracy = if (questionsAnswered > 0) correctAnswers.toFloat() / questionsAnswered else 0f,
                    sessionsCompleted = sessions.filter { it.status == SessionStatus.COMPLETED }.size,
                    minutesStudied = minutesStudied,
                    pointsEarned = pointsEarned
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentSessions(userId: String, limit: Int): Result<List<com.studywise.ai.data.local.entity.LearningSessionEntity>> {
        return try {
            val sessions = sessionDao.getAllSessions(userId).take(limit)
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentAchievements(userId: String, limit: Int): Result<List<Achievement>> {
        return try {
            // For MVP, return sample recent achievements
            val achievements = getAchievements(userId).getOrNull() ?: emptyList()
            Result.success(achievements.filter { it.isUnlocked }.take(limit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper functions
    private fun calculateTrend(sessions: List<com.studywise.ai.data.local.entity.LearningSessionEntity>): ProgressTrend {
        if (sessions.size < 2) return ProgressTrend.STABLE
        
        val recentAccuracy = sessions.take(2).let { recent ->
            val questions = recent.sumOf { it.questionsAnswered }
            val correct = recent.sumOf { it.correctAnswers }
            if (questions > 0) correct.toFloat() / questions else 0f
        }
        
        val olderAccuracy = sessions.drop(2).let { older ->
            val questions = older.sumOf { it.questionsAnswered }
            val correct = older.sumOf { it.correctAnswers }
            if (questions > 0) correct.toFloat() / questions else 0f
        }
        
        return when {
            recentAccuracy > olderAccuracy + 0.1f -> ProgressTrend.IMPROVING
            recentAccuracy < olderAccuracy - 0.1f -> ProgressTrend.DECLINING
            else -> ProgressTrend.STABLE
        }
    }

    private fun createDefaultMilestones(): List<SkillMilestone> {
        return listOf(
            SkillMilestone(1, "Beginner - Answer 5 questions", false, null),
            SkillMilestone(3, "Novice - Achieve 60% accuracy", false, null),
            SkillMilestone(5, "Intermediate - Answer 25 questions", false, null),
            SkillMilestone(7, "Advanced - Achieve 80% accuracy", false, null),
            SkillMilestone(10, "Master - Perfect streak of 10", false, null)
        )
    }

    private fun createMilestones(mastery: Float, progressList: List<com.studywise.ai.data.local.entity.ProgressEntity>): List<SkillMilestone> {
        val totalQuestions = progressList.sumOf { it.questionsAnswered }
        val totalCorrect = progressList.sumOf { it.correctAnswers }
        val accuracy = if (totalQuestions > 0) totalCorrect.toFloat() / totalQuestions else 0f
        
        return listOf(
            SkillMilestone(
                1, 
                "Beginner - Answer 5 questions", 
                totalQuestions >= 5,
                if (totalQuestions >= 5) progressList.first().updatedAt else null
            ),
            SkillMilestone(
                3, 
                "Novice - Achieve 60% accuracy", 
                accuracy >= 0.6f,
                if (accuracy >= 0.6f) Date() else null
            ),
            SkillMilestone(
                5, 
                "Intermediate - Answer 25 questions", 
                totalQuestions >= 25,
                if (totalQuestions >= 25) Date() else null
            ),
            SkillMilestone(
                7, 
                "Advanced - Achieve 80% accuracy", 
                accuracy >= 0.8f,
                if (accuracy >= 0.8f) Date() else null
            ),
            SkillMilestone(
                10, 
                "Master - Perfect streak of 10", 
                false, // Would need to check consecutive correct answers
                null
            )
        )
    }

    private fun createAllAchievements(): List<Achievement> {
        return listOf(
            // Streak achievements
            Achievement(
                id = "streak_7",
                title = "Week Warrior",
                description = "Complete 7 days in a row",
                iconRes = 0, // Would be actual resource ID
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.STREAK,
                points = 100
            ),
            Achievement(
                id = "streak_30",
                title = "Monthly Master",
                description = "Complete 30 days in a row",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.STREAK,
                points = 500
            ),
            
            // Accuracy achievements
            Achievement(
                id = "accuracy_80",
                title = "Sharp Mind",
                description = "Achieve 80% accuracy in a session",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.ACCURACY,
                points = 50
            ),
            Achievement(
                id = "perfect_10",
                title = "Perfect Ten",
                description = "Answer 10 questions correctly in a row",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.ACCURACY,
                points = 150
            ),
            
            // Completion achievements
            Achievement(
                id = "questions_100",
                title = "Century Club",
                description = "Answer 100 questions total",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.COMPLETION,
                points = 200
            ),
            Achievement(
                id = "sessions_50",
                title = "Dedicated Learner",
                description = "Complete 50 learning sessions",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.COMPLETION,
                points = 300
            ),
            
            // Mastery achievements
            Achievement(
                id = "skill_master",
                title = "Skill Master",
                description = "Master any skill to level 10",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.MASTERY,
                points = 400
            ),
            Achievement(
                id = "subject_expert",
                title = "Subject Expert",
                description = "Achieve 90% mastery in a subject",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.MASTERY,
                points = 500
            ),
            
            // Exploration achievements
            Achievement(
                id = "explorer_5",
                title = "Explorer",
                description = "Try 5 different subjects",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.EXPLORATION,
                points = 100
            ),
            Achievement(
                id = "photo_learner",
                title = "Visual Learner",
                description = "Complete 10 photo learning sessions",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.EXPLORATION,
                points = 150
            ),
            
            // Speed achievements
            Achievement(
                id = "quick_thinker",
                title = "Quick Thinker",
                description = "Answer 5 questions correctly in under 2 minutes",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.SPEED,
                points = 100
            ),
            Achievement(
                id = "speed_demon",
                title = "Speed Demon",
                description = "Complete a session in under 5 minutes",
                iconRes = 0,
                progress = 0f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.SPEED,
                points = 75
            )
        )
    }

    private suspend fun getUnlockedAchievementIds(userId: String): Set<String> {
        // In a real implementation, this would query a database table
        // For now, return empty set
        return emptySet()
    }

    private suspend fun getUserStats(userId: String): UserStats {
        val sessions = sessionDao.getAllSessions(userId)
        val totalQuestions = sessions.sumOf { it.questionsAnswered }
        val totalCorrect = sessions.sumOf { it.correctAnswers }
        val streak = getDailyStreak(userId).getOrDefault(0)
        val subjects = sessions.map { it.subject }.distinct().size
        
        return UserStats(
            totalQuestions = totalQuestions,
            totalCorrect = totalCorrect,
            totalSessions = sessions.size,
            currentStreak = streak,
            subjectsExplored = subjects,
            highestAccuracy = sessions.maxOfOrNull { session ->
                if (session.questionsAnswered > 0) {
                    session.correctAnswers.toFloat() / session.questionsAnswered
                } else 0f
            } ?: 0f
        )
    }

    private fun calculateAchievementProgress(achievement: Achievement, stats: UserStats): Float {
        return when (achievement.id) {
            "streak_7" -> (stats.currentStreak / 7f).coerceIn(0f, 1f)
            "streak_30" -> (stats.currentStreak / 30f).coerceIn(0f, 1f)
            "accuracy_80" -> if (stats.highestAccuracy >= 0.8f) 1f else stats.highestAccuracy / 0.8f
            "perfect_10" -> 0f // Would need to track consecutive correct answers
            "questions_100" -> (stats.totalQuestions / 100f).coerceIn(0f, 1f)
            "sessions_50" -> (stats.totalSessions / 50f).coerceIn(0f, 1f)
            "skill_master" -> 0f // Would need to check skill levels
            "subject_expert" -> 0f // Would need to check subject mastery
            "explorer_5" -> (stats.subjectsExplored / 5f).coerceIn(0f, 1f)
            "photo_learner" -> 0f // Would need to track photo sessions
            "quick_thinker" -> 0f // Would need to track answer times
            "speed_demon" -> 0f // Would need to track session duration
            else -> 0f
        }
    }

    private data class UserStats(
        val totalQuestions: Int,
        val totalCorrect: Int,
        val totalSessions: Int,
        val currentStreak: Int,
        val subjectsExplored: Int,
        val highestAccuracy: Float
    )
    
    // Add missing helper methods for analytics
    suspend fun getCurrentStreak(studentId: String): Int {
        return getDailyStreak(studentId).getOrDefault(0)
    }
    
    fun getProgressByStudent(studentId: String): Flow<List<StudentSkillMasteryEntity>> {
        return studentSkillMasteryDao.observeStudentMasteries(studentId)
    }
}