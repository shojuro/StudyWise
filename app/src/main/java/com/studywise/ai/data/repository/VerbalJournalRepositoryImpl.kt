package com.studywise.ai.data.repository

import com.studywise.ai.data.local.dao.VerbalJournalDao
import com.studywise.ai.data.local.entity.*
import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Implementation of VerbalJournalRepository
 */
@Singleton
class VerbalJournalRepositoryImpl @Inject constructor(
    private val verbalJournalDao: VerbalJournalDao,
    private val json: Json
) : VerbalJournalRepository {
    
    // ===== Journal Entry Management =====
    
    override suspend fun createJournalEntry(
        userId: String,
        targetDurationMinutes: Int,
        correctionTier: CorrectionTier
    ): VerbalJournalEntry {
        val entry = VerbalJournalEntry(
            id = UUID.randomUUID().toString(),
            userId = userId,
            sessionDate = Date(),
            startTime = Date(),
            endTime = null,
            targetDurationMinutes = targetDurationMinutes,
            correctionTier = correctionTier,
            audioFilePath = null,
            transcript = "",
            status = SessionStatus.ACTIVE,
            conversationTurns = emptyList(),
            sessionAnalysis = null,
            engagementLevel = EngagementLevel.MEDIUM,
            topics = emptyList(),
            mood = null
        )
        
        val entity = entry.toEntity()
        verbalJournalDao.insertJournalEntry(entity)
        
        return entry
    }
    
    override suspend fun updateJournalEntry(entry: VerbalJournalEntry) {
        val entity = entry.toEntity()
        verbalJournalDao.updateJournalEntry(entity)
    }
    
    override suspend fun getJournalEntry(entryId: String): VerbalJournalEntry? {
        val entity = verbalJournalDao.getJournalEntry(entryId) ?: return null
        val turns = verbalJournalDao.getConversationTurns(entryId)
        val analysis = verbalJournalDao.getSessionAnalysis(entryId)
        
        return entity.toDomain(turns, analysis)
    }
    
    override fun getUserJournalEntries(userId: String): Flow<List<VerbalJournalEntry>> {
        return verbalJournalDao.getUserJournalEntries(userId).map { entities ->
            entities.map { entity ->
                val turns = verbalJournalDao.getConversationTurns(entity.id)
                val analysis = verbalJournalDao.getSessionAnalysis(entity.id)
                entity.toDomain(turns, analysis)
            }
        }
    }
    
    override fun getJournalEntriesInDateRange(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<VerbalJournalEntry>> {
        return verbalJournalDao.getUserJournalEntriesInDateRange(userId, startDate, endDate)
            .map { entities ->
                entities.map { entity ->
                    val turns = verbalJournalDao.getConversationTurns(entity.id)
                    val analysis = verbalJournalDao.getSessionAnalysis(entity.id)
                    entity.toDomain(turns, analysis)
                }
            }
    }
    
    override suspend fun deleteJournalEntry(entryId: String) {
        val entry = verbalJournalDao.getJournalEntry(entryId) ?: return
        
        // Delete audio files
        entry.audioFilePath?.let { File(it).delete() }
        
        // Delete conversation turn audio files
        val turns = verbalJournalDao.getConversationTurns(entryId)
        turns.forEach { turn ->
            turn.audioSegmentPath?.let { File(it).delete() }
        }
        
        // Delete from database (cascades to related tables)
        verbalJournalDao.deleteJournalEntry(entry)
    }
    
    // ===== Conversation Management =====
    
    override suspend fun addConversationTurn(entryId: String, turn: ConversationTurn) {
        val existingTurns = verbalJournalDao.getConversationTurns(entryId)
        val entity = ConversationTurnEntity(
            id = turn.id,
            journalEntryId = entryId,
            speaker = turn.speaker.name,
            timestamp = turn.timestamp,
            audioSegmentPath = turn.audioSegmentPath,
            transcript = turn.transcript,
            transcriptionConfidence = turn.transcriptionConfidence,
            aiResponse = turn.aiResponse,
            sequenceNumber = existingTurns.size
        )
        
        verbalJournalDao.insertConversationTurn(entity)
        
        // Add any errors
        if (turn.errors.isNotEmpty()) {
            addSpeechErrors(turn.id, turn.errors)
        }
    }
    
    override suspend fun getConversationTurns(entryId: String): List<ConversationTurn> {
        val turns = verbalJournalDao.getConversationTurns(entryId)
        return turns.map { entity ->
            val errors = verbalJournalDao.getSpeechErrors(entity.id)
            entity.toDomain(errors)
        }
    }
    
    override suspend fun saveAudioSegment(turnId: String, audioFile: File): String {
        // In a real implementation, this would handle file management
        // For now, return the file path
        return audioFile.absolutePath
    }
    
    // ===== Error Detection and Analysis =====
    
    override suspend fun addSpeechErrors(turnId: String, errors: List<ErrorInstance>) {
        val entities = errors.map { error ->
            SpeechErrorEntity(
                id = error.id,
                conversationTurnId = turnId,
                errorType = error.errorType.id,
                errorCategory = error.errorType.category.name,
                severity = error.severity.name,
                originalText = error.originalText,
                correctedText = error.correctedText,
                explanation = error.explanation,
                timestampMillis = error.timestamp,
                confidence = error.confidence,
                l1AdjustedWeight = error.l1AdjustedWeight,
                contextWindow = error.contextWindow
            )
        }
        
        verbalJournalDao.insertSpeechErrors(entities)
    }
    
    override suspend fun getEntryErrors(entryId: String): List<ErrorInstance> {
        val errors = verbalJournalDao.getAllErrorsForEntry(entryId)
        return errors.map { it.toDomain() }
    }
    
    override suspend fun getErrorFrequencyAnalysis(
        userId: String,
        startDate: Date
    ): Map<String, Int> {
        val frequencies = verbalJournalDao.getErrorFrequencySince(userId, startDate)
        return frequencies.associate { it.errorType to it.count }
    }
    
    // ===== Session Analysis =====
    
    override suspend fun saveSessionAnalysis(entryId: String, analysis: SessionAnalysis) {
        val entity = SessionAnalysisEntity(
            id = analysis.sessionId,
            sessionId = entryId,
            analyzedAt = analysis.analyzedAt,
            totalSpeakingMinutes = analysis.overallStats.totalSpeakingMinutes,
            wordsSpoken = analysis.overallStats.wordsSpoken,
            averageWordsPerMinute = analysis.overallStats.averageWordsPerMinute,
            overallAccuracy = analysis.overallStats.overallAccuracy,
            fluencyScore = analysis.overallStats.fluencyScore,
            pronunciationScore = analysis.overallStats.pronunciationScore,
            vocabularyDiversity = analysis.overallStats.vocabularyDiversity,
            averagePauseDuration = analysis.overallStats.pausePattern.averagePauseDuration,
            pauseFrequency = analysis.overallStats.pausePattern.pauseFrequency,
            fillerWordCount = analysis.overallStats.pausePattern.fillerWordCount,
            hesitationCount = analysis.overallStats.pausePattern.hesitationCount,
            naturalPauseRatio = analysis.overallStats.pausePattern.naturalPauseRatio,
            errorsByType = json.encodeToString(analysis.errorBreakdown.errorsByType),
            errorsByCategory = json.encodeToString(
                analysis.errorBreakdown.errorsByCategory.mapKeys { it.key.name }
            ),
            errorsBySeverity = json.encodeToString(
                analysis.errorBreakdown.errorsBySeverity.mapKeys { it.key.name }
            ),
            persistentErrors = json.encodeToString(analysis.errorBreakdown.persistentErrors),
            newErrors = json.encodeToString(analysis.errorBreakdown.newErrors),
            improvementFromLastSession = analysis.errorBreakdown.improvementFromLastSession,
            achievementsEarned = json.encodeToString(analysis.achievements.map { it.id })
        )
        
        verbalJournalDao.insertSessionAnalysis(entity)
        
        // Save improvement areas
        val improvementEntities = analysis.improvementAreas.map { area ->
            ImprovementAreaEntity(
                id = UUID.randomUUID().toString(),
                analysisId = analysis.sessionId,
                errorType = area.errorType,
                frequency = area.frequency,
                impact = area.impact.name,
                practiceExercises = json.encodeToString(area.practiceExercises),
                estimatedPracticeTime = area.estimatedPracticeTime,
                resourceLinks = json.encodeToString(area.resourceLinks)
            )
        }
        verbalJournalDao.insertImprovementAreas(improvementEntities)
        
        // Save recommendations
        saveRecommendations(analysis.sessionId, analysis.recommendations)
        
        // Update journal entry stats
        val entry = verbalJournalDao.getJournalEntry(entryId)
        if (entry != null) {
            val updatedEntry = entry.copy(
                status = SessionStatus.ANALYZED.name,
                overallAccuracy = analysis.overallStats.overallAccuracy,
                wordsSpoken = analysis.overallStats.wordsSpoken
            )
            verbalJournalDao.updateJournalEntry(updatedEntry)
        }
    }
    
    override suspend fun getSessionAnalysis(entryId: String): SessionAnalysis? {
        val entity = verbalJournalDao.getSessionAnalysis(entryId) ?: return null
        val improvements = verbalJournalDao.getImprovementAreas(entity.id)
        val recommendations = verbalJournalDao.getRecommendations(entity.id)
        
        return entity.toDomain(improvements, recommendations)
    }
    
    override suspend fun getRecentAnalyses(userId: String, limit: Int): List<SessionAnalysis> {
        val entities = verbalJournalDao.getRecentAnalyses(userId, limit)
        return entities.map { entity ->
            val improvements = verbalJournalDao.getImprovementAreas(entity.id)
            val recommendations = verbalJournalDao.getRecommendations(entity.id)
            entity.toDomain(improvements, recommendations)
        }
    }
    
    // ===== User Profile Management =====
    
    override suspend fun getOrCreateProfile(
        userId: String,
        nativeLanguage: String
    ): VerbalJournalProfile {
        val existing = verbalJournalDao.getProfile(userId)
        if (existing != null) {
            return existing.toDomain()
        }
        
        val profile = VerbalJournalProfile(
            userId = userId,
            nativeLanguage = nativeLanguage,
            currentLevel = ProficiencyLevel.INTERMEDIATE,
            currentTier = CorrectionTier.CRITICAL,
            targetAccent = "General American",
            dailyTargetMinutes = 15,
            preferredTopics = emptyList(),
            correctionStyle = CorrectionStyle.BALANCED,
            streakDays = 0,
            totalSpeakingMinutes = 0,
            lastSessionDate = null,
            breakInWeek = 1,
            achievements = emptyList(),
            progressHistory = emptyList()
        )
        
        val entity = profile.toEntity()
        verbalJournalDao.insertOrUpdateProfile(entity)
        
        return profile
    }
    
    override suspend fun updateProfile(profile: VerbalJournalProfile) {
        val entity = profile.toEntity()
        verbalJournalDao.insertOrUpdateProfile(entity)
    }
    
    override fun observeProfile(userId: String): Flow<VerbalJournalProfile?> {
        return verbalJournalDao.observeProfile(userId).map { it?.toDomain() }
    }
    
    override suspend fun updateUserLevel(
        userId: String,
        tier: CorrectionTier,
        level: ProficiencyLevel
    ) {
        verbalJournalDao.updateUserLevel(userId, tier.name, level.name)
    }
    
    // ===== Progress Tracking =====
    
    override suspend fun updateStreak(userId: String, streakDays: Int, lastSessionDate: Date) {
        verbalJournalDao.updateStreak(userId, streakDays, lastSessionDate)
    }
    
    override suspend fun addSpeakingMinutes(userId: String, minutes: Int) {
        verbalJournalDao.addSpeakingMinutes(userId, minutes)
    }
    
    override suspend fun saveProgressSnapshot(userId: String, snapshot: ProgressSnapshot) {
        val entity = SpeechProgressSnapshotEntity(
            userId = userId,
            date = snapshot.date,
            tier = snapshot.tier.name,
            accuracy = snapshot.accuracy,
            minutesSpoken = snapshot.minutesSpoken,
            errorsFound = snapshot.errorsFound,
            fluencyScore = snapshot.fluencyScore,
            milestone = snapshot.milestone
        )
        verbalJournalDao.insertProgressSnapshot(entity)
    }
    
    override fun getProgressSnapshots(
        userId: String,
        startDate: Date
    ): Flow<List<ProgressSnapshot>> {
        return verbalJournalDao.getProgressSnapshots(userId, startDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun calculateCurrentStreak(userId: String): Int {
        return verbalJournalDao.calculateCurrentStreak(userId)
    }
    
    // ===== Daily Prompts =====
    
    override suspend fun getDailyPrompt(date: Date, userLevel: ProficiencyLevel): JournalPrompt {
        val existing = verbalJournalDao.getDailyPrompt(date)
        if (existing != null) {
            return existing.toDomain()
        }
        
        // Generate a new prompt if none exists
        val prompt = generateDailyPrompt(date, userLevel)
        val entity = prompt.toEntity()
        verbalJournalDao.insertDailyPrompt(entity)
        
        return prompt
    }
    
    override fun getPromptsInRange(startDate: Date, endDate: Date): Flow<List<JournalPrompt>> {
        return verbalJournalDao.getPromptsInRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    // ===== Achievements =====
    
    override suspend fun awardAchievement(userId: String, achievement: Achievement) {
        val entity = VerbalJournalAchievementEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            achievementId = achievement.id,
            name = achievement.name,
            description = achievement.description,
            iconUrl = achievement.iconUrl,
            earnedAt = achievement.earnedAt,
            category = achievement.category.name,
            points = achievement.points
        )
        verbalJournalDao.insertAchievement(entity)
    }
    
    override fun getUserAchievements(userId: String): Flow<List<Achievement>> {
        return verbalJournalDao.getUserAchievements(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun checkAndAwardAchievements(
        userId: String,
        sessionAnalysis: SessionAnalysis
    ): List<Achievement> {
        val newAchievements = mutableListOf<Achievement>()
        
        // Check streak achievements
        val streak = calculateCurrentStreak(userId)
        if (streak == 7) {
            val achievement = Achievement(
                id = "streak_7",
                name = "Week Warrior",
                description = "Completed 7 days in a row!",
                iconUrl = "achievement_streak_7",
                earnedAt = Date(),
                category = AchievementCategory.STREAK,
                points = 100
            )
            awardAchievement(userId, achievement)
            newAchievements.add(achievement)
        }
        
        // Check accuracy achievements
        if (sessionAnalysis.overallStats.overallAccuracy >= 0.9f) {
            val achievement = Achievement(
                id = "accuracy_90",
                name = "Precision Speaker",
                description = "Achieved 90% accuracy in a session!",
                iconUrl = "achievement_accuracy_90",
                earnedAt = Date(),
                category = AchievementCategory.ACCURACY,
                points = 150
            )
            awardAchievement(userId, achievement)
            newAchievements.add(achievement)
        }
        
        return newAchievements
    }
    
    // ===== Metrics and Analytics =====
    
    override suspend fun getUserMetrics(userId: String, period: String): VerbalJournalMetrics {
        val startDate = when (period) {
            "daily" -> Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
            "weekly" -> Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)
            "monthly" -> Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
            else -> Date(0)
        }
        
        val basicMetrics = verbalJournalDao.getBasicMetrics(userId, startDate)
        val advancedMetrics = verbalJournalDao.getAdvancedMetrics(userId, startDate)
        val recentAnalyses = getRecentAnalyses(userId, 10)
        
        // Calculate improvement metrics
        val accuracyImprovement = if (recentAnalyses.size >= 2) {
            recentAnalyses.first().overallStats.overallAccuracy - 
            recentAnalyses.last().overallStats.overallAccuracy
        } else 0f
        
        return VerbalJournalMetrics(
            userId = userId,
            period = period,
            accuracyImprovement = accuracyImprovement,
            tierProgressionDays = null, // Calculate based on profile history
            speakingFluencyGains = 0f, // Calculate from analyses
            errorReductionRate = 0f, // Calculate from error frequencies
            streakRetention = calculateCurrentStreak(userId),
            sessionCompletionRate = basicMetrics?.sessionCount?.toFloat() ?: 0f,
            averageSessionLength = basicMetrics?.totalMinutes?.toFloat()?.div(
                basicMetrics.sessionCount.coerceAtLeast(1)
            ) ?: 0f,
            totalMinutesSpoken = basicMetrics?.totalMinutes ?: 0,
            vocabularyGrowth = 0, // Implement vocabulary tracking
            mostImprovedAreas = emptyList(),
            challengingAreas = emptyList()
        )
    }
    
    override suspend fun getSessionStats(userId: String, startDate: Date): SessionStatistics {
        val basicMetrics = verbalJournalDao.getBasicMetrics(userId, startDate)
        val advancedMetrics = verbalJournalDao.getAdvancedMetrics(userId, startDate)
        
        return SessionStatistics(
            totalSessions = basicMetrics?.sessionCount ?: 0,
            totalMinutes = basicMetrics?.totalMinutes ?: 0,
            averageSessionLength = basicMetrics?.totalMinutes?.toFloat()?.div(
                basicMetrics.sessionCount.coerceAtLeast(1)
            ) ?: 0f,
            completionRate = 0.8f, // Implement based on target vs actual duration
            activeDays = advancedMetrics?.activeDays ?: 0
        )
    }
    
    override suspend fun getImprovementTrends(userId: String, weeks: Int): ImprovementTrends {
        // Implementation would analyze historical data
        return ImprovementTrends(
            accuracyTrend = emptyList(),
            fluencyTrend = emptyList(),
            errorReductionTrend = emptyMap(),
            overallImprovement = 0f
        )
    }
    
    // ===== Recommendations =====
    
    override suspend fun getRecommendations(
        userId: String,
        basedOnSessions: Int
    ): List<Recommendation> {
        val recentAnalyses = getRecentAnalyses(userId, basedOnSessions)
        // Generate recommendations based on analyses
        return emptyList()
    }
    
    override suspend fun saveRecommendations(
        analysisId: String,
        recommendations: List<Recommendation>
    ) {
        val entities = recommendations.map { rec ->
            SessionRecommendationEntity(
                id = rec.id,
                analysisId = analysisId,
                type = rec.type.name,
                title = rec.title,
                description = rec.description,
                priority = rec.priority,
                estimatedTime = rec.estimatedTime,
                relatedSkills = json.encodeToString(rec.relatedSkills)
            )
        }
        verbalJournalDao.insertRecommendations(entities)
    }
    
    // ===== Audio Management =====
    
    override suspend fun cleanupOldAudioFiles(olderThanDays: Int) {
        // Implementation would delete old audio files
    }
    
    override suspend fun getAudioStorageSize(userId: String): Long {
        // Implementation would calculate total audio file size
        return 0L
    }
    
    // ===== Helper Functions =====
    
    private fun generateDailyPrompt(date: Date, level: ProficiencyLevel): JournalPrompt {
        // Simple prompt generation - in production would be more sophisticated
        val categories = PromptCategory.values()
        val category = categories[abs(date.hashCode()) % categories.size]
        
        val prompts = mapOf(
            PromptCategory.REFLECTION to "What was the most interesting thing that happened to you today?",
            PromptCategory.STORYTELLING to "Tell me about a memorable experience from your childhood.",
            PromptCategory.OPINION to "What's your opinion on the importance of learning languages?",
            PromptCategory.DESCRIPTION to "Describe your ideal vacation destination in detail.",
            PromptCategory.PLANNING to "What are your goals for the next year?",
            PromptCategory.PROBLEM_SOLVING to "How would you solve the problem of plastic pollution?",
            PromptCategory.CREATIVE to "If you could have any superpower, what would it be and why?",
            PromptCategory.PROFESSIONAL to "Describe your dream job and why it appeals to you."
        )
        
        return JournalPrompt(
            id = UUID.randomUUID().toString(),
            date = date,
            promptText = prompts[category] ?: "Tell me about your day.",
            category = category,
            difficulty = level,
            estimatedMinutes = when (level) {
                ProficiencyLevel.BEGINNER -> 5
                ProficiencyLevel.INTERMEDIATE -> 10
                ProficiencyLevel.ADVANCED -> 15
                ProficiencyLevel.NATIVE_LEVEL -> 20
            },
            followUpQuestions = listOf(
                "Can you elaborate on that?",
                "How did that make you feel?",
                "What happened next?"
            ),
            vocabularyHints = emptyList()
        )
    }
}

// Extension functions for entity conversion

private fun VerbalJournalEntry.toEntity(): VerbalJournalEntryEntity {
    return VerbalJournalEntryEntity(
        id = id,
        userId = userId,
        sessionDate = sessionDate,
        startTime = startTime,
        endTime = endTime,
        targetDurationMinutes = targetDurationMinutes,
        actualDurationMinutes = actualDurationMinutes,
        audioFilePath = audioFilePath,
        transcript = transcript,
        correctionTier = correctionTier.name,
        status = status.name,
        engagementLevel = engagementLevel.name,
        topics = Json.encodeToString(topics),
        mood = mood,
        overallAccuracy = sessionAnalysis?.overallStats?.overallAccuracy,
        wordsSpoken = sessionAnalysis?.overallStats?.wordsSpoken ?: 0
    )
}

private fun VerbalJournalEntryEntity.toDomain(
    turns: List<ConversationTurnEntity>,
    analysis: SessionAnalysisEntity?
): VerbalJournalEntry {
    return VerbalJournalEntry(
        id = id,
        userId = userId,
        sessionDate = sessionDate,
        startTime = startTime,
        endTime = endTime,
        targetDurationMinutes = targetDurationMinutes,
        actualDurationMinutes = actualDurationMinutes,
        audioFilePath = audioFilePath,
        transcript = transcript,
        correctionTier = CorrectionTier.valueOf(correctionTier),
        status = SessionStatus.valueOf(status),
        conversationTurns = emptyList(), // Would be populated separately
        sessionAnalysis = null, // Would be populated separately
        engagementLevel = EngagementLevel.valueOf(engagementLevel),
        topics = try { Json.decodeFromString(topics) } catch (e: Exception) { emptyList() },
        mood = mood
    )
}

private fun ConversationTurnEntity.toDomain(errors: List<SpeechErrorEntity>): ConversationTurn {
    return ConversationTurn(
        id = id,
        speaker = Speaker.valueOf(speaker),
        timestamp = timestamp,
        audioSegmentPath = audioSegmentPath,
        transcript = transcript,
        transcriptionConfidence = transcriptionConfidence,
        errors = errors.map { it.toDomain() },
        aiResponse = aiResponse
    )
}

private fun SpeechErrorEntity.toDomain(): ErrorInstance {
    val errorType = ErrorTypes.getErrorTypeById(errorType) ?: ErrorType(
        id = errorType,
        name = errorType,
        category = ErrorCategory.valueOf(errorCategory),
        severity = ErrorSeverity.valueOf(severity),
        correctionTier = CorrectionTier.CRITICAL,
        description = "",
        examples = emptyList()
    )
    
    return ErrorInstance(
        id = id,
        errorType = errorType,
        severity = ErrorSeverity.valueOf(severity),
        originalText = originalText,
        correctedText = correctedText,
        explanation = explanation,
        timestamp = timestampMillis,
        confidence = confidence,
        l1AdjustedWeight = l1AdjustedWeight,
        contextWindow = contextWindow
    )
}

private fun SessionAnalysisEntity.toDomain(
    improvements: List<ImprovementAreaEntity>,
    recommendations: List<SessionRecommendationEntity>
): SessionAnalysis {
    return SessionAnalysis(
        sessionId = sessionId,
        analyzedAt = analyzedAt,
        overallStats = SessionStats(
            totalSpeakingMinutes = totalSpeakingMinutes,
            wordsSpoken = wordsSpoken,
            averageWordsPerMinute = averageWordsPerMinute,
            pausePattern = PauseAnalysis(
                averagePauseDuration = averagePauseDuration,
                pauseFrequency = pauseFrequency,
                fillerWordCount = fillerWordCount,
                hesitationCount = hesitationCount,
                naturalPauseRatio = naturalPauseRatio
            ),
            overallAccuracy = overallAccuracy,
            tierSpecificAccuracy = emptyMap(), // Would need to be stored/calculated
            fluencyScore = fluencyScore,
            pronunciationScore = pronunciationScore,
            vocabularyDiversity = vocabularyDiversity
        ),
        errorBreakdown = ErrorBreakdown(
            errorsByType = try { Json.decodeFromString(errorsByType) } catch (e: Exception) { emptyMap() },
            errorsByCategory = emptyMap(), // Would need conversion
            errorsBySeverity = emptyMap(), // Would need conversion
            errorsByTier = emptyMap(), // Would need to be calculated
            improvementFromLastSession = improvementFromLastSession,
            persistentErrors = try { Json.decodeFromString(persistentErrors) } catch (e: Exception) { emptyList() },
            newErrors = try { Json.decodeFromString(newErrors) } catch (e: Exception) { emptyList() }
        ),
        improvementAreas = improvements.map { it.toDomain() },
        achievements = emptyList(), // Would need to be loaded
        recommendations = recommendations.map { it.toDomain() }
    )
}

private fun ImprovementAreaEntity.toDomain(): ImprovementArea {
    return ImprovementArea(
        errorType = errorType,
        frequency = frequency,
        impact = ErrorSeverity.valueOf(impact),
        practiceExercises = try { Json.decodeFromString(practiceExercises) } catch (e: Exception) { emptyList() },
        estimatedPracticeTime = estimatedPracticeTime,
        resourceLinks = try { Json.decodeFromString(resourceLinks) } catch (e: Exception) { emptyList() }
    )
}

private fun SessionRecommendationEntity.toDomain(): Recommendation {
    return Recommendation(
        id = id,
        type = RecommendationType.valueOf(type),
        title = title,
        description = description,
        priority = priority,
        estimatedTime = estimatedTime,
        relatedSkills = try { Json.decodeFromString(relatedSkills) } catch (e: Exception) { emptyList() }
    )
}

private fun VerbalJournalProfileEntity.toDomain(): VerbalJournalProfile {
    return VerbalJournalProfile(
        userId = userId,
        nativeLanguage = nativeLanguage,
        currentLevel = ProficiencyLevel.valueOf(currentLevel),
        currentTier = CorrectionTier.valueOf(currentTier),
        targetAccent = targetAccent,
        dailyTargetMinutes = dailyTargetMinutes,
        preferredTopics = try { Json.decodeFromString(preferredTopics) } catch (e: Exception) { emptyList() },
        correctionStyle = CorrectionStyle.valueOf(correctionStyle),
        streakDays = streakDays,
        totalSpeakingMinutes = totalSpeakingMinutes,
        lastSessionDate = lastSessionDate,
        breakInWeek = breakInWeek,
        achievements = emptyList(), // Would need to be loaded
        progressHistory = emptyList() // Would need to be loaded
    )
}

private fun VerbalJournalProfile.toEntity(): VerbalJournalProfileEntity {
    return VerbalJournalProfileEntity(
        userId = userId,
        nativeLanguage = nativeLanguage,
        currentLevel = currentLevel.name,
        currentTier = currentTier.name,
        targetAccent = targetAccent,
        dailyTargetMinutes = dailyTargetMinutes,
        preferredTopics = Json.encodeToString(preferredTopics),
        correctionStyle = correctionStyle.name,
        streakDays = streakDays,
        totalSpeakingMinutes = totalSpeakingMinutes,
        lastSessionDate = lastSessionDate,
        breakInWeek = breakInWeek
    )
}

private fun SpeechProgressSnapshotEntity.toDomain(): ProgressSnapshot {
    return ProgressSnapshot(
        date = date,
        tier = CorrectionTier.valueOf(tier),
        accuracy = accuracy,
        minutesSpoken = minutesSpoken,
        errorsFound = errorsFound,
        fluencyScore = fluencyScore,
        milestone = milestone
    )
}

private fun DailyJournalPromptEntity.toDomain(): JournalPrompt {
    return JournalPrompt(
        id = id,
        date = date,
        promptText = promptText,
        category = PromptCategory.valueOf(category),
        difficulty = ProficiencyLevel.valueOf(difficulty),
        estimatedMinutes = estimatedMinutes,
        followUpQuestions = try { Json.decodeFromString(followUpQuestions) } catch (e: Exception) { emptyList() },
        vocabularyHints = try { Json.decodeFromString(vocabularyHints) } catch (e: Exception) { emptyList() }
    )
}

private fun JournalPrompt.toEntity(): DailyJournalPromptEntity {
    return DailyJournalPromptEntity(
        id = id,
        date = date,
        promptText = promptText,
        category = category.name,
        difficulty = difficulty.name,
        estimatedMinutes = estimatedMinutes,
        followUpQuestions = Json.encodeToString(followUpQuestions),
        vocabularyHints = Json.encodeToString(vocabularyHints)
    )
}

private fun VerbalJournalAchievementEntity.toDomain(): Achievement {
    return Achievement(
        id = achievementId,
        name = name,
        description = description,
        iconUrl = iconUrl,
        earnedAt = earnedAt,
        category = AchievementCategory.valueOf(category),
        points = points
    )
}