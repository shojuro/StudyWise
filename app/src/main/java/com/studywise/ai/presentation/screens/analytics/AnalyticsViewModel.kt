package com.studywise.ai.presentation.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.service.AnalyticsServiceImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val totalEventsToday: Int = 0,
    val eventTypes: Map<String, Int> = emptyMap(),
    val activeUserId: String = "anonymous",
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsService: AnalyticsServiceImpl // Using impl to access getAnalyticsSummary
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
    
    init {
        loadAnalytics()
    }
    
    fun refreshAnalytics() {
        loadAnalytics()
    }
    
    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val summary = analyticsService.getAnalyticsSummary()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalEventsToday = summary["total_events_today"] as? Int ?: 0,
                    eventTypes = summary["event_types"] as? Map<String, Int> ?: emptyMap(),
                    activeUserId = summary["active_user_id"] as? String ?: "anonymous"
                )
                
                // Clean up old analytics files periodically
                analyticsService.cleanupOldAnalytics()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load analytics: ${e.message}"
                )
            }
        }
    }
}