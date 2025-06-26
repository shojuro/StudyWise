package com.studywise.ai.presentation.screens.dashboard

import com.studywise.ai.data.local.dao.ProgressDao
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.data.local.preferences.UserPreferences
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
class StudentDashboardViewModelTest {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var progressDao: ProgressDao
    private lateinit var viewModel: StudentDashboardViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    private val userPreferencesFlow = MutableStateFlow(UserPreferences())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        preferencesManager = mockk {
            every { userPreferences } returns userPreferencesFlow
        }
        progressDao = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // When
        viewModel = StudentDashboardViewModel(preferencesManager, progressDao)
        
        // Then - check immediate state
        val initialState = viewModel.uiState.value
        assertTrue(initialState.isLoading)
        assertEquals("", initialState.userName)
        assertTrue(initialState.subjects.isEmpty())
    }

    @Test
    fun `should load user name from preferences`() = runTest {
        // Given
        userPreferencesFlow.value = UserPreferences(userName = "John Doe")
        
        // When
        viewModel = StudentDashboardViewModel(preferencesManager, progressDao)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("John Doe", state.userName)
    }

    @Test
    fun `should use default name when preferences has no name`() = runTest {
        // Given
        userPreferencesFlow.value = UserPreferences(userName = null)
        
        // When
        viewModel = StudentDashboardViewModel(preferencesManager, progressDao)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("Student", state.userName)
    }

    @Test
    fun `should load subject progress on initialization`() = runTest {
        // When
        viewModel = StudentDashboardViewModel(preferencesManager, progressDao)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(4, state.subjects.size)
        
        // Verify English subject
        val english = state.subjects[0]
        assertEquals("English", english.subject)
        assertEquals(0.65f, english.progress)
        assertEquals("Today", english.lastPracticed)
        assertEquals(450, english.totalPoints)
        
        // Verify Mathematics subject
        val math = state.subjects[1]
        assertEquals("Mathematics", math.subject)
        assertEquals(0.45f, math.progress)
        assertEquals("Yesterday", math.lastPracticed)
        assertEquals(320, math.totalPoints)
        
        // Verify Science subject
        val science = state.subjects[2]
        assertEquals("Science", science.subject)
        assertEquals(0.80f, science.progress)
        assertEquals("2 days ago", science.lastPracticed)
        assertEquals(580, science.totalPoints)
        
        // Verify History subject
        val history = state.subjects[3]
        assertEquals("History", history.subject)
        assertEquals(0.30f, history.progress)
        assertEquals(null, history.lastPracticed)
        assertEquals(150, history.totalPoints)
    }

    @Test
    fun `should update user name when preferences change`() = runTest {
        // Given
        userPreferencesFlow.value = UserPreferences(userName = "Initial Name")
        viewModel = StudentDashboardViewModel(preferencesManager, progressDao)
        advanceUntilIdle()
        
        // When
        userPreferencesFlow.value = UserPreferences(userName = "Updated Name")
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("Updated Name", state.userName)
    }

    @Test
    fun `subjects should be ordered correctly`() = runTest {
        // When
        viewModel = StudentDashboardViewModel(preferencesManager, progressDao)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        val subjectNames = state.subjects.map { it.subject }
        assertEquals(
            listOf("English", "Mathematics", "Science", "History"),
            subjectNames
        )
    }

    @Test
    fun `progress values should be between 0 and 1`() = runTest {
        // When
        viewModel = StudentDashboardViewModel(preferencesManager, progressDao)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        state.subjects.forEach { subject ->
            assertTrue(subject.progress >= 0f && subject.progress <= 1f,
                "Progress for ${subject.subject} should be between 0 and 1")
        }
    }
}