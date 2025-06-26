package com.studywise.ai.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.*
import com.studywise.ai.domain.repository.ProgressRepository
import com.studywise.ai.domain.repository.SchoolRepository
import com.studywise.ai.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ClassStats(
    val totalStudents: Int,
    val activeToday: Int,
    val averageMinutesPerStudent: Int,
    val topPerformers: List<StudentSummary>,
    val strugglingStudents: List<StudentSummary>,
    val subjectDistribution: Map<String, Int>
)

data class StudentSummary(
    val studentId: String,
    val name: String,
    val gradeLevel: Int,
    val profileImage: String?,
    val overallProgress: Double,
    val weeklyMinutes: Int,
    val accuracy: Float,
    val lastActive: Date?,
    val currentStreak: Int,
    val totalPoints: Int,
    val strongSubjects: List<String>,
    val needsHelpWith: List<String>,
    val attendanceRate: Float
)

data class TeacherDashboardUiState(
    val isLoading: Boolean = true,
    val classStats: ClassStats? = null,
    val allStudents: List<StudentSummary> = emptyList(),
    val selectedStudentId: String? = null,
    val selectedStudentDetails: StudentProgressDetails? = null,
    val filter: StudentFilter = StudentFilter.ALL,
    val sortBy: SortOption = SortOption.NAME,
    val announcements: List<Announcement> = emptyList(),
    val upcomingAssignments: List<Assignment> = emptyList(),
    val error: String? = null
)

enum class StudentFilter {
    ALL,
    TOP_PERFORMERS,
    NEEDS_ATTENTION,
    INACTIVE
}

enum class SortOption {
    NAME,
    PROGRESS,
    LAST_ACTIVE,
    POINTS
}

data class StudentProgressDetails(
    val studentInfo: StudentSummary,
    val weeklyProgress: WeeklyProgress,
    val subjectProgress: List<SubjectProgress>,
    val recentAchievements: List<Achievement>,
    val learningGoals: List<String>,
    val parentContact: ParentContact?
)

data class ParentContact(
    val parentName: String,
    val parentEmail: String,
    val parentPhone: String?
)

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val date: Date,
    val priority: AnnouncementPriority
)

enum class AnnouncementPriority {
    LOW, MEDIUM, HIGH
}

data class Assignment(
    val id: String,
    val title: String,
    val subject: String,
    val dueDate: Date,
    val completionRate: Float
)

@HiltViewModel
class TeacherDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository,
    private val schoolRepository: SchoolRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherDashboardUiState())
    val uiState: StateFlow<TeacherDashboardUiState> = _uiState.asStateFlow()

    private val teacherId = preferencesManager.userPreferences
        .map { it.userId ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    init {
        loadClassData()
        loadAnnouncements()
        loadUpcomingAssignments()
    }

    private fun loadClassData() {
        viewModelScope.launch {
            teacherId.collect { currentTeacherId ->
                if (currentTeacherId.isNotEmpty()) {
                    _uiState.update { it.copy(isLoading = true) }
                    
                    // Get all students for this teacher's class
                    userRepository.getStudentsForTeacher(currentTeacherId).fold(
                        onSuccess = { students ->
                            val studentSummaries = mutableListOf<StudentSummary>()
                            
                            students.forEach { student ->
                                val summary = loadStudentSummary(student)
                                studentSummaries.add(summary)
                            }
                            
                            val classStats = calculateClassStats(studentSummaries)
                            
                            _uiState.update { 
                                it.copy(
                                    allStudents = applyFilterAndSort(studentSummaries, it.filter, it.sortBy),
                                    classStats = classStats,
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

    private suspend fun loadStudentSummary(student: User): StudentSummary {
        val weeklyProgress = progressRepository.getWeeklyProgress(student.id).getOrNull()
        val currentStreak = progressRepository.getDailyStreak(student.id).getOrNull() ?: 0
        val totalPoints = progressRepository.getTotalPoints(student.id).getOrNull() ?: 0
        val recentSessions = progressRepository.getRecentSessions(student.id, 10).getOrNull()
        val subjectProgress = progressRepository.getAllSubjectsProgress(student.id).getOrNull() ?: emptyList()
        
        val weeklyMinutes = weeklyProgress?.totalMinutes ?: 0
        val accuracy = weeklyProgress?.averageAccuracy ?: 0f
        val overallProgress = subjectProgress.map { it.averageMastery }.average() * 100
        
        val strongSubjects = subjectProgress
            .filter { it.averageMastery > 0.7 }
            .map { it.subject }
            .take(3)
            
        val needsHelpWith = subjectProgress
            .filter { it.averageMastery < 0.5 }
            .map { it.subject }
            .take(3)
        
        // Calculate attendance rate based on expected sessions vs actual
        val expectedSessionsPerWeek = 5
        val actualSessions = weeklyProgress?.dailyStats?.count { it.sessionsCompleted > 0 } ?: 0
        val attendanceRate = (actualSessions.toFloat() / expectedSessionsPerWeek).coerceIn(0f, 1f)
        
        return StudentSummary(
            studentId = student.id,
            name = student.name,
            gradeLevel = student.gradeLevel ?: 1,
            profileImage = student.profileImage,
            overallProgress = overallProgress,
            weeklyMinutes = weeklyMinutes,
            accuracy = accuracy,
            lastActive = recentSessions?.firstOrNull()?.completedAt,
            currentStreak = currentStreak,
            totalPoints = totalPoints,
            strongSubjects = strongSubjects,
            needsHelpWith = needsHelpWith,
            attendanceRate = attendanceRate
        )
    }

    private fun calculateClassStats(students: List<StudentSummary>): ClassStats {
        val activeToday = students.count { student ->
            student.lastActive?.let { lastActive ->
                val today = Date()
                val diff = today.time - lastActive.time
                diff < 24 * 60 * 60 * 1000 // Active within last 24 hours
            } ?: false
        }
        
        val avgMinutes = if (students.isNotEmpty()) {
            students.sumOf { it.weeklyMinutes } / students.size
        } else 0
        
        val topPerformers = students
            .sortedByDescending { it.overallProgress }
            .take(5)
            
        val strugglingStudents = students
            .filter { it.overallProgress < 50 || it.attendanceRate < 0.5 }
            .sortedBy { it.overallProgress }
            .take(5)
            
        val subjectDistribution = students
            .flatMap { it.strongSubjects }
            .groupingBy { it }
            .eachCount()
        
        return ClassStats(
            totalStudents = students.size,
            activeToday = activeToday,
            averageMinutesPerStudent = avgMinutes,
            topPerformers = topPerformers,
            strugglingStudents = strugglingStudents,
            subjectDistribution = subjectDistribution
        )
    }

    private fun applyFilterAndSort(
        students: List<StudentSummary>,
        filter: StudentFilter,
        sortBy: SortOption
    ): List<StudentSummary> {
        val filtered = when (filter) {
            StudentFilter.ALL -> students
            StudentFilter.TOP_PERFORMERS -> students.filter { it.overallProgress >= 70 }
            StudentFilter.NEEDS_ATTENTION -> students.filter { 
                it.overallProgress < 50 || it.attendanceRate < 0.5 || it.needsHelpWith.isNotEmpty()
            }
            StudentFilter.INACTIVE -> students.filter { student ->
                student.lastActive?.let { lastActive ->
                    val now = Date()
                    val diff = now.time - lastActive.time
                    diff > 3 * 24 * 60 * 60 * 1000 // Inactive for more than 3 days
                } ?: true
            }
        }
        
        return when (sortBy) {
            SortOption.NAME -> filtered.sortedBy { it.name }
            SortOption.PROGRESS -> filtered.sortedByDescending { it.overallProgress }
            SortOption.LAST_ACTIVE -> filtered.sortedByDescending { it.lastActive?.time ?: 0 }
            SortOption.POINTS -> filtered.sortedByDescending { it.totalPoints }
        }
    }

    fun selectStudent(studentId: String) {
        _uiState.update { it.copy(selectedStudentId = studentId) }
        loadStudentDetails(studentId)
    }

    private fun loadStudentDetails(studentId: String) {
        viewModelScope.launch {
            val student = _uiState.value.allStudents.find { it.studentId == studentId }
            if (student != null) {
                val weeklyProgress = progressRepository.getWeeklyProgress(studentId).getOrNull()
                val subjects = progressRepository.getAllSubjectsProgress(studentId).getOrNull()
                val achievements = progressRepository.getRecentAchievements(studentId, 5).getOrNull()
                val parentInfo = userRepository.getParentForChild(studentId).getOrNull()
                
                if (weeklyProgress != null && subjects != null && achievements != null) {
                    val parentContact = parentInfo?.let {
                        ParentContact(
                            parentName = it.name,
                            parentEmail = it.email,
                            parentPhone = null // Would come from additional profile data
                        )
                    }
                    
                    val details = StudentProgressDetails(
                        studentInfo = student,
                        weeklyProgress = weeklyProgress,
                        subjectProgress = subjects,
                        recentAchievements = achievements,
                        learningGoals = generateLearningGoals(student, subjects),
                        parentContact = parentContact
                    )
                    
                    _uiState.update { it.copy(selectedStudentDetails = details) }
                }
            }
        }
    }

    private fun generateLearningGoals(
        student: StudentSummary,
        subjects: List<SubjectProgress>
    ): List<String> {
        val goals = mutableListOf<String>()
        
        // Add goals for struggling subjects
        student.needsHelpWith.forEach { subject ->
            goals.add("Improve $subject performance to at least 50%")
        }
        
        // Add attendance goal if needed
        if (student.attendanceRate < 0.8f) {
            goals.add("Increase attendance rate to 80%")
        }
        
        // Add streak goal
        if (student.currentStreak < 5) {
            goals.add("Maintain a 5-day learning streak")
        }
        
        // Add general improvement goals
        if (goals.size < 3) {
            goals.add("Complete at least 30 minutes of practice daily")
            goals.add("Achieve 80% accuracy in all subjects")
        }
        
        return goals.take(5)
    }

    fun setFilter(filter: StudentFilter) {
        _uiState.update { currentState ->
            currentState.copy(
                filter = filter,
                allStudents = applyFilterAndSort(
                    currentState.allStudents,
                    filter,
                    currentState.sortBy
                )
            )
        }
    }

    fun setSortOption(sortOption: SortOption) {
        _uiState.update { currentState ->
            currentState.copy(
                sortBy = sortOption,
                allStudents = applyFilterAndSort(
                    currentState.allStudents,
                    currentState.filter,
                    sortOption
                )
            )
        }
    }

    private fun loadAnnouncements() {
        // For MVP, load sample announcements
        val sampleAnnouncements = listOf(
            Announcement(
                id = "1",
                title = "Parent-Teacher Conference Next Week",
                content = "Please remind parents about the upcoming conference on Friday",
                date = Date(),
                priority = AnnouncementPriority.HIGH
            ),
            Announcement(
                id = "2",
                title = "New Reading Materials Available",
                content = "Check out the new grade-appropriate books in the library",
                date = Date(System.currentTimeMillis() - 86400000),
                priority = AnnouncementPriority.MEDIUM
            )
        )
        
        _uiState.update { it.copy(announcements = sampleAnnouncements) }
    }

    private fun loadUpcomingAssignments() {
        // For MVP, load sample assignments
        val sampleAssignments = listOf(
            Assignment(
                id = "1",
                title = "Chapter 5 Reading",
                subject = "English",
                dueDate = Date(System.currentTimeMillis() + 3 * 86400000),
                completionRate = 0.65f
            ),
            Assignment(
                id = "2",
                title = "Math Practice Problems",
                subject = "Mathematics",
                dueDate = Date(System.currentTimeMillis() + 5 * 86400000),
                completionRate = 0.45f
            )
        )
        
        _uiState.update { it.copy(upcomingAssignments = sampleAssignments) }
    }

    fun refreshData() {
        loadClassData()
        loadAnnouncements()
        loadUpcomingAssignments()
    }
}