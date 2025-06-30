package com.studywise.ai.presentation.screens.verbaljournal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.repository.UserRepository
import com.studywise.ai.domain.usecase.verbaljournal.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerbalJournalHistoryViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getHistoryUseCase: GetVerbalJournalHistoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    
    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser().first()
                    ?: throw IllegalStateException("User not logged in")
                
                getHistoryUseCase(currentUser.id, _filterState.value.filter)
                    .collect { sessions ->
                        _uiState.value = HistoryUiState.Success(sessions)
                    }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(
                    e.message ?: "Failed to load history"
                )
            }
        }
    }
    
    fun applyFilter(filter: SessionHistoryFilter) {
        _filterState.value = FilterState(
            filter = filter,
            isActive = filter.status != null || 
                      filter.minDuration != null || 
                      filter.minAccuracy != null ||
                      filter.topics.isNotEmpty()
        )
        loadHistory()
    }
    
    fun clearFilter() {
        _filterState.value = FilterState()
        loadHistory()
    }
    
    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
    }
    
    fun refresh() {
        loadHistory()
    }
}

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val sessions: List<SessionHistoryItem>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

data class FilterState(
    val filter: SessionHistoryFilter = SessionHistoryFilter(),
    val isActive: Boolean = false
)