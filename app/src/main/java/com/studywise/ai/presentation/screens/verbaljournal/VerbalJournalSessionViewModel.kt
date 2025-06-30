package com.studywise.ai.presentation.screens.verbaljournal

import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.VerbalJournalRepository
import com.studywise.ai.domain.usecase.verbaljournal.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VerbalJournalSessionViewModel @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository,
    private val processVerbalJournalTurnUseCase: ProcessVerbalJournalTurnUseCase,
    private val endVerbalJournalSessionUseCase: EndVerbalJournalSessionUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()
    
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private val _transcriptionState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState.asStateFlow()
    
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var recordingJob: Job? = null
    private var sessionTimerJob: Job? = null
    private var currentSessionId: String? = null
    
    fun initializeSession(sessionId: String) {
        currentSessionId = sessionId
        viewModelScope.launch {
            try {
                val session = verbalJournalRepository.getJournalEntry(sessionId)
                if (session == null) {
                    _uiState.value = SessionUiState.Error("Session not found")
                    return@launch
                }
                
                val profile = verbalJournalRepository.getOrCreateProfile(
                    userId = session.userId,
                    nativeLanguage = "English"
                )
                
                _uiState.value = SessionUiState.Active(
                    sessionId = sessionId,
                    profile = profile,
                    targetMinutes = session.targetDurationMinutes,
                    elapsedMinutes = 0,
                    conversationTurns = session.conversationTurns,
                    currentAiResponse = session.conversationTurns.lastOrNull()?.transcript ?: "",
                    isAiThinking = false,
                    showCorrections = shouldShowCorrections(profile),
                    currentErrors = emptyList()
                )
                
                // Start session timer
                startSessionTimer()
                
            } catch (e: Exception) {
                _uiState.value = SessionUiState.Error(
                    e.message ?: "Failed to initialize session"
                )
            }
        }
    }
    
    fun toggleRecording() {
        when (_recordingState.value) {
            is RecordingState.Idle -> startRecording()
            is RecordingState.Recording -> stopRecording()
            is RecordingState.Processing -> {} // Do nothing while processing
        }
    }
    
    private fun startRecording() {
        try {
            // Create temp file for recording
            val tempFile = File.createTempFile("verbal_journal_", ".m4a")
            recordingFile = tempFile
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(android.app.Application())
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(tempFile.absolutePath)
                prepare()
                start()
            }
            
            _recordingState.value = RecordingState.Recording(0)
            
            // Start recording timer
            recordingJob = viewModelScope.launch {
                var seconds = 0
                while (isActive) {
                    delay(1000)
                    seconds++
                    _recordingState.value = RecordingState.Recording(seconds)
                }
            }
            
        } catch (e: Exception) {
            _uiState.value = SessionUiState.Error(
                "Failed to start recording: ${e.message}"
            )
        }
    }
    
    private fun stopRecording() {
        recordingJob?.cancel()
        
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            _recordingState.value = RecordingState.Processing
            
            // Process the recording
            recordingFile?.let { file ->
                processRecording(file)
            }
            
        } catch (e: Exception) {
            _recordingState.value = RecordingState.Idle
            _uiState.value = SessionUiState.Error(
                "Failed to stop recording: ${e.message}"
            )
        }
    }
    
    private fun processRecording(audioFile: File) {
        viewModelScope.launch {
            try {
                val sessionId = currentSessionId ?: return@launch
                
                // Simulate transcription (in production, use speech-to-text service)
                _transcriptionState.value = TranscriptionState.Transcribing
                delay(2000) // Simulate API call
                
                val mockTranscript = generateMockTranscript()
                _transcriptionState.value = TranscriptionState.Complete(
                    transcript = mockTranscript,
                    confidence = 0.95f
                )
                
                // Process the turn
                processVerbalJournalTurnUseCase(
                    sessionId = sessionId,
                    audioFile = audioFile,
                    transcript = mockTranscript,
                    transcriptionConfidence = 0.95f
                ).collect { result ->
                    when (result) {
                        is ProcessTurnResult.Success -> {
                            updateUiWithTurnResult(result)
                            
                            // Reset states after a delay
                            delay(3000)
                            _transcriptionState.value = TranscriptionState.Idle
                            _recordingState.value = RecordingState.Idle
                        }
                        is ProcessTurnResult.Error -> {
                            _uiState.value = SessionUiState.Error(result.message)
                        }
                    }
                }
                
            } catch (e: Exception) {
                _recordingState.value = RecordingState.Idle
                _transcriptionState.value = TranscriptionState.Idle
                _uiState.value = SessionUiState.Error(
                    "Failed to process recording: ${e.message}"
                )
            }
        }
    }
    
    private fun updateUiWithTurnResult(result: ProcessTurnResult.Success) {
        val currentState = _uiState.value as? SessionUiState.Active ?: return
        
        _uiState.value = currentState.copy(
            conversationTurns = currentState.conversationTurns + result.userTurn,
            currentAiResponse = result.aiResponse,
            isAiThinking = false,
            currentErrors = if (result.shouldShowCorrections) result.detectedErrors else emptyList()
        )
    }
    
    fun retryRecording() {
        _transcriptionState.value = TranscriptionState.Idle
        _recordingState.value = RecordingState.Idle
    }
    
    fun skipCorrections() {
        val currentState = _uiState.value as? SessionUiState.Active ?: return
        _uiState.value = currentState.copy(currentErrors = emptyList())
    }
    
    fun endSession() {
        sessionTimerJob?.cancel()
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            endVerbalJournalSessionUseCase(sessionId).collect { result ->
                when (result) {
                    is EndSessionResult.Success -> {
                        _uiState.value = SessionUiState.Completed(
                            sessionAnalysis = result.sessionAnalysis,
                            newAchievements = result.newAchievements
                        )
                    }
                    is EndSessionResult.Error -> {
                        _uiState.value = SessionUiState.Error(result.message)
                    }
                }
            }
        }
    }
    
    private fun startSessionTimer() {
        sessionTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(60000) // Update every minute
                val currentState = _uiState.value as? SessionUiState.Active ?: return@launch
                _uiState.value = currentState.copy(
                    elapsedMinutes = currentState.elapsedMinutes + 1
                )
            }
        }
    }
    
    private fun shouldShowCorrections(profile: VerbalJournalProfile): Boolean {
        return when (profile.correctionStyle) {
            CorrectionStyle.GENTLE -> false
            CorrectionStyle.BALANCED -> profile.breakInWeek >= 4
            CorrectionStyle.INTENSIVE -> true
        }
    }
    
    private fun generateMockTranscript(): String {
        val transcripts = listOf(
            "I really enjoy learning new languages because it opens up so many opportunities.",
            "Yesterday I go to the market and buy some vegetables for dinner.",
            "The weather is very nice today, I think I will go for walk in park.",
            "My favorite hobby is read books, especially science fiction novels.",
            "I have been studying English for three years and I still finding it challenging."
        )
        return transcripts.random()
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        recordingJob?.cancel()
        sessionTimerJob?.cancel()
    }
}

// UI States
sealed class SessionUiState {
    object Loading : SessionUiState()
    
    data class Active(
        val sessionId: String,
        val profile: VerbalJournalProfile,
        val targetMinutes: Int,
        val elapsedMinutes: Int,
        val conversationTurns: List<ConversationTurn>,
        val currentAiResponse: String,
        val isAiThinking: Boolean,
        val showCorrections: Boolean,
        val currentErrors: List<ErrorInstance>
    ) : SessionUiState()
    
    data class Completed(
        val sessionAnalysis: SessionAnalysis,
        val newAchievements: List<Achievement>
    ) : SessionUiState()
    
    data class Error(val message: String) : SessionUiState()
}

// Recording States
sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(val duration: Int) : RecordingState()
    object Processing : RecordingState()
}

// Transcription States
sealed class TranscriptionState {
    object Idle : TranscriptionState()
    object Transcribing : TranscriptionState()
    data class Complete(val transcript: String, val confidence: Float) : TranscriptionState()
}