package com.studywise.ai.presentation.screens.auth

import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.domain.model.User
import com.studywise.ai.domain.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: RegisterViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = RegisterViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default values`() = runTest {
        // When
        val state = viewModel.uiState.first()

        // Then
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertEquals(UserRole.STUDENT, state.selectedRole)
        assertEquals(6, state.selectedGrade)
        assertFalse(state.isLoading)
        assertFalse(state.registerSuccess)
        assertEquals(null, state.nameError)
        assertEquals(null, state.emailError)
        assertEquals(null, state.passwordError)
        assertEquals(null, state.confirmPasswordError)
        assertEquals(null, state.generalError)
    }

    @Test
    fun `onNameChange should update name and clear error`() = runTest {
        // Given
        viewModel.onNameChange("")
        viewModel.register() // Trigger validation to set error
        advanceUntilIdle()

        // When
        viewModel.onNameChange("John Doe")
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("John Doe", state.name)
        assertEquals(null, state.nameError)
    }

    @Test
    fun `onEmailChange should update email and clear error`() = runTest {
        // When
        viewModel.onEmailChange("test@example.com")
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("test@example.com", state.email)
        assertEquals(null, state.emailError)
    }

    @Test
    fun `onPasswordChange should update password and validate confirm password`() = runTest {
        // Given
        viewModel.onConfirmPasswordChange("password123")
        
        // When
        viewModel.onPasswordChange("password456")
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("password456", state.password)
        assertEquals(null, state.passwordError)
        assertEquals("Passwords do not match", state.confirmPasswordError)
    }

    @Test
    fun `onPasswordChange should clear confirm password error when passwords match`() = runTest {
        // Given
        viewModel.onConfirmPasswordChange("password123")
        viewModel.onPasswordChange("password456") // Create mismatch first
        
        // When
        viewModel.onPasswordChange("password123") // Now make them match
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("password123", state.password)
        assertEquals(null, state.confirmPasswordError)
    }

    @Test
    fun `onConfirmPasswordChange should validate against password`() = runTest {
        // Given
        viewModel.onPasswordChange("password123")
        
        // When
        viewModel.onConfirmPasswordChange("password456")
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("password456", state.confirmPassword)
        assertEquals("Passwords do not match", state.confirmPasswordError)
    }

    @Test
    fun `onRoleChange should update selected role`() = runTest {
        // When
        viewModel.onRoleChange(UserRole.TEACHER)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(UserRole.TEACHER, state.selectedRole)
    }

    @Test
    fun `onGradeChange should update selected grade`() = runTest {
        // When
        viewModel.onGradeChange(8)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(8, state.selectedGrade)
    }

    @Test
    fun `register with empty name should show error`() = runTest {
        // Given
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        
        // When
        viewModel.register()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("Name is required", state.nameError)
        assertFalse(state.isLoading)
        assertFalse(state.registerSuccess)
        
        // Verify repository was not called
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `register with invalid email should show error`() = runTest {
        // Given
        viewModel.onNameChange("John Doe")
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        
        // When
        viewModel.register()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("Invalid email format", state.emailError)
        
        // Verify repository was not called
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `register with short password should show error`() = runTest {
        // Given
        viewModel.onNameChange("John Doe")
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("12345")
        viewModel.onConfirmPasswordChange("12345")
        
        // When
        viewModel.register()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("Password must be at least 6 characters", state.passwordError)
        
        // Verify repository was not called
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `register with mismatched passwords should show error`() = runTest {
        // Given
        viewModel.onNameChange("John Doe")
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password456")
        
        // When
        viewModel.register()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("Passwords do not match", state.confirmPasswordError)
        
        // Verify repository was not called
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `register with valid student data should succeed`() = runTest {
        // Given
        val mockUser = mockk<User> {
            every { id } returns "user123"
            every { name } returns "John Doe"
            every { email } returns "test@example.com"
        }
        
        viewModel.onNameChange("John Doe")
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.onRoleChange(UserRole.STUDENT)
        viewModel.onGradeChange(7)
        
        coEvery { 
            authRepository.register("John Doe", "test@example.com", "password123", "STUDENT", 7) 
        } returns Result.success(mockUser)
        
        // When
        viewModel.register()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertTrue(state.registerSuccess)
        assertEquals(null, state.generalError)
        
        // Verify repository was called with correct parameters
        coVerify { 
            authRepository.register("John Doe", "test@example.com", "password123", "STUDENT", 7) 
        }
    }

    @Test
    fun `register with valid teacher data should not pass grade`() = runTest {
        // Given
        val mockUser = mockk<User> {
            every { id } returns "teacher123"
            every { name } returns "Jane Teacher"
            every { email } returns "teacher@example.com"
        }
        
        viewModel.onNameChange("Jane Teacher")
        viewModel.onEmailChange("teacher@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.onRoleChange(UserRole.TEACHER)
        
        coEvery { 
            authRepository.register("Jane Teacher", "teacher@example.com", "password123", "TEACHER", null) 
        } returns Result.success(mockUser)
        
        // When
        viewModel.register()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.registerSuccess)
        
        // Verify grade was null for teacher
        coVerify { 
            authRepository.register("Jane Teacher", "teacher@example.com", "password123", "TEACHER", null) 
        }
    }

    @Test
    fun `register failure should show error message`() = runTest {
        // Given
        viewModel.onNameChange("John Doe")
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        
        val errorMessage = "Email already exists"
        coEvery { 
            authRepository.register(any(), any(), any(), any(), any()) 
        } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.register()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertFalse(state.registerSuccess)
        assertEquals(errorMessage, state.generalError)
    }

    @Test
    fun `register should trim whitespace from name and email`() = runTest {
        // Given
        val mockUser = mockk<User>()
        viewModel.onNameChange("  John Doe  ")
        viewModel.onEmailChange("  test@example.com  ")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        
        coEvery { 
            authRepository.register("John Doe", "test@example.com", "password123", "STUDENT", 6) 
        } returns Result.success(mockUser)
        
        // When
        viewModel.register()
        advanceUntilIdle()
        
        // Then
        coVerify { 
            authRepository.register("John Doe", "test@example.com", "password123", "STUDENT", 6) 
        }
    }

    @Test
    fun `multiple validation errors should all be shown`() = runTest {
        // Given - all fields empty
        
        // When
        viewModel.register()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("Name is required", state.nameError)
        assertEquals("Email is required", state.emailError)
        assertEquals("Password is required", state.passwordError)
        assertEquals("Please confirm your password", state.confirmPasswordError)
    }
}