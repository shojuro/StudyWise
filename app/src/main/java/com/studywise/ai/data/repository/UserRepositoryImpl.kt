package com.studywise.ai.data.repository

import android.net.Uri
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager
) : UserRepository {

    override suspend fun getCurrentUser(): Result<UserEntity> {
        return try {
            val userId = preferencesManager.userPreferences.firstOrNull()?.userId
                ?: return Result.failure(Exception("No user logged in"))
            
            val user = userDao.getUserById(userId)
                ?: return Result.failure(Exception("User not found"))
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: UserEntity): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        // For MVP, just return the URI string
        // In production, this would upload to cloud storage
        return Result.success(imageUri.toString())
    }

    override suspend fun updateNotificationPreferences(
        enableDailyReminders: Boolean,
        enableProgressUpdates: Boolean
    ): Result<Unit> {
        return try {
            preferencesManager.updateNotificationSettings(
                enableDailyReminders = enableDailyReminders,
                enableProgressUpdates = enableProgressUpdates
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeCurrentUser(): Flow<UserEntity?> {
        // For MVP, return empty flow
        // In production, this would observe the current user from database
        return kotlinx.coroutines.flow.flowOf(null)
    }
}