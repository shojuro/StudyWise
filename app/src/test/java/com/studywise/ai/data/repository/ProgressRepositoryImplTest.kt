package com.studywise.ai.data.repository

import com.google.common.truth.Truth.assertThat
import com.studywise.ai.data.local.dao.LearningSessionDao
import com.studywise.ai.data.local.dao.ProgressDao
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.entity.LearningSessionEntity
import com.studywise.ai.data.local.entity.ProgressEntity
import com.studywise.ai.data.local.entity.SessionStatus
import com.studywise.ai.domain.model.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.*

class ProgressRepositoryImplTest {
    
    private lateinit var progressDao: ProgressDao
    private lateinit var sessionDao: LearningSessionDao
    private lateinit var userDao: UserDao
    private lateinit var progressRepository: ProgressRepositoryImpl
    
    @Before
    fun setup() {
        progressDao = mockk()
        sessionDao = mockk()
        userDao = mockk()
        progressRepository = ProgressRepositoryImpl(progressDao, sessionDao, userDao)
    }
    
    @Test
    fun `getWeeklyProgress should return correct weekly data`() = runTest {
        // Given
        val userId = "user123"
        val sessions = listOf(
            createSession(userId, Date(), 30, 10, 8),
            createSession(userId, Date(System.currentTimeMillis() - 86400000), 45, 15, 12),
            createSession(userId, Date(System.currentTimeMillis() - 2 * 86400000), 60, 20, 18)
        )
        
        coEvery { sessionDao.getSessionsBetweenDates(userId, any(), any()) } returns sessions
        
        // When
        val result = progressRepository.getWeeklyProgress(userId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val weeklyProgress = result.getOrNull()
        assertThat(weeklyProgress).isNotNull()
        assertThat(weeklyProgress?.totalQuestions).isEqualTo(45)
        assertThat(weeklyProgress?.totalCorrect).isEqualTo(38)
        assertThat(weeklyProgress?.totalTimeMinutes).isEqualTo(135)
        assertThat(weeklyProgress?.dailyProgress).hasSize(7)
    }
    
    @Test
    fun `getSubjectProgress should calculate correct subject statistics`() = runTest {
        // Given
        val userId = "user123"
        val subject = "Mathematics"
        val sessions = listOf(
            createSession(userId, Date(), 30, 10, 8, subject),
            createSession(userId, Date(System.currentTimeMillis() - 86400000), 45, 15, 12, subject),
            createSession(userId, Date(System.currentTimeMillis() - 2 * 86400000), 60, 20, 15, subject)
        )
        val progressList = listOf(
            createProgress(userId, "skill1", 0.7f, 30, 25),
            createProgress(userId, "skill2", 0.8f, 40, 35)
        )
        
        coEvery { sessionDao.getSessionsBySubject(userId, subject) } returns sessions
        coEvery { progressDao.getProgressBySubject(userId, subject) } returns progressList
        
        // When
        val result = progressRepository.getSubjectProgress(userId, subject)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val subjectProgress = result.getOrNull()
        assertThat(subjectProgress).isNotNull()
        assertThat(subjectProgress?.subject).isEqualTo(subject)
        assertThat(subjectProgress?.totalSessions).isEqualTo(3)
        assertThat(subjectProgress?.totalQuestions).isEqualTo(45)
        assertThat(subjectProgress?.correctAnswers).isEqualTo(35)
        assertThat(subjectProgress?.totalTimeMinutes).isEqualTo(135)
    }
    
    @Test
    fun `getDailyStreak should calculate correct streak`() = runTest {
        // Given
        val userId = "user123"
        val today = Date()
        val yesterday = Date(System.currentTimeMillis() - 86400000)
        val twoDaysAgo = Date(System.currentTimeMillis() - 2 * 86400000)
        
        val sessions = listOf(
            createSession(userId, today, 30, 10, 8),
            createSession(userId, yesterday, 45, 15, 12),
            createSession(userId, twoDaysAgo, 60, 20, 15)
        )
        
        coEvery { sessionDao.getAllSessions(userId) } returns sessions
        
        // When
        val result = progressRepository.getDailyStreak(userId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(3)
    }
    
    @Test
    fun `getDailyStreak with gap should return correct streak`() = runTest {
        // Given
        val userId = "user123"
        val today = Date()
        val yesterday = Date(System.currentTimeMillis() - 86400000)
        val fourDaysAgo = Date(System.currentTimeMillis() - 4 * 86400000)
        
        val sessions = listOf(
            createSession(userId, today, 30, 10, 8),
            createSession(userId, yesterday, 45, 15, 12),
            createSession(userId, fourDaysAgo, 60, 20, 15) // Gap in streak
        )
        
        coEvery { sessionDao.getAllSessions(userId) } returns sessions
        
        // When
        val result = progressRepository.getDailyStreak(userId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(2) // Only counts consecutive days
    }
    
    @Test
    fun `getAchievements should return all achievement types`() = runTest {
        // Given
        val userId = "user123"
        val sessions = listOf(
            createSession(userId, Date(), 30, 10, 10), // Perfect score
            createSession(userId, Date(System.currentTimeMillis() - 86400000), 45, 15, 12),
            createSession(userId, Date(System.currentTimeMillis() - 2 * 86400000), 60, 20, 15)
        )
        
        coEvery { sessionDao.getAllSessions(userId) } returns sessions
        coEvery { sessionDao.getCompletedSessionCount(userId) } returns 10
        coEvery { sessionDao.getTotalPointsEarned(userId) } returns 500
        
        // When
        val result = progressRepository.getAchievements(userId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val achievements = result.getOrNull()
        assertThat(achievements).isNotNull()
        assertThat(achievements?.size).isGreaterThan(0)
        
        // Should have achievements for different categories
        val categories = achievements?.map { it.category }?.toSet()
        assertThat(categories).contains(AchievementCategory.STREAK)
        assertThat(categories).contains(AchievementCategory.ACCURACY)
        assertThat(categories).contains(AchievementCategory.COMPLETION)
    }
    
    @Test
    fun `getTotalPoints should return sum of points`() = runTest {
        // Given
        val userId = "user123"
        val totalPoints = 1500
        
        coEvery { sessionDao.getTotalPointsEarned(userId) } returns totalPoints
        
        // When
        val result = progressRepository.getTotalPoints(userId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(totalPoints)
    }
    
    @Test
    fun `getProgressTrend should identify improving trend`() = runTest {
        // Given
        val userId = "user123"
        val recentSessions = listOf(
            createSession(userId, Date(), 10, 10, 9), // 90% accuracy
            createSession(userId, Date(System.currentTimeMillis() - 86400000), 10, 10, 8), // 80% accuracy
            createSession(userId, Date(System.currentTimeMillis() - 2 * 86400000), 10, 10, 7), // 70% accuracy
            createSession(userId, Date(System.currentTimeMillis() - 3 * 86400000), 10, 10, 6), // 60% accuracy
        )
        
        coEvery { sessionDao.getSessionsAfterDate(userId, any()) } returns recentSessions
        
        // When
        val result = progressRepository.getProgressTrend(userId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(ProgressTrend.IMPROVING)
    }
    
    @Test
    fun `error from dao should propagate as failure`() = runTest {
        // Given
        val userId = "user123"
        val exception = RuntimeException("Database error")
        
        coEvery { sessionDao.getSessionsBetweenDates(userId, any(), any()) } throws exception
        
        // When
        val result = progressRepository.getWeeklyProgress(userId)
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
    
    // Helper functions
    private fun createSession(
        userId: String,
        startedAt: Date,
        durationMinutes: Int,
        questionsAnswered: Int,
        correctAnswers: Int,
        subject: String = "Mathematics"
    ): LearningSessionEntity {
        return LearningSessionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            subject = subject,
            startedAt = startedAt,
            completedAt = Date(startedAt.time + durationMinutes * 60 * 1000),
            durationMinutes = durationMinutes,
            questionsAnswered = questionsAnswered,
            correctAnswers = correctAnswers,
            pointsEarned = correctAnswers * 10,
            status = SessionStatus.COMPLETED
        )
    }
    
    private fun createProgress(
        userId: String,
        skillId: String,
        masteryLevel: Float,
        questionsAttempted: Int,
        questionsCorrect: Int
    ): ProgressEntity {
        return ProgressEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            skillId = skillId,
            gradeLevel = 5,
            masteryLevel = masteryLevel,
            questionsAttempted = questionsAttempted,
            questionsCorrect = questionsCorrect,
            lastPracticedAt = Date(),
            streakDays = 1,
            totalPointsEarned = questionsCorrect * 10,
            subject = "Mathematics"
        )
    }
}