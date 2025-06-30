package com.studywise.ai.presentation.screens.verbaljournal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.repository.UserRepository
import com.studywise.ai.domain.usecase.verbaljournal.GetVerbalJournalProgressUseCase
import com.studywise.ai.domain.usecase.verbaljournal.VerbalJournalProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerbalJournalProgressViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getProgressUseCase: GetVerbalJournalProgressUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
    
    init {
        loadProgress()
    }
    
    private fun loadProgress() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser().first()
                    ?: throw IllegalStateException("User not logged in")
                
                getProgressUseCase(currentUser.id).collect { progress ->
                    _uiState.value = ProgressUiState.Success(progress)
                }
            } catch (e: Exception) {
                _uiState.value = ProgressUiState.Error(
                    e.message ?: "Failed to load progress"
                )
            }
        }
    }
    
    fun refresh() {
        loadProgress()
    }
}

sealed class ProgressUiState {
    object Loading : ProgressUiState()
    data class Success(val progress: VerbalJournalProgress) : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}