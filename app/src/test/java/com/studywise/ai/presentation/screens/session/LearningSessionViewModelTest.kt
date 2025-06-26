package com.studywise.ai.presentation.screens.session

import androidx.lifecycle.SavedStateHandle
import com.studywise.ai.data.local.dao.LearningSessionDao
import com.studywise.ai.data.local.dao.ProgressDao
import com.studywise.ai.data.local.entity.LearningSessionEntity
import com.studywise.ai.data.local.entity.SessionStatus
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.data.local.preferences.UserPreferences
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.domain.model.Question
import com.studywise.ai.domain.model.QuestionDifficulty
import com.studywise.ai.domain.repository.QuestionRepository
import com.studywise.ai.domain.service.AnalyticsService
import com.studywise.ai.util.textextraction.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LearningSessionViewModelTest {

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var questionRepository: QuestionRepository
    private lateinit var learningSessionDao: LearningSessionDao
    private lateinit var progressDao: ProgressDao
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var photoTextExtractor: PhotoTextExtractor
    private lateinit var documentTextExtractor: DocumentTextExtractor
    private lateinit var voiceTextCapture: VoiceTextCapture
    private lateinit var analyticsService: AnalyticsService
    private lateinit var viewModel: LearningSessionViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    private val userPreferencesFlow = MutableStateFlow(UserPreferences(userId = "user123"))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        savedStateHandle = SavedStateHandle(mapOf("subject" to "English"))
        questionRepository = mockk()
        learningSessionDao = mockk(relaxed = true)
        progressDao = mockk(relaxed = true)
        preferencesManager = mockk {
            every { userPreferences } returns userPreferencesFlow
        }
        photoTextExtractor = mockk()
        documentTextExtractor = mockk()
        voiceTextCapture = mockk(relaxed = true)
        analyticsService = mockk(relaxed = true)
        
        viewModel = LearningSessionViewModel(
            savedStateHandle,
            questionRepository,
            learningSessionDao,
            progressDao,
            preferencesManager,
            photoTextExtractor,
            documentTextExtractor,
            voiceTextCapture,
            analyticsService
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have subject from saved state`() = runTest {
        // When
        val state = viewModel.uiState.first()

        // Then
        assertEquals("English", state.subject)
        assertTrue(state.waitingForBookText)
        assertFalse(state.isLoading)
        assertEquals("", state.bookText)
    }

    @Test
    fun `onBookTextSubmitted should start session when user is loaded`() = runTest {
        // Given
        val bookText = "This is a sample text from a book."
        val question = Question(
            id = "q1",
            text = "What is the main idea?",
            correctAnswer = "The main idea",
            hints = listOf("Think about the topic"),
            explanation = "Good explanation",
            skillId = "skill1",
            difficulty = QuestionDifficulty.EASY
        )
        
        coEvery { 
            questionRepository.generateQuestion(any(), any(), any(), any()) 
        } returns Result.success(question)

        // When
        viewModel.onBookTextSubmitted(bookText)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(bookText, state.bookText)
        assertFalse(state.waitingForBookText)
        assertEquals(question, state.currentQuestion)
        
        // Verify session was created
        coVerify { 
            learningSessionDao.insertSession(match { 
                it.userId == "user123" && it.subject == "English" 
            })
        }
        
        // Verify analytics
        verify {
            analyticsService.logEvent(match<AnalyticsEvent.SessionStarted> {
                it.subject == "English" && it.userId == "user123"
            })
        }
    }

    @Test
    fun `submitAnswer should evaluate answer and log analytics`() = runTest {
        // Given - Set up a session with a question
        val question = Question(
            id = "q1",
            text = "What is 2+2?",
            correctAnswer = "4",
            hints = listOf("Add the numbers"),
            explanation = "2+2 equals 4",
            skillId = "math_addition",
            difficulty = QuestionDifficulty.EASY
        )
        
        coEvery { 
            questionRepository.generateQuestion(any(), any(), any(), any()) 
        } returns Result.success(question)
        
        viewModel.onBookTextSubmitted("Math problems")
        advanceUntilIdle()
        
        viewModel.onAnswerChange("4")

        // When
        viewModel.submitAnswer()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isAnswerSubmitted)
        assertTrue(state.feedback.contains("Excellent"))
        assertEquals(1, state.currentStreak)
        assertEquals(10, state.pointsEarned) // 10 points for correct answer without hint
        
        // Verify analytics
        verify {
            analyticsService.logEvent(match<AnalyticsEvent.QuestionAnswered> {
                it.subject == "English" && 
                it.skillId == "math_addition" && 
                it.isCorrect
            })
        }
    }

    @Test
    fun `incorrect answer should reset streak and show explanation`() = runTest {
        // Given
        val question = Question(
            id = "q1",
            text = "What is 2+2?",
            correctAnswer = "4",
            hints = listOf("Add the numbers"),
            explanation = "2+2 equals 4",
            skillId = "math_addition",
            difficulty = QuestionDifficulty.EASY
        )
        
        coEvery { 
            questionRepository.generateQuestion(any(), any(), any(), any()) 
        } returns Result.success(question)
        
        viewModel.onBookTextSubmitted("Math problems")
        advanceUntilIdle()
        
        viewModel.onAnswerChange("5") // Wrong answer

        // When
        viewModel.submitAnswer()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isAnswerSubmitted)
        assertTrue(state.feedback.contains("Not quite"))
        assertTrue(state.feedback.contains("4")) // Shows correct answer
        assertEquals(0, state.currentStreak)
        assertEquals(0, state.pointsEarned)
    }

    @Test
    fun `requestHint should show hint and reduce points`() = runTest {
        // Given
        val question = Question(
            id = "q1",
            text = "What is 2+2?",
            correctAnswer = "4",
            hints = listOf("Add the two numbers together"),
            explanation = "2+2 equals 4",
            skillId = "math_addition",
            difficulty = QuestionDifficulty.EASY
        )
        
        coEvery { 
            questionRepository.generateQuestion(any(), any(), any(), any()) 
        } returns Result.success(question)
        
        viewModel.onBookTextSubmitted("Math problems")
        advanceUntilIdle()

        // When
        viewModel.requestHint()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.showHint)
        assertEquals("Add the two numbers together", state.currentHint)
        
        // Now submit correct answer with hint shown
        viewModel.onAnswerChange("4")
        viewModel.submitAnswer()
        advanceUntilIdle()
        
        val finalState = viewModel.uiState.first()
        assertEquals(5, finalState.pointsEarned) // Only 5 points with hint
    }

    @Test
    fun `nextQuestion should increment progress and load new question`() = runTest {
        // Given - Complete first question
        val question1 = Question(
            id = "q1",
            text = "Question 1",
            correctAnswer = "Answer 1",
            hints = listOf(),
            explanation = "Explanation 1",
            skillId = "skill1",
            difficulty = QuestionDifficulty.EASY
        )
        
        val question2 = Question(
            id = "q2",
            text = "Question 2",
            correctAnswer = "Answer 2",
            hints = listOf(),
            explanation = "Explanation 2",
            skillId = "skill2",
            difficulty = QuestionDifficulty.MEDIUM
        )
        
        coEvery { 
            questionRepository.generateQuestion(any(), any(), any(), any()) 
        } returnsMany listOf(Result.success(question1), Result.success(question2))
        
        viewModel.onBookTextSubmitted("Test text")
        advanceUntilIdle()
        
        viewModel.onAnswerChange("Answer 1")
        viewModel.submitAnswer()
        advanceUntilIdle()

        // When
        viewModel.nextQuestion()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(question2, state.currentQuestion)
        assertEquals(1, state.questionsCompleted)
        assertEquals(0.1f, state.sessionProgress) // 1/10 questions
        assertFalse(state.isAnswerSubmitted)
        assertEquals("", state.userAnswer)
    }

    @Test
    fun `completing 10 questions should finish session`() = runTest {
        // Given
        val question = Question(
            id = "q1",
            text = "Question",
            correctAnswer = "Answer",
            hints = listOf(),
            explanation = "Explanation",
            skillId = "skill1",
            difficulty = QuestionDifficulty.EASY
        )
        
        coEvery { 
            questionRepository.generateQuestion(any(), any(), any(), any()) 
        } returns Result.success(question)
        
        coEvery {
            learningSessionDao.getSessionById(any())
        } returns LearningSessionEntity(
            id = "session1",
            userId = "user123",
            subject = "English",
            bookTitle = "Test Book",
            startedAt = Date(),
            status = SessionStatus.IN_PROGRESS
        )
        
        viewModel.onBookTextSubmitted("Test text")
        advanceUntilIdle()

        // Complete 10 questions
        repeat(10) {
            viewModel.onAnswerChange("Answer")
            viewModel.submitAnswer()
            advanceUntilIdle()
            if (it < 9) {
                viewModel.nextQuestion()
                advanceUntilIdle()
            }
        }

        // When - Complete the last question
        viewModel.nextQuestion()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isSessionComplete)
        assertEquals(1f, state.sessionProgress)
        
        // Verify session completion
        coVerify {
            learningSessionDao.completeSession(any(), any(), SessionStatus.COMPLETED, any(), 10, any())
        }
        
        // Verify completion analytics
        verify {
            analyticsService.logEvent(match<AnalyticsEvent.SessionCompleted> {
                it.subject == "English" && 
                it.userId == "user123" &&
                it.questionsAnswered == 10
            })
        }
    }
}