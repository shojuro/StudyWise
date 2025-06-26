package com.studywise.ai.ui

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studywise.ai.domain.model.EducationalContent
import com.studywise.ai.presentation.screens.learning.PhotoLearningScreen
import com.studywise.ai.presentation.screens.learning.PhotoLearningUiState
import com.studywise.ai.presentation.screens.learning.PhotoLearningViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoLearningScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var viewModel: PhotoLearningViewModel
    private lateinit var uiState: MutableStateFlow<PhotoLearningUiState>
    
    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)
        uiState = MutableStateFlow(PhotoLearningUiState())
        every { viewModel.uiState } returns uiState
    }
    
    @Test
    fun photoLearningScreen_InitialState_ShowsCameraButton() {
        // Given/When
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Photo Learning")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Take a Photo")
            .assertIsDisplayed()
            .assertIsEnabled()
        
        composeTestRule
            .onNodeWithText("Take a photo of any object to learn about it!")
            .assertIsDisplayed()
    }
    
    @Test
    fun photoLearningScreen_ImageCaptured_ShowsImage() {
        // Given
        val imageUri = mockk<Uri>()
        every { imageUri.toString() } returns "content://test/image.jpg"
        
        uiState.value = PhotoLearningUiState(
            imageUri = imageUri,
            detectedObject = null
        )
        
        // When
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithContentDescription("Captured image")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Analyzing image...")
            .assertIsDisplayed()
    }
    
    @Test
    fun photoLearningScreen_ObjectDetected_ShowsObjectName() {
        // Given
        val imageUri = mockk<Uri>()
        every { imageUri.toString() } returns "content://test/image.jpg"
        
        uiState.value = PhotoLearningUiState(
            imageUri = imageUri,
            detectedObject = "Apple",
            isAnalyzing = false
        )
        
        // When
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("I found: Apple")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Learn More")
            .assertIsDisplayed()
            .assertIsEnabled()
    }
    
    @Test
    fun photoLearningScreen_LowConfidenceDetection_ShowsManualEntry() {
        // Given
        uiState.value = PhotoLearningUiState(
            imageUri = mockk(),
            showManualEntry = true,
            detectionConfidence = 0.65f
        )
        
        // When
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("What's in the photo?")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("I couldn't identify the object with high confidence (65%).")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Enter object name")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Submit")
            .assertIsDisplayed()
    }
    
    @Test
    fun photoLearningScreen_ManualEntry_UpdatesViewModel() {
        // Given
        uiState.value = PhotoLearningUiState(
            showManualEntry = true
        )
        
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // When
        composeTestRule
            .onNodeWithText("Enter object name")
            .performTextInput("Telescope")
        
        composeTestRule
            .onNodeWithText("Submit")
            .performClick()
        
        // Then
        verify {
            viewModel.updateManualObjectName("Telescope")
            viewModel.submitManualEntry()
        }
    }
    
    @Test
    fun photoLearningScreen_EducationalContent_DisplaysCorrectly() {
        // Given
        val content = EducationalContent(
            objectName = "Apple",
            gradeLevel = 3,
            subject = "Science",
            mainContent = "An apple is a delicious fruit that grows on trees.",
            funFact = "Apples float in water because they are 25% air!",
            questions = listOf(
                "What color can apples be?",
                "Where do apples grow?",
                "Why are apples good for you?"
            ),
            suggestedActivities = listOf(
                "Count different colored apples",
                "Draw an apple tree"
            )
        )
        
        uiState.value = PhotoLearningUiState(
            imageUri = mockk(),
            detectedObject = "Apple",
            educationalContent = content,
            isGeneratingContent = false
        )
        
        // When
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Learning About: Apple")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("An apple is a delicious fruit that grows on trees.")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Fun Fact!")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Apples float in water because they are 25% air!")
            .assertIsDisplayed()
        
        // Questions section
        composeTestRule
            .onNodeWithText("Let's Think!")
            .assertIsDisplayed()
        
        content.questions.forEach { question ->
            composeTestRule
                .onNodeWithText(question)
                .assertIsDisplayed()
        }
        
        // Activities section
        composeTestRule
            .onNodeWithText("Try These Activities")
            .assertIsDisplayed()
        
        content.suggestedActivities.forEach { activity ->
            composeTestRule
                .onNodeWithText(activity)
                .assertIsDisplayed()
        }
    }
    
    @Test
    fun photoLearningScreen_LoadingContent_ShowsProgress() {
        // Given
        uiState.value = PhotoLearningUiState(
            imageUri = mockk(),
            detectedObject = "Book",
            isGeneratingContent = true
        )
        
        // When
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithTag("ContentLoadingIndicator")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Creating educational content...")
            .assertIsDisplayed()
    }
    
    @Test
    fun photoLearningScreen_Error_ShowsErrorMessage() {
        // Given
        uiState.value = PhotoLearningUiState(
            error = "Failed to analyze image. Please try again."
        )
        
        // When
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Failed to analyze image. Please try again.")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Try Again")
            .assertIsDisplayed()
            .assertIsEnabled()
    }
    
    @Test
    fun photoLearningScreen_TryNewPhoto_ResetsState() {
        // Given
        uiState.value = PhotoLearningUiState(
            imageUri = mockk(),
            detectedObject = "Calculator",
            educationalContent = mockk()
        )
        
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // When
        composeTestRule
            .onNodeWithText("Try Another Photo")
            .performClick()
        
        // Then
        verify { viewModel.resetState() }
    }
    
    @Test
    fun photoLearningScreen_NavigateBack_Works() {
        // Given
        var navigateBackCalled = false
        
        composeTestRule.setContent {
            PhotoLearningScreen(
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
    fun photoLearningScreen_ShareContent_CallsViewModel() {
        // Given
        val content = EducationalContent(
            objectName = "Keys",
            gradeLevel = 3,
            subject = "Science",
            mainContent = "Keys are tools we use to open locks.",
            funFact = "The oldest key was found in ancient Egypt!",
            questions = listOf("What are keys used for?"),
            suggestedActivities = listOf("Find different types of keys")
        )
        
        uiState.value = PhotoLearningUiState(
            educationalContent = content
        )
        
        composeTestRule.setContent {
            PhotoLearningScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
        
        // When
        composeTestRule
            .onNodeWithContentDescription("Share")
            .performClick()
        
        // Then
        verify { viewModel.shareContent() }
    }
}