package com.studywise.ai.presentation.screens.learning

import android.net.Uri
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.domain.model.IdentifiedObject
import com.studywise.ai.domain.model.SocraticLesson
import com.studywise.ai.domain.model.User
import com.studywise.ai.domain.repository.AIRepository
import com.studywise.ai.domain.repository.UserRepository
import com.studywise.ai.domain.service.AnalyticsService
import com.studywise.ai.utils.AudioRecorder
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoLearningViewModelTest {

    private lateinit var aiRepository: AIRepository
    private lateinit var userRepository: UserRepository
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var analyticsService: AnalyticsService
    private lateinit var viewModel: PhotoLearningViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        aiRepository = mockk()
        userRepository = mockk()
        audioRecorder = mockk(relaxed = true)
        analyticsService = mockk(relaxed = true)
        
        // Mock user repository to return a user with grade
        coEvery { userRepository.getCurrentUser() } returns Result.success(
            mockk<User> { every { grade } returns 6 }
        )
        
        viewModel = PhotoLearningViewModel(aiRepository, userRepository, audioRecorder, analyticsService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should load user grade`() = runTest {
        // When
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        // Then
        assertEquals(6, state.currentGrade)
        assertFalse(state.isAnalyzing)
        assertEquals(null, state.identifiedObject)
        assertFalse(state.lessonStarted)
    }

    @Test
    fun `analyzePhoto should identify object and log analytics`() = runTest {
        // Given
        val photoUri = mockk<Uri>()
        val identifiedObject = IdentifiedObject(
            name = "Apple",
            description = "A red fruit",
            category = "Food",
            confidence = 0.95f,
            educationalValue = "Great for learning about healthy foods"
        )
        val gradedSentences = mapOf(
            2 to listOf("I see an apple.", "The apple is red."),
            3 to listOf("The apple is a healthy fruit.", "I ate an apple for lunch.")
        )

        coEvery { aiRepository.identifyObject(photoUri) } returns Result.success(identifiedObject)
        coEvery { 
            aiRepository.generateGradedSentences("Apple", 2, 12) 
        } returns Result.success(gradedSentences)

        // When
        viewModel.analyzePhoto(photoUri)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isAnalyzing)
        assertEquals(identifiedObject, state.identifiedObject)
        assertEquals(gradedSentences, state.gradedSentences)
        assertTrue(state.showGradeSelector)

        // Verify analytics
        verify {
            analyticsService.logEvent(match<AnalyticsEvent.PhotoAnalyzed> {
                it.objectName == "Apple" && it.confidence == 0.95f
            })
        }
    }

    @Test
    fun `analyzePhoto failure should show error`() = runTest {
        // Given
        val photoUri = mockk<Uri>()
        val errorMessage = "Failed to process image"
        coEvery { aiRepository.identifyObject(photoUri) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.analyzePhoto(photoUri)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isAnalyzing)
        assertEquals("Failed to identify object: $errorMessage", state.error)
        assertEquals(null, state.identifiedObject)
    }

    @Test
    fun `selectGrade should update current grade`() = runTest {
        // Given
        val newGrade = 8

        // When
        viewModel.selectGrade(newGrade)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(newGrade, state.currentGrade)
        assertFalse(state.showGradeSelector)
    }

    @Test
    fun `startLesson should create Socratic lesson and log analytics`() = runTest {
        // Given
        val objectName = "Apple"
        val identifiedObject = IdentifiedObject(
            name = objectName,
            description = "A red fruit",
            category = "Food",
            confidence = 0.95f,
            educationalValue = "Great for learning"
        )
        val lesson = mockk<SocraticLesson> {
            every { initialQuestion } returns "What do you notice about this apple?"
        }

        // Set up initial state with identified object
        viewModel.analyzePhoto(mockk())
        coEvery { aiRepository.identifyObject(any()) } returns Result.success(identifiedObject)
        advanceUntilIdle()

        coEvery { 
            aiRepository.createSocraticLesson(objectName, 6, 5) 
        } returns Result.success(lesson)

        // When
        viewModel.startLesson()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.lessonStarted)
        assertEquals(lesson, state.socraticLesson)
        assertEquals(1, state.conversationHistory.size)
        assertEquals(Speaker.AI, state.conversationHistory.first().speaker)
        assertEquals(lesson.initialQuestion, state.conversationHistory.first().text)

        // Verify analytics
        verify {
            analyticsService.logEvent(match<AnalyticsEvent.SocraticLessonStarted> {
                it.objectName == objectName && it.grade == 6
            })
        }
    }

    @Test
    fun `startRecording should update state when successful`() = runTest {
        // Given
        every { audioRecorder.startRecording() } returns Result.success(Unit)

        // When
        viewModel.startRecording()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isRecording)
        assertEquals(null, state.error)
    }

    @Test
    fun `stopRecording should transcribe audio and process response`() = runTest {
        // Given
        val audioFile = mockk<File>()
        val transcribedText = "I think it's red because apples are usually red"
        
        every { audioRecorder.stopRecording() } returns Result.success(audioFile)
        coEvery { aiRepository.transcribeAudio(audioFile) } returns Result.success(transcribedText)

        // Start recording first
        every { audioRecorder.startRecording() } returns Result.success(Unit)
        viewModel.startRecording()
        advanceUntilIdle()

        // When
        viewModel.stopRecording()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isRecording)
        
        // Verify transcription was processed
        coVerify { aiRepository.transcribeAudio(audioFile) }
    }

    @Test
    fun `resetLesson should clear all state`() = runTest {
        // Given - Set up some state
        viewModel.selectGrade(8)
        advanceUntilIdle()

        // When
        viewModel.resetLesson()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(null, state.identifiedObject)
        assertEquals(emptyMap<Int, List<String>>(), state.gradedSentences)
        assertFalse(state.lessonStarted)
        assertEquals(8, state.currentGrade) // Grade should be preserved
        
        // Verify cleanup
        verify { audioRecorder.release() }
    }
}