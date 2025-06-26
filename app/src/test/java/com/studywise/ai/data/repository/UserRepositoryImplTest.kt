package com.studywise.ai.data.repository

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.data.local.preferences.UserPreferences
import com.studywise.ai.domain.model.User
import io.mockk.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

class UserRepositoryImplTest {
    
    private lateinit var userDao: UserDao
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var userRepository: UserRepositoryImpl
    
    @Before
    fun setup() {
        userDao = mockk()
        preferencesManager = mockk()
        userRepository = UserRepositoryImpl(userDao, preferencesManager)
    }
    
    @Test
    fun `getCurrentUser should return success when user exists`() = runTest {
        // Given
        val userId = UUID.randomUUID().toString()
        val userEntity = createTestUserEntity(userId)
        val userPreferences = UserPreferences(userId = userId)
        
        every { preferencesManager.userPreferences } returns flowOf(userPreferences)
        coEvery { userDao.getUserById(userId) } returns userEntity
        
        // When
        val result = userRepository.getCurrentUser()
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(userEntity)
    }
    
    @Test
    fun `getCurrentUser should return failure when no user logged in`() = runTest {
        // Given
        val userPreferences = UserPreferences(userId = null)
        every { preferencesManager.userPreferences } returns flowOf(userPreferences)
        
        // When
        val result = userRepository.getCurrentUser()
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("No user logged in")
    }
    
    @Test
    fun `getCurrentUser should return failure when user not found`() = runTest {
        // Given
        val userId = UUID.randomUUID().toString()
        val userPreferences = UserPreferences(userId = userId)
        
        every { preferencesManager.userPreferences } returns flowOf(userPreferences)
        coEvery { userDao.getUserById(userId) } returns null
        
        // When
        val result = userRepository.getCurrentUser()
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("User not found")
    }
    
    @Test
    fun `updateUserProfile should return success`() = runTest {
        // Given
        val userEntity = createTestUserEntity()
        coEvery { userDao.updateUser(userEntity) } just Runs
        
        // When
        val result = userRepository.updateUserProfile(userEntity)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { userDao.updateUser(userEntity) }
    }
    
    @Test
    fun `uploadProfileImage should return URI string`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val uriString = "content://media/images/123"
        every { uri.toString() } returns uriString
        
        // When
        val result = userRepository.uploadProfileImage(uri)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(uriString)
    }
    
    @Test
    fun `updateNotificationPreferences should update preferences`() = runTest {
        // Given
        coEvery { 
            preferencesManager.updateNotificationPreferences(any(), any()) 
        } just Runs
        
        // When
        val result = userRepository.updateNotificationPreferences(true, false)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { 
            preferencesManager.updateNotificationPreferences(true, false) 
        }
    }
    
    @Test
    fun `getChildrenForParent should return list of children`() = runTest {
        // Given
        val parentId = UUID.randomUUID().toString()
        val childEntities = listOf(
            createTestUserEntity(role = UserRole.STUDENT, parentId = parentId),
            createTestUserEntity(role = UserRole.STUDENT, parentId = parentId)
        )
        
        every { userDao.getChildrenOfParent(parentId) } returns flowOf(childEntities)
        
        // When
        val result = userRepository.getChildrenForParent(parentId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val children = result.getOrNull()
        assertThat(children).hasSize(2)
        assertThat(children?.all { it.role == UserRole.STUDENT }).isTrue()
        assertThat(children?.all { it.parentId == parentId }).isTrue()
    }
    
    @Test
    fun `getChildrenForParent should handle empty list`() = runTest {
        // Given
        val parentId = UUID.randomUUID().toString()
        every { userDao.getChildrenOfParent(parentId) } returns flowOf(emptyList())
        
        // When
        val result = userRepository.getChildrenForParent(parentId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()
    }
    
    @Test
    fun `getParentForChild should return parent when exists`() = runTest {
        // Given
        val parentId = UUID.randomUUID().toString()
        val childId = UUID.randomUUID().toString()
        val childEntity = createTestUserEntity(childId, role = UserRole.STUDENT, parentId = parentId)
        val parentEntity = createTestUserEntity(parentId, role = UserRole.PARENT)
        
        coEvery { userDao.getUserById(childId) } returns childEntity
        coEvery { userDao.getUserById(parentId) } returns parentEntity
        
        // When
        val result = userRepository.getParentForChild(childId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val parent = result.getOrNull()
        assertThat(parent?.id).isEqualTo(parentId)
        assertThat(parent?.role).isEqualTo(UserRole.PARENT)
    }
    
    @Test
    fun `getParentForChild should return null when child has no parent`() = runTest {
        // Given
        val childId = UUID.randomUUID().toString()
        val childEntity = createTestUserEntity(childId, role = UserRole.STUDENT, parentId = null)
        
        coEvery { userDao.getUserById(childId) } returns childEntity
        
        // When
        val result = userRepository.getParentForChild(childId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()
    }
    
    @Test
    fun `getStudentsForTeacher should return list of students`() = runTest {
        // Given
        val teacherId = UUID.randomUUID().toString()
        val studentEntities = listOf(
            createTestUserEntity(role = UserRole.STUDENT),
            createTestUserEntity(role = UserRole.STUDENT)
        )
        
        every { userDao.getUsersByRole(UserRole.STUDENT) } returns flowOf(studentEntities)
        
        // When
        val result = userRepository.getStudentsForTeacher(teacherId)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val students = result.getOrNull()
        assertThat(students).hasSize(2)
        assertThat(students?.all { it.role == UserRole.STUDENT }).isTrue()
    }
    
    @Test
    fun `observeCurrentUser should return flow of null for MVP`() = runTest {
        // When
        val flow = userRepository.observeCurrentUser()
        
        // Then
        flow.collect { user ->
            assertThat(user).isNull()
        }
    }
    
    // Helper function to create test user entities
    private fun createTestUserEntity(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test User",
        email: String = "test@example.com",
        role: UserRole = UserRole.STUDENT,
        parentId: String? = null
    ): UserEntity {
        return UserEntity(
            id = id,
            name = name,
            email = email,
            password = "password123",
            role = role,
            grade = if (role == UserRole.STUDENT) 5 else null,
            createdAt = Date(),
            isActive = true,
            parentId = parentId
        )
    }
}