package com.studywise.ai.presentation.screens.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.content.QuestionSelector
import com.studywise.ai.data.local.content.SkillProgressionManager
import com.studywise.ai.data.local.dao.LearningSessionDao
import com.studywise.ai.data.local.dao.ProgressDao
import com.studywise.ai.data.local.entity.*
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.domain.model.Question
import com.studywise.ai.domain.repository.EducationalContentRepository
import com.studywise.ai.domain.service.AnalyticsService
import com.studywise.ai.util.textextraction.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Enhanced learning session that uses the comprehensive educational content system
 */
@HiltViewModel
class EnhancedLearningSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val educationalContentRepository: EducationalContentRepository,
    private val learningSessionDao: LearningSessionDao,
    private val progressDao: ProgressDao,
    private val preferencesManager: PreferencesManager,
    private val photoTextExtractor: PhotoTextExtractor,
    private val documentTextExtractor: DocumentTextExtractor,
    private val voiceTextCapture: VoiceTextCapture,
    private val analyticsService: AnalyticsService,
    private val questionSelector: QuestionSelector,
    private val skillProgressionManager: SkillProgressionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedLearningSessionUiState())
    val uiState: StateFlow<EnhancedLearningSessionUiState> = _uiState.asStateFlow()

    private var currentSessionId: String = UUID.randomUUID().toString()
    private var userId: String = ""
    private val subject: String = savedStateHandle.get<String>("subject") ?: ""
    private var gradeLevel: Int = 6 // Default grade, should be loaded from user profile
    
    // Track current skill and progress
    private var currentSkillId: Long? = null
    private var sessionQuestions = mutableListOf<Question>()
    private var currentQuestionIndex = 0

    init {
        _uiState.value = _uiState.value.copy(subject = subject)
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            preferencesManager.userPreferences.collect { preferences ->
                userId = preferences.userId ?: ""
                gradeLevel = preferences.gradeLevel ?: 6
                
                if (userId.isNotEmpty() && _uiState.value.bookText.isNotEmpty()) {
                    startEnhancedSession()
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
        
        if (text.length < 50) {
            _uiState.value = _uiState.value.copy(
                bookTextError = "Please enter at least 50 characters from your book"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            waitingForBookText = false,
            isLoading = true
        )

        if (userId.isNotEmpty()) {
            startEnhancedSession()
        }
    }
    
    fun onInputMethodSelected(method: InputMethod) {
        _uiState.value = _uiState.value.copy(
            selectedInputMethod = method,
            bookTextError = null
        )
    }
    
    // [Input method handlers same as original - onPhotoSelected, onDocumentSelected, etc.]
    fun onPhotoSelected(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingInput = true)
            
            photoTextExtractor.extractText(uri).fold(
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
            voiceTextCapture.clearTranscript()
            voiceTextCapture.startListening().collect { state ->
                _uiState.value = _uiState.value.copy(voiceState = state)
                
                when (state) {
                    is VoiceTextCapture.VoiceState.Success -> {
                        _uiState.value = _uiState.value.copy(
                            bookText = state.text,
                            bookTextError = null
                        )
                    }
                    is VoiceTextCapture.VoiceState.Transcribing -> {
                        _uiState.value = _uiState.value.copy(
                            bookText = state.partialText
                        )
                    }
                    is VoiceTextCapture.VoiceState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            bookTextError = state.message
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun stopVoiceCapture() {
        voiceTextCapture.stopListening()
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceTextCapture.VoiceState.Idle
        )
    }

    private fun startEnhancedSession() {
        viewModelScope.launch {
            // Log analytics event
            analyticsService.logEvent(
                AnalyticsEvent.SessionStarted(
                    subject = subject,
                    userId = userId
                )
            )
            
            // Determine session type based on context
            val sessionType = determineSessionType()
            
            // Create session entity
            val session = LearningSessionEntity(
                id = currentSessionId,
                userId = userId,
                subject = subject,
                bookTitle = "Content from: ${_uiState.value.bookText.take(50)}...",
                startedAt = Date(),
                questionsAnswered = 0,
                pointsEarned = 0,
                status = SessionStatus.IN_PROGRESS
            )
            learningSessionDao.insertSession(session)

            // Select appropriate skill and questions
            selectSkillAndQuestions(sessionType)
        }
    }
    
    private suspend fun determineSessionType(): QuestionSelector.SessionType {
        // Check if user has any mastery data
        val masteryData = educationalContentRepository.getStudentProgress(userId, gradeLevel)
        
        return when {
            masteryData.getOrNull()?.isEmpty() == true -> QuestionSelector.SessionType.DIAGNOSTIC
            _uiState.value.questionsCompleted % 5 == 0 -> QuestionSelector.SessionType.REVIEW
            else -> QuestionSelector.SessionType.PRACTICE
        }
    }
    
    private suspend fun selectSkillAndQuestions(sessionType: QuestionSelector.SessionType) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Get category based on subject
        val category = mapSubjectToCategory(subject)
        
        // Get appropriate skills for the grade and category
        val skills = educationalContentRepository.getSkillsByCategory(category)
            .getOrNull() ?: emptyList()
        
        if (skills.isEmpty()) {
            handleNoSkillsAvailable()
            return
        }
        
        // Use the skill progression manager to select the best skill
        val selectedSkill = skillProgressionManager.selectNextSkill(
            studentId = userId,
            gradeLevel = gradeLevel,
            availableSkills = skills,
            sessionType = sessionType
        ).getOrNull()
        
        if (selectedSkill == null) {
            handleNoSkillsAvailable()
            return
        }
        
        currentSkillId = selectedSkill.id
        
        // Use question selector to get optimal questions
        val questions = questionSelector.selectQuestions(
            skill = selectedSkill,
            gradeLevel = gradeLevel,
            sessionType = sessionType,
            count = determineQuestionCount(sessionType),
            studentId = userId
        ).getOrNull() ?: emptyList()
        
        if (questions.isEmpty()) {
            // Generate questions from templates if none exist
            generateQuestionsFromTemplates(selectedSkill)
        } else {
            sessionQuestions = questions.toMutableList()
            val masteryLevel = skillProgressionManager.getSkillMasteryLevel(userId, selectedSkill.id)
            _uiState.value = _uiState.value.copy(
                totalQuestions = sessionQuestions.size,
                currentSkill = SkillInfo(
                    id = selectedSkill.id,
                    name = selectedSkill.name,
                    description = selectedSkill.description,
                    category = selectedSkill.category.name,
                    masteryLevel = masteryLevel
                ),
                sessionType = sessionType.name,
                isLoading = false
            )
            loadNextQuestion()
        }
    }
    
    private suspend fun generateQuestionsFromTemplates(skill: SkillEntity) {
        val generatedQuestions = educationalContentRepository.generateQuestionsFromTemplates(
            skillId = skill.id,
            gradeLevel = gradeLevel,
            count = 5,
            difficultyLevel = null
        ).getOrNull() ?: emptyList()
        
        if (generatedQuestions.isNotEmpty()) {
            sessionQuestions = generatedQuestions.toMutableList()
            val masteryLevel = skillProgressionManager.getSkillMasteryLevel(userId, skill.id)
            _uiState.value = _uiState.value.copy(
                totalQuestions = sessionQuestions.size,
                currentSkill = SkillInfo(
                    id = skill.id,
                    name = skill.name,
                    description = skill.description,
                    category = skill.category.name,
                    masteryLevel = masteryLevel
                ),
                isLoading = false
            )
            loadNextQuestion()
        } else {
            handleNoQuestionsAvailable()
        }
    }
    
    private fun loadNextQuestion() {
        if (currentQuestionIndex >= sessionQuestions.size) {
            completeSession()
            return
        }
        
        val question = sessionQuestions[currentQuestionIndex]
        
        // Contextualize the question with the book text
        val contextualizedPrompt = contextualizePrompt(question.prompt, _uiState.value.bookText)
        
        _uiState.value = _uiState.value.copy(
            currentQuestion = EnhancedQuestion(
                id = question.id,
                skillId = question.skillId,
                prompt = contextualizedPrompt,
                originalPrompt = question.prompt,
                hints = question.hints ?: emptyList(),
                followUpQuestions = question.followUpQuestions ?: emptyList(),
                difficulty = question.difficulty,
                metadata = question.metadata ?: emptyMap()
            ),
            userAnswer = "",
            isAnswerSubmitted = false,
            feedback = "",
            showHint = false,
            currentHintIndex = 0,
            showFollowUp = false,
            currentFollowUpIndex = 0,
            sessionProgress = (currentQuestionIndex.toFloat() / sessionQuestions.size)
        )
    }
    
    private fun contextualizePrompt(prompt: String, bookText: String): String {
        // Extract a relevant excerpt from the book text
        val excerpt = extractRelevantExcerpt(bookText, prompt)
        
        // Add context introduction if needed
        return if (excerpt.isNotEmpty()) {
            "Based on this excerpt from your text:\n\n\"$excerpt\"\n\n$prompt"
        } else {
            prompt
        }
    }
    
    private fun extractRelevantExcerpt(bookText: String, prompt: String): String {
        // Simple excerpt extraction - take a reasonable chunk
        // In a real implementation, this would be more sophisticated
        val maxLength = 200
        return if (bookText.length > maxLength) {
            bookText.take(maxLength) + "..."
        } else {
            bookText
        }
    }
    
    fun onAnswerChange(answer: String) {
        if (!_uiState.value.isAnswerSubmitted) {
            _uiState.value = _uiState.value.copy(userAnswer = answer)
        }
    }
    
    fun submitAnswer() {
        if (_uiState.value.userAnswer.isBlank()) return
        
        val startTime = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(isAnswerSubmitted = true, isEvaluating = true)
        
        viewModelScope.launch {
            val question = _uiState.value.currentQuestion ?: return@launch
            
            // In a real implementation, this would use AI evaluation
            // For now, we'll use a simplified evaluation
            val evaluation = evaluateAnswer(question, _uiState.value.userAnswer)
            
            // Track the attempt
            educationalContentRepository.trackQuestionAttempt(
                studentId = userId,
                questionId = question.id.toLongOrNull() ?: 0L,
                wasCorrect = evaluation.isCorrect,
                responseTime = (System.currentTimeMillis() - startTime).toInt()
            )
            
            // Calculate points
            val pointsEarned = calculatePoints(
                isCorrect = evaluation.isCorrect,
                difficulty = question.difficulty,
                hintsUsed = _uiState.value.currentHintIndex,
                responseTime = System.currentTimeMillis() - startTime
            )
            
            val newStreak = if (evaluation.isCorrect) _uiState.value.currentStreak + 1 else 0
            
            _uiState.value = _uiState.value.copy(
                feedback = evaluation.feedback,
                currentStreak = newStreak,
                pointsEarned = _uiState.value.pointsEarned + pointsEarned,
                isEvaluating = false,
                showFollowUp = evaluation.isCorrect && question.followUpQuestions.isNotEmpty()
            )
            
            // Update session progress
            updateSessionProgress(pointsEarned)
            
            // Update skill mastery
            currentSkillId?.let { skillId ->
                updateSkillMastery(skillId, evaluation.isCorrect)
            }
        }
    }
    
    fun requestHint() {
        val question = _uiState.value.currentQuestion ?: return
        val hints = question.hints
        
        if (_uiState.value.currentHintIndex < hints.size) {
            _uiState.value = _uiState.value.copy(
                showHint = true,
                currentHint = hints[_uiState.value.currentHintIndex],
                currentHintIndex = _uiState.value.currentHintIndex + 1
            )
        }
    }
    
    fun answerFollowUp(answer: String) {
        // Handle follow-up question responses
        // This would integrate with the Socratic method
        _uiState.value = _uiState.value.copy(
            followUpResponse = answer,
            showFollowUp = false
        )
    }
    
    fun continueToNext() {
        currentQuestionIndex++
        _uiState.value = _uiState.value.copy(
            questionsCompleted = _uiState.value.questionsCompleted + 1
        )
        
        if (currentQuestionIndex >= sessionQuestions.size) {
            completeSession()
        } else {
            loadNextQuestion()
        }
    }
    
    private suspend fun updateSessionProgress(pointsEarned: Int) {
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
    
    private suspend fun updateSkillMastery(skillId: Long, wasCorrect: Boolean) {
        val mastery = educationalContentRepository.getStudentMastery(userId, skillId)
            .getOrNull()
        
        if (mastery != null) {
            // Update existing mastery
            val updatedMastery = mastery.copy(
                practiceCount = mastery.practiceCount + 1,
                correctCount = mastery.correctCount + if (wasCorrect) 1 else 0,
                lastPracticed = Date(),
                accuracyRate = ((mastery.correctCount + if (wasCorrect) 1 else 0).toFloat() / 
                               (mastery.practiceCount + 1).toFloat())
            )
            educationalContentRepository.updateStudentMastery(updatedMastery)
        } else {
            // Create new mastery record
            val newMastery = StudentSkillMasteryEntity(
                studentId = userId,
                skillId = skillId,
                gradeLevel = gradeLevel,
                masteryLevel = if (wasCorrect) 0.2f else 0.1f,
                confidenceScore = 0.5f,
                practiceCount = 1,
                correctCount = if (wasCorrect) 1 else 0,
                accuracyRate = if (wasCorrect) 1.0f else 0.0f,
                firstPracticed = Date(),
                lastPracticed = Date()
            )
            educationalContentRepository.updateStudentMastery(newMastery)
        }
    }
    
    private fun completeSession() {
        viewModelScope.launch {
            // Update session status
            val session = learningSessionDao.getSessionById(currentSessionId)
            session?.let {
                learningSessionDao.updateSession(
                    it.copy(
                        status = SessionStatus.COMPLETED,
                        completedAt = Date()
                    )
                )
            }
            
            // Log analytics
            analyticsService.logEvent(
                AnalyticsEvent.SessionCompleted(
                    subject = subject,
                    userId = userId,
                    questionsCompleted = _uiState.value.questionsCompleted,
                    pointsEarned = _uiState.value.pointsEarned
                )
            )
            
            _uiState.value = _uiState.value.copy(
                isSessionComplete = true,
                sessionProgress = 1.0f
            )
        }
    }
    
    // Helper methods
    private fun mapSubjectToCategory(subject: String): String {
        return when (subject.lowercase()) {
            "english", "reading", "literature" -> "reading_literature"
            "science", "social studies", "history" -> "reading_informational"
            "writing", "essay" -> "writing"
            "grammar", "language" -> "language_grammar"
            "vocabulary", "speaking" -> "vocabulary_speaking"
            else -> "reading_literature" // Default
        }
    }
    
    private fun determineQuestionCount(sessionType: QuestionSelector.SessionType): Int {
        return when (sessionType) {
            QuestionSelector.SessionType.DIAGNOSTIC -> 5
            QuestionSelector.SessionType.PRACTICE -> 10
            QuestionSelector.SessionType.REVIEW -> 5
            QuestionSelector.SessionType.CHALLENGE -> 7
        }
    }
    
    private fun calculatePoints(
        isCorrect: Boolean,
        difficulty: Float,
        hintsUsed: Int,
        responseTime: Long
    ): Int {
        if (!isCorrect) return 0
        
        val basePoints = (10 * difficulty).toInt()
        val hintPenalty = hintsUsed * 2
        val speedBonus = if (responseTime < 30000) 2 else 0 // Bonus for quick answers
        
        return (basePoints - hintPenalty + speedBonus).coerceAtLeast(1)
    }
    
    private fun evaluateAnswer(
        question: EnhancedQuestion,
        userAnswer: String
    ): AnswerEvaluation {
        // Simplified evaluation for MVP
        // In production, this would use AI evaluation
        val isCorrect = userAnswer.length > 10 // Simple check for now
        
        val feedback = if (isCorrect) {
            "Great thinking! Your response shows good understanding of the concept."
        } else {
            "Let's think about this differently. Consider what the question is really asking."
        }
        
        return AnswerEvaluation(
            isCorrect = isCorrect,
            feedback = feedback,
            suggestedFollowUp = if (isCorrect && question.followUpQuestions.isNotEmpty()) {
                question.followUpQuestions.first()
            } else null
        )
    }
    
    private fun handleNoSkillsAvailable() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = "No skills available for your grade level. Please contact support."
        )
    }
    
    private fun handleNoQuestionsAvailable() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = "Unable to generate questions at this time. Please try again."
        )
    }
    
    fun retrySession() {
        // Clear error state
        _uiState.value = _uiState.value.copy(error = null)
        
        // Retry based on current state
        when {
            _uiState.value.waitingForBookText -> {
                // Reset to book text input state
                _uiState.value = EnhancedLearningSessionUiState(
                    subject = subject,
                    waitingForBookText = true
                )
            }
            _uiState.value.currentQuestion == null -> {
                // Try to load questions again
                startSession()
            }
            else -> {
                // Clear any error and continue with current question
                _uiState.value = _uiState.value.copy(
                    error = null,
                    isEvaluating = false,
                    isLoading = false
                )
            }
        }
    }
}

// Enhanced UI State
data class EnhancedLearningSessionUiState(
    val subject: String = "",
    val currentQuestion: EnhancedQuestion? = null,
    val userAnswer: String = "",
    val isAnswerSubmitted: Boolean = false,
    val feedback: String = "",
    val showHint: Boolean = false,
    val currentHint: String = "",
    val currentHintIndex: Int = 0,
    val showFollowUp: Boolean = false,
    val currentFollowUpIndex: Int = 0,
    val followUpResponse: String = "",
    val sessionProgress: Float = 0f,
    val questionsCompleted: Int = 0,
    val totalQuestions: Int = 10,
    val currentStreak: Int = 0,
    val pointsEarned: Int = 0,
    val isLoading: Boolean = false,
    val isEvaluating: Boolean = false,
    val isSessionComplete: Boolean = false,
    val bookText: String = "",
    val bookTextError: String? = null,
    val waitingForBookText: Boolean = true,
    val selectedInputMethod: InputMethod = InputMethod.TEXT,
    val voiceState: VoiceTextCapture.VoiceState = VoiceTextCapture.VoiceState.Idle,
    val isProcessingInput: Boolean = false,
    val currentSkill: SkillInfo? = null,
    val sessionType: String = "",
    val error: String? = null
)

// Enhanced question model with educational content
data class EnhancedQuestion(
    val id: String,
    val skillId: String,
    val prompt: String,
    val originalPrompt: String,
    val hints: List<String>,
    val followUpQuestions: List<String>,
    val difficulty: Float,
    val metadata: Map<String, String>
)

data class SkillInfo(
    val id: Long,
    val name: String,
    val description: String,
    val category: String,
    val masteryLevel: Float = 0f
)

data class AnswerEvaluation(
    val isCorrect: Boolean,
    val feedback: String,
    val suggestedFollowUp: String? = null
)