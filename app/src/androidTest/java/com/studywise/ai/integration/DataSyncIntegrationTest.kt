package com.studywise.ai.integration

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.studywise.ai.data.local.dao.*
import com.studywise.ai.data.local.entity.*
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.data.remote.api.StudyWiseApiService
import com.studywise.ai.data.remote.dto.*
import com.studywise.ai.data.sync.DataSyncWorker
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response
import java.util.*
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DataSyncIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    private lateinit var context: Context
    private lateinit var apiService: StudyWiseApiService
    private lateinit var userDao: UserDao
    private lateinit var progressDao: ProgressDao
    private lateinit var sessionDao: LearningSessionDao
    private lateinit var questionDao: QuestionDao
    private lateinit var preferencesManager: PreferencesManager
    
    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        
        // Initialize WorkManager for testing
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        
        // Mock dependencies
        apiService = mockk()
        userDao = mockk()
        progressDao = mockk()
        sessionDao = mockk()
        questionDao = mockk()
        preferencesManager = mockk()
    }
    
    @Test
    fun testDataSyncWorker_SuccessfulSync() = runTest {
        // Given
        val userId = "test-user-123"
        val userPrefs = com.studywise.ai.data.local.preferences.UserPreferences(
            userId = userId,
            lastSyncTime = System.currentTimeMillis() - 3600000 // 1 hour ago
        )
        
        every { preferencesManager.userPreferences } returns flowOf(userPrefs)
        coEvery { preferencesManager.updateLastSyncTime(any()) } just Runs
        
        // User sync
        val localUser = UserEntity(
            id = userId,
            name = "Test User",
            email = "test@example.com",
            password = "hashed",
            role = UserRole.STUDENT,
            gradeLevel = 5,
            createdAt = Date(),
            syncStatus = SyncStatus.PENDING
        )
        coEvery { userDao.getUserById(userId) } returns localUser
        coEvery { userDao.updateUser(any()) } just Runs
        coEvery { apiService.updateUserProfile(any(), any(), any(), any()) } returns Response.success(Unit)
        coEvery { apiService.getUserProfile(userId) } returns Response.success(
            UserDto(
                id = userId,
                name = "Test User Updated",
                email = "test@example.com",
                role = "STUDENT",
                gradeLevel = 5,
                profileImage = null,
                lastLoginAt = Date(),
                createdAt = Date()
            )
        )
        
        // Session sync
        val pendingSessions = listOf(
            LearningSessionEntity(
                id = "session-1",
                userId = userId,
                subject = "Math",
                startedAt = Date(),
                completedAt = Date(),
                durationMinutes = 30,
                questionsAnswered = 10,
                correctAnswers = 8,
                pointsEarned = 80,
                status = SessionStatus.COMPLETED,
                syncStatus = SyncStatus.PENDING
            )
        )
        coEvery { sessionDao.getPendingSyncSessions(userId) } returns pendingSessions
        coEvery { sessionDao.updateSession(any()) } just Runs
        coEvery { sessionDao.getSessionById(any()) } returns null
        coEvery { sessionDao.insertSession(any()) } just Runs
        coEvery { 
            apiService.uploadLearningSession(any(), any(), any(), any(), any(), any(), any(), any(), any()) 
        } returns Response.success(Unit)
        coEvery { 
            apiService.getLearningSessionsSince(userId, any()) 
        } returns Response.success(emptyList())
        
        // Progress sync
        coEvery { progressDao.getPendingSyncProgress(userId) } returns emptyList()
        coEvery { apiService.getUserProgress(userId) } returns Response.success(emptyList())
        
        // Question sync
        coEvery { questionDao.getPendingSyncResponses(userId) } returns emptyList()
        coEvery { apiService.getQuestionsForGrade(5) } returns Response.success(emptyList())
        
        // Build worker
        val worker = TestListenableWorkerBuilder<DataSyncWorker>(
            context = context,
            inputData = androidx.work.Data.EMPTY
        ).build()
        
        // When
        val result = worker.doWork()
        
        // Then
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        
        // Verify sync operations
        coVerify {
            userDao.getUserById(userId)
            apiService.updateUserProfile(userId, localUser.name, localUser.gradeLevel, localUser.profileImage)
            apiService.getUserProfile(userId)
            sessionDao.getPendingSyncSessions(userId)
            apiService.uploadLearningSession(
                "session-1", userId, "Math", any(), any(), 30, 10, 8, 80
            )
            preferencesManager.updateLastSyncTime(any())
        }
    }
    
    @Test
    fun testDataSyncWorker_NoUserLoggedIn() = runTest {
        // Given
        val userPrefs = com.studywise.ai.data.local.preferences.UserPreferences(
            userId = null // No user logged in
        )
        every { preferencesManager.userPreferences } returns flowOf(userPrefs)
        
        // Build worker
        val worker = TestListenableWorkerBuilder<DataSyncWorker>(
            context = context,
            inputData = androidx.work.Data.EMPTY
        ).build()
        
        // When
        val result = worker.doWork()
        
        // Then
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        
        // Should not attempt any sync operations
        coVerify(exactly = 0) {
            userDao.getUserById(any())
            apiService.updateUserProfile(any(), any(), any(), any())
            sessionDao.getPendingSyncSessions(any())
        }
    }
    
    @Test
    fun testDataSyncWorker_NetworkError_ShouldRetry() = runTest {
        // Given
        val userId = "test-user-123"
        val userPrefs = com.studywise.ai.data.local.preferences.UserPreferences(
            userId = userId
        )
        
        every { preferencesManager.userPreferences } returns flowOf(userPrefs)
        
        val localUser = UserEntity(
            id = userId,
            name = "Test User",
            email = "test@example.com",
            password = "hashed",
            role = UserRole.STUDENT,
            gradeLevel = 5,
            createdAt = Date(),
            syncStatus = SyncStatus.PENDING
        )
        coEvery { userDao.getUserById(userId) } returns localUser
        
        // Simulate network error
        coEvery { 
            apiService.updateUserProfile(any(), any(), any(), any()) 
        } returns Response.error(500, mockk(relaxed = true))
        
        // Build worker
        val worker = TestListenableWorkerBuilder<DataSyncWorker>(
            context = context,
            inputData = androidx.work.Data.EMPTY,
            runAttemptCount = 1 // First attempt
        ).build()
        
        // When
        val result = worker.doWork()
        
        // Then
        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }
    
    @Test
    fun testDataSyncWorker_MaxRetriesExceeded_ShouldFail() = runTest {
        // Given
        val userId = "test-user-123"
        val userPrefs = com.studywise.ai.data.local.preferences.UserPreferences(
            userId = userId
        )
        
        every { preferencesManager.userPreferences } returns flowOf(userPrefs)
        
        // Simulate persistent error
        coEvery { userDao.getUserById(userId) } throws RuntimeException("Database error")
        
        // Build worker with max retries exceeded
        val worker = TestListenableWorkerBuilder<DataSyncWorker>(
            context = context,
            inputData = androidx.work.Data.EMPTY,
            runAttemptCount = 4 // Exceeds MAX_RETRY_ATTEMPTS
        ).build()
        
        // When
        val result = worker.doWork()
        
        // Then
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }
    
    @Test
    fun testDataSyncWorker_PartialSyncFailure() = runTest {
        // Given
        val userId = "test-user-123"
        val userPrefs = com.studywise.ai.data.local.preferences.UserPreferences(
            userId = userId,
            lastSyncTime = 0L
        )
        
        every { preferencesManager.userPreferences } returns flowOf(userPrefs)
        coEvery { preferencesManager.updateLastSyncTime(any()) } just Runs
        
        // User sync succeeds
        val localUser = UserEntity(
            id = userId,
            name = "Test User",
            email = "test@example.com",
            password = "hashed",
            role = UserRole.STUDENT,
            gradeLevel = 5,
            createdAt = Date(),
            syncStatus = SyncStatus.SYNCED
        )
        coEvery { userDao.getUserById(userId) } returns localUser
        coEvery { apiService.getUserProfile(userId) } returns Response.success(
            UserDto(
                id = userId,
                name = "Test User",
                email = "test@example.com",
                role = "STUDENT",
                gradeLevel = 5,
                profileImage = null,
                lastLoginAt = Date(),
                createdAt = Date()
            )
        )
        
        // Session sync fails but should continue
        coEvery { sessionDao.getPendingSyncSessions(userId) } throws RuntimeException("Session error")
        
        // Progress sync succeeds
        coEvery { progressDao.getPendingSyncProgress(userId) } returns emptyList()
        coEvery { apiService.getUserProgress(userId) } returns Response.success(emptyList())
        
        // Question sync succeeds
        coEvery { questionDao.getPendingSyncResponses(userId) } returns emptyList()
        coEvery { apiService.getQuestionsForGrade(5) } returns Response.success(emptyList())
        
        // Build worker
        val worker = TestListenableWorkerBuilder<DataSyncWorker>(
            context = context,
            inputData = androidx.work.Data.EMPTY
        ).build()
        
        // When
        val result = worker.doWork()
        
        // Then
        // Should still succeed despite partial failure
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        
        // Verify other sync operations continued
        coVerify {
            userDao.getUserById(userId)
            progressDao.getPendingSyncProgress(userId)
            questionDao.getPendingSyncResponses(userId)
            preferencesManager.updateLastSyncTime(any())
        }
    }
}