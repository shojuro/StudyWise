package com.studywise.ai.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.studywise.ai.data.local.dao.*
import com.studywise.ai.data.local.entity.SyncStatus
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.data.remote.api.StudyWiseApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: StudyWiseApiService,
    private val userDao: UserDao,
    private val progressDao: ProgressDao,
    private val sessionDao: LearningSessionDao,
    private val questionDao: QuestionDao,
    private val questionResponseDao: QuestionResponseDao,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting data sync...")
            
            // Check if user is logged in
            val userId = preferencesManager.userPreferences.first().userId
            if (userId.isNullOrEmpty()) {
                Timber.w("No user logged in, skipping sync")
                return@withContext Result.success()
            }

            // Check network connectivity
            if (!isNetworkAvailable()) {
                Timber.w("No network connection, will retry later")
                return@withContext Result.retry()
            }

            // Sync user data
            syncUserData(userId)
            
            // Sync learning sessions
            syncLearningSessions(userId)
            
            // Sync progress data
            syncProgressData(userId)
            
            // Sync questions and answers
            syncQuestionData(userId)
            
            // Update last sync timestamp
            preferencesManager.updateLastSyncTime(System.currentTimeMillis())
            
            Timber.d("Data sync completed successfully")
            return@withContext Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Error during data sync")
            return@withContext if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun syncUserData(userId: String) {
        try {
            // Upload any local user profile changes
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                val response = apiService.updateUserProfile(
                    userId = userId,
                    name = localUser.name,
                    gradeLevel = localUser.grade,
                    profileImage = null
                )
                
                if (response.isSuccessful) {
                    // Mark as synced
                    userDao.updateUser(localUser)
                }
            }
            
            // Download latest user data from server
            val remoteUserResponse = apiService.getUserProfile(userId)
            if (remoteUserResponse.isSuccessful) {
                remoteUserResponse.body()?.let { remoteUser ->
                    // Update local database with server data
                    userDao.updateUser(
                        localUser!!.copy(
                            name = remoteUser.name,
                            grade = remoteUser.gradeLevel,
                            lastLoginAt = remoteUser.lastLoginAt
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing user data")
        }
    }

    private suspend fun syncLearningSessions(userId: String) {
        try {
            // Upload pending local sessions
            val pendingSessions = sessionDao.getPendingSyncSessions(userId)
            pendingSessions.forEach { session ->
                try {
                    val response = apiService.uploadLearningSession(
                        sessionId = session.id,
                        userId = session.userId,
                        subject = session.subject,
                        startedAt = session.startedAt.time,
                        completedAt = session.completedAt?.time,
                        durationMinutes = session.durationMinutes,
                        questionsAnswered = session.questionsAnswered,
                        correctAnswers = session.correctAnswers,
                        pointsEarned = session.pointsEarned
                    )
                    
                    if (response.isSuccessful) {
                        // Mark session as synced
                        sessionDao.updateSession(
                            session.copy(syncStatus = SyncStatus.SYNCED)
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error uploading session ${session.id}")
                }
            }
            
            // Download new sessions from server
            val lastSyncTime = preferencesManager.userPreferences.first().lastSyncTime ?: 0L
            val remoteSessionsResponse = apiService.getLearningSessionsSince(userId, lastSyncTime)
            
            if (remoteSessionsResponse.isSuccessful) {
                remoteSessionsResponse.body()?.forEach { remoteSession ->
                    // Check if session already exists locally
                    val localSession = sessionDao.getSessionById(remoteSession.id)
                    if (localSession == null) {
                        // Insert new session from server
                        sessionDao.insertSession(
                            remoteSession.toEntity().copy(syncStatus = SyncStatus.SYNCED)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing learning sessions")
        }
    }

    private suspend fun syncProgressData(userId: String) {
        try {
            // Upload local progress updates
            val pendingProgress = progressDao.getPendingSyncProgress(userId)
            pendingProgress.forEach { progress ->
                try {
                    val response = apiService.updateProgress(
                        userId = userId,
                        skillId = progress.skillId,
                        masteryLevel = progress.masteryLevel,
                        questionsAnswered = progress.questionsAnswered,
                        correctAnswers = progress.correctAnswers,
                        lastPracticedAt = progress.lastPracticedAt?.time ?: 0L
                    )
                    
                    if (response.isSuccessful) {
                        progressDao.updateProgress(
                            progress.copy(syncStatus = SyncStatus.SYNCED)
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error uploading progress for skill ${progress.skillId}")
                }
            }
            
            // Download latest progress from server
            val remoteProgressResponse = apiService.getUserProgress(userId)
            if (remoteProgressResponse.isSuccessful) {
                remoteProgressResponse.body()?.forEach { remoteProgress ->
                    val localProgress = progressDao.getProgress(userId, remoteProgress.skillId)
                    if (localProgress == null || remoteProgress.updatedAt > localProgress.updatedAt) {
                        // Update or insert progress
                        progressDao.insertProgress(
                            remoteProgress.toEntity().copy(syncStatus = SyncStatus.SYNCED)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing progress data")
        }
    }

    private suspend fun syncQuestionData(userId: String) {
        try {
            // Upload user's question responses
            val pendingResponses = questionResponseDao.getPendingSyncResponses(userId)
            pendingResponses.forEach { response ->
                try {
                    val uploadResponse = apiService.uploadQuestionResponse(
                        userId = userId,
                        questionId = response.questionId,
                        userAnswer = response.userAnswer,
                        isCorrect = response.isCorrect,
                        timeSpentSeconds = response.timeSpentSeconds,
                        timestamp = response.timestamp.time
                    )
                    
                    if (uploadResponse.isSuccessful) {
                        questionResponseDao.updateResponse(
                            response.copy(syncStatus = SyncStatus.SYNCED)
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error uploading response for question ${response.questionId}")
                }
            }
            
            // Download new questions for user's grade level
            val user = userDao.getUserById(userId)
            user?.grade?.let { gradeLevel ->
                val questionsResponse = apiService.getQuestionsForGrade(gradeLevel)
                if (questionsResponse.isSuccessful) {
                    questionsResponse.body()?.forEach { remoteQuestion ->
                        val localQuestion = questionDao.getQuestionById(remoteQuestion.id)
                        if (localQuestion == null) {
                            questionDao.insertQuestion(remoteQuestion.toEntity())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing question data")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        // In production, implement proper network check
        // For MVP, assume network is available
        return true
    }

    companion object {
        const val WORK_NAME = "data_sync_work"
        const val MAX_RETRY_ATTEMPTS = 3
        
        fun buildWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            return PeriodicWorkRequestBuilder<DataSyncWorker>(
                15, TimeUnit.MINUTES // Sync every 15 minutes
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
        
        fun buildOneTimeWorkRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            return OneTimeWorkRequestBuilder<DataSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
    }
}