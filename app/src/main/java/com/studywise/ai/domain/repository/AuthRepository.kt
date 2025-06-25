package com.studywise.ai.domain.repository

import com.studywise.ai.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        grade: Int? = null,
        parentId: String? = null
    ): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    fun observeCurrentUser(): Flow<User?>
    suspend fun updateUserProfile(user: User): Result<User>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun validateParentCode(parentCode: String): Result<String> // Returns parent ID
    suspend fun generateParentCode(parentId: String): String
}