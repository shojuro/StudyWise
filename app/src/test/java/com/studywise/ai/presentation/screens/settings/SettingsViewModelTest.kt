package com.studywise.ai.presentation.screens.settings

import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.data.local.preferences.ThemeMode
import com.studywise.ai.data.local.preferences.UserPreferences
import com.studywise.ai.domain.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: SettingsViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    private val userPreferencesFlow = MutableStateFlow(UserPreferences())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        preferencesManager = mockk(relaxed = true) {
            every { userPreferences } returns userPreferencesFlow
        }
        authRepository = mockk()
        
        viewModel = SettingsViewModel(preferencesManager, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should load from preferences`() = runTest {
        // Given
        val preferences = UserPreferences(
            themeMode = ThemeMode.DARK,
            textSize = 1.2f,
            highContrast = true,
            notificationsEnabled = false,
            studyReminderTime = "9:00 AM",
            userEmail = "test@example.com",
            userName = "John Doe",
            userRole = "STUDENT"
        )
        userPreferencesFlow.value = preferences
        
        // When
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals(1.2f, state.textSize)
        assertTrue(state.highContrast)
        assertFalse(state.notificationsEnabled)
        assertEquals("9:00 AM", state.studyReminderTime)
        assertEquals("test@example.com", state.userEmail)
        assertEquals("John Doe", state.userName)
        assertEquals("STUDENT", state.userRole)
    }

    @Test
    fun `onThemeModeChange should update theme and show success message`() = runTest {
        // When
        viewModel.onThemeModeChange(ThemeMode.LIGHT)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(ThemeMode.LIGHT, state.themeMode)
        assertFalse(state.showThemeDialog)
        assertEquals("Theme updated", state.successMessage)
        
        // Verify preferences were updated
        coVerify { preferencesManager.updateThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `onTextSizeChange should update text size`() = runTest {
        // When
        viewModel.onTextSizeChange(1.5f)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(1.5f, state.textSize)
        assertEquals("Text size updated", state.successMessage)
        
        // Verify preferences were updated
        coVerify { preferencesManager.updateTextSize(1.5f) }
    }

    @Test
    fun `onHighContrastChange should toggle high contrast`() = runTest {
        // When enabling
        viewModel.onHighContrastChange(true)
        advanceUntilIdle()
        
        // Then
        var state = viewModel.uiState.first()
        assertTrue(state.highContrast)
        assertEquals("High contrast enabled", state.successMessage)
        
        // When disabling
        viewModel.clearMessage()
        viewModel.onHighContrastChange(false)
        advanceUntilIdle()
        
        // Then
        state = viewModel.uiState.first()
        assertFalse(state.highContrast)
        assertEquals("High contrast disabled", state.successMessage)
        
        // Verify preferences were updated
        coVerify { preferencesManager.updateHighContrast(true) }
        coVerify { preferencesManager.updateHighContrast(false) }
    }

    @Test
    fun `onNotificationsEnabledChange should toggle notifications`() = runTest {
        // When enabling
        viewModel.onNotificationsEnabledChange(true)
        advanceUntilIdle()
        
        // Then
        var state = viewModel.uiState.first()
        assertTrue(state.notificationsEnabled)
        assertEquals("Notifications enabled", state.successMessage)
        
        // When disabling
        viewModel.clearMessage()
        viewModel.onNotificationsEnabledChange(false)
        advanceUntilIdle()
        
        // Then
        state = viewModel.uiState.first()
        assertFalse(state.notificationsEnabled)
        assertEquals("Notifications disabled", state.successMessage)
    }

    @Test
    fun `onDailyRemindersChange should update daily reminders`() = runTest {
        // Given
        _uiState.value = _uiState.value.copy(progressUpdates = true)
        
        // When
        viewModel.onDailyRemindersChange(true)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.dailyReminders)
        assertEquals("Daily reminders enabled", state.successMessage)
        
        // Verify correct parameters passed
        coVerify { 
            preferencesManager.updateNotificationSettings(
                enableDailyReminders = true,
                enableProgressUpdates = true
            )
        }
    }

    @Test
    fun `onStudyReminderTimeChange should update reminder time`() = runTest {
        // Given
        viewModel.showReminderTimeDialog()
        
        // When
        viewModel.onStudyReminderTimeChange("8:00 PM")
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("8:00 PM", state.studyReminderTime)
        assertFalse(state.showReminderTimeDialog)
        assertEquals("Study reminder time updated", state.successMessage)
        
        // Verify preferences were updated
        coVerify { preferencesManager.updateStudyReminderTime("8:00 PM") }
    }

    @Test
    fun `dialog states should be managed correctly`() = runTest {
        // Test theme dialog
        viewModel.showThemeDialog()
        assertTrue(viewModel.uiState.first().showThemeDialog)
        
        viewModel.dismissThemeDialog()
        assertFalse(viewModel.uiState.first().showThemeDialog)
        
        // Test logout dialog
        viewModel.showLogoutDialog()
        assertTrue(viewModel.uiState.first().showLogoutDialog)
        
        viewModel.dismissLogoutDialog()
        assertFalse(viewModel.uiState.first().showLogoutDialog)
        
        // Test reminder time dialog
        viewModel.showReminderTimeDialog()
        assertTrue(viewModel.uiState.first().showReminderTimeDialog)
        
        viewModel.dismissReminderTimeDialog()
        assertFalse(viewModel.uiState.first().showReminderTimeDialog)
    }

    @Test
    fun `clearMessage should clear both success and error messages`() = runTest {
        // Given
        viewModel.onThemeModeChange(ThemeMode.DARK)
        advanceUntilIdle()
        
        // When
        viewModel.clearMessage()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(null, state.successMessage)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `logout success should clear session and dismiss dialog`() = runTest {
        // Given
        viewModel.showLogoutDialog()
        coEvery { authRepository.logout() } just Runs
        coEvery { preferencesManager.clearUserSession() } just Runs
        
        // When
        viewModel.logout()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertFalse(state.showLogoutDialog)
        assertEquals(null, state.errorMessage)
        
        // Verify logout was called
        coVerify { authRepository.logout() }
        coVerify { preferencesManager.clearUserSession() }
    }

    @Test
    fun `logout failure should show error message`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { authRepository.logout() } throws Exception(errorMessage)
        
        // When
        viewModel.logout()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.errorMessage)
        
        // Verify session was not cleared on error
        coVerify(exactly = 0) { preferencesManager.clearUserSession() }
    }

    @Test
    fun `preferences changes should update UI state`() = runTest {
        // Given initial state
        advanceUntilIdle()
        
        // When preferences change
        userPreferencesFlow.value = UserPreferences(
            themeMode = ThemeMode.LIGHT,
            textSize = 0.8f,
            highContrast = false,
            userEmail = "updated@example.com",
            userName = "Jane Doe"
        )
        advanceUntilIdle()
        
        // Then UI state should reflect changes
        val state = viewModel.uiState.first()
        assertEquals(ThemeMode.LIGHT, state.themeMode)
        assertEquals(0.8f, state.textSize)
        assertFalse(state.highContrast)
        assertEquals("updated@example.com", state.userEmail)
        assertEquals("Jane Doe", state.userName)
    }
}