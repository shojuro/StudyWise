package com.studywise.ai.presentation.screens.learning

import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.model.IdentifiedObject
import com.studywise.ai.domain.model.SocraticLesson
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.domain.repository.AIRepository
import com.studywise.ai.domain.repository.UserRepository
import com.studywise.ai.domain.service.AnalyticsService
import com.studywise.ai.utils.AudioRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class PhotoLearningUiState(
    val isAnalyzing: Boolean = false,
    val identifiedObject: IdentifiedObject? = null,
    val gradedSentences: Map<Int, List<String>> = emptyMap(),
    val currentGrade: Int = 4,
    val socraticLesson: SocraticLesson? = null,
    val currentQuestionIndex: Int = 0,
    val conversationHistory: List<ConversationEntry> = emptyList(),
    val isRecording: Boolean = false,
    val isSpeaking: Boolean = false,
    val error: String? = null,
    val showGradeSelector: Boolean = false,
    val lessonStarted: Boolean = false,
    val lessonComplete: Boolean = false,
    val showManualEntry: Boolean = false,
    val manualObjectName: String = "",
    val detectionConfidence: Float? = null
)

data class ConversationEntry(
    val speaker: Speaker,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Speaker {
    AI, STUDENT
}

@HiltViewModel
class PhotoLearningViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val userRepository: UserRepository,
    private val audioRecorder: AudioRecorder,
    private val analyticsService: AnalyticsService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PhotoLearningUiState())
    val uiState: StateFlow<PhotoLearningUiState> = _uiState.asStateFlow()
    
    private var mediaPlayer: MediaPlayer? = null
    
    init {
        loadUserGrade()
    }
    
    private fun loadUserGrade() {
        viewModelScope.launch {
            userRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    user.grade?.let { grade ->
                        _uiState.value = _uiState.value.copy(currentGrade = grade)
                    }
                },
                onFailure = {
                    // Default grade is already set
                }
            )
        }
    }
    
    fun analyzePhoto(photoUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, error = null)
            
            aiRepository.identifyObject(photoUri).fold(
                onSuccess = { identifiedObject ->
                    _uiState.value = _uiState.value.copy(
                        identifiedObject = identifiedObject,
                        isAnalyzing = false
                    )
                    
                    // Log analytics event
                    analyticsService.logEvent(
                        AnalyticsEvent.PhotoAnalyzed(
                            objectName = identifiedObject.name,
                            confidence = identifiedObject.confidence
                        )
                    )
                    
                    generateGradedSentences(identifiedObject.name)
                },
                onFailure = { error ->
                    // Check if it's a low confidence error
                    if (error.message?.contains("confidence") == true) {
                        _uiState.value = _uiState.value.copy(
                            isAnalyzing = false,
                            error = error.message,
                            showManualEntry = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isAnalyzing = false,
                            error = "Failed to identify object: ${error.message}",
                            showManualEntry = true
                        )
                    }
                }
            )
        }
    }
    
    private fun generateGradedSentences(objectName: String) {
        viewModelScope.launch {
            aiRepository.generateGradedSentences(objectName, 2, 12).fold(
                onSuccess = { sentences ->
                    _uiState.value = _uiState.value.copy(
                        gradedSentences = sentences,
                        showGradeSelector = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to generate sentences: ${error.message}"
                    )
                }
            )
        }
    }
    
    fun selectGrade(grade: Int) {
        _uiState.value = _uiState.value.copy(
            currentGrade = grade,
            showGradeSelector = false
        )
    }
    
    fun startLesson() {
        val objectName = _uiState.value.identifiedObject?.name ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(lessonStarted = true)
            
            aiRepository.createSocraticLesson(
                objectName = objectName,
                grade = _uiState.value.currentGrade,
                duration = 5
            ).fold(
                onSuccess = { lesson ->
                    _uiState.value = _uiState.value.copy(
                        socraticLesson = lesson,
                        conversationHistory = listOf(
                            ConversationEntry(
                                speaker = Speaker.AI,
                                text = lesson.initialQuestion
                            )
                        )
                    )
                    
                    // Log analytics event
                    analyticsService.logEvent(
                        AnalyticsEvent.SocraticLessonStarted(
                            objectName = objectName,
                            grade = _uiState.value.currentGrade
                        )
                    )
                    
                    speakText(lesson.initialQuestion)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to create lesson: ${error.message}"
                    )
                }
            )
        }
    }
    
    fun onStudentResponse(response: String) {
        val lesson = _uiState.value.socraticLesson ?: return
        val currentQuestion = if (_uiState.value.currentQuestionIndex < lesson.guidingQuestions.size) {
            lesson.guidingQuestions[_uiState.value.currentQuestionIndex]
        } else {
            lesson.initialQuestion
        }
        
        // Add student response to history
        val updatedHistory = _uiState.value.conversationHistory + ConversationEntry(
            speaker = Speaker.STUDENT,
            text = response
        )
        _uiState.value = _uiState.value.copy(conversationHistory = updatedHistory)
        
        viewModelScope.launch {
            // Generate AI response
            aiRepository.generateSocraticResponse(
                context = "Learning about: ${lesson.objectName}. Current question: $currentQuestion",
                studentResponse = response,
                grade = _uiState.value.currentGrade
            ).fold(
                onSuccess = { aiResponse ->
                    val newHistory = updatedHistory + ConversationEntry(
                        speaker = Speaker.AI,
                        text = aiResponse
                    )
                    
                    val nextQuestionIndex = _uiState.value.currentQuestionIndex + 1
                    val lessonComplete = nextQuestionIndex >= lesson.guidingQuestions.size
                    
                    _uiState.value = _uiState.value.copy(
                        conversationHistory = newHistory,
                        currentQuestionIndex = nextQuestionIndex,
                        lessonComplete = lessonComplete
                    )
                    
                    speakText(aiResponse)
                    
                    // If lesson is complete, add closing message
                    if (lessonComplete) {
                        val closingMessage = "Great job learning about ${lesson.objectName}! You asked wonderful questions and discovered so much. Keep being curious!"
                        val finalHistory = newHistory + ConversationEntry(
                            speaker = Speaker.AI,
                            text = closingMessage
                        )
                        _uiState.value = _uiState.value.copy(conversationHistory = finalHistory)
                        speakText(closingMessage)
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to generate response: ${error.message}"
                    )
                }
            )
        }
    }
    
    fun startRecording() {
        audioRecorder.startRecording().fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(isRecording = true)
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start recording: ${error.message}"
                )
            }
        )
    }
    
    fun stopRecording() {
        audioRecorder.stopRecording().fold(
            onSuccess = { audioFile ->
                _uiState.value = _uiState.value.copy(isRecording = false)
                transcribeAudio(audioFile)
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isRecording = false,
                    error = "Failed to stop recording: ${error.message}"
                )
            }
        )
    }
    
    private fun transcribeAudio(audioFile: File) {
        viewModelScope.launch {
            aiRepository.transcribeAudio(audioFile).fold(
                onSuccess = { transcription ->
                    onStudentResponse(transcription)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to transcribe audio: ${error.message}"
                    )
                }
            )
        }
    }
    
    private fun speakText(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSpeaking = true)
            
            aiRepository.generateSpeech(text).fold(
                onSuccess = { audioData ->
                    playAudio(audioData)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSpeaking = false,
                        error = "Failed to generate speech: ${error.message}"
                    )
                }
            )
        }
    }
    
    private fun playAudio(audioData: ByteArray) {
        try {
            val tempFile = File.createTempFile("speech", ".mp3")
            FileOutputStream(tempFile).use { it.write(audioData) }
            
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                setOnCompletionListener {
                    _uiState.value = _uiState.value.copy(isSpeaking = false)
                    tempFile.delete()
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isSpeaking = false,
                error = "Failed to play audio: ${e.message}"
            )
        }
    }
    
    fun resetLesson() {
        _uiState.value = PhotoLearningUiState(currentGrade = _uiState.value.currentGrade)
        mediaPlayer?.release()
        audioRecorder.release()
    }
    
    fun onManualObjectNameChange(name: String) {
        _uiState.value = _uiState.value.copy(manualObjectName = name)
    }
    
    fun submitManualObject() {
        val objectName = _uiState.value.manualObjectName.trim()
        if (objectName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter an object name"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                showManualEntry = false,
                error = null
            )
            
            // For manual entry, we'll create a temporary object and then generate proper content
            val tempObject = IdentifiedObject(
                name = objectName,
                description = "Generating educational content...",
                category = "Manual Entry",
                confidence = 1.0f,
                educationalValue = "Loading..."
            )
            
            _uiState.value = _uiState.value.copy(
                identifiedObject = tempObject,
                isAnalyzing = true
            )
            
            // Generate proper educational content using AI
            generateEducationalContentForManualObject(objectName)
        }
    }
    
    private fun generateEducationalContentForManualObject(objectName: String) {
        viewModelScope.launch {
            // Use AI to generate proper educational content for the manual object
            aiRepository.generateContentForManualObject(objectName).fold(
                onSuccess = { identifiedObject ->
                    _uiState.value = _uiState.value.copy(
                        identifiedObject = identifiedObject,
                        isAnalyzing = false
                    )
                    
                    // Log analytics event
                    analyticsService.logEvent(
                        AnalyticsEvent.PhotoAnalyzed(
                            objectName = identifiedObject.name,
                            confidence = identifiedObject.confidence
                        )
                    )
                    
                    generateGradedSentences(identifiedObject.name)
                },
                onFailure = { error ->
                    // If AI fails, use a basic fallback
                    val article = if (objectName.first().lowercaseChar() in listOf('a', 'e', 'i', 'o', 'u')) "an" else "a"
                    val fallbackObject = IdentifiedObject(
                        name = objectName,
                        description = "This is $article $objectName.",
                        category = "Manual Entry",
                        confidence = 1.0f,
                        educationalValue = "Learning about ${objectName}s helps us understand our world!"
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        identifiedObject = fallbackObject,
                        isAnalyzing = false,
                        error = "Could not generate AI content: ${error.message}"
                    )
                    
                    generateGradedSentences(objectName)
                }
            )
        }
    }
    
    fun dismissManualEntry() {
        _uiState.value = _uiState.value.copy(
            showManualEntry = false,
            manualObjectName = "",
            error = null
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        audioRecorder.release()
    }
}