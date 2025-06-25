package com.studywise.ai.data.repository

import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.User
import com.studywise.ai.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val hashedPassword = hashPassword(password)
            val userEntity = userDao.getUserByCredentials(email, hashedPassword)
            
            if (userEntity != null) {
                userDao.updateLastLogin(userEntity.id, System.currentTimeMillis())
                preferencesManager.updateUserSession(
                    userId = userEntity.id,
                    userEmail = userEntity.email,
                    userName = userEntity.name,
                    userRole = userEntity.role.name
                )
                Result.success(userEntity.toDomainModel())
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        grade: Int?,
        parentId: String?
    ): Result<User> {
        return try {
            // Check if email already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("Email already registered"))
            }

            // Create new user
            val userId = UUID.randomUUID().toString()
            val hashedPassword = hashPassword(password)
            val userEntity = UserEntity(
                id = userId,
                name = name,
                email = email,
                password = hashedPassword,
                role = UserRole.valueOf(role),
                grade = grade,
                createdAt = Date(),
                parentId = parentId
            )

            userDao.insertUser(userEntity)
            
            // Auto-login after registration
            preferencesManager.updateUserSession(
                userId = userId,
                userEmail = email,
                userName = name,
                userRole = role
            )

            Result.success(userEntity.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        preferencesManager.clearUserSession()
    }

    override suspend fun getCurrentUser(): User? {
        val preferences = preferencesManager.userPreferences
        var currentUser: User? = null
        
        preferences.collect { prefs ->
            if (prefs.isLoggedIn && prefs.userId != null) {
                val userEntity = userDao.getUserById(prefs.userId)
                currentUser = userEntity?.toDomainModel()
            }
            return@collect
        }
        
        return currentUser
    }

    override fun observeCurrentUser(): Flow<User?> = flow {
        preferencesManager.userPreferences.collect { prefs ->
            if (prefs.isLoggedIn && prefs.userId != null) {
                val userEntity = userDao.getUserById(prefs.userId)
                emit(userEntity?.toDomainModel())
            } else {
                emit(null)
            }
        }
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            val userEntity = user.toEntity()
            userDao.updateUser(userEntity)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val preferences = preferencesManager.userPreferences
            var userId: String? = null
            
            preferences.collect { prefs ->
                userId = prefs.userId
                return@collect
            }
            
            userId?.let { id ->
                val user = userDao.getUserById(id)
                if (user != null && user.password == hashPassword(oldPassword)) {
                    val updatedUser = user.copy(password = hashPassword(newPassword))
                    userDao.updateUser(updatedUser)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Invalid old password"))
                }
            } ?: Result.failure(Exception("User not logged in"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateParentCode(parentCode: String): Result<String> {
        // Parent codes are formatted as "PARENT-{first 8 chars of parent ID}"
        return try {
            if (parentCode.startsWith("PARENT-")) {
                val parentIdPrefix = parentCode.substring(7)
                // In a real app, you'd validate this against a server
                // For MVP, we'll simulate validation
                Result.success(parentIdPrefix)
            } else {
                Result.failure(Exception("Invalid parent code format"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateParentCode(parentId: String): String {
        return "PARENT-${parentId.take(8).uppercase()}"
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun UserEntity.toDomainModel(): User {
        return User(
            id = id,
            name = name,
            email = email,
            role = role,
            grade = grade,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt?.let { Date(it) },
            isActive = isActive,
            parentId = parentId
        )
    }

    private fun User.toEntity(): UserEntity {
        return UserEntity(
            id = id,
            name = name,
            email = email,
            password = "", // Password is not updated through this method
            role = role,
            grade = grade,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt?.time,
            isActive = isActive,
            parentId = parentId
        )
    }
}