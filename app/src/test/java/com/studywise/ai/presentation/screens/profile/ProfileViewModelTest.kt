package com.studywise.ai.presentation.screens.profile

import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.domain.repository.UserRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: ProfileViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    
    private val mockUser = UserEntity(
        id = "user123",
        name = "John Doe",
        email = "john@example.com",
        role = UserRole.STUDENT,
        grade = 8,
        schoolId = "school123",
        passwordHash = "hash",
        createdAt = Date(1640995200000), // Jan 1, 2022
        updatedAt = Date()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        coEvery { userRepository.getCurrentUser() } returns Result.success(mockUser)
        
        // When
        viewModel = ProfileViewModel(userRepository)
        
        // Then - check immediate state
        val initialState = viewModel.uiState.value
        assertTrue(initialState.isLoading)
    }

    @Test
    fun `loadUserProfile success should update state with user data`() = runTest {
        // Given
        coEvery { userRepository.getCurrentUser() } returns Result.success(mockUser)
        
        // When
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(mockUser, state.user)
        assertEquals("John Doe", state.displayName)
        assertEquals("john@example.com", state.email)
        assertEquals("STUDENT", state.role)
        assertEquals(8, state.grade)
        assertEquals("January 2022", state.memberSince)
        assertEquals("John Doe", state.editDisplayName)
    }

    @Test
    fun `loadUserProfile failure should show error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { userRepository.getCurrentUser() } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(null, state.user)
        assertEquals("Failed to load profile", state.saveError)
    }

    @Test
    fun `loadUserStatistics should populate mock statistics`() = runTest {
        // Given
        coEvery { userRepository.getCurrentUser() } returns Result.success(mockUser)
        
        // When
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(42, state.totalSessions)
        assertEquals(156, state.totalQuestionsAnswered)
        assertEquals(0.78f, state.averageMastery)
        assertEquals(7, state.currentStreak)
        assertEquals(14, state.longestStreak)
        assertEquals(8, state.skillsMastered)
        assertEquals(1250, state.totalPointsEarned)
        assertEquals(3, state.recentAchievements.size)
        
        // Verify achievements
        val firstAchievement = state.recentAchievements[0]
        assertEquals("Week Warrior", firstAchievement.title)
        assertEquals("Complete 7 days in a row", firstAchievement.description)
    }

    @Test
    fun `toggleEditMode should switch edit state`() = runTest {
        // Given
        coEvery { userRepository.getCurrentUser() } returns Result.success(mockUser)
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        // When - enable edit mode
        viewModel.toggleEditMode()
        
        // Then
        var state = viewModel.uiState.first()
        assertTrue(state.isEditMode)
        assertEquals("John Doe", state.editDisplayName)
        assertEquals(null, state.saveError)
        
        // When - disable edit mode
        viewModel.toggleEditMode()
        
        // Then
        state = viewModel.uiState.first()
        assertFalse(state.isEditMode)
    }

    @Test
    fun `onDisplayNameChange should update edit display name`() = runTest {
        // Given
        coEvery { userRepository.getCurrentUser() } returns Result.success(mockUser)
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        viewModel.toggleEditMode()
        
        // When
        viewModel.onDisplayNameChange("Jane Doe")
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("Jane Doe", state.editDisplayName)
        assertEquals("John Doe", state.displayName) // Original name unchanged
    }

    @Test
    fun `onBioChange should update edit bio`() = runTest {
        // Given
        coEvery { userRepository.getCurrentUser() } returns Result.success(mockUser)
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        // When
        viewModel.onBioChange("I love learning!")
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("I love learning!", state.editBio)
    }

    @Test
    fun `saveProfile success should update profile and exit edit mode`() = runTest {
        // Given
        coEvery { userRepository.getCurrentUser() } returns Result.success(mockUser)
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        viewModel.toggleEditMode()
        viewModel.onDisplayNameChange("Jane Doe")
        
        val updatedUser = mockUser.copy(name = "Jane Doe")
        coEvery { userRepository.updateUserProfile(updatedUser) } returns Result.success(updatedUser)
        
        // When
        viewModel.saveProfile()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertFalse(state.isEditMode)
        assertEquals("Jane Doe", state.displayName)
        assertEquals(updatedUser, state.user)
        assertEquals(null, state.saveError)
        
        // Verify repository was called
        coVerify { userRepository.updateUserProfile(updatedUser) }
    }

    @Test
    fun `saveProfile failure should show error`() = runTest {
        // Given
        coEvery { userRepository.getCurrentUser() } returns Result.success(mockUser)
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        viewModel.toggleEditMode()
        viewModel.onDisplayNameChange("Jane Doe")
        
        val errorMessage = "Update failed"
        coEvery { userRepository.updateUserProfile(any()) } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.saveProfile()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertTrue(state.isEditMode) // Still in edit mode
        assertEquals("John Doe", state.displayName) // Name not updated
        assertEquals(errorMessage, state.saveError)
    }

    @Test
    fun `saveProfile with no user should return early`() = runTest {
        // Given - no user loaded
        coEvery { userRepository.getCurrentUser() } returns Result.failure(Exception("No user"))
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        // When
        viewModel.saveProfile()
        advanceUntilIdle()
        
        // Then - verify updateUserProfile was never called
        coVerify(exactly = 0) { userRepository.updateUserProfile(any()) }
    }

    @Test
    fun `cancelEdit should revert changes and exit edit mode`() = runTest {
        // Given
        coEvery { userRepository.getCurrentUser() } returns Result.success(mockUser)
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        viewModel.toggleEditMode()
        viewModel.onDisplayNameChange("Jane Doe")
        viewModel.onBioChange("New bio")
        
        // When
        viewModel.cancelEdit()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isEditMode)
        assertEquals("John Doe", state.editDisplayName) // Reverted to original
        assertEquals("John Doe", state.displayName)
        assertEquals(null, state.saveError)
    }

    @Test
    fun `teacher role should not have grade`() = runTest {
        // Given
        val teacherUser = mockUser.copy(role = UserRole.TEACHER, grade = null)
        coEvery { userRepository.getCurrentUser() } returns Result.success(teacherUser)
        
        // When
        viewModel = ProfileViewModel(userRepository)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("TEACHER", state.role)
        assertEquals(null, state.grade)
    }
}