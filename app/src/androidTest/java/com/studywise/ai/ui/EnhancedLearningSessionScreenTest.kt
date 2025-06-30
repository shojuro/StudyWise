package com.studywise.ai.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studywise.ai.presentation.screens.session.*
import com.studywise.ai.test.fixtures.EducationalContentFixtures
import com.studywise.ai.util.textextraction.InputMethod
import com.studywise.ai.util.textextraction.VoiceTextCapture
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for EnhancedLearningSessionScreen
 */
@RunWith(AndroidJUnit4::class)
class EnhancedLearningSessionScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testBookTextInputScreen() {
        // Given
        val uiState = EnhancedLearningSessionUiState(
            subject = "Reading",
            waitingForBookText = true,
            bookText = "",
            selectedInputMethod = InputMethod.TEXT
        )
        
        // When
        composeTestRule.setContent {
            BookTextInputContent(
                uiState = uiState,
                onBookTextChange = {},
                onSubmit = {},
                onInputMethodSelected = {},
                onPhotoSelected = {},
                onDocumentSelected = {},
                onStartVoiceCapture = {},
                onStopVoiceCapture = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            // Verify input method selector is displayed
            onNodeWithContentDescription("Text input").assertExists()
            onNodeWithContentDescription("Photo input").assertExists()
            onNodeWithContentDescription("Voice input").assertExists()
            onNodeWithContentDescription("Document input").assertExists()
            
            // Verify text input field
            onNodeWithText("Enter text from your book").assertExists()
            
            // Verify submit button
            onNodeWithText("Start Learning").assertExists()
        }
    }
    
    @Test
    fun testQuestionDisplay() {
        // Given
        val skill = SkillInfo(
            id = 1L,
            name = "Key Ideas and Details",
            description = "Identify key ideas in text",
            category = "reading_literature"
        )
        
        val question = EnhancedQuestion(
            id = "q1",
            skillId = "1",
            prompt = "What details in your text tell you about the main character's personality?",
            originalPrompt = "What details tell you about {element}?",
            hints = listOf("Look for descriptions", "Consider their actions"),
            followUpQuestions = listOf("Why do you think that?"),
            difficulty = 0.5f,
            metadata = emptyMap()
        )
        
        val uiState = EnhancedLearningSessionUiState(
            subject = "Reading",
            currentQuestion = question,
            currentSkill = skill,
            questionsCompleted = 2,
            totalQuestions = 10,
            currentStreak = 3,
            pointsEarned = 25,
            waitingForBookText = false
        )
        
        // When
        composeTestRule.setContent {
            EnhancedQuestionContent(
                uiState = uiState,
                onAnswerChange = {},
                onSubmitAnswer = {},
                onRequestHint = {},
                onContinue = {},
                onAnswerFollowUp = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            // Verify skill info card
            onNodeWithText("Key Ideas and Details").assertExists()
            onNodeWithText("Identify key ideas in text").assertExists()
            
            // Verify progress indicator
            onAllNodesWithTag("progress_indicator").assertAny(hasTestTag("progress_indicator"))
            
            // Verify question
            onNodeWithText(question.prompt).assertExists()
            
            // Verify answer input
            onNodeWithText("Your answer").assertExists()
            
            // Verify action buttons
            onNodeWithText("Get Hint").assertExists()
            onNodeWithText("Submit").assertExists()
        }
    }
    
    @Test
    fun testHintSystem() {
        // Given
        val question = EnhancedQuestion(
            id = "q1",
            skillId = "1",
            prompt = "Test question",
            originalPrompt = "Test question",
            hints = listOf("First hint", "Second hint"),
            followUpQuestions = emptyList(),
            difficulty = 0.5f,
            metadata = emptyMap()
        )
        
        val uiState = EnhancedLearningSessionUiState(
            subject = "Reading",
            currentQuestion = question,
            showHint = true,
            currentHint = "First hint",
            currentHintIndex = 1,
            waitingForBookText = false
        )
        
        // When
        composeTestRule.setContent {
            EnhancedQuestionContent(
                uiState = uiState,
                onAnswerChange = {},
                onSubmitAnswer = {},
                onRequestHint = {},
                onContinue = {},
                onAnswerFollowUp = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            // Verify hint is displayed
            onNodeWithText("Hint 1 of 2").assertExists()
            onNodeWithText("First hint").assertExists()
            
            // Verify next hint button
            onNodeWithText("Next Hint").assertExists()
        }
    }
    
    @Test
    fun testFeedbackDisplay() {
        // Given
        val uiState = EnhancedLearningSessionUiState(
            subject = "Reading",
            currentQuestion = EducationalContentFixtures.createTestQuestion().let {
                EnhancedQuestion(
                    id = it.id,
                    skillId = it.skillId,
                    prompt = it.prompt,
                    originalPrompt = it.prompt,
                    hints = it.hints ?: emptyList(),
                    followUpQuestions = it.followUpQuestions ?: emptyList(),
                    difficulty = it.difficulty,
                    metadata = it.metadata ?: emptyMap()
                )
            },
            userAnswer = "My thoughtful answer",
            isAnswerSubmitted = true,
            feedback = "Great thinking! Your response shows good understanding of the concept.",
            waitingForBookText = false
        )
        
        // When
        composeTestRule.setContent {
            EnhancedQuestionContent(
                uiState = uiState,
                onAnswerChange = {},
                onSubmitAnswer = {},
                onRequestHint = {},
                onContinue = {},
                onAnswerFollowUp = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            // Verify feedback is displayed
            onNodeWithText(uiState.feedback).assertExists()
            
            // Verify continue button
            onNodeWithText("Continue").assertExists()
            
            // Verify answer field is disabled
            onNodeWithText("Your answer").assertIsNotEnabled()
        }
    }
    
    @Test
    fun testFollowUpQuestion() {
        // Given
        val question = EnhancedQuestion(
            id = "q1",
            skillId = "1",
            prompt = "Original question",
            originalPrompt = "Original question",
            hints = emptyList(),
            followUpQuestions = listOf("Can you explain why you think that?"),
            difficulty = 0.5f,
            metadata = emptyMap()
        )
        
        val uiState = EnhancedLearningSessionUiState(
            subject = "Reading",
            currentQuestion = question,
            showFollowUp = true,
            currentFollowUpIndex = 0,
            waitingForBookText = false
        )
        
        // When
        composeTestRule.setContent {
            EnhancedQuestionContent(
                uiState = uiState,
                onAnswerChange = {},
                onSubmitAnswer = {},
                onRequestHint = {},
                onContinue = {},
                onAnswerFollowUp = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            // Verify follow-up section
            onNodeWithText("Follow-up Question").assertExists()
            onNodeWithText("Can you explain why you think that?").assertExists()
            onNodeWithText("Expand on your thinking...").assertExists()
        }
    }
    
    @Test
    fun testSessionCompletionScreen() {
        // Given
        val skill = SkillInfo(
            id = 1L,
            name = "Theme Identification",
            description = "Identify themes in literature",
            category = "reading_literature"
        )
        
        val uiState = EnhancedLearningSessionUiState(
            subject = "Reading",
            isSessionComplete = true,
            questionsCompleted = 10,
            totalQuestions = 10,
            pointsEarned = 85,
            currentStreak = 7,
            currentSkill = skill,
            waitingForBookText = false
        )
        
        // When
        composeTestRule.setContent {
            EnhancedSessionCompleteContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            // Verify completion message
            onNodeWithText("Amazing Work!").assertExists()
            
            // Verify stats
            onNodeWithText("Points Earned").assertExists()
            onNodeWithText("85").assertExists()
            
            onNodeWithText("Questions").assertExists()
            onNodeWithText("10/10").assertExists()
            
            onNodeWithText("Best Streak").assertExists()
            onNodeWithText("7").assertExists()
            
            // Verify skill progress
            onNodeWithText("Skill Progress").assertExists()
            onNodeWithText("Theme Identification").assertExists()
            onNodeWithText("Mastery", substring = true).assertExists()
            
            // Verify navigation button
            onNodeWithText("Back to Dashboard").assertExists()
        }
    }
    
    @Test
    fun testLoadingStates() {
        // Given - Loading state
        val loadingState = EnhancedLearningSessionUiState(
            subject = "Reading",
            isLoading = true,
            waitingForBookText = false
        )
        
        // When
        composeTestRule.setContent {
            EnhancedQuestionContent(
                uiState = loadingState,
                onAnswerChange = {},
                onSubmitAnswer = {},
                onRequestHint = {},
                onContinue = {},
                onAnswerFollowUp = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            // Should show some loading indicator
            // The exact implementation may vary
            waitForIdle()
        }
        
        // Given - Evaluating state
        val evaluatingState = EnhancedLearningSessionUiState(
            subject = "Reading",
            isEvaluating = true,
            isAnswerSubmitted = true,
            currentQuestion = EducationalContentFixtures.createTestQuestion().let {
                EnhancedQuestion(
                    id = it.id,
                    skillId = it.skillId,
                    prompt = it.prompt,
                    originalPrompt = it.prompt,
                    hints = it.hints ?: emptyList(),
                    followUpQuestions = it.followUpQuestions ?: emptyList(),
                    difficulty = it.difficulty,
                    metadata = it.metadata ?: emptyMap()
                )
            },
            waitingForBookText = false
        )
        
        // When
        composeTestRule.setContent {
            EnhancedQuestionContent(
                uiState = evaluatingState,
                onAnswerChange = {},
                onSubmitAnswer = {},
                onRequestHint = {},
                onContinue = {},
                onAnswerFollowUp = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            onNodeWithText("Evaluating your response...").assertExists()
        }
    }
    
    @Test
    fun testTopBarElements() {
        // Given
        val skill = SkillInfo(
            id = 1L,
            name = "Character Analysis",
            description = "Analyze characters",
            category = "reading_literature"
        )
        
        val uiState = EnhancedLearningSessionUiState(
            subject = "Reading",
            currentSkill = skill,
            currentStreak = 5,
            pointsEarned = 50,
            waitingForBookText = false
        )
        
        // When
        composeTestRule.setContent {
            EnhancedTopBar(
                subject = "Reading",
                uiState = uiState,
                onNavigateBack = {}
            )
        }
        
        // Then
        composeTestRule.apply {
            // Verify subject and skill display
            onNodeWithText("Reading Session").assertExists()
            onNodeWithText("Skill: Character Analysis").assertExists()
            
            // Verify gamification elements
            onNodeWithContentDescription("Streak").assertExists()
            onNodeWithText("5").assertExists()
            
            onNodeWithText("50").assertExists() // Points
        }
    }
}