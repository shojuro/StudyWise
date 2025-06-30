package com.studywise.ai.presentation.screens.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.entity.StudentSkillMasteryEntity
import com.studywise.ai.domain.repository.ProgressRepository
import com.studywise.ai.domain.repository.QuestionRepository
import com.studywise.ai.domain.usecase.education.GetStudentProgressUseCase
import com.studywise.ai.domain.usecase.education.GetSkillMasteryUseCase
import com.studywise.ai.domain.usecase.GetUserStatsUseCase
import com.studywise.ai.domain.gamification.Achievement
import com.studywise.ai.domain.gamification.AchievementRarity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * ViewModel for Student Analytics Dashboard
 */
@HiltViewModel
class StudentAnalyticsViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val questionRepository: QuestionRepository,
    private val getStudentProgressUseCase: GetStudentProgressUseCase,
    private val getSkillMasteryUseCase: GetSkillMasteryUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase
) : ViewModel() {
    
    private val _analyticsState = MutableStateFlow<AnalyticsState>(AnalyticsState.Loading)
    val analyticsState: StateFlow<AnalyticsState> = _analyticsState.asStateFlow()
    
    private val _selectedTimeRange = MutableStateFlow(TimeRange.WEEK)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()
    
    init {
        loadAnalytics()
    }
    
    fun updateTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
        loadAnalytics()
    }
    
    fun refreshAnalytics() {
        loadAnalytics()
    }
    
    private fun loadAnalytics() {
        viewModelScope.launch {
            _analyticsState.value = AnalyticsState.Loading
            
            try {
                // Get student ID (in production, this would come from auth service)
                val studentId = "current_student_id"
                
                // Fetch all analytics data
                val overallProgress = fetchOverallProgress(studentId)
                val performanceMetrics = fetchPerformanceMetrics(studentId)
                val skillMastery = fetchSkillMastery(studentId)
                val learningPatterns = fetchLearningPatterns(studentId)
                val subjectPerformance = fetchSubjectPerformance(studentId)
                val recentAchievements = fetchRecentAchievements(studentId)
                val studyTimeData = fetchStudyTimeData(studentId)
                val recommendations = generateRecommendations(
                    overallProgress,
                    performanceMetrics,
                    skillMastery,
                    learningPatterns
                )
                
                _analyticsState.value = AnalyticsState.Success(
                    AnalyticsData(
                        overallProgress = overallProgress,
                        performanceMetrics = performanceMetrics,
                        skillMastery = skillMastery,
                        learningPatterns = learningPatterns,
                        subjectPerformance = subjectPerformance,
                        recentAchievements = recentAchievements,
                        studyTimeData = studyTimeData,
                        recommendations = recommendations
                    )
                )
            } catch (e: Exception) {
                _analyticsState.value = AnalyticsState.Error(
                    message = "Failed to load analytics: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun fetchOverallProgress(studentId: String): OverallProgress {
        val progress = getStudentProgressUseCase(studentId).getOrThrow()
        
        return OverallProgress(
            completionRate = progress.overallCompletion,
            skillsMastered = progress.skillsMastered,
            currentStreak = progress.currentStreak,
            totalPoints = progress.totalPoints
        )
    }
    
    private suspend fun fetchPerformanceMetrics(studentId: String): PerformanceMetrics {
        val stats = getUserStatsUseCase(studentId, _selectedTimeRange.value).getOrThrow()
        
        return PerformanceMetrics(
            accuracyRate = stats.accuracyRate,
            accuracyTrend = stats.accuracyTrend,
            avgResponseTime = stats.avgResponseTime,
            responseTimeTrend = stats.responseTimeTrend,
            questionsPerDay = stats.questionsPerDay,
            questionsPerDayTrend = stats.questionsPerDayTrend,
            improvementRate = stats.improvementRate,
            improvementTrend = stats.improvementTrend
        )
    }
    
    private suspend fun fetchSkillMastery(studentId: String): List<SkillMasteryData> {
        return getSkillMasteryUseCase(studentId).getOrThrow().map { mastery ->
            SkillMasteryData(
                skillName = mastery.skillName,
                skillCode = mastery.skillCode,
                mastery = mastery.masteryLevel,
                questionsAnswered = mastery.questionsAnswered,
                correctAnswers = mastery.correctAnswers,
                recentImprovement = mastery.recentImprovement
            )
        }
    }
    
    private suspend fun fetchLearningPatterns(studentId: String): LearningPatterns {
        // In production, this would analyze actual learning data
        return LearningPatterns(
            bestLearningTime = "3:00 PM - 5:00 PM",
            averageSessionDuration = 25,
            preferredDifficulty = "Medium",
            learningStyles = listOf(
                LearningStylePreference("Visual", 0.7f),
                LearningStylePreference("Interactive", 0.6f),
                LearningStylePreference("Reading", 0.4f)
            ),
            focusAreas = listOf("Character Analysis", "Theme Identification")
        )
    }
    
    private suspend fun fetchSubjectPerformance(studentId: String): List<SubjectPerformance> {
        // In production, would fetch actual subject data
        return listOf(
            SubjectPerformance(
                name = "Reading",
                progress = 0.75f,
                questionsCompleted = 150,
                accuracy = 0.82f,
                icon = Icons.Default.Book,
                color = Color(0xFF4CAF50)
            ),
            SubjectPerformance(
                name = "Writing",
                progress = 0.60f,
                questionsCompleted = 120,
                accuracy = 0.78f,
                icon = Icons.Default.Create,
                color = Color(0xFF2196F3)
            ),
            SubjectPerformance(
                name = "Grammar",
                progress = 0.85f,
                questionsCompleted = 200,
                accuracy = 0.88f,
                icon = Icons.Default.Spellcheck,
                color = Color(0xFF9C27B0)
            ),
            SubjectPerformance(
                name = "Vocabulary",
                progress = 0.70f,
                questionsCompleted = 180,
                accuracy = 0.75f,
                icon = Icons.Default.TextFields,
                color = Color(0xFFFF9800)
            )
        )
    }
    
    private suspend fun fetchRecentAchievements(studentId: String): List<Achievement> {
        // In production, would fetch from achievements repository
        return listOf(
            Achievement(
                id = "speed_reader",
                name = "Speed Reader",
                description = "Read 10 passages in record time",
                iconUrl = "achievement_speed",
                rarity = AchievementRarity.RARE,
                unlockedAt = Date()
            ),
            Achievement(
                id = "streak_master",
                name = "Streak Master",
                description = "Maintained a 7-day streak",
                iconUrl = "achievement_streak",
                rarity = AchievementRarity.EPIC,
                unlockedAt = Date()
            ),
            Achievement(
                id = "accuracy_ace",
                name = "Accuracy Ace",
                description = "Achieved 90% accuracy",
                iconUrl = "achievement_accuracy",
                rarity = AchievementRarity.LEGENDARY,
                unlockedAt = Date()
            )
        )
    }
    
    private suspend fun fetchStudyTimeData(studentId: String): StudyTimeData {
        // In production, would calculate from session data
        val weeklyData = listOf(
            DayStudyData("Monday", 45),
            DayStudyData("Tuesday", 60),
            DayStudyData("Wednesday", 30),
            DayStudyData("Thursday", 75),
            DayStudyData("Friday", 90),
            DayStudyData("Saturday", 120),
            DayStudyData("Sunday", 60)
        )
        
        val totalMinutes = weeklyData.sumOf { it.minutes }
        val dailyAverage = totalMinutes / 7
        val mostProductiveDay = weeklyData.maxByOrNull { it.minutes }?.dayName ?: "Saturday"
        
        return StudyTimeData(
            totalMinutesThisWeek = totalMinutes,
            dailyAverage = dailyAverage,
            mostProductiveDay = mostProductiveDay,
            weeklyData = weeklyData,
            monthlyTrend = 0.15f // 15% increase
        )
    }
    
    private fun generateRecommendations(
        progress: OverallProgress,
        metrics: PerformanceMetrics,
        skillMastery: List<SkillMasteryData>,
        patterns: LearningPatterns
    ): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        // Low accuracy recommendation
        if (metrics.accuracyRate < 0.7f) {
            recommendations.add(
                Recommendation(
                    title = "Improve Accuracy",
                    description = "Your accuracy is below 70%. Try using hints more strategically and take your time with each question.",
                    priority = Priority.HIGH,
                    icon = Icons.Default.TipsAndUpdates,
                    actionable = true
                )
            )
        }
        
        // Weak skill recommendation
        val weakestSkill = skillMastery.minByOrNull { it.mastery }
        if (weakestSkill != null && weakestSkill.mastery < 0.5f) {
            recommendations.add(
                Recommendation(
                    title = "Focus on ${weakestSkill.skillName}",
                    description = "This skill needs more practice. Dedicate 15 minutes daily to improve.",
                    priority = Priority.HIGH,
                    icon = Icons.Default.School,
                    actionable = true
                )
            )
        }
        
        // Study time recommendation
        if (patterns.averageSessionDuration < 20) {
            recommendations.add(
                Recommendation(
                    title = "Extend Study Sessions",
                    description = "Your sessions average ${patterns.averageSessionDuration} minutes. Try to reach 25-30 minutes for better retention.",
                    priority = Priority.MEDIUM,
                    icon = Icons.Default.Timer,
                    actionable = false
                )
            )
        }
        
        // Streak recommendation
        if (progress.currentStreak < 3) {
            recommendations.add(
                Recommendation(
                    title = "Build a Learning Streak",
                    description = "Practice daily to build momentum. You're ${3 - progress.currentStreak} days away from a streak bonus!",
                    priority = Priority.MEDIUM,
                    icon = Icons.Default.LocalFireDepartment,
                    actionable = true
                )
            )
        }
        
        // Optimal time recommendation
        recommendations.add(
            Recommendation(
                title = "Study at Peak Performance Time",
                description = "Your best learning time is ${patterns.bestLearningTime}. Schedule sessions during this window.",
                priority = Priority.LOW,
                icon = Icons.Default.Schedule,
                actionable = false
            )
        )
        
        return recommendations.sortedBy { it.priority.ordinal }
    }
}

// State classes
sealed class AnalyticsState {
    object Loading : AnalyticsState()
    data class Success(val data: AnalyticsData) : AnalyticsState()
    data class Error(val message: String) : AnalyticsState()
}

// Data models
data class AnalyticsData(
    val overallProgress: OverallProgress,
    val performanceMetrics: PerformanceMetrics,
    val skillMastery: List<SkillMasteryData>,
    val learningPatterns: LearningPatterns,
    val subjectPerformance: List<SubjectPerformance>,
    val recentAchievements: List<Achievement>,
    val studyTimeData: StudyTimeData,
    val recommendations: List<Recommendation>
)

data class OverallProgress(
    val completionRate: Float,
    val skillsMastered: Int,
    val currentStreak: Int,
    val totalPoints: Int
)

data class PerformanceMetrics(
    val accuracyRate: Float,
    val accuracyTrend: Float,
    val avgResponseTime: Int,
    val responseTimeTrend: Float,
    val questionsPerDay: Int,
    val questionsPerDayTrend: Float,
    val improvementRate: Float,
    val improvementTrend: Float
)

data class SkillMasteryData(
    val skillName: String,
    val skillCode: String,
    val mastery: Float,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val recentImprovement: Float
)

data class LearningPatterns(
    val bestLearningTime: String,
    val averageSessionDuration: Int,
    val preferredDifficulty: String,
    val learningStyles: List<LearningStylePreference>,
    val focusAreas: List<String>
)

data class LearningStylePreference(
    val name: String,
    val preference: Float
)

data class SubjectPerformance(
    val name: String,
    val progress: Float,
    val questionsCompleted: Int,
    val accuracy: Float,
    val icon: ImageVector,
    val color: Color
)

data class StudyTimeData(
    val totalMinutesThisWeek: Int,
    val dailyAverage: Int,
    val mostProductiveDay: String,
    val weeklyData: List<DayStudyData>,
    val monthlyTrend: Float
)

data class DayStudyData(
    val dayName: String,
    val minutes: Int
)

data class Recommendation(
    val title: String,
    val description: String,
    val priority: Priority,
    val icon: ImageVector,
    val actionable: Boolean
)

enum class Priority(val color: Color) {
    HIGH(Color(0xFFFF5252)),
    MEDIUM(Color(0xFFFF9800)),
    LOW(Color(0xFF4CAF50))
}

enum class TimeRange(val displayName: String) {
    DAY("Today"),
    WEEK("This Week"),
    MONTH("This Month"),
    ALL_TIME("All Time")
}