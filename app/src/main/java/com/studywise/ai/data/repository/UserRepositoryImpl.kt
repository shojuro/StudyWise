package com.studywise.ai.data.repository

import android.net.Uri
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    override suspend fun getChildrenForParent(parentId: String): Result<List<com.studywise.ai.domain.model.User>> {
        return try {
            val childEntities = userDao.getChildrenOfParent(parentId).first()
            val children = childEntities.map { entity ->
                com.studywise.ai.domain.model.User(
                    id = entity.id,
                    name = entity.name,
                    email = entity.email,
                    role = entity.role,
                    grade = entity.grade,
                    createdAt = entity.createdAt,
                    lastLoginAt = entity.lastLoginAt,
                    isActive = entity.isActive,
                    parentId = entity.parentId
                )
            }
            Result.success(children)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getParentForChild(childId: String): Result<com.studywise.ai.domain.model.User?> {
        return try {
            val childEntity = userDao.getUserById(childId)
            if (childEntity?.parentId != null) {
                val parentEntity = userDao.getUserById(childEntity.parentId)
                if (parentEntity != null) {
                    val parent = com.studywise.ai.domain.model.User(
                        id = parentEntity.id,
                        name = parentEntity.name,
                        email = parentEntity.email,
                        role = parentEntity.role,
                        grade = parentEntity.grade,
                        createdAt = parentEntity.createdAt,
                        lastLoginAt = parentEntity.lastLoginAt,
                        isActive = parentEntity.isActive,
                        parentId = parentEntity.parentId
                    )
                    Result.success(parent)
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStudentsForTeacher(teacherId: String): Result<List<com.studywise.ai.domain.model.User>> {
        return try {
            // For MVP, get all students
            // In production, this would use teacher-class relationships
            val students = userDao.getUsersByRole(com.studywise.ai.data.local.entity.UserRole.STUDENT).first().map { entity ->
                com.studywise.ai.domain.model.User(
                    id = entity.id,
                    name = entity.name,
                    email = entity.email,
                    role = entity.role,
                    grade = entity.grade,
                    createdAt = entity.createdAt,
                    lastLoginAt = entity.lastLoginAt,
                    isActive = entity.isActive,
                    parentId = entity.parentId
                )
            }
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}