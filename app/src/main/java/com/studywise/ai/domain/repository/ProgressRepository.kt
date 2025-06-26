package com.studywise.ai.domain.repository

import com.studywise.ai.data.local.entity.LearningSessionEntity
import com.studywise.ai.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface ProgressRepository {
    suspend fun getWeeklyProgress(userId: String, weekOffset: Int = 0): Result<WeeklyProgress>
    suspend fun getSubjectProgress(userId: String, subject: String): Result<SubjectProgress>
    suspend fun getAllSubjectsProgress(userId: String): Result<List<SubjectProgress>>
    suspend fun getSkillProgress(userId: String, skillId: String): Result<SkillProgress>
    suspend fun getAchievements(userId: String): Result<List<Achievement>>
    suspend fun getUnlockedAchievements(userId: String): Result<List<Achievement>>
    suspend fun getDailyStreak(userId: String): Result<Int>
    suspend fun getTotalPoints(userId: String): Result<Int>
    suspend fun getProgressTrend(userId: String, daysBack: Int = 7): Result<ProgressTrend>
    suspend fun getDailyProgress(userId: String, date: Date): Result<DailyProgress>
    suspend fun getRecentSessions(userId: String, limit: Int): Result<List<LearningSessionEntity>>
    suspend fun getRecentAchievements(userId: String, limit: Int): Result<List<Achievement>>
    
    fun observeWeeklyProgress(userId: String): Flow<WeeklyProgress>
    fun observeAchievements(userId: String): Flow<List<Achievement>>
    fun observeStreak(userId: String): Flow<Int>
    
    suspend fun recordProgress(progressData: ProgressData): Result<Unit>
    suspend fun unlockAchievement(userId: String, achievementId: String): Result<Unit>
}