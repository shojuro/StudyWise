package com.studywise.ai.data.repository

import com.google.common.truth.Truth.assertThat
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.data.local.preferences.PreferencesManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

class AuthRepositoryImplTest {
    
    private lateinit var userDao: UserDao
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var authRepository: AuthRepositoryImpl
    
    @Before
    fun setup() {
        userDao = mockk()
        preferencesManager = mockk()
        authRepository = AuthRepositoryImpl(userDao, preferencesManager)
    }
    
    @Test
    fun `login with valid credentials should return success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userEntity = UserEntity(
            id = UUID.randomUUID().toString(),
            name = "Test User",
            email = email,
            password = password,
            role = UserRole.STUDENT,
            createdAt = Date(),
            isActive = true
        )
        
        coEvery { userDao.getUserByCredentials(email, password) } returns userEntity
        coEvery { userDao.updateLastLogin(any(), any()) } just Runs
        coEvery { 
            preferencesManager.updateUserSession(any(), any(), any(), any()) 
        } just Runs
        
        // When
        val result = authRepository.login(email, password)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(userEntity)
        
        coVerify {
            userDao.getUserByCredentials(email, password)
            userDao.updateLastLogin(userEntity.id, any())
            preferencesManager.updateUserSession(
                userEntity.id,
                userEntity.email,
                userEntity.name,
                userEntity.role.name
            )
        }
    }
    
    @Test
    fun `login with invalid credentials should return failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        
        coEvery { userDao.getUserByCredentials(email, password) } returns null
        
        // When
        val result = authRepository.login(email, password)
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Invalid email or password")
        
        coVerify(exactly = 0) { 
            preferencesManager.updateUserSession(any(), any(), any(), any()) 
        }
    }
    
    @Test
    fun `register with new email should create user successfully`() = runTest {
        // Given
        val email = "newuser@example.com"
        val password = "password123"
        val name = "New User"
        val role = UserRole.STUDENT
        
        coEvery { userDao.getUserByEmail(email) } returns null
        coEvery { userDao.insertUser(any()) } just Runs
        coEvery { 
            preferencesManager.updateUserSession(any(), any(), any(), any()) 
        } just Runs
        
        // When
        val result = authRepository.register(email, password, name, role)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val createdUser = result.getOrNull()
        assertThat(createdUser).isNotNull()
        assertThat(createdUser?.email).isEqualTo(email)
        assertThat(createdUser?.name).isEqualTo(name)
        assertThat(createdUser?.role).isEqualTo(role)
        
        coVerify {
            userDao.getUserByEmail(email)
            userDao.insertUser(any())
            preferencesManager.updateUserSession(any(), email, name, role.name)
        }
    }
    
    @Test
    fun `register with existing email should return failure`() = runTest {
        // Given
        val email = "existing@example.com"
        val existingUser = UserEntity(
            id = UUID.randomUUID().toString(),
            name = "Existing User",
            email = email,
            password = "oldpassword",
            role = UserRole.STUDENT,
            createdAt = Date(),
            isActive = true
        )
        
        coEvery { userDao.getUserByEmail(email) } returns existingUser
        
        // When
        val result = authRepository.register(email, "newpassword", "New Name", UserRole.STUDENT)
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Email already exists")
        
        coVerify(exactly = 0) { userDao.insertUser(any()) }
        coVerify(exactly = 0) { preferencesManager.updateUserSession(any(), any(), any(), any()) }
    }
    
    @Test
    fun `logout should clear user session`() = runTest {
        // Given
        coEvery { preferencesManager.clearUserSession() } just Runs
        
        // When
        authRepository.logout()
        
        // Then
        coVerify { preferencesManager.clearUserSession() }
    }
    
    @Test
    fun `verifyTwoFactorCode with demo code should return success`() = runTest {
        // Given
        val userId = "user123"
        val code = "123456" // Demo code
        
        // When
        val result = authRepository.verifyTwoFactorCode(userId, code)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isTrue()
    }
    
    @Test
    fun `verifyTwoFactorCode with invalid code should return failure`() = runTest {
        // Given
        val userId = "user123"
        val code = "000000"
        
        // When
        val result = authRepository.verifyTwoFactorCode(userId, code)
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Invalid verification code")
    }
    
    @Test
    fun `database error during login should return failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val exception = RuntimeException("Database error")
        
        coEvery { userDao.getUserByCredentials(email, password) } throws exception
        
        // When
        val result = authRepository.login(email, password)
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}