package com.studywise.ai.presentation.screens.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.dao.ProgressDao
import com.studywise.ai.data.local.dao.LearningSessionDao
import com.studywise.ai.data.local.entity.ProgressEntity
import com.studywise.ai.data.local.entity.LearningSessionEntity
import com.studywise.ai.data.local.entity.SessionStatus
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.domain.model.Question
import com.studywise.ai.domain.model.QuestionDifficulty
import com.studywise.ai.domain.repository.QuestionRepository
import com.studywise.ai.domain.service.AnalyticsService
import com.studywise.ai.util.textextraction.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class LearningSessionUiState(
    val subject: String = "",
    val currentQuestion: Question? = null,
    val userAnswer: String = "",
    val isAnswerSubmitted: Boolean = false,
    val feedback: String = "",
    val showHint: Boolean = false,
    val currentHint: String = "",
    val sessionProgress: Float = 0f,
    val questionsCompleted: Int = 0,
    val totalQuestions: Int = 10,
    val currentStreak: Int = 0,
    val pointsEarned: Int = 0,
    val isLoading: Boolean = false,
    val isSessionComplete: Boolean = false,
    val bookText: String = "",
    val bookTextError: String? = null,
    val waitingForBookText: Boolean = true,
    val selectedInputMethod: InputMethod = InputMethod.TEXT,
    val voiceState: VoiceTextCapture.VoiceState = VoiceTextCapture.VoiceState.Idle,
    val isProcessingInput: Boolean = false
)

@HiltViewModel
class LearningSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val questionRepository: QuestionRepository,
    private val learningSessionDao: LearningSessionDao,
    private val progressDao: ProgressDao,
    private val preferencesManager: PreferencesManager,
    private val photoTextExtractor: PhotoTextExtractor,
    private val documentTextExtractor: DocumentTextExtractor,
    private val voiceTextCapture: VoiceTextCapture,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningSessionUiState())
    val uiState: StateFlow<LearningSessionUiState> = _uiState.asStateFlow()

    private var currentSessionId: String = UUID.randomUUID().toString()
    private var userId: String = ""
    private val subject: String = savedStateHandle.get<String>("subject") ?: ""

    init {
        _uiState.value = _uiState.value.copy(subject = subject)
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            preferencesManager.userPreferences.collect { preferences ->
                userId = preferences.userId ?: ""
                if (userId.isNotEmpty() && _uiState.value.bookText.isNotEmpty()) {
                    startSession()
                }
            }
        }
    }

    fun onBookTextChange(text: String) {
        _uiState.value = _uiState.value.copy(
            bookText = text,
            bookTextError = null
        )
    }

    fun submitBookText() {
        val text = _uiState.value.bookText.trim()
        
        if (text.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                bookTextError = "Please provide some text to analyze"
            )
            return
        }
        
        if (text.length < 50) {
            _uiState.value = _uiState.value.copy(
                bookTextError = "Please enter at least 50 characters from your book"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            waitingForBookText = false,
            isLoading = true,
            bookTextError = null,
            voiceState = VoiceTextCapture.VoiceState.Idle // Reset voice state
        )

        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    startSession()
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        waitingForBookText = true,
                        bookTextError = "Failed to start session: ${e.message}"
                    )
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                waitingForBookText = true,
                bookTextError = "Please log in to start a session"
            )
        }
    }
    
    fun onInputMethodSelected(method: InputMethod) {
        _uiState.value = _uiState.value.copy(
            selectedInputMethod = method,
            bookTextError = null
        )
    }
    
    fun onPhotoSelected(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingInput = true, bookTextError = null)
            
            try {
                photoTextExtractor.extractText(uri).fold(
                    onSuccess = { extractedText ->
                        if (extractedText.isNotBlank()) {
                            _uiState.value = _uiState.value.copy(
                                bookText = extractedText,
                                isProcessingInput = false,
                                bookTextError = null
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                bookTextError = "No text found in the image",
                                isProcessingInput = false
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            bookTextError = "Failed to extract text: ${error.message}",
                            isProcessingInput = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    bookTextError = "Error processing image: ${e.message}",
                    isProcessingInput = false
                )
            }
        }
    }
    
    fun onDocumentSelected(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingInput = true)
            
            documentTextExtractor.extractText(uri).fold(
                onSuccess = { extractedText ->
                    _uiState.value = _uiState.value.copy(
                        bookText = extractedText,
                        isProcessingInput = false,
                        bookTextError = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        bookTextError = error.message,
                        isProcessingInput = false
                    )
                }
            )
        }
    }
    
    fun startVoiceCapture() {
        viewModelScope.launch {
            try {
                voiceTextCapture.clearTranscript()
                voiceTextCapture.startListening().collect { state ->
                    _uiState.value = _uiState.value.copy(voiceState = state)
                    
                    when (state) {
                        is VoiceTextCapture.VoiceState.Success -> {
                            if (state.text.isNotBlank()) {
                                _uiState.value = _uiState.value.copy(
                                    bookText = state.text,
                                    bookTextError = null
                                )
                            }
                        }
                        is VoiceTextCapture.VoiceState.Transcribing -> {
                            _uiState.value = _uiState.value.copy(
                                bookText = state.partialText,
                                bookTextError = null
                            )
                        }
                        is VoiceTextCapture.VoiceState.Error -> {
                            _uiState.value = _uiState.value.copy(
                                bookTextError = state.message,
                                voiceState = VoiceTextCapture.VoiceState.Idle
                            )
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    bookTextError = "Voice capture error: ${e.message}",
                    voiceState = VoiceTextCapture.VoiceState.Idle
                )
            }
        }
    }
    
    fun stopVoiceCapture() {
        voiceTextCapture.stopListening()
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceTextCapture.VoiceState.Idle
        )
    }

    private fun startSession() {
        viewModelScope.launch {
            // Log analytics event
            analyticsService.logEvent(
                AnalyticsEvent.SessionStarted(
                    subject = subject,
                    userId = userId
                )
            )
            
            // Create session entity
            val session = LearningSessionEntity(
                id = currentSessionId,
                userId = userId,
                subject = subject,
                bookTitle = "Content-Agnostic Text", // Generic title for MVP
                startedAt = Date(),
                questionsAnswered = 0,
                pointsEarned = 0,
                status = SessionStatus.IN_PROGRESS
            )
            learningSessionDao.insertSession(session)

            // Load first question
            loadNextQuestion()
        }
    }

    private suspend fun loadNextQuestion() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        // Determine difficulty based on performance
        val difficulty = when {
            _uiState.value.currentStreak >= 3 -> QuestionDifficulty.HARD
            _uiState.value.currentStreak >= 1 -> QuestionDifficulty.MEDIUM
            else -> QuestionDifficulty.EASY
        }

        // Get grade-appropriate question (default to 6 for MVP)
        val userGrade = 6
        
        questionRepository.generateQuestion(
            subject = subject,
            gradeLevel = userGrade,
            difficulty = difficulty,
            bookContext = _uiState.value.bookText
        ).fold(
            onSuccess = { question ->
                _uiState.value = _uiState.value.copy(
                    currentQuestion = question,
                    userAnswer = "",
                    isAnswerSubmitted = false,
                    feedback = "",
                    showHint = false,
                    currentHint = "",
                    isLoading = false
                )
            },
            onFailure = {
                // For MVP, use a fallback question
                val fallbackQuestion = createFallbackQuestion()
                _uiState.value = _uiState.value.copy(
                    currentQuestion = fallbackQuestion,
                    userAnswer = "",
                    isAnswerSubmitted = false,
                    feedback = "",
                    showHint = false,
                    currentHint = "",
                    isLoading = false
                )
            }
        )
    }

    fun onAnswerChange(answer: String) {
        if (!_uiState.value.isAnswerSubmitted) {
            _uiState.value = _uiState.value.copy(userAnswer = answer)
        }
    }

    fun submitAnswer() {
        if (_uiState.value.userAnswer.isBlank()) return

        val startTime = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(isAnswerSubmitted = true)

        viewModelScope.launch {
            val question = _uiState.value.currentQuestion ?: return@launch
            
            // For MVP, use simple validation
            val isCorrect = evaluateAnswer(
                question = question,
                userAnswer = _uiState.value.userAnswer
            )
            
            // Log analytics event
            analyticsService.logEvent(
                AnalyticsEvent.QuestionAnswered(
                    subject = subject,
                    skillId = question.skillId ?: "unknown",
                    isCorrect = isCorrect,
                    responseTime = System.currentTimeMillis() - startTime
                )
            )

            val pointsEarned = when {
                isCorrect && !_uiState.value.showHint -> 10
                isCorrect && _uiState.value.showHint -> 5
                else -> 0
            }

            val newStreak = if (isCorrect) _uiState.value.currentStreak + 1 else 0

            _uiState.value = _uiState.value.copy(
                feedback = if (isCorrect) {
                    "Excellent thinking! ${question.explanation}"
                } else {
                    "Not quite. ${question.explanation} The answer is: ${question.correctAnswer}"
                },
                currentStreak = newStreak,
                pointsEarned = _uiState.value.pointsEarned + pointsEarned
            )

            // Update session progress
            val currentSession = learningSessionDao.getSessionById(currentSessionId)
            currentSession?.let { session ->
                learningSessionDao.updateSession(
                    session.copy(
                        questionsAnswered = session.questionsAnswered + 1,
                        pointsEarned = session.pointsEarned + pointsEarned
                    )
                )
            }
        }
    }

    fun requestHint() {
        val question = _uiState.value.currentQuestion ?: return
        
        _uiState.value = _uiState.value.copy(
            showHint = true,
            currentHint = question.hints.firstOrNull() ?: "Think about the main concept being tested here."
        )
    }

    fun continueToNext() {
        val completed = _uiState.value.questionsCompleted + 1
        
        if (completed >= _uiState.value.totalQuestions) {
            completeSession()
        } else {
            _uiState.value = _uiState.value.copy(
                questionsCompleted = completed,
                sessionProgress = completed.toFloat() / _uiState.value.totalQuestions
            )
            viewModelScope.launch {
                loadNextQuestion()
            }
        }
    }

    private fun completeSession() {
        viewModelScope.launch {
            val startTime = learningSessionDao.getSessionById(currentSessionId)?.startedAt ?: Date()
            val endTime = Date()
            val durationSeconds = ((endTime.time - startTime.time) / 1000).toLong()
            val durationMinutes = (durationSeconds / 60).toInt()
            
            // Calculate mastery score
            val masteryScore = if (_uiState.value.questionsCompleted > 0) {
                _uiState.value.pointsEarned.toFloat() / (_uiState.value.questionsCompleted * 10f)
            } else 0f
            
            // Log analytics event
            analyticsService.logEvent(
                AnalyticsEvent.SessionCompleted(
                    subject = subject,
                    userId = userId,
                    duration = durationSeconds,
                    questionsAnswered = _uiState.value.questionsCompleted,
                    masteryScore = masteryScore
                )
            )

            // Update session as completed
            learningSessionDao.completeSession(
                sessionId = currentSessionId,
                completedAt = endTime,
                status = SessionStatus.COMPLETED,
                duration = durationMinutes,
                questionsAnswered = _uiState.value.questionsCompleted,
                pointsEarned = _uiState.value.pointsEarned
            )

            // Update user progress
            updateUserProgress()

            _uiState.value = _uiState.value.copy(
                isSessionComplete = true,
                sessionProgress = 1f
            )
        }
    }

    private suspend fun updateUserProgress() {
        val userGrade = 6 // Default grade for MVP
        val skillId = "${subject}_$userGrade"
        
        val existingProgress = progressDao.getProgress(userId, skillId)
        
        if (existingProgress != null) {
            val updatedProgress = existingProgress.copy(
                questionsAttempted = existingProgress.questionsAttempted + _uiState.value.totalQuestions,
                questionsCorrect = existingProgress.questionsCorrect + _uiState.value.questionsCompleted,
                totalPointsEarned = existingProgress.totalPointsEarned + _uiState.value.pointsEarned,
                lastPracticedAt = Date(),
                streakDays = if (wasToday(existingProgress.lastPracticedAt)) {
                    existingProgress.streakDays
                } else {
                    existingProgress.streakDays + 1
                },
                masteryLevel = calculateMasteryLevel(
                    existingProgress.questionsCorrect + _uiState.value.questionsCompleted,
                    existingProgress.questionsAttempted + _uiState.value.totalQuestions
                )
            )
            progressDao.updateProgress(updatedProgress)
        } else {
            val newProgress = ProgressEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                skillId = skillId,
                gradeLevel = userGrade,
                masteryLevel = 0.1f,
                questionsAttempted = _uiState.value.totalQuestions,
                questionsCorrect = _uiState.value.questionsCompleted,
                totalPointsEarned = _uiState.value.pointsEarned,
                lastPracticedAt = Date(),
                streakDays = 1
            )
            progressDao.insertProgress(newProgress)
        }
    }

    private fun createFallbackQuestion(): Question {
        // Create grade-appropriate fallback questions based on subject
        val userGrade = 6 // Default grade for MVP
        return when (subject) {
            "English" -> Question(
                id = UUID.randomUUID().toString(),
                skillId = "main_idea_identification",
                gradeLevel = userGrade,
                prompt = "Based on the text you provided, what is the main idea being communicated?",
                type = "comprehension",
                difficulty = 0.5f,
                hints = listOf(
                    "Look for the topic that appears most frequently",
                    "What is the author trying to tell you?"
                ),
                correctAnswer = "Varies based on student interpretation",
                explanation = "Understanding the main idea helps you grasp the author's primary message."
            )
            "Mathematics" -> Question(
                id = UUID.randomUUID().toString(),
                skillId = "multiplication_basics",
                gradeLevel = userGrade,
                prompt = "If you have 3 groups of 4 items each, how many items do you have in total?",
                type = "multiplication",
                difficulty = 0.3f,
                hints = listOf(
                    "Think of it as adding 4 + 4 + 4",
                    "This is the same as 3 × 4"
                ),
                correctAnswer = "12",
                explanation = "Multiplication is repeated addition. 3 groups of 4 equals 12."
            )
            else -> Question(
                id = UUID.randomUUID().toString(),
                skillId = "general_reflection",
                gradeLevel = userGrade,
                prompt = "What would you like to learn more about from your reading?",
                type = "reflection",
                difficulty = 0.3f,
                hints = listOf("Think about what interested you most"),
                correctAnswer = "Varies based on student interest",
                explanation = "Curiosity drives learning!"
            )
        }
    }

    private fun evaluateAnswer(question: Question, userAnswer: String): Boolean {
        // For MVP, simple evaluation
        return when (question.type) {
            "multiple_choice", "true_false" -> 
                userAnswer.trim().equals(question.correctAnswer, ignoreCase = true)
            "comprehension", "reflection" -> 
                userAnswer.trim().length >= 10 // Require thoughtful response
            else -> 
                userAnswer.trim().equals(question.correctAnswer, ignoreCase = true)
        }
    }

    private fun calculateMasteryLevel(
        correctAnswers: Int,
        totalAttempts: Int
    ): Float {
        return if (totalAttempts > 0) {
            (correctAnswers.toFloat() / totalAttempts).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    private fun wasToday(date: Date?): Boolean {
        if (date == null) return false
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.time = date
        val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
        return today == dateDay
    }
    
    override fun onCleared() {
        super.onCleared()
        photoTextExtractor.close()
        voiceTextCapture.stopListening()
    }
}