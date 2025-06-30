package com.studywise.ai.domain.usecase.verbaljournal

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.VerbalJournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

/**
 * Use case for retrieving Verbal Journal session history
 */
class GetVerbalJournalHistoryUseCase @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository
) {
    
    /**
     * Get paginated session history with filters
     */
    operator fun invoke(
        userId: String,
        filter: SessionHistoryFilter = SessionHistoryFilter()
    ): Flow<List<SessionHistoryItem>> {
        return verbalJournalRepository.getUserJournalEntries(userId)
            .map { entries ->
                entries
                    .filter { entry -> applyFilter(entry, filter) }
                    .sortedByDescending { it.sessionDate }
                    .take(filter.limit)
                    .map { entry -> mapToHistoryItem(entry) }
            }
    }
    
    /**
     * Get detailed session by ID
     */
    suspend fun getSessionDetails(sessionId: String): SessionDetails? {
        val entry = verbalJournalRepository.getJournalEntry(sessionId) ?: return null
        val turns = verbalJournalRepository.getConversationTurns(sessionId)
        val analysis = verbalJournalRepository.getSessionAnalysis(sessionId)
        
        return SessionDetails(
            entry = entry,
            conversationTurns = turns,
            analysis = analysis,
            errorSummary = createErrorSummary(turns),
            transcript = createFullTranscript(turns),
            audioAvailable = entry.audioFilePath != null
        )
    }
    
    /**
     * Apply filters to entries
     */
    private fun applyFilter(
        entry: VerbalJournalEntry,
        filter: SessionHistoryFilter
    ): Boolean {
        // Date range filter
        if (filter.startDate != null && entry.sessionDate < filter.startDate) {
            return false
        }
        if (filter.endDate != null && entry.sessionDate > filter.endDate) {
            return false
        }
        
        // Status filter
        if (filter.status != null && entry.status != filter.status) {
            return false
        }
        
        // Duration filter
        if (filter.minDuration != null && entry.actualDurationMinutes < filter.minDuration) {
            return false
        }
        
        // Accuracy filter
        if (filter.minAccuracy != null) {
            val accuracy = entry.sessionAnalysis?.overallStats?.overallAccuracy ?: 0f
            if (accuracy < filter.minAccuracy) {
                return false
            }
        }
        
        // Topic filter
        if (filter.topics.isNotEmpty()) {
            if (!entry.topics.any { it in filter.topics }) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Map entry to history item
     */
    private fun mapToHistoryItem(entry: VerbalJournalEntry): SessionHistoryItem {
        val analysis = entry.sessionAnalysis
        
        return SessionHistoryItem(
            id = entry.id,
            date = entry.sessionDate,
            duration = entry.actualDurationMinutes,
            status = entry.status,
            accuracy = analysis?.overallStats?.overallAccuracy,
            fluencyScore = analysis?.overallStats?.fluencyScore,
            wordsSpoken = analysis?.overallStats?.wordsSpoken ?: 0,
            errorCount = analysis?.errorBreakdown?.errorsByType?.values?.sum() ?: 0,
            topics = entry.topics,
            engagementLevel = entry.engagementLevel,
            hasAudio = entry.audioFilePath != null,
            achievements = analysis?.achievements?.map { it.name } ?: emptyList()
        )
    }
    
    /**
     * Create error summary from conversation turns
     */
    private fun createErrorSummary(turns: List<ConversationTurn>): ErrorSummary {
        val allErrors = turns.flatMap { it.errors }
        val errorsByType = allErrors.groupingBy { it.errorType.id }.eachCount()
        val errorsBySeverity = allErrors.groupingBy { it.severity }.eachCount()
        
        val topErrors = errorsByType.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { ErrorTypeSummary(it.key, it.value, getErrorDescription(it.key)) }
        
        return ErrorSummary(
            totalErrors = allErrors.size,
            errorsByType = errorsByType,
            errorsBySeverity = errorsBySeverity,
            topErrors = topErrors
        )
    }
    
    /**
     * Create full transcript from conversation turns
     */
    private fun createFullTranscript(turns: List<ConversationTurn>): String {
        return turns.joinToString("\n\n") { turn ->
            val speaker = when (turn.speaker) {
                Speaker.USER -> "You"
                Speaker.AI -> "AI"
            }
            "$speaker: ${turn.transcript}"
        }
    }
    
    /**
     * Get error description
     */
    private fun getErrorDescription(errorType: String): String {
        return ErrorTypes.getErrorTypeById(errorType)?.description
            ?: "Unknown error type"
    }
}

/**
 * Filter options for session history
 */
data class SessionHistoryFilter(
    val startDate: Date? = null,
    val endDate: Date? = null,
    val status: SessionStatus? = null,
    val minDuration: Int? = null,
    val minAccuracy: Float? = null,
    val topics: List<String> = emptyList(),
    val limit: Int = 50
)

/**
 * Session history list item
 */
data class SessionHistoryItem(
    val id: String,
    val date: Date,
    val duration: Int,
    val status: SessionStatus,
    val accuracy: Float?,
    val fluencyScore: Float?,
    val wordsSpoken: Int,
    val errorCount: Int,
    val topics: List<String>,
    val engagementLevel: EngagementLevel,
    val hasAudio: Boolean,
    val achievements: List<String>
)

/**
 * Detailed session information
 */
data class SessionDetails(
    val entry: VerbalJournalEntry,
    val conversationTurns: List<ConversationTurn>,
    val analysis: SessionAnalysis?,
    val errorSummary: ErrorSummary,
    val transcript: String,
    val audioAvailable: Boolean
)

/**
 * Error summary for a session
 */
data class ErrorSummary(
    val totalErrors: Int,
    val errorsByType: Map<String, Int>,
    val errorsBySeverity: Map<ErrorSeverity, Int>,
    val topErrors: List<ErrorTypeSummary>
)

/**
 * Summary of a specific error type
 */
data class ErrorTypeSummary(
    val type: String,
    val count: Int,
    val description: String
)