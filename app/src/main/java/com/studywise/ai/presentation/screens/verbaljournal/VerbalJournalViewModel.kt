package com.studywise.ai.presentation.screens.verbaljournal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.model.verbaljournal.JournalPrompt
import com.studywise.ai.domain.repository.UserRepository
import com.studywise.ai.domain.service.VerbalJournalConversationService
import com.studywise.ai.domain.usecase.verbaljournal.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class VerbalJournalViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getProgressUseCase: GetVerbalJournalProgressUseCase,
    private val getHistoryUseCase: GetVerbalJournalHistoryUseCase,
    private val manageBreakInUseCase: ManageBreakInPeriodUseCase,
    private val startSessionUseCase: StartVerbalJournalSessionUseCase,
    private val conversationService: VerbalJournalConversationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<VerbalJournalUiState>(VerbalJournalUiState.Loading)
    val uiState: StateFlow<VerbalJournalUiState> = _uiState.asStateFlow()
    
    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()
    
    init {
        loadVerbalJournalData()
    }
    
    private fun loadVerbalJournalData() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser().first()
                    ?: throw IllegalStateException("User not logged in")
                
                // Load all data in parallel
                combine(
                    getProgressUseCase(currentUser.id),
                    getHistoryUseCase(currentUser.id),
                    manageBreakInUseCase(currentUser.id),
                    flow { 
                        emit(
                            conversationService.getJournalPrompt(
                                Date(),
                                getProgressUseCase(currentUser.id).first().profile
                            )
                        )
                    }
                ) { progress, history, breakInStatus, todayPrompt ->
                    VerbalJournalUiState.Success(
                        progress = progress,
                        recentSessions = history,
                        breakInStatus = breakInStatus as BreakInPeriodStatus.Success,
                        todayPrompt = todayPrompt
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = VerbalJournalUiState.Error(
                    e.message ?: "Failed to load Verbal Journal data"
                )
            }
        }
    }
    
    fun startNewSession() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser().first()
                    ?: throw IllegalStateException("User not logged in")
                
                startSessionUseCase(currentUser.id).collect { result ->
                    when (result) {
                        is StartSessionResult.Success -> {
                            _sessionId.value = result.sessionId
                        }
                        is StartSessionResult.Error -> {
                            _uiState.value = VerbalJournalUiState.Error(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = VerbalJournalUiState.Error(
                    e.message ?: "Failed to start session"
                )
            }
        }
    }
    
    fun refresh() {
        loadVerbalJournalData()
    }
}

sealed class VerbalJournalUiState {
    object Loading : VerbalJournalUiState()
    
    data class Success(
        val progress: VerbalJournalProgress,
        val recentSessions: List<SessionHistoryItem>,
        val breakInStatus: BreakInPeriodStatus.Success,
        val todayPrompt: JournalPrompt
    ) : VerbalJournalUiState()
    
    data class Error(val message: String) : VerbalJournalUiState()
}