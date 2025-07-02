package com.studywise.ai.data.repository

import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.data.service.security.PasswordHashMigrator
import com.studywise.ai.domain.model.User
import com.studywise.ai.domain.repository.AuthRepository
import com.studywise.ai.domain.service.security.HashType
import com.studywise.ai.domain.service.security.PasswordHashingService
import com.studywise.ai.domain.service.security.SessionManager
import com.studywise.ai.domain.service.security.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager,
    private val passwordHashingService: PasswordHashingService,
    private val passwordHashMigrator: PasswordHashMigrator,
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Get user by email first
            val userEntity = userDao.getUserByEmail(email)
            
            if (userEntity != null) {
                // Verify password with current hash
                val isPasswordValid = passwordHashingService.verifyPassword(password, userEntity.password)
                
                if (isPasswordValid) {
                    // Check if password hash needs upgrade
                    val (newHash, wasUpgraded) = passwordHashMigrator.migrateIfNeeded(
                        userEntity.password, 
                        password
                    )
                    
                    // Update user if hash was upgraded
                    if (wasUpgraded) {
                        val updatedUser = userEntity.copy(
                            password = newHash,
                            passwordHashType = HashType.BCRYPT.name
                        )
                        userDao.updateUser(updatedUser)
                        Timber.i("Password hash upgraded for user: ${userEntity.email}")
                    }
                    
                    // Update last login
                    userDao.updateLastLogin(userEntity.id, System.currentTimeMillis())
                    
                    // Generate tokens
                    val tokenResult = tokenManager.generateTokens(
                        userId = userEntity.id,
                        role = userEntity.role.name
                    )
                    
                    if (tokenResult.isSuccess) {
                        val authToken = tokenResult.getOrThrow()
                        
                        // Create session
                        sessionManager.createSession(userEntity.id, authToken)
                        
                        // Update legacy session (for backward compatibility)
                        preferencesManager.updateUserSession(
                            userId = userEntity.id,
                            userEmail = userEntity.email,
                            userName = userEntity.name,
                            userRole = userEntity.role.name
                        )
                        
                        Result.success(userEntity.toDomainModel())
                    } else {
                        Result.failure(Exception("Failed to generate authentication tokens"))
                    }
                } else {
                    Result.failure(Exception("Invalid email or password"))
                }
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Login failed")
            Result.failure(e)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        grade: Int?,
        parentId: String?,
        birthDate: LocalDate?
    ): Result<User> {
        return try {
            // Check if email already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("Email already registered"))
            }

            // Create new user
            val userId = UUID.randomUUID().toString()
            val hashedPassword = passwordHashingService.hashPassword(password)
            val userEntity = UserEntity(
                id = userId,
                name = name,
                email = email,
                password = hashedPassword,
                passwordHashType = HashType.BCRYPT.name,
                role = UserRole.valueOf(role),
                grade = grade,
                birthDate = birthDate,
                createdAt = Date(),
                parentId = parentId
            )

            userDao.insertUser(userEntity)
            
            // Auto-login after registration
            // Generate tokens
            val tokenResult = tokenManager.generateTokens(
                userId = userId,
                role = role
            )
            
            if (tokenResult.isSuccess) {
                val authToken = tokenResult.getOrThrow()
                
                // Create session
                sessionManager.createSession(userId, authToken)
                
                // Update legacy session (for backward compatibility)
                preferencesManager.updateUserSession(
                    userId = userId,
                    userEmail = email,
                    userName = name,
                    userRole = role
                )
                
                Result.success(userEntity.toDomainModel())
            } else {
                Result.failure(Exception("Failed to generate authentication tokens"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        // End session and clear tokens
        sessionManager.endSession()
        
        // Clear legacy session (for backward compatibility)
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
                if (user != null && passwordHashingService.verifyPassword(oldPassword, user.password)) {
                    val newHashedPassword = passwordHashingService.hashPassword(newPassword)
                    val updatedUser = user.copy(
                        password = newHashedPassword,
                        passwordHashType = HashType.BCRYPT.name
                    )
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

    private fun UserEntity.toDomainModel(): User {
        return User(
            id = id,
            name = name,
            email = email,
            role = role,
            grade = grade,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt,
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
            lastLoginAt = lastLoginAt,
            isActive = isActive,
            parentId = parentId
        )
    }
}