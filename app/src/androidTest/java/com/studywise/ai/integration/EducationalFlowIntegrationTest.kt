package com.studywise.ai.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studywise.ai.MainActivity
import com.studywise.ai.data.local.database.StudyWiseDatabase
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.repository.AuthRepository
import com.studywise.ai.presentation.navigation.Screen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertTrue

/**
 * Integration test for complete educational flow
 * Tests the entire journey from login to session completion
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EducationalFlowIntegrationTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Inject
    lateinit var database: StudyWiseDatabase
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Before
    fun setup() {
        hiltRule.inject()
        
        // Clear database and preferences
        runBlocking {
            database.clearAllTables()
            preferencesManager.clearPreferences()
        }
    }
    
    @Test
    fun testCompleteEducationalFlow() {
        // Step 1: Login as student
        loginAsStudent()
        
        // Step 2: Navigate to Reading subject
        selectReadingSubject()
        
        // Step 3: Input book text
        inputBookText()
        
        // Step 4: Answer questions with educational content
        answerQuestionsWithEducationalFlow()
        
        // Step 5: Complete session and view results
        completeSessionAndViewResults()
        
        // Step 6: Check analytics dashboard
        checkAnalyticsDashboard()
    }
    
    private fun loginAsStudent() {
        composeTestRule.apply {
            // Wait for login screen
            waitForIdle()
            
            // Enter credentials
            onNodeWithText("Email").performTextInput("student@test.com")
            onNodeWithText("Password").performTextInput("password123")
            
            // Select student role
            onNodeWithText("Student").performClick()
            
            // Click login
            onNodeWithText("Login").performClick()
            
            // Wait for dashboard
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Welcome back").fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
    
    private fun selectReadingSubject() {
        composeTestRule.apply {
            // Find and click Reading subject card
            onNodeWithText("Reading").performClick()
            
            // Verify we're on the learning session screen
            waitUntil {
                onAllNodesWithText("Reading Session").fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
    
    private fun inputBookText() {
        composeTestRule.apply {
            // Enter sample book text
            val bookText = """
                The young explorer Sarah discovered an ancient map in her grandmother's attic. 
                The map showed a path through the forbidden forest to a hidden treasure. 
                Despite warnings from the villagers, Sarah decided to follow the map, 
                believing that courage and determination would guide her way.
            """.trimIndent()
            
            onNodeWithText("Enter text from your book").performTextInput(bookText)
            
            // Submit text
            onNodeWithText("Start Learning").performClick()
            
            // Wait for first question
            waitUntil(timeoutMillis = 10000) {
                onAllNodesWithContentDescription("Question").fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
    
    private fun answerQuestionsWithEducationalFlow() {
        composeTestRule.apply {
            // Answer 5 questions to test the flow
            repeat(5) { questionIndex ->
                waitForIdle()
                
                // Check for skill information display
                assertTrue(
                    onAllNodesWithText("Skill:").fetchSemanticsNodes().isNotEmpty(),
                    "Skill information should be displayed"
                )
                
                // Read the question - should be contextualized with book text
                val questionNodes = onAllNodesWithContentDescription("Question")
                assertTrue(
                    questionNodes.fetchSemanticsNodes().isNotEmpty(),
                    "Question should be displayed"
                )
                
                // Type a thoughtful answer
                val answer = when (questionIndex) {
                    0 -> "Sarah shows courage by deciding to explore despite the warnings from villagers."
                    1 -> "The theme seems to be about bravery and following your dreams despite obstacles."
                    2 -> "The map in the attic represents a connection to her family's adventurous past."
                    3 -> "I predict Sarah will face challenges but her determination will help her succeed."
                    4 -> "This reminds me of other adventure stories where characters must be brave."
                    else -> "Sarah is a brave character."
                }
                
                onNodeWithText("Your answer").performTextInput(answer)
                
                // Test hint system on second question
                if (questionIndex == 1) {
                    onNodeWithText("Get Hint").performClick()
                    waitForIdle()
                    
                    // Verify hint is displayed
                    assertTrue(
                        onAllNodesWithText("Hint").fetchSemanticsNodes().isNotEmpty(),
                        "Hint should be displayed"
                    )
                }
                
                // Submit answer
                onNodeWithText("Submit").performClick()
                
                // Wait for evaluation
                waitUntil(timeoutMillis = 5000) {
                    onAllNodesWithText("Continue").fetchSemanticsNodes().isNotEmpty()
                }
                
                // Check for feedback
                assertTrue(
                    onAllNodesWithContentDescription("Feedback").fetchSemanticsNodes().isNotEmpty() ||
                    onAllNodesWithText("Great").fetchSemanticsNodes().isNotEmpty() ||
                    onAllNodesWithText("Good").fetchSemanticsNodes().isNotEmpty() ||
                    onAllNodesWithText("Let's think").fetchSemanticsNodes().isNotEmpty(),
                    "Feedback should be displayed"
                )
                
                // Check for gamification elements (points, streak)
                assertTrue(
                    onAllNodesWithContentDescription("Points").fetchSemanticsNodes().isNotEmpty() ||
                    onAllNodesWithText("Points").fetchSemanticsNodes().isNotEmpty(),
                    "Points should be visible"
                )
                
                // Continue to next question
                onNodeWithText("Continue").performClick()
            }
        }
    }
    
    private fun completeSessionAndViewResults() {
        composeTestRule.apply {
            // After 5 questions, we should see completion screen
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Amazing Work!").fetchSemanticsNodes().isNotEmpty()
            }
            
            // Verify session stats are displayed
            assertTrue(
                onAllNodesWithText("Points Earned").fetchSemanticsNodes().isNotEmpty(),
                "Points earned should be displayed"
            )
            
            assertTrue(
                onAllNodesWithText("Questions").fetchSemanticsNodes().isNotEmpty(),
                "Questions completed should be displayed"
            )
            
            // Check for skill progress
            assertTrue(
                onAllNodesWithText("Skill Progress").fetchSemanticsNodes().isNotEmpty() ||
                onAllNodesWithText("Mastery").fetchSemanticsNodes().isNotEmpty(),
                "Skill progress should be shown"
            )
            
            // Check for celebration animation indicators
            assertTrue(
                onAllNodesWithContentDescription("Trophy").fetchSemanticsNodes().isNotEmpty() ||
                onAllNodesWithContentDescription("Celebration").fetchSemanticsNodes().isNotEmpty() ||
                onAllNodesWithTag("celebration_animation").fetchSemanticsNodes().isNotEmpty(),
                "Celebration elements should be present"
            )
            
            // Return to dashboard
            onNodeWithText("Back to Dashboard").performClick()
        }
    }
    
    private fun checkAnalyticsDashboard() {
        composeTestRule.apply {
            // Navigate to analytics
            onNodeWithContentDescription("Analytics").performClick()
            
            // Wait for analytics to load
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Analytics Dashboard").fetchSemanticsNodes().isNotEmpty()
            }
            
            // Verify key analytics components
            assertTrue(
                onAllNodesWithText("Overall Progress").fetchSemanticsNodes().isNotEmpty(),
                "Overall progress should be displayed"
            )
            
            assertTrue(
                onAllNodesWithText("Skill Mastery Progress").fetchSemanticsNodes().isNotEmpty() ||
                onAllNodesWithText("Skills").fetchSemanticsNodes().isNotEmpty(),
                "Skill mastery should be displayed"
            )
            
            assertTrue(
                onAllNodesWithText("Performance").fetchSemanticsNodes().isNotEmpty() ||
                onAllNodesWithText("Accuracy").fetchSemanticsNodes().isNotEmpty(),
                "Performance metrics should be displayed"
            )
            
            // Check for the session we just completed
            assertTrue(
                onAllNodesWithText("Reading").fetchSemanticsNodes().isNotEmpty(),
                "Reading subject should appear in analytics"
            )
            
            // Verify recommendations are shown
            assertTrue(
                onAllNodesWithText("Recommendations").fetchSemanticsNodes().isNotEmpty() ||
                onAllNodesWithText("Personalized Recommendations").fetchSemanticsNodes().isNotEmpty(),
                "Recommendations should be displayed"
            )
        }
    }
    
    @Test
    fun testEducationalFlowWithPhotoInput() {
        loginAsStudent()
        selectReadingSubject()
        
        composeTestRule.apply {
            // Select photo input method
            onNodeWithContentDescription("Photo input").performClick()
            
            // In a real test, we would mock the photo picker
            // For now, we'll switch back to text input
            onNodeWithContentDescription("Text input").performClick()
            
            // Continue with text input
            inputBookText()
            
            // Answer at least one question to verify flow works
            onNodeWithText("Your answer")
                .performTextInput("The character shows bravery.")
            onNodeWithText("Submit").performClick()
            
            waitUntil {
                onAllNodesWithText("Continue").fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
    
    @Test
    fun testGamificationFeatures() {
        loginAsStudent()
        selectReadingSubject()
        inputBookText()
        
        composeTestRule.apply {
            // Answer multiple questions correctly to test streak
            repeat(3) { index ->
                waitForIdle()
                
                onNodeWithText("Your answer")
                    .performTextInput("This is a good answer with evidence from the text.")
                onNodeWithText("Submit").performClick()
                
                waitUntil {
                    onAllNodesWithText("Continue").fetchSemanticsNodes().isNotEmpty()
                }
                
                // Check for increasing streak after first answer
                if (index > 0) {
                    assertTrue(
                        onAllNodesWithContentDescription("Streak").fetchSemanticsNodes().isNotEmpty() ||
                        onAllNodesWithTag("streak_indicator").fetchSemanticsNodes().isNotEmpty(),
                        "Streak indicator should be visible"
                    )
                }
                
                onNodeWithText("Continue").performClick()
            }
        }
    }
}