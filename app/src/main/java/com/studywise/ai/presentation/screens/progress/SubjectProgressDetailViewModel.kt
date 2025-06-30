package com.studywise.ai.presentation.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.model.SubjectProgress
import com.studywise.ai.domain.repository.ProgressRepository
import com.studywise.ai.domain.service.AnalyticsService
import com.studywise.ai.domain.service.trackSubjectProgressViewed
import com.studywise.ai.domain.service.trackProgressExported
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SubjectProgressDetailViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val preferencesManager: PreferencesManager,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectProgressDetailUiState())
    val uiState: StateFlow<SubjectProgressDetailUiState> = _uiState.asStateFlow()

    private val userId = preferencesManager.userPreferences
        .map { it.userId ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun loadSubjectProgress(subject: String) {
        viewModelScope.launch {
            val currentUserId = userId.value
            if (currentUserId.isNotEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
                
                // Load subject progress
                progressRepository.getSubjectProgress(currentUserId, subject).fold(
                    onSuccess = { subjectProgress ->
                        _uiState.update { 
                            it.copy(
                                subjectProgress = subjectProgress,
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
                
                // Load session history
                loadSessionHistory(currentUserId, subject)
                
                // Generate recommendations
                generateRecommendations(subject)
                
                // Track analytics
                analyticsService.trackSubjectProgressViewed(subject)
            }
        }
    }

    private suspend fun loadSessionHistory(userId: String, subject: String) {
        progressRepository.getRecentSessions(userId, 20).fold(
            onSuccess = { sessions ->
                val subjectSessions = sessions
                    .filter { it.subject == subject }
                    .map { session ->
                        LearningSession(
                            date = session.startedAt,
                            questionsAnswered = session.questionsAnswered,
                            correctAnswers = session.correctAnswers,
                            accuracy = if (session.questionsAnswered > 0) {
                                session.correctAnswers.toFloat() / session.questionsAnswered
                            } else 0f,
                            minutesSpent = if (session.completedAt != null) {
                                ((session.completedAt.time - session.startedAt.time) / 60000).toInt()
                            } else 0,
                            pointsEarned = session.pointsEarned,
                            skills = emptyList() // TODO: Add skills tracking
                        )
                    }
                    .sortedByDescending { it.date }
                
                _uiState.update { it.copy(sessionHistory = subjectSessions) }
            },
            onFailure = { /* Handle error */ }
        )
    }

    private fun generateRecommendations(subject: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val recommendations = mutableListOf<String>()
            
            currentState.subjectProgress?.let { progress ->
                // Based on accuracy
                if (progress.averageAccuracy < 0.6f) {
                    recommendations.add("Focus on understanding core concepts before advancing")
                    recommendations.add("Try easier questions to build confidence")
                } else if (progress.averageAccuracy > 0.9f) {
                    recommendations.add("Challenge yourself with advanced topics")
                    recommendations.add("Consider helping others learn this subject")
                }
                
                // Based on frequency
                val daysSinceLastPractice = progress.lastPracticed?.let {
                    (Date().time - it.time) / (1000 * 60 * 60 * 24)
                } ?: Long.MAX_VALUE
                
                if (daysSinceLastPractice > 7) {
                    recommendations.add("It's been a while! Regular practice helps retention")
                }
                
                // Based on skill gaps
                progress.skillMastery.filter { it.value < 0.5f }.forEach { (skill, _) ->
                    recommendations.add("Strengthen your ${skill.replace("_", " ").lowercase()} skills")
                }
                
                // Based on trend
                when (progress.trend) {
                    com.studywise.ai.domain.model.ProgressTrend.DECLINING -> {
                        recommendations.add("Your performance is declining. Take a break and review basics")
                    }
                    com.studywise.ai.domain.model.ProgressTrend.IMPROVING -> {
                        recommendations.add("Great progress! Keep up the momentum")
                    }
                    com.studywise.ai.domain.model.ProgressTrend.STABLE -> {
                        recommendations.add("Try new learning strategies to break through the plateau")
                    }
                }
            }
            
            _uiState.update { it.copy(recommendations = recommendations.take(3)) }
        }
    }

    fun exportProgress() {
        _uiState.update { it.copy(showExportDialog = true) }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(showExportDialog = false) }
    }

    fun confirmExport(format: ExportFormat) {
        viewModelScope.launch {
            // Track analytics
            analyticsService.trackProgressExported(format.name)
            
            // TODO: Implement actual export functionality
            when (format) {
                ExportFormat.PDF -> exportAsPdf()
                ExportFormat.CSV -> exportAsCsv()
                ExportFormat.JSON -> exportAsJson()
            }
            
            _uiState.update { it.copy(showExportDialog = false) }
        }
    }

    private suspend fun exportAsPdf() {
        val currentState = _uiState.value
        val subjectProgress = currentState.subjectProgress ?: return
        
        // Create PDF content as HTML (which can be converted to PDF)
        val htmlContent = generateProgressHtml(subjectProgress, currentState.sessionHistory, currentState.recommendations)
        
        // In a full implementation, you would use a PDF library like iText or similar
        // For now, we'll save as HTML which can be opened in browser and printed to PDF
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val fileName = "progress_${subjectProgress.subject}_${dateFormatter.format(Date())}.html"
        
        try {
            // Save to app's external files directory
            val file = File("/sdcard/Download", fileName)
            FileWriter(file).use { writer ->
                writer.write(htmlContent)
            }
            
            // In a real app, you would also notify the user and possibly open the file
            println("Progress exported to: ${file.absolutePath}")
        } catch (e: Exception) {
            println("Export failed: ${e.message}")
        }
    }

    private suspend fun exportAsCsv() {
        val currentState = _uiState.value
        val sessionHistory = currentState.sessionHistory
        
        val csvContent = buildString {
            // CSV Header
            appendLine("Date,Accuracy,Minutes Spent,Points Earned,Questions Answered")
            
            // CSV Data
            sessionHistory.forEach { session ->
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                appendLine("${dateFormatter.format(session.date)},${session.accuracy},${session.minutesSpent},${session.pointsEarned},${session.questionsAnswered}")
            }
        }
        
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val fileName = "sessions_${dateFormatter.format(Date())}.csv"
        
        try {
            val file = File("/sdcard/Download", fileName)
            FileWriter(file).use { writer ->
                writer.write(csvContent)
            }
            println("CSV exported to: ${file.absolutePath}")
        } catch (e: Exception) {
            println("Export failed: ${e.message}")
        }
    }

    private suspend fun exportAsJson() {
        val currentState = _uiState.value
        val subjectProgress = currentState.subjectProgress ?: return
        
        val jsonContent = JSONObject().apply {
            put("subject", subjectProgress.subject)
            put("totalSessions", subjectProgress.totalSessions)
            put("totalQuestions", subjectProgress.totalQuestions)
            put("correctAnswers", subjectProgress.correctAnswers)
            put("averageAccuracy", subjectProgress.averageAccuracy)
            put("totalTimeMinutes", subjectProgress.totalTimeMinutes)
            put("lastPracticed", subjectProgress.lastPracticed?.toString() ?: "null")
            
            val skillMasteryJson = JSONObject()
            subjectProgress.skillMastery.forEach { (skill, mastery) ->
                skillMasteryJson.put(skill, mastery)
            }
            put("skillMastery", skillMasteryJson)
            
            val sessionHistoryArray = JSONArray()
            currentState.sessionHistory.forEach { session ->
                val sessionJson = JSONObject().apply {
                    put("date", session.date.toString())
                    put("accuracy", session.accuracy)
                    put("minutesSpent", session.minutesSpent)
                    put("pointsEarned", session.pointsEarned)
                    put("questionsAnswered", session.questionsAnswered)
                }
                sessionHistoryArray.put(sessionJson)
            }
            put("sessionHistory", sessionHistoryArray)
            
            val recommendationsArray = JSONArray()
            currentState.recommendations.forEach { rec ->
                recommendationsArray.put(rec)
            }
            put("recommendations", recommendationsArray)
        }
        
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val fileName = "progress_${subjectProgress.subject}_${dateFormatter.format(Date())}.json"
        
        try {
            val file = File("/sdcard/Download", fileName)
            FileWriter(file).use { writer ->
                writer.write(jsonContent.toString())
            }
            println("JSON exported to: ${file.absolutePath}")
        } catch (e: Exception) {
            println("Export failed: ${e.message}")
        }
    }
    
    private fun generateProgressHtml(
        progress: SubjectProgress,
        sessions: List<LearningSession>,
        recommendations: List<String>
    ): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>${progress.subject} Progress Report</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 40px; }
                .header { text-align: center; margin-bottom: 30px; }
                .stats { display: flex; justify-content: space-around; margin: 20px 0; }
                .stat { text-align: center; }
                .stat-value { font-size: 24px; font-weight: bold; color: #2196F3; }
                .section { margin: 30px 0; }
                .section h2 { color: #333; border-bottom: 2px solid #2196F3; padding-bottom: 5px; }
                table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                th, td { padding: 8px 12px; text-align: left; border-bottom: 1px solid #ddd; }
                th { background-color: #f5f5f5; }
                .recommendation { background-color: #e8f5e8; padding: 10px; margin: 5px 0; border-radius: 5px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>${progress.subject} Progress Report</h1>
                <p>Generated on ${SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())}</p>
            </div>
            
            <div class="stats">
                <div class="stat">
                    <div class="stat-value">${progress.totalSessions}</div>
                    <div>Total Sessions</div>
                </div>
                <div class="stat">
                    <div class="stat-value">${progress.totalQuestions}</div>
                    <div>Questions</div>
                </div>
                <div class="stat">
                    <div class="stat-value">${(progress.averageAccuracy * 100).toInt()}%</div>
                    <div>Accuracy</div>
                </div>
                <div class="stat">
                    <div class="stat-value">${progress.totalTimeMinutes}</div>
                    <div>Minutes</div>
                </div>
            </div>
            
            <div class="section">
                <h2>Session History</h2>
                <table>
                    <tr>
                        <th>Date</th>
                        <th>Accuracy</th>
                        <th>Minutes</th>
                        <th>Points</th>
                        <th>Questions</th>
                    </tr>
                    ${sessions.joinToString("") { session ->
                        """<tr>
                            <td>${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(session.date)}</td>
                            <td>${(session.accuracy * 100).toInt()}%</td>
                            <td>${session.minutesSpent}</td>
                            <td>${session.pointsEarned}</td>
                            <td>${session.questionsAnswered}</td>
                        </tr>"""
                    }}
                </table>
            </div>
            
            <div class="section">
                <h2>Recommendations</h2>
                ${recommendations.joinToString("") { recommendation ->
                    """<div class="recommendation">$recommendation</div>"""
                }}
            </div>
        </body>
        </html>
    """.trimIndent()
}