package com.studywise.ai.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.*
import com.studywise.ai.domain.repository.ProgressRepository
import com.studywise.ai.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ChildProgressSummary(
    val childId: String,
    val childName: String,
    val gradeLevel: Int,
    val profileImage: String?,
    val todayMinutes: Int,
    val weeklyMinutes: Int,
    val currentStreak: Int,
    val lastActive: Date?,
    val totalPoints: Int,
    val recentSubjects: List<String>,
    val overallProgress: Double
)

data class ParentDashboardUiState(
    val isLoading: Boolean = true,
    val childrenProgress: List<ChildProgressSummary> = emptyList(),
    val selectedChildId: String? = null,
    val selectedChildDetails: ChildProgressDetails? = null,
    val overallFamilyStats: FamilyStats? = null,
    val error: String? = null
)

data class ChildProgressDetails(
    val childInfo: ChildProgressSummary,
    val weeklyProgress: WeeklyProgress,
    val subjectProgress: List<SubjectProgress>,
    val recentAchievements: List<Achievement>,
    val upcomingGoals: List<String>
)

data class FamilyStats(
    val totalChildren: Int,
    val totalMinutesThisWeek: Int,
    val averageMinutesPerChild: Int,
    val mostActiveChild: String?,
    val familyStreak: Int
)

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()

    private val parentId = preferencesManager.userPreferences
        .map { it.userId ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    init {
        loadChildrenData()
    }

    private fun loadChildrenData() {
        viewModelScope.launch {
            parentId.collect { currentParentId ->
                if (currentParentId.isNotEmpty()) {
                    _uiState.update { it.copy(isLoading = true) }
                    
                    userRepository.getChildrenForParent(currentParentId).fold(
                        onSuccess = { children ->
                            val childProgressList = mutableListOf<ChildProgressSummary>()
                            
                            children.forEach { child ->
                                val progressSummary = loadChildProgressSummary(child)
                                childProgressList.add(progressSummary)
                            }
                            
                            val familyStats = calculateFamilyStats(childProgressList)
                            
                            _uiState.update { 
                                it.copy(
                                    childrenProgress = childProgressList,
                                    overallFamilyStats = familyStats,
                                    isLoading = false
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    error = error.message,
                                    isLoading = false
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    private suspend fun loadChildProgressSummary(child: User): ChildProgressSummary {
        val todayStats = progressRepository.getDailyProgress(child.id, Date()).getOrNull()
        val weeklyProgress = progressRepository.getWeeklyProgress(child.id).getOrNull()
        val currentStreak = progressRepository.getDailyStreak(child.id).getOrNull() ?: 0
        val totalPoints = progressRepository.getTotalPoints(child.id).getOrNull() ?: 0
        val recentSessions = progressRepository.getRecentSessions(child.id, 5).getOrNull()
        
        val recentSubjects = recentSessions?.map { it.subject }?.distinct() ?: emptyList()
        val weeklyMinutes = weeklyProgress?.totalMinutes ?: 0
        val overallProgress = weeklyProgress?.dailyStats?.map { it.pointsEarned }?.average() ?: 0.0
        
        return ChildProgressSummary(
            childId = child.id,
            childName = child.name,
            gradeLevel = child.gradeLevel ?: 1,
            profileImage = child.profileImage,
            todayMinutes = todayStats?.minutesStudied ?: 0,
            weeklyMinutes = weeklyMinutes,
            currentStreak = currentStreak,
            lastActive = recentSessions?.firstOrNull()?.completedAt,
            totalPoints = totalPoints,
            recentSubjects = recentSubjects.take(3),
            overallProgress = overallProgress
        )
    }

    private fun calculateFamilyStats(children: List<ChildProgressSummary>): FamilyStats {
        val totalMinutes = children.sumOf { it.weeklyMinutes }
        val avgMinutes = if (children.isNotEmpty()) totalMinutes / children.size else 0
        val mostActive = children.maxByOrNull { it.weeklyMinutes }?.childName
        val minStreak = children.minOfOrNull { it.currentStreak } ?: 0
        
        return FamilyStats(
            totalChildren = children.size,
            totalMinutesThisWeek = totalMinutes,
            averageMinutesPerChild = avgMinutes,
            mostActiveChild = mostActive,
            familyStreak = minStreak
        )
    }

    fun selectChild(childId: String) {
        _uiState.update { it.copy(selectedChildId = childId) }
        loadChildDetails(childId)
    }

    private fun loadChildDetails(childId: String) {
        viewModelScope.launch {
            val childSummary = _uiState.value.childrenProgress.find { it.childId == childId }
            if (childSummary != null) {
                val weeklyProgress = progressRepository.getWeeklyProgress(childId).getOrNull()
                val subjects = progressRepository.getAllSubjectsProgress(childId).getOrNull()
                val achievements = progressRepository.getRecentAchievements(childId, 5).getOrNull()
                
                if (weeklyProgress != null && subjects != null && achievements != null) {
                    val details = ChildProgressDetails(
                        childInfo = childSummary,
                        weeklyProgress = weeklyProgress,
                        subjectProgress = subjects,
                        recentAchievements = achievements,
                        upcomingGoals = generateUpcomingGoals(subjects)
                    )
                    
                    _uiState.update { it.copy(selectedChildDetails = details) }
                }
            }
        }
    }

    private fun generateUpcomingGoals(subjects: List<SubjectProgress>): List<String> {
        val goals = mutableListOf<String>()
        
        subjects.filter { it.averageMastery < 0.7 }.forEach { subject ->
            goals.add("Improve ${subject.subject} mastery to 70%")
        }
        
        if (goals.size < 3) {
            goals.add("Complete 5 learning sessions this week")
            goals.add("Maintain daily streak for 7 days")
        }
        
        return goals.take(3)
    }

    fun refreshData() {
        loadChildrenData()
    }
}