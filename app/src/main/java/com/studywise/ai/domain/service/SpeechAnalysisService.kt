package com.studywise.ai.domain.service

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.VerbalJournalRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Service for analyzing speech sessions and generating insights
 */
@Singleton
class SpeechAnalysisService @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository,
    private val errorDetectionService: SpeechErrorDetectionService
) {
    
    /**
     * Analyze a completed journal session
     */
    suspend fun analyzeSession(
        journalEntry: VerbalJournalEntry,
        userProfile: VerbalJournalProfile
    ): SessionAnalysis {
        // Get all conversation turns
        val turns = verbalJournalRepository.getConversationTurns(journalEntry.id)
        
        // Calculate basic statistics
        val sessionStats = calculateSessionStats(journalEntry, turns)
        
        // Analyze errors
        val allErrors = turns.flatMap { it.errors }
        val errorBreakdown = analyzeErrors(allErrors, userProfile)
        
        // Identify improvement areas
        val improvementAreas = identifyImprovementAreas(errorBreakdown, allErrors)
        
        // Check for achievements
        val achievements = checkAchievements(sessionStats, errorBreakdown, userProfile)
        
        // Generate recommendations
        val recommendations = generateRecommendations(
            errorBreakdown,
            improvementAreas,
            userProfile
        )
        
        return SessionAnalysis(
            sessionId = journalEntry.id,
            analyzedAt = Date(),
            overallStats = sessionStats,
            errorBreakdown = errorBreakdown,
            improvementAreas = improvementAreas,
            achievements = achievements,
            recommendations = recommendations
        )
    }
    
    /**
     * Calculate session statistics
     */
    private fun calculateSessionStats(
        entry: VerbalJournalEntry,
        turns: List<ConversationTurn>
    ): SessionStats {
        val userTurns = turns.filter { it.speaker == Speaker.USER }
        val totalWords = userTurns.sumOf { countWords(it.transcript) }
        val totalMinutes = entry.actualDurationMinutes
        
        // Calculate pause analysis
        val pauseAnalysis = analyzePauses(userTurns)
        
        // Calculate accuracy
        val totalErrors = userTurns.sumOf { it.errors.size }
        val accuracy = if (totalWords > 0) {
            1f - (totalErrors.toFloat() / totalWords)
        } else 0f
        
        // Calculate tier-specific accuracy
        val tierAccuracy = calculateTierSpecificAccuracy(userTurns, entry.correctionTier)
        
        // Mock scores for now - in production would analyze audio
        val fluencyScore = calculateFluencyScore(pauseAnalysis, totalWords, totalMinutes)
        val pronunciationScore = 0.85f // Would come from audio analysis
        val vocabularyDiversity = calculateVocabularyDiversity(userTurns)
        
        return SessionStats(
            totalSpeakingMinutes = totalMinutes,
            wordsSpoken = totalWords,
            averageWordsPerMinute = if (totalMinutes > 0) totalWords.toFloat() / totalMinutes else 0f,
            pausePattern = pauseAnalysis,
            overallAccuracy = accuracy.coerceIn(0f, 1f),
            tierSpecificAccuracy = tierAccuracy,
            fluencyScore = fluencyScore,
            pronunciationScore = pronunciationScore,
            vocabularyDiversity = vocabularyDiversity
        )
    }
    
    /**
     * Analyze pauses in speech
     */
    private fun analyzePauses(userTurns: List<ConversationTurn>): PauseAnalysis {
        // Simplified analysis - in production would use audio timing data
        val fillerWords = listOf("um", "uh", "er", "like", "you know", "I mean")
        var fillerCount = 0
        var hesitationCount = 0
        
        userTurns.forEach { turn ->
            val transcript = turn.transcript.lowercase()
            fillerWords.forEach { filler ->
                fillerCount += transcript.split(filler).size - 1
            }
            
            // Count repeated words as hesitations
            val words = transcript.split(" ")
            for (i in 1 until words.size) {
                if (words[i] == words[i - 1] && words[i].length > 2) {
                    hesitationCount++
                }
            }
        }
        
        return PauseAnalysis(
            averagePauseDuration = 0.8f, // Mock value
            pauseFrequency = 0.15f, // Mock value
            fillerWordCount = fillerCount,
            hesitationCount = hesitationCount,
            naturalPauseRatio = 0.7f // Mock value
        )
    }
    
    /**
     * Calculate tier-specific accuracy
     */
    private fun calculateTierSpecificAccuracy(
        userTurns: List<ConversationTurn>,
        currentTier: CorrectionTier
    ): Map<CorrectionTier, Float> {
        val accuracyMap = mutableMapOf<CorrectionTier, Float>()
        val totalWords = userTurns.sumOf { countWords(it.transcript) }.toFloat()
        
        CorrectionTier.values().forEach { tier ->
            val tierErrors = userTurns.sumOf { turn ->
                turn.errors.count { error ->
                    error.errorType.correctionTier.level <= tier.level
                }
            }
            
            val accuracy = if (totalWords > 0) {
                1f - (tierErrors / totalWords)
            } else 0f
            
            accuracyMap[tier] = accuracy.coerceIn(0f, 1f)
        }
        
        return accuracyMap
    }
    
    /**
     * Calculate fluency score
     */
    private fun calculateFluencyScore(
        pauseAnalysis: PauseAnalysis,
        totalWords: Int,
        totalMinutes: Int
    ): Float {
        val wordsPerMinute = if (totalMinutes > 0) totalWords.toFloat() / totalMinutes else 0f
        
        // Target WPM ranges by level
        val targetWPM = 120f // Average conversational speed
        val wpmScore = min(wordsPerMinute / targetWPM, 1f)
        
        // Filler word penalty
        val fillerPenalty = min(pauseAnalysis.fillerWordCount * 0.02f, 0.3f)
        
        // Hesitation penalty
        val hesitationPenalty = min(pauseAnalysis.hesitationCount * 0.01f, 0.2f)
        
        return (wpmScore - fillerPenalty - hesitationPenalty).coerceIn(0f, 1f)
    }
    
    /**
     * Calculate vocabulary diversity
     */
    private fun calculateVocabularyDiversity(userTurns: List<ConversationTurn>): Float {
        val allWords = userTurns.flatMap { turn ->
            turn.transcript.lowercase()
                .split(Regex("\\W+"))
                .filter { it.length > 2 } // Ignore very short words
        }
        
        val uniqueWords = allWords.distinct().size
        val totalWords = allWords.size
        
        return if (totalWords > 0) {
            (uniqueWords.toFloat() / totalWords).coerceIn(0f, 1f)
        } else 0f
    }
    
    /**
     * Analyze errors and create breakdown
     */
    private fun analyzeErrors(
        allErrors: List<ErrorInstance>,
        userProfile: VerbalJournalProfile
    ): ErrorBreakdown {
        // Group errors by type
        val errorsByType = allErrors.groupingBy { it.errorType.id }.eachCount()
        
        // Group by category
        val errorsByCategory = allErrors.groupingBy { it.errorType.category }.eachCount()
        
        // Group by severity
        val errorsBySeverity = allErrors.groupingBy { it.severity }.eachCount()
        
        // Group by tier
        val errorsByTier = CorrectionTier.values().associateWith { tier ->
            allErrors.filter { it.errorType.correctionTier == tier }
        }
        
        // Calculate improvement from last session
        val improvement = calculateImprovement(userProfile.userId)
        
        // Identify persistent and new errors
        val (persistent, new) = identifyErrorTrends(allErrors, userProfile.userId)
        
        return ErrorBreakdown(
            errorsByType = errorsByType,
            errorsByCategory = errorsByCategory,
            errorsBySeverity = errorsBySeverity,
            errorsByTier = errorsByTier,
            improvementFromLastSession = improvement,
            persistentErrors = persistent,
            newErrors = new
        )
    }
    
    /**
     * Identify areas for improvement
     */
    private fun identifyImprovementAreas(
        errorBreakdown: ErrorBreakdown,
        allErrors: List<ErrorInstance>
    ): List<ImprovementArea> {
        return errorBreakdown.errorsByType
            .filter { it.value >= 2 } // At least 2 occurrences
            .map { (errorType, count) ->
                val errorTypeObj = ErrorTypes.getErrorTypeById(errorType)
                    ?: return@map null
                
                val exercises = generatePracticeExercises(errorTypeObj)
                
                ImprovementArea(
                    errorType = errorType,
                    frequency = count,
                    impact = errorTypeObj.severity,
                    practiceExercises = exercises,
                    estimatedPracticeTime = exercises.size * 5, // 5 minutes per exercise
                    resourceLinks = getResourceLinks(errorType)
                )
            }
            .filterNotNull()
            .sortedByDescending { it.frequency * it.impact.weight }
            .take(5) // Top 5 areas
    }
    
    /**
     * Generate practice exercises for an error type
     */
    private fun generatePracticeExercises(errorType: ErrorType): List<String> {
        return when (errorType.id) {
            "article_usage" -> listOf(
                "Fill in the blanks with a/an/the or leave blank",
                "Identify incorrect article usage in sentences",
                "Practice with profession and general concept rules"
            )
            "subject_verb_agreement" -> listOf(
                "Match subjects with correct verb forms",
                "Identify agreement errors in paragraphs",
                "Practice with irregular verbs"
            )
            "preposition_semantic" -> listOf(
                "Complete sentences with correct prepositions",
                "Match verbs/adjectives with their prepositions",
                "Practice common collocations"
            )
            else -> listOf(
                "Practice identifying this error type",
                "Complete targeted exercises",
                "Review example corrections"
            )
        }
    }
    
    /**
     * Get resource links for an error type
     */
    private fun getResourceLinks(errorType: String): List<String> {
        // In production, would return actual resource URLs
        return listOf(
            "https://studywise.ai/resources/$errorType",
            "https://studywise.ai/practice/$errorType"
        )
    }
    
    /**
     * Check for achievements
     */
    private suspend fun checkAchievements(
        stats: SessionStats,
        errorBreakdown: ErrorBreakdown,
        userProfile: VerbalJournalProfile
    ): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        
        // Accuracy achievements
        if (stats.overallAccuracy >= 0.9f) {
            achievements.add(
                Achievement(
                    id = "accuracy_master",
                    name = "Accuracy Master",
                    description = "Achieved 90%+ accuracy in a session",
                    iconUrl = "achievement_accuracy",
                    earnedAt = Date(),
                    category = AchievementCategory.ACCURACY,
                    points = 100
                )
            )
        }
        
        // Fluency achievements
        if (stats.fluencyScore >= 0.85f) {
            achievements.add(
                Achievement(
                    id = "fluent_speaker",
                    name = "Fluent Speaker",
                    description = "Demonstrated excellent speaking fluency",
                    iconUrl = "achievement_fluency",
                    earnedAt = Date(),
                    category = AchievementCategory.FLUENCY,
                    points = 150
                )
            )
        }
        
        // Session length achievements
        if (stats.totalSpeakingMinutes >= 30) {
            achievements.add(
                Achievement(
                    id = "marathon_speaker",
                    name = "Marathon Speaker",
                    description = "Completed a 30-minute speaking session",
                    iconUrl = "achievement_marathon",
                    earnedAt = Date(),
                    category = AchievementCategory.MILESTONE,
                    points = 200
                )
            )
        }
        
        return achievements
    }
    
    /**
     * Generate personalized recommendations
     */
    private fun generateRecommendations(
        errorBreakdown: ErrorBreakdown,
        improvementAreas: List<ImprovementArea>,
        userProfile: VerbalJournalProfile
    ): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        // Error-based recommendations
        improvementAreas.take(3).forEach { area ->
            recommendations.add(
                Recommendation(
                    id = UUID.randomUUID().toString(),
                    type = RecommendationType.PRACTICE_EXERCISE,
                    title = "Practice ${area.errorType.replace("_", " ").capitalize()}",
                    description = "Focus on reducing ${area.errorType} errors with targeted exercises",
                    priority = area.frequency,
                    estimatedTime = area.estimatedPracticeTime,
                    relatedSkills = listOf(area.errorType)
                )
            )
        }
        
        // Topic recommendations based on interests
        if (userProfile.preferredTopics.isNotEmpty()) {
            recommendations.add(
                Recommendation(
                    id = UUID.randomUUID().toString(),
                    type = RecommendationType.TOPIC_SUGGESTION,
                    title = "Explore ${userProfile.preferredTopics.first()}",
                    description = "Practice speaking about your interests to maintain engagement",
                    priority = 5,
                    estimatedTime = 15,
                    relatedSkills = listOf("vocabulary", "fluency")
                )
            )
        }
        
        // Difficulty adjustment if needed
        val accuracy = errorBreakdown.errorsBySeverity.values.sum()
        if (accuracy > 20) {
            recommendations.add(
                Recommendation(
                    id = UUID.randomUUID().toString(),
                    type = RecommendationType.DIFFICULTY_ADJUSTMENT,
                    title = "Consider adjusting difficulty",
                    description = "You might benefit from focusing on fewer error types",
                    priority = 8,
                    estimatedTime = 0,
                    relatedSkills = emptyList()
                )
            )
        }
        
        return recommendations.sortedByDescending { it.priority }
    }
    
    /**
     * Calculate improvement from previous session
     */
    private suspend fun calculateImprovement(userId: String): Float {
        val recentAnalyses = verbalJournalRepository.getRecentAnalyses(userId, 2)
        
        return if (recentAnalyses.size >= 2) {
            val current = recentAnalyses[0]
            val previous = recentAnalyses[1]
            
            current.overallStats.overallAccuracy - previous.overallStats.overallAccuracy
        } else {
            0f
        }
    }
    
    /**
     * Identify persistent and new error patterns
     */
    private suspend fun identifyErrorTrends(
        currentErrors: List<ErrorInstance>,
        userId: String
    ): Pair<List<String>, List<String>> {
        val recentAnalyses = verbalJournalRepository.getRecentAnalyses(userId, 3)
        
        if (recentAnalyses.size < 2) {
            return Pair(emptyList(), currentErrors.map { it.errorType.id }.distinct())
        }
        
        val previousErrorTypes = recentAnalyses
            .drop(1) // Skip current
            .flatMap { it.errorBreakdown.errorsByType.keys }
            .distinct()
        
        val currentErrorTypes = currentErrors.map { it.errorType.id }.distinct()
        
        val persistent = currentErrorTypes.filter { it in previousErrorTypes }
        val new = currentErrorTypes.filter { it !in previousErrorTypes }
        
        return Pair(persistent, new)
    }
    
    /**
     * Count words in text
     */
    private fun countWords(text: String): Int {
        return text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
    }
    
    /**
     * Calculate speaking progress metrics
     */
    suspend fun calculateProgressMetrics(
        userId: String,
        period: Int = 30 // days
    ): VerbalJournalMetrics {
        val startDate = Date(System.currentTimeMillis() - period * 24 * 60 * 60 * 1000L)
        
        // Get recent sessions
        val sessions = verbalJournalRepository.getJournalEntriesInDateRange(
            userId,
            startDate,
            Date()
        ).first()
        
        // Get analyses
        val analyses = sessions.mapNotNull { session ->
            verbalJournalRepository.getSessionAnalysis(session.id)
        }
        
        if (analyses.isEmpty()) {
            return VerbalJournalMetrics(
                userId = userId,
                period = "$period days",
                accuracyImprovement = 0f,
                tierProgressionDays = null,
                speakingFluencyGains = 0f,
                errorReductionRate = 0f,
                streakRetention = 0,
                sessionCompletionRate = 0f,
                averageSessionLength = 0f,
                totalMinutesSpoken = 0,
                vocabularyGrowth = 0,
                mostImprovedAreas = emptyList(),
                challengingAreas = emptyList()
            )
        }
        
        // Calculate metrics
        val firstAnalysis = analyses.last()
        val lastAnalysis = analyses.first()
        
        val accuracyImprovement = lastAnalysis.overallStats.overallAccuracy - 
                                 firstAnalysis.overallStats.overallAccuracy
        
        val fluencyGains = lastAnalysis.overallStats.fluencyScore - 
                          firstAnalysis.overallStats.fluencyScore
        
        val totalMinutes = sessions.sumOf { it.actualDurationMinutes }
        val completedSessions = sessions.count { it.status == SessionStatus.ANALYZED }
        val completionRate = if (sessions.isNotEmpty()) {
            completedSessions.toFloat() / sessions.size
        } else 0f
        
        // Error reduction
        val initialErrors = firstAnalysis.errorBreakdown.errorsByType.values.sum()
        val currentErrors = lastAnalysis.errorBreakdown.errorsByType.values.sum()
        val errorReduction = if (initialErrors > 0) {
            (initialErrors - currentErrors).toFloat() / initialErrors
        } else 0f
        
        // Most improved areas
        val errorChanges = mutableMapOf<String, Int>()
        analyses.windowed(2).forEach { (prev, curr) ->
            curr.errorBreakdown.errorsByType.forEach { (type, count) ->
                val prevCount = prev.errorBreakdown.errorsByType[type] ?: 0
                val change = prevCount - count
                errorChanges[type] = (errorChanges[type] ?: 0) + change
            }
        }
        
        val mostImproved = errorChanges
            .filter { it.value > 0 }
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
        
        val challenging = errorChanges
            .filter { it.value <= 0 }
            .entries
            .sortedBy { it.value }
            .take(3)
            .map { it.key }
        
        return VerbalJournalMetrics(
            userId = userId,
            period = "$period days",
            accuracyImprovement = accuracyImprovement,
            tierProgressionDays = null, // Would calculate from profile history
            speakingFluencyGains = fluencyGains,
            errorReductionRate = errorReduction,
            streakRetention = verbalJournalRepository.calculateCurrentStreak(userId),
            sessionCompletionRate = completionRate,
            averageSessionLength = if (sessions.isNotEmpty()) {
                totalMinutes.toFloat() / sessions.size
            } else 0f,
            totalMinutesSpoken = totalMinutes,
            vocabularyGrowth = 0, // Would need vocabulary tracking
            mostImprovedAreas = mostImproved,
            challengingAreas = challenging
        )
    }
}