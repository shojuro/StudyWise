package com.studywise.ai.presentation.screens.auth

import com.studywise.ai.data.local.entity.UserRole
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.domain.model.User
import com.studywise.ai.domain.repository.AuthRepository
import com.studywise.ai.domain.service.AnalyticsService
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
class LoginViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var analyticsService: AnalyticsService
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        analyticsService = mockk(relaxed = true)
        viewModel = LoginViewModel(authRepository, analyticsService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty fields and no errors`() = runTest {
        // Given & When
        val state = viewModel.uiState.first()

        // Then
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(null, state.emailError)
        assertEquals(null, state.passwordError)
        assertEquals(null, state.generalError)
        assertFalse(state.isLoading)
        assertFalse(state.loginSuccess)
    }

    @Test
    fun `onEmailChange should update email and clear email error`() = runTest {
        // Given
        val newEmail = "test@example.com"

        // When
        viewModel.onEmailChange(newEmail)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(newEmail, state.email)
        assertEquals(null, state.emailError)
    }

    @Test
    fun `onPasswordChange should update password and clear password error`() = runTest {
        // Given
        val newPassword = "password123"

        // When
        viewModel.onPasswordChange(newPassword)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(newPassword, state.password)
        assertEquals(null, state.passwordError)
    }

    @Test
    fun `login with empty email should show email error`() = runTest {
        // Given
        viewModel.onPasswordChange("password123")

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals("Email is required", state.emailError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `login with invalid email should show email error`() = runTest {
        // Given
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals("Invalid email format", state.emailError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `login with empty password should show password error`() = runTest {
        // Given
        viewModel.onEmailChange("test@example.com")

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals("Password is required", state.passwordError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `successful login should update state and log analytics`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val user = User(
            id = "user123",
            email = email,
            name = "Test User",
            role = UserRole.STUDENT,
            schoolId = "school123",
            grade = 6
        )

        coEvery { authRepository.login(email, password) } returns Result.success(user)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertTrue(state.loginSuccess)
        assertEquals(UserRole.STUDENT, state.userRole)
        assertEquals(null, state.generalError)

        // Verify analytics
        verify {
            analyticsService.logEvent(match<AnalyticsEvent.FeatureUsed> {
                it.featureName == "login_success" && it.userId == user.id
            })
            analyticsService.setUserId(user.id)
            analyticsService.setUserProperty("user_role", UserRole.STUDENT.name)
        }
    }

    @Test
    fun `failed login should show error and log analytics`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"

        coEvery { authRepository.login(email, password) } returns Result.failure(Exception(errorMessage))

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        // When
        viewModel.login()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertFalse(state.loginSuccess)
        assertEquals(errorMessage, state.generalError)

        // Verify analytics
        verify {
            analyticsService.logEvent(match<AnalyticsEvent.ErrorOccurred> {
                it.errorType == "login_failed" && 
                it.errorMessage == errorMessage &&
                it.screen == "login"
            })
        }
    }
}