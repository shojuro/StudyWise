package com.studywise.ai.presentation.screens.verbaljournal

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.UserRepository
import com.studywise.ai.domain.repository.VerbalJournalRepository
import com.studywise.ai.domain.service.SpeechToTextService
import com.studywise.ai.domain.service.VerbalJournalConversationService
import com.studywise.ai.domain.service.SpeechAnalysisService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VerbalJournalSessionViewModel @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository,
    private val userRepository: UserRepository,
    private val speechToTextService: SpeechToTextService,
    private val conversationService: VerbalJournalConversationService,
    private val speechAnalysisService: SpeechAnalysisService,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId") ?: ""
    
    private val _uiState = MutableStateFlow(VerbalJournalSessionUiState())
    val uiState: StateFlow<VerbalJournalSessionUiState> = _uiState.asStateFlow()
    
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private val _transcriptionState = MutableStateFlow(TranscriptionState())
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState.asStateFlow()
    
    private var currentEntry: VerbalJournalEntry? = null
    private var userProfile: VerbalJournalProfile? = null
    private var conversationContext: ConversationContext? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null
    private var recordingStartTime: Long = 0
    
    init {
        initializeSession(sessionId)
    }
    
    fun initializeSession(sessionId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Get current user
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No user logged in"
                        )
                    }
                    return@launch
                }
                
                // Get or create user profile
                userProfile = verbalJournalRepository.getUserProfile(currentUser.id)
                    ?: createDefaultProfile(currentUser.id)
                
                // Check if this is a new session or resuming
                if (sessionId.isNotEmpty()) {
                    // Resume existing session
                    currentEntry = verbalJournalRepository.getEntry(sessionId)
                    if (currentEntry == null) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Session not found"
                            )
                        }
                        return@launch
                    }
                    
                    // Load conversation history
                    val turns = verbalJournalRepository.getConversationTurns(sessionId)
                    
                    conversationContext = ConversationContext(
                        sessionId = sessionId,
                        currentTopic = currentEntry?.topic,
                        conversationHistory = turns,
                        userProfile = userProfile!!,
                        sessionGoals = determineSessionGoals(),
                        currentPhase = determineConversationPhase(turns.size)
                    )
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            sessionStatus = currentEntry?.status ?: SessionStatus.IN_PROGRESS,
                            conversationTurns = turns,
                            currentPhase = conversationContext?.currentPhase ?: ConversationPhase.GREETING
                        )
                    }
                } else {
                    // Start new session - show topic selection
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            showTopicSelection = true
                        )
                    }
                }
                
                // Check break-in period
                updateBreakInPeriodStatus()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to initialize session: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun selectSessionType(sessionType: SessionType) {
        _uiState.update { 
            it.copy(selectedSessionType = sessionType)
        }
        
        when (sessionType) {
            SessionType.DAILY_PROMPT -> loadDailyPrompt()
            SessionType.TOPIC_BASED -> showTopicSuggestions()
            SessionType.FREE_CONVERSATION -> startFreeConversation()
        }
    }
    
    fun selectTopic(topic: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val currentUser = userRepository.getCurrentUser() ?: return@launch
                
                // Create new session entry
                currentEntry = verbalJournalRepository.createEntry(
                    userId = currentUser.id,
                    sessionType = _uiState.value.selectedSessionType ?: SessionType.FREE_CONVERSATION,
                    topic = topic,
                    promptId = _uiState.value.dailyPrompt?.id,
                    targetDurationMinutes = userProfile?.goals?.dailyGoalMinutes ?: 10,
                    difficulty = userProfile?.proficiencyLevel?.toDifficulty() ?: DifficultyLevel.INTERMEDIATE
                )
                
                conversationContext = ConversationContext(
                    sessionId = currentEntry!!.id,
                    currentTopic = topic,
                    conversationHistory = emptyList(),
                    userProfile = userProfile!!,
                    sessionGoals = determineSessionGoals(),
                    currentPhase = ConversationPhase.GREETING
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        showTopicSelection = false,
                        sessionStatus = SessionStatus.IN_PROGRESS,
                        currentTopic = topic
                    )
                }
                
                // Start with AI greeting
                generateAIResponse()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to start session: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun toggleRecording() {
        when (_recordingState.value) {
            RecordingState.IDLE -> startRecording()
            RecordingState.RECORDING -> stopRecording()
            else -> {} // Do nothing if processing
        }
    }
    
    private fun startRecording() {
        try {
            // Create audio file
            val audioDir = File(context.cacheDir, "verbal_journal_audio")
            audioDir.mkdirs()
            currentAudioFile = File(audioDir, "${UUID.randomUUID()}.m4a")
            
            // Initialize MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(currentAudioFile?.absolutePath)
                prepare()
                start()
            }
            
            recordingStartTime = System.currentTimeMillis()
            _recordingState.value = RecordingState.RECORDING
            
            // Start recording timer
            startRecordingTimer()
            
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(error = "Failed to start recording: ${e.message}")
            }
        }
    }
    
    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            val recordingDuration = (System.currentTimeMillis() - recordingStartTime) / 1000f
            _recordingState.value = RecordingState.PROCESSING
            
            // Process the audio
            processRecordedAudio(currentAudioFile!!, recordingDuration)
            
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(error = "Failed to stop recording: ${e.message}")
            }
            _recordingState.value = RecordingState.IDLE
        }
    }
    
    private fun processRecordedAudio(audioFile: File, durationSeconds: Float) {
        viewModelScope.launch {
            try {
                _transcriptionState.update { 
                    it.copy(
                        isTranscribing = true,
                        transcribedText = ""
                    )
                }
                
                // Convert audio to text
                val transcription = speechToTextService.transcribeAudio(audioFile)
                
                _transcriptionState.update { 
                    it.copy(
                        isTranscribing = false,
                        transcribedText = transcription
                    )
                }
                
                // Analyze speech
                val metrics = speechAnalysisService.analyzeSpeech(
                    audioFile = audioFile,
                    transcription = transcription,
                    durationSeconds = durationSeconds
                )
                
                // Save conversation turn
                val turn = verbalJournalRepository.addConversationTurn(
                    entryId = currentEntry!!.id,
                    role = ConversationRole.USER,
                    transcription = transcription,
                    audioFilePath = audioFile.absolutePath,
                    durationSeconds = durationSeconds,
                    metrics = metrics
                )
                
                // Detect and save errors
                val errors = speechAnalysisService.detectErrors(
                    transcription = transcription,
                    userProfile = userProfile!!,
                    conversationContext = conversationContext!!
                )
                
                if (errors.isNotEmpty()) {
                    verbalJournalRepository.addSpeechErrors(turn.id, errors)
                }
                
                // Update UI
                _uiState.update { 
                    it.copy(
                        conversationTurns = it.conversationTurns + turn,
                        lastUserTranscription = transcription,
                        lastSpeechMetrics = metrics,
                        detectedErrors = errors
                    )
                }
                
                // Update conversation context
                conversationContext = conversationContext?.copy(
                    conversationHistory = conversationContext!!.conversationHistory + turn
                )
                
                _recordingState.value = RecordingState.IDLE
                
                // Generate AI response
                generateAIResponse()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to process audio: ${e.message}")
                }
                _recordingState.value = RecordingState.IDLE
            }
        }
    }
    
    private fun generateAIResponse() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isAIThinking = true) }
                
                // Get AI response based on conversation context
                val aiResponse = conversationService.generateResponse(
                    context = conversationContext!!,
                    lastUserInput = _uiState.value.lastUserTranscription
                )
                
                // Save AI turn
                val aiTurn = verbalJournalRepository.addConversationTurn(
                    entryId = currentEntry!!.id,
                    role = ConversationRole.AI,
                    transcription = aiResponse.text,
                    audioFilePath = aiResponse.audioUrl,
                    durationSeconds = 0f, // AI responses don't have duration
                    metrics = null
                )
                
                // Update UI
                _uiState.update { 
                    it.copy(
                        isAIThinking = false,
                        conversationTurns = it.conversationTurns + aiTurn,
                        lastAIResponse = aiResponse,
                        currentPhase = determineConversationPhase(it.conversationTurns.size + 1)
                    )
                }
                
                // Update conversation context
                conversationContext = conversationContext?.copy(
                    conversationHistory = conversationContext!!.conversationHistory + aiTurn,
                    currentPhase = _uiState.value.currentPhase
                )
                
                // Check if session goals are met
                checkSessionCompletion()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isAIThinking = false,
                        error = "Failed to generate response: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun endSession() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Complete the session
                verbalJournalRepository.completeSession(currentEntry!!.id)
                
                // Generate session analysis
                val analysis = speechAnalysisService.generateSessionAnalysis(
                    entryId = currentEntry!!.id,
                    turns = _uiState.value.conversationTurns,
                    errors = verbalJournalRepository.getSessionErrors(currentEntry!!.id)
                )
                
                verbalJournalRepository.saveSessionAnalysis(analysis)
                
                // Generate recommendations
                val recommendations = conversationService.generateRecommendations(
                    analysis = analysis,
                    userProfile = userProfile!!
                )
                
                verbalJournalRepository.addRecommendations(analysis.id, recommendations)
                
                // Update user statistics
                val sessionDuration = _uiState.value.conversationTurns
                    .filter { it.role == ConversationRole.USER }
                    .sumOf { it.durationSeconds.toInt() } / 60
                
                verbalJournalRepository.updateProfileStatistics(
                    userId = userProfile!!.userId,
                    sessionCompleted = true,
                    minutesSpoken = sessionDuration,
                    lastSessionDate = LocalDateTime.now()
                )
                
                // Check achievements
                val unlockedAchievements = verbalJournalRepository.checkAndUnlockAchievements(
                    profileId = userProfile!!.id
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        sessionStatus = SessionStatus.COMPLETED,
                        sessionAnalysis = analysis,
                        recommendations = recommendations,
                        unlockedAchievements = unlockedAchievements,
                        showResults = true
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to end session: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun skipToNextPhase() {
        val nextPhase = when (_uiState.value.currentPhase) {
            ConversationPhase.GREETING -> ConversationPhase.WARM_UP
            ConversationPhase.WARM_UP -> ConversationPhase.MAIN_TOPIC
            ConversationPhase.MAIN_TOPIC -> ConversationPhase.DEEP_DIVE
            ConversationPhase.DEEP_DIVE -> ConversationPhase.WRAP_UP
            ConversationPhase.WRAP_UP -> ConversationPhase.FEEDBACK
            ConversationPhase.FEEDBACK -> ConversationPhase.FEEDBACK
        }
        
        conversationContext = conversationContext?.copy(currentPhase = nextPhase)
        _uiState.update { it.copy(currentPhase = nextPhase) }
        
        // Generate appropriate AI response for new phase
        generateAIResponse()
    }
    
    fun requestHint() {
        viewModelScope.launch {
            val hint = conversationService.generateHint(
                context = conversationContext!!,
                userLevel = userProfile!!.proficiencyLevel
            )
            
            _uiState.update { 
                it.copy(currentHint = hint)
            }
        }
    }
    
    fun reportTechnicalIssue(issue: String) {
        // Log technical issue
        viewModelScope.launch {
            // Implementation for logging issues
        }
    }
    
    private fun createDefaultProfile(userId: Int): VerbalJournalProfile {
        val profile = VerbalJournalProfile(
            id = UUID.randomUUID().toString(),
            userId = userId,
            proficiencyLevel = ProficiencyLevel.INTERMEDIATE,
            nativeLanguage = "Unknown",
            targetLanguage = "en",
            preferences = UserPreferences(
                preferredTopics = emptyList(),
                preferredSessionTime = null,
                enableReminders = true,
                enableBreakInPeriod = true
            ),
            goals = UserGoals(
                dailyGoalMinutes = 10,
                weeklyGoalSessions = 5
            ),
            statistics = UserStatistics(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        viewModelScope.launch {
            verbalJournalRepository.createOrUpdateProfile(profile)
        }
        
        return profile
    }
    
    private fun determineSessionGoals(): SessionGoals {
        return SessionGoals(
            targetDuration = userProfile?.goals?.dailyGoalMinutes ?: 10,
            focusAreas = listOf(ImprovementAreaType.FLUENCY, ImprovementAreaType.PRONUNCIATION),
            specificObjectives = listOf("Practice natural conversation", "Build confidence"),
            difficultyLevel = userProfile?.proficiencyLevel?.toDifficulty() ?: DifficultyLevel.INTERMEDIATE
        )
    }
    
    private fun determineConversationPhase(turnCount: Int): ConversationPhase {
        return when {
            turnCount <= 2 -> ConversationPhase.GREETING
            turnCount <= 4 -> ConversationPhase.WARM_UP
            turnCount <= 10 -> ConversationPhase.MAIN_TOPIC
            turnCount <= 16 -> ConversationPhase.DEEP_DIVE
            turnCount <= 18 -> ConversationPhase.WRAP_UP
            else -> ConversationPhase.FEEDBACK
        }
    }
    
    private fun updateBreakInPeriodStatus() {
        viewModelScope.launch {
            val isInBreakIn = verbalJournalRepository.isUserInBreakInPeriod(userProfile!!.userId)
            val weekInProgram = verbalJournalRepository.getWeekInProgram(userProfile!!.userId)
            val schedule = weekInProgram?.let { 
                verbalJournalRepository.getBreakInSchedule(userProfile!!.userId)
            }
            
            _uiState.update { 
                it.copy(
                    isBreakInPeriod = isInBreakIn,
                    weekInProgram = weekInProgram,
                    breakInSchedule = schedule
                )
            }
        }
    }
    
    private fun loadDailyPrompt() {
        viewModelScope.launch {
            val prompt = verbalJournalRepository.getDailyPrompt()
            _uiState.update { 
                it.copy(
                    dailyPrompt = prompt,
                    suggestedTopics = prompt?.let { listOf(it.promptText) } ?: emptyList()
                )
            }
        }
    }
    
    private fun showTopicSuggestions() {
        viewModelScope.launch {
            val recentEntries = verbalJournalRepository.getRecentCompletedEntries(
                userId = userProfile!!.userId,
                limit = 5
            )
            val recentTopics = recentEntries.mapNotNull { it.topic }
            
            val suggestion = TopicSuggestionEngine.suggestTopic(
                profile = userProfile!!,
                recentTopics = recentTopics,
                currentDate = java.time.LocalDate.now(),
                sessionType = SessionType.TOPIC_BASED
            )
            
            _uiState.update { 
                it.copy(
                    suggestedTopics = listOf(suggestion.topic) + getAdditionalTopicSuggestions(),
                    topicScaffolding = suggestion.scaffolding,
                    topicVocabulary = suggestion.vocabulary
                )
            }
        }
    }
    
    private fun startFreeConversation() {
        selectTopic("Let's have a conversation!")
    }
    
    private fun getAdditionalTopicSuggestions(): List<String> {
        return when (userProfile?.proficiencyLevel) {
            ProficiencyLevel.BEGINNER -> listOf(
                "My daily routine",
                "My favorite food",
                "My family"
            )
            ProficiencyLevel.INTERMEDIATE -> listOf(
                "A memorable trip",
                "Technology in my life",
                "My hobbies and interests"
            )
            else -> listOf(
                "Current events",
                "Future goals",
                "Cultural experiences"
            )
        }
    }
    
    private fun startRecordingTimer() {
        viewModelScope.launch {
            while (_recordingState.value == RecordingState.RECORDING) {
                val elapsed = (System.currentTimeMillis() - recordingStartTime) / 1000
                _uiState.update { 
                    it.copy(recordingDuration = elapsed.toInt())
                }
                delay(100)
            }
        }
    }
    
    private fun checkSessionCompletion() {
        val sessionDuration = _uiState.value.conversationTurns
            .filter { it.role == ConversationRole.USER }
            .sumOf { it.durationSeconds } / 60
        
        if (sessionDuration >= (userProfile?.goals?.dailyGoalMinutes ?: 10)) {
            // Suggest ending session
            _uiState.update { 
                it.copy(suggestEndSession = true)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        // Clean up any temporary audio files
    }
}

// UI State
data class VerbalJournalSessionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sessionStatus: SessionStatus = SessionStatus.IN_PROGRESS,
    val showTopicSelection: Boolean = false,
    val selectedSessionType: SessionType? = null,
    val suggestedTopics: List<String> = emptyList(),
    val topicScaffolding: List<String> = emptyList(),
    val topicVocabulary: List<String> = emptyList(),
    val dailyPrompt: DailyPrompt? = null,
    val currentTopic: String? = null,
    val conversationTurns: List<ConversationTurn> = emptyList(),
    val currentPhase: ConversationPhase = ConversationPhase.GREETING,
    val lastUserTranscription: String = "",
    val lastAIResponse: AIResponse? = null,
    val lastSpeechMetrics: SpeechMetrics? = null,
    val detectedErrors: List<SpeechError> = emptyList(),
    val isAIThinking: Boolean = false,
    val recordingDuration: Int = 0,
    val currentHint: String? = null,
    val isBreakInPeriod: Boolean = false,
    val weekInProgram: Int? = null,
    val breakInSchedule: BreakInSchedule? = null,
    val suggestEndSession: Boolean = false,
    val showResults: Boolean = false,
    val sessionAnalysis: SessionAnalysis? = null,
    val recommendations: List<SessionRecommendation> = emptyList(),
    val unlockedAchievements: List<Achievement> = emptyList()
)

enum class RecordingState {
    IDLE,
    RECORDING,
    PROCESSING
}

data class TranscriptionState(
    val isTranscribing: Boolean = false,
    val transcribedText: String = "",
    val confidence: Float = 0f
)

// Extension functions
private fun ProficiencyLevel.toDifficulty(): DifficultyLevel {
    return when (this) {
        ProficiencyLevel.BEGINNER, ProficiencyLevel.ELEMENTARY -> DifficultyLevel.BEGINNER
        ProficiencyLevel.INTERMEDIATE, ProficiencyLevel.UPPER_INTERMEDIATE -> DifficultyLevel.INTERMEDIATE
        ProficiencyLevel.ADVANCED, ProficiencyLevel.PROFICIENT -> DifficultyLevel.ADVANCED
    }
}