package com.studywise.ai.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.domain.repository.SchoolRepository
import com.studywise.ai.domain.repository.UserRepository
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.School
import com.studywise.ai.domain.model.SchoolType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SchoolSelectionUiState(
    val searchQuery: String = "",
    val schools: List<School> = emptyList(),
    val selectedSchool: School? = null,
    val selectedType: SchoolType = SchoolType.ALL,
    val isLoading: Boolean = false,
    val showManualEntry: Boolean = false,
    val manualSchoolName: String = "",
    val manualSchoolCity: String = ""
)

@HiltViewModel
class SchoolSelectionViewModel @Inject constructor(
    private val schoolRepository: SchoolRepository,
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolSelectionUiState())
    val uiState: StateFlow<SchoolSelectionUiState> = _uiState.asStateFlow()

    init {
        loadPopularSchools()
    }

    private fun loadPopularSchools() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // For MVP, use mock data
            val mockSchools = listOf(
                School(
                    id = "1",
                    name = "Lincoln High School",
                    city = "San Francisco",
                    state = "CA",
                    type = SchoolType.PUBLIC
                ),
                School(
                    id = "2",
                    name = "St. Mary's Academy",
                    city = "Los Angeles",
                    state = "CA",
                    type = SchoolType.PRIVATE
                ),
                School(
                    id = "3",
                    name = "Washington Middle School",
                    city = "Seattle",
                    state = "WA",
                    type = SchoolType.PUBLIC
                ),
                School(
                    id = "4",
                    name = "Homeschool Network",
                    city = "",
                    state = "",
                    type = SchoolType.HOMESCHOOL
                )
            )
            
            _uiState.value = _uiState.value.copy(
                schools = mockSchools,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isEmpty()) {
            loadPopularSchools()
        }
    }

    fun searchSchools() {
        if (_uiState.value.searchQuery.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // For MVP, filter mock data
            val allSchools = listOf(
                School("1", "Lincoln High School", "San Francisco", "CA", SchoolType.PUBLIC),
                School("2", "St. Mary's Academy", "Los Angeles", "CA", SchoolType.PRIVATE),
                School("3", "Washington Middle School", "Seattle", "WA", SchoolType.PUBLIC),
                School("4", "Homeschool Network", "", "", SchoolType.HOMESCHOOL),
                School("5", "Jefferson Elementary", "Portland", "OR", SchoolType.PUBLIC),
                School("6", "Private Learning Center", "Phoenix", "AZ", SchoolType.PRIVATE)
            )
            
            val query = _uiState.value.searchQuery.lowercase()
            val filteredSchools = allSchools.filter { school ->
                school.name.lowercase().contains(query) ||
                school.city.lowercase().contains(query) ||
                school.state.lowercase().contains(query)
            }.filter { school ->
                _uiState.value.selectedType == SchoolType.ALL || 
                school.type == _uiState.value.selectedType
            }
            
            _uiState.value = _uiState.value.copy(
                schools = filteredSchools,
                isLoading = false
            )
        }
    }

    fun onSchoolTypeSelected(type: SchoolType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        searchSchools()
    }

    fun onSchoolSelected(school: School) {
        _uiState.value = _uiState.value.copy(selectedSchool = school)
    }

    fun onManualEntryClick() {
        _uiState.value = _uiState.value.copy(showManualEntry = true)
    }

    fun dismissManualEntry() {
        _uiState.value = _uiState.value.copy(
            showManualEntry = false,
            manualSchoolName = "",
            manualSchoolCity = ""
        )
    }

    fun onManualSchoolNameChange(name: String) {
        _uiState.value = _uiState.value.copy(manualSchoolName = name)
    }

    fun onManualSchoolCityChange(city: String) {
        _uiState.value = _uiState.value.copy(manualSchoolCity = city)
    }

    fun confirmManualEntry() {
        val manualSchool = School(
            id = "manual_${System.currentTimeMillis()}",
            name = _uiState.value.manualSchoolName,
            city = _uiState.value.manualSchoolCity,
            type = SchoolType.PRIVATE
        )
        _uiState.value = _uiState.value.copy(
            selectedSchool = manualSchool,
            showManualEntry = false
        )
    }

    fun confirmSelection() {
        // Save selected school to preferences
        viewModelScope.launch {
            _uiState.value.selectedSchool?.let { school ->
                try {
                    // Save school ID to preferences
                    preferencesManager.updateSchoolId(school.id)
                    
                    // In a full implementation, we might also:
                    // 1. Update user profile with school information
                    // 2. Load school-specific content or settings
                    // 3. Notify analytics about school selection
                    
                } catch (e: Exception) {
                    // Handle error - in a real app you'd show an error message
                    println("Failed to save school selection: ${e.message}")
                }
            }
        }
    }
}