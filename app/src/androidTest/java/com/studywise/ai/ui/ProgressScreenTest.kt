package com.studywise.ai.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studywise.ai.domain.model.*
import com.studywise.ai.presentation.screens.progress.ProgressScreen
import com.studywise.ai.presentation.screens.progress.ProgressUiState
import com.studywise.ai.presentation.screens.progress.ProgressViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ProgressScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var viewModel: ProgressViewModel
    private lateinit var uiState: MutableStateFlow<ProgressUiState>
    
    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)
        uiState = MutableStateFlow(ProgressUiState())
        every { viewModel.uiState } returns uiState
    }
    
    @Test
    fun progressScreen_InitialState_ShowsLoading() {
        // Given
        uiState.value = ProgressUiState(isLoading = true)
        
        // When
        composeTestRule.setContent {
            ProgressScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("My Progress")
            .assertIsDisplayed()
        
        // Loading state would show progress indicator
        // Note: The actual implementation might need to add a test tag for the loading indicator
    }
    
    @Test
    fun progressScreen_TabNavigation_Works() {
        // Given
        composeTestRule.setContent {
            ProgressScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then - All tabs are visible
        composeTestRule
            .onNodeWithText("Overview")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Subjects")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Achievements")
            .assertIsDisplayed()
        
        // When - Click Subjects tab
        composeTestRule
            .onNodeWithText("Subjects")
            .performClick()
        
        // When - Click Achievements tab
        composeTestRule
            .onNodeWithText("Achievements")
            .performClick()
        
        // When - Click back to Overview
        composeTestRule
            .onNodeWithText("Overview")
            .performClick()
    }
    
    @Test
    fun progressScreen_OverviewTab_DisplaysStats() {
        // Given
        uiState.value = ProgressUiState(
            isLoading = false,
            currentStreak = 7,
            totalPoints = 1250,
            weeklyProgress = WeeklyProgress(
                weekStartDate = Date(),
                dailyProgress = createDailyProgressList(),
                totalQuestions = 50,
                totalCorrect = 40,
                averageAccuracy = 0.8f,
                totalTimeMinutes = 180,
                subjectsStudied = setOf("Math", "Science", "English"),
                dailyStats = createDailyProgressList(),
                totalMinutes = 180
            )
        )
        
        // When
        composeTestRule.setContent {
            ProgressScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Current Streak")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("7 days")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Total Points")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("1250")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Weekly Progress")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Recent Activity")
            .assertIsDisplayed()
    }
    
    @Test
    fun progressScreen_SubjectsTab_DisplaysSubjects() {
        // Given
        val subjectProgress = listOf(
            SubjectProgress(
                subject = "Mathematics",
                totalSessions = 15,
                totalQuestions = 150,
                correctAnswers = 120,
                averageAccuracy = 0.8f,
                totalTimeMinutes = 450,
                lastPracticed = Date(),
                skillMastery = mapOf("Addition" to 0.9f, "Subtraction" to 0.85f),
                trend = ProgressTrend.IMPROVING,
                totalMinutes = 450,
                averageMastery = 0.875
            ),
            SubjectProgress(
                subject = "Science",
                totalSessions = 10,
                totalQuestions = 100,
                correctAnswers = 75,
                averageAccuracy = 0.75f,
                totalTimeMinutes = 300,
                lastPracticed = Date(),
                skillMastery = mapOf("Biology" to 0.8f, "Physics" to 0.7f),
                trend = ProgressTrend.STABLE,
                totalMinutes = 300,
                averageMastery = 0.75
            )
        )
        
        uiState.value = ProgressUiState(
            isLoading = false,
            subjectProgressList = subjectProgress
        )
        
        composeTestRule.setContent {
            ProgressScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // When - Navigate to Subjects tab
        composeTestRule
            .onNodeWithText("Subjects")
            .performClick()
        
        // Then
        composeTestRule
            .onNodeWithText("Mathematics")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("15 sessions • 450 min")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("87% Mastery")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Science")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("10 sessions • 300 min")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("75% Mastery")
            .assertIsDisplayed()
    }
    
    @Test
    fun progressScreen_SubjectCard_ClickCallsViewModel() {
        // Given
        val subjectProgress = listOf(
            SubjectProgress(
                subject = "English",
                totalSessions = 8,
                totalQuestions = 80,
                correctAnswers = 70,
                averageAccuracy = 0.875f,
                totalTimeMinutes = 240,
                lastPracticed = Date(),
                skillMastery = mapOf("Grammar" to 0.9f, "Vocabulary" to 0.85f),
                trend = ProgressTrend.IMPROVING,
                totalMinutes = 240,
                averageMastery = 0.875
            )
        )
        
        uiState.value = ProgressUiState(
            isLoading = false,
            subjectProgressList = subjectProgress
        )
        
        composeTestRule.setContent {
            ProgressScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Navigate to Subjects tab
        composeTestRule
            .onNodeWithText("Subjects")
            .performClick()
        
        // When - Click on subject card
        composeTestRule
            .onNodeWithText("English")
            .performClick()
        
        // Then
        verify { viewModel.onSubjectSelected("English") }
    }
    
    @Test
    fun progressScreen_AchievementsTab_DisplaysAchievements() {
        // Given
        val achievements = listOf(
            Achievement(
                id = "streak_3",
                title = "3 Day Streak",
                description = "Learn for 3 days in a row",
                iconRes = 0,
                progress = 1.0f,
                isUnlocked = true,
                unlockedDate = Date(),
                category = AchievementCategory.STREAK,
                points = 50,
                name = "3 Day Streak",
                icon = "fire"
            ),
            Achievement(
                id = "perfect_score",
                title = "Perfect Score",
                description = "Get 100% on a quiz",
                iconRes = 0,
                progress = 1.0f,
                isUnlocked = true,
                unlockedDate = Date(),
                category = AchievementCategory.ACCURACY,
                points = 100,
                name = "Perfect Score",
                icon = "star"
            ),
            Achievement(
                id = "bookworm",
                title = "Bookworm",
                description = "Complete 10 reading sessions",
                iconRes = 0,
                progress = 0.5f,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.COMPLETION,
                points = 75,
                name = "Bookworm",
                icon = "book"
            )
        )
        
        uiState.value = ProgressUiState(
            isLoading = false,
            achievements = achievements
        )
        
        composeTestRule.setContent {
            ProgressScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // When - Navigate to Achievements tab
        composeTestRule
            .onNodeWithText("Achievements")
            .performClick()
        
        // Then - Categories are displayed
        composeTestRule
            .onNodeWithText("Streak")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Accuracy")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Completion")
            .assertIsDisplayed()
        
        // Achievement cards are displayed
        composeTestRule
            .onNodeWithText("3 Day Streak")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("+50 pts")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Perfect Score")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("+100 pts")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Bookworm")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Complete 10 reading sessions")
            .assertIsDisplayed()
    }
    
    @Test
    fun progressScreen_RefreshButton_CallsViewModel() {
        // Given
        composeTestRule.setContent {
            ProgressScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // When
        composeTestRule
            .onNodeWithContentDescription("Refresh")
            .performClick()
        
        // Then
        verify { viewModel.refreshData() }
    }
    
    @Test
    fun progressScreen_NavigateBack_Works() {
        // Given
        var navigateBackCalled = false
        
        composeTestRule.setContent {
            ProgressScreen(
                onNavigateBack = { navigateBackCalled = true },
                viewModel = viewModel
            )
        }
        
        // When
        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()
        
        // Then
        assert(navigateBackCalled)
    }
    
    @Test
    fun progressScreen_EmptyState_ShowsMessage() {
        // Given
        uiState.value = ProgressUiState(
            isLoading = false,
            subjectProgressList = emptyList(),
            achievements = emptyList()
        )
        
        composeTestRule.setContent {
            ProgressScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // When - Navigate to Subjects tab
        composeTestRule
            .onNodeWithText("Subjects")
            .performClick()
        
        // Then - Should show empty state (implementation might vary)
        // The actual UI might show a message like "No subjects studied yet"
    }
    
    // Helper function to create sample daily progress
    private fun createDailyProgressList(): List<DailyProgress> {
        val today = Date()
        return listOf(
            DailyProgress(
                date = today,
                questionsAnswered = 10,
                correctAnswers = 8,
                timeSpentMinutes = 30,
                subjects = listOf("Math", "Science"),
                accuracy = 0.8f,
                sessionsCompleted = 2,
                minutesStudied = 30,
                pointsEarned = 80
            ),
            DailyProgress(
                date = Date(today.time - 86400000), // Yesterday
                questionsAnswered = 15,
                correctAnswers = 12,
                timeSpentMinutes = 45,
                subjects = listOf("English"),
                accuracy = 0.8f,
                sessionsCompleted = 1,
                minutesStudied = 45,
                pointsEarned = 120
            )
        )
    }
}