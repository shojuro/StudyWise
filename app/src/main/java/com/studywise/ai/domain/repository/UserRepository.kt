package com.studywise.ai.domain.repository

import android.net.Uri
import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(): Result<UserEntity>
    suspend fun updateUserProfile(user: UserEntity): Result<Unit>
    suspend fun uploadProfileImage(imageUri: Uri): Result<String>
    suspend fun updateNotificationPreferences(
        enableDailyReminders: Boolean,
        enableProgressUpdates: Boolean
    ): Result<Unit>
    fun observeCurrentUser(): Flow<UserEntity?>
    suspend fun getChildrenForParent(parentId: String): Result<List<User>>
    suspend fun getParentForChild(childId: String): Result<User?>
    suspend fun getStudentsForTeacher(teacherId: String): Result<List<User>>
}