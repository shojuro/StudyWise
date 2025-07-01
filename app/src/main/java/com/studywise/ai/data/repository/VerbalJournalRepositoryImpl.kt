package com.studywise.ai.data.repository

import com.studywise.ai.data.local.dao.VerbalJournalDao
import com.studywise.ai.data.local.entity.verbaljournal.*
import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.VerbalJournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerbalJournalRepositoryImpl @Inject constructor(
    private val verbalJournalDao: VerbalJournalDao
) : VerbalJournalRepository {
    
    override suspend fun createEntry(
        userId: Int,
        sessionType: SessionType,
        topic: String?,
        promptId: String?,
        targetDurationMinutes: Int,
        language: String,
        difficulty: DifficultyLevel
    ): VerbalJournalEntry {
        val entry = VerbalJournalEntryEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            sessionType = sessionType.name,
            topic = topic,
            promptId = promptId,
            targetDurationMinutes = targetDurationMinutes,
            actualDurationSeconds = 0,
            totalTurns = 0,
            overallScore = 0f,
            status = SessionStatus.IN_PROGRESS.name,
            createdAt = LocalDateTime.now(),
            completedAt = null,
            language = language,
            difficulty = difficulty.name
        )
        
        verbalJournalDao.insertEntry(entry)
        return entry.toDomainModel()
    }
    
    override suspend fun updateEntry(entry: VerbalJournalEntry) {
        verbalJournalDao.updateEntry(entry.toEntity())
    }
    
    override suspend fun getEntry(entryId: String): VerbalJournalEntry? {
        return verbalJournalDao.getEntry(entryId)?.toDomainModel()
    }
    
    override fun getUserEntries(userId: Int): Flow<List<VerbalJournalEntry>> {
        return verbalJournalDao.getUserEntries(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getRecentCompletedEntries(userId: Int, limit: Int): List<VerbalJournalEntry> {
        return verbalJournalDao.getRecentCompletedEntries(userId, limit).map { it.toDomainModel() }
    }
    
    override suspend fun completeSession(entryId: String, completedAt: LocalDateTime) {
        verbalJournalDao.completeSession(entryId, completedAt)
    }
    
    override suspend fun addConversationTurn(
        entryId: String,
        role: ConversationRole,
        transcription: String,
        audioFilePath: String?,
        durationSeconds: Float,
        metrics: SpeechMetrics?
    ): ConversationTurn {
        val existingTurns = verbalJournalDao.getEntryTurns(entryId)
        val turnNumber = existingTurns.size + 1
        
        val turn = ConversationTurnEntity(
            id = UUID.randomUUID().toString(),
            entryId = entryId,
            turnNumber = turnNumber,
            role = role.name,
            audioFilePath = audioFilePath,
            transcription = transcription,
            durationSeconds = durationSeconds,
            wordsPerMinute = metrics?.wordsPerMinute,
            fluencyScore = metrics?.fluencyScore,
            pronunciationScore = metrics?.pronunciationScore,
            grammarScore = metrics?.grammarScore,
            vocabularyScore = metrics?.vocabularyScore,
            coherenceScore = metrics?.coherenceScore,
            timestamp = LocalDateTime.now()
        )
        
        verbalJournalDao.insertTurn(turn)
        
        // Update entry turn count
        val entry = verbalJournalDao.getEntry(entryId)
        entry?.let {
            verbalJournalDao.updateEntry(it.copy(
                totalTurns = turnNumber,
                actualDurationSeconds = it.actualDurationSeconds + durationSeconds.toInt()
            ))
        }
        
        return turn.toDomainModel()
    }
    
    override suspend fun getConversationTurns(entryId: String): List<ConversationTurn> {
        return verbalJournalDao.getEntryTurns(entryId).map { it.toDomainModel() }
    }
    
    override suspend fun addSpeechErrors(turnId: String, errors: List<SpeechError>) {
        val entities = errors.map { it.toEntity() }
        verbalJournalDao.insertErrors(entities)
    }
    
    override suspend fun getSpeechErrors(turnId: String): List<SpeechError> {
        return verbalJournalDao.getTurnErrors(turnId).map { it.toDomainModel() }
    }
    
    override suspend fun getSessionErrors(entryId: String): List<SpeechError> {
        return verbalJournalDao.getEntryErrors(entryId).map { it.toDomainModel() }
    }
    
    override suspend fun saveSessionAnalysis(analysis: SessionAnalysis) {
        verbalJournalDao.insertAnalysis(analysis.toEntity())
    }
    
    override suspend fun getSessionAnalysis(entryId: String): SessionAnalysis? {
        return verbalJournalDao.getAnalysis(entryId)?.toDomainModel()
    }
    
    override suspend fun createOrUpdateProfile(profile: VerbalJournalProfile) {
        verbalJournalDao.insertProfile(profile.toEntity())
    }
    
    override suspend fun getUserProfile(userId: Int): VerbalJournalProfile? {
        return verbalJournalDao.getUserProfile(userId)?.toDomainModel()
    }
    
    override fun observeUserProfile(userId: Int): Flow<VerbalJournalProfile?> {
        return verbalJournalDao.observeUserProfile(userId).map { it?.toDomainModel() }
    }
    
    override suspend fun updateProfileStatistics(
        userId: Int,
        sessionCompleted: Boolean,
        minutesSpoken: Int,
        lastSessionDate: LocalDateTime
    ) {
        val profile = verbalJournalDao.getUserProfile(userId) ?: return
        
        val updatedProfile = profile.copy(
            totalSessions = if (sessionCompleted) profile.totalSessions + 1 else profile.totalSessions,
            totalMinutesSpoken = profile.totalMinutesSpoken + minutesSpoken,
            lastSessionDate = lastSessionDate,
            updatedAt = LocalDateTime.now()
        )
        
        verbalJournalDao.updateProfile(updatedProfile)
    }
    
    override suspend fun updateStreak(userId: Int, currentStreak: Int, longestStreak: Int) {
        val profile = verbalJournalDao.getUserProfile(userId) ?: return
        
        val updatedProfile = profile.copy(
            currentStreak = currentStreak,
            longestStreak = maxOf(longestStreak, profile.longestStreak),
            updatedAt = LocalDateTime.now()
        )
        
        verbalJournalDao.updateProfile(updatedProfile)
    }
    
    override suspend fun addImprovementAreas(profileId: String, areas: List<ImprovementArea>) {
        val entities = areas.map { it.toEntity() }
        verbalJournalDao.insertImprovementAreas(entities)
    }
    
    override suspend fun updateImprovementArea(area: ImprovementArea) {
        verbalJournalDao.updateImprovementArea(area.toEntity())
    }
    
    override suspend fun getActiveImprovementAreas(profileId: String): List<ImprovementArea> {
        return verbalJournalDao.getActiveImprovementAreas(profileId).map { it.toDomainModel() }
    }
    
    override suspend fun markImprovementAreaAchieved(areaId: String, achievedAt: LocalDateTime) {
        // Implementation would fetch area, update achievedAt and isActive, then save
    }
    
    override suspend fun addRecommendations(analysisId: String, recommendations: List<SessionRecommendation>) {
        val entities = recommendations.map { it.toEntity() }
        verbalJournalDao.insertRecommendations(entities)
    }
    
    override suspend fun getSessionRecommendations(entryId: String): List<SessionRecommendation> {
        return verbalJournalDao.getEntryRecommendations(entryId).map { it.toDomainModel() }
    }
    
    override suspend fun markRecommendationCompleted(recommendationId: String, completedAt: LocalDateTime) {
        // Implementation would fetch recommendation, update completion, then save
    }
    
    override suspend fun getDailyPrompt(date: LocalDate): DailyPrompt? {
        return verbalJournalDao.getDailyPrompt(date)?.toDomainModel()
    }
    
    override suspend fun getPromptsForDateRange(startDate: LocalDate, endDate: LocalDate): List<DailyPrompt> {
        return verbalJournalDao.getPromptsInRange(startDate, endDate).map { it.toDomainModel() }
    }
    
    override suspend fun saveDailyPrompt(prompt: DailyPrompt) {
        verbalJournalDao.insertPrompt(prompt.toEntity())
    }
    
    override suspend fun createAchievement(achievement: Achievement) {
        verbalJournalDao.insertAchievement(achievement.toEntity())
    }
    
    override suspend fun updateAchievementProgress(achievementId: String, progress: Float) {
        // Implementation would fetch achievement, update progress, then save
    }
    
    override suspend fun unlockAchievement(achievementId: String, unlockedAt: LocalDateTime) {
        // Implementation would fetch achievement, update unlock status, then save
    }
    
    override fun observeAchievements(profileId: String): Flow<List<Achievement>> {
        return verbalJournalDao.observeAchievements(profileId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getUnlockedAchievements(profileId: String): List<Achievement> {
        return verbalJournalDao.getUnlockedAchievements(profileId).map { it.toDomainModel() }
    }
    
    override suspend fun checkAndUnlockAchievements(profileId: String): List<Achievement> {
        // Complex implementation to check achievement criteria and unlock if met
        return emptyList()
    }
    
    override suspend fun saveProgressSnapshot(snapshot: SpeechProgressSnapshot) {
        verbalJournalDao.insertSnapshot(snapshot.toEntity())
    }
    
    override suspend fun getRecentSnapshots(
        profileId: String,
        periodType: PeriodType,
        limit: Int
    ): List<SpeechProgressSnapshot> {
        return verbalJournalDao.getRecentSnapshots(profileId, periodType.name, limit)
            .map { it.toDomainModel() }
    }
    
    override suspend fun getProgressInDateRange(
        profileId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<SpeechProgressSnapshot> {
        return verbalJournalDao.getSnapshotsInRange(profileId, startDate, endDate)
            .map { it.toDomainModel() }
    }
    
    override suspend fun generateProgressSnapshot(
        profileId: String,
        periodType: PeriodType
    ): SpeechProgressSnapshot {
        // Complex implementation to calculate metrics and generate snapshot
        throw NotImplementedError("Generate progress snapshot requires aggregation logic")
    }
    
    override suspend fun getTotalSessionTime(userId: Int): Int {
        val entries = verbalJournalDao.getRecentCompletedEntries(userId, Int.MAX_VALUE)
        return entries.sumOf { it.actualDurationSeconds }
    }
    
    override suspend fun getAverageSessionDuration(userId: Int): Float {
        val entries = verbalJournalDao.getRecentCompletedEntries(userId, Int.MAX_VALUE)
        return if (entries.isNotEmpty()) {
            entries.map { it.actualDurationSeconds }.average().toFloat()
        } else 0f
    }
    
    override suspend fun getSessionCountByType(userId: Int): Map<SessionType, Int> {
        val entries = verbalJournalDao.getRecentCompletedEntries(userId, Int.MAX_VALUE)
        return entries.groupingBy { SessionType.valueOf(it.sessionType) }.eachCount()
    }
    
    override suspend fun getMostCommonErrors(userId: Int, limit: Int): List<ErrorPattern> {
        // Complex implementation requiring aggregation
        return emptyList()
    }
    
    override suspend fun getSkillProgress(userId: Int): Map<ImprovementAreaType, Float> {
        // Complex implementation requiring aggregation
        return emptyMap()
    }
    
    override suspend fun getCompletedSessionsOnDate(userId: Int, date: LocalDateTime): Int {
        return verbalJournalDao.getCompletedSessionsOnDate(userId, date)
    }
    
    override suspend fun getBreakInSchedule(userId: Int): BreakInSchedule? {
        val profile = verbalJournalDao.getUserProfile(userId) ?: return null
        if (!profile.enableBreakInPeriod) return null
        
        val weekInProgram = getWeekInProgram(userId) ?: return null
        return BreakInPeriodSchedules.getScheduleForWeek(weekInProgram)
    }
    
    override suspend fun isUserInBreakInPeriod(userId: Int): Boolean {
        val profile = verbalJournalDao.getUserProfile(userId) ?: return false
        if (!profile.enableBreakInPeriod) return false
        
        val startDate = profile.createdAt.toLocalDate()
        val currentDate = LocalDate.now()
        return BreakInPeriodSchedules.isInBreakInPeriod(startDate, currentDate)
    }
    
    override suspend fun getWeekInProgram(userId: Int): Int? {
        val profile = verbalJournalDao.getUserProfile(userId) ?: return null
        val startDate = profile.createdAt.toLocalDate()
        val currentDate = LocalDate.now()
        val daysSinceStart = currentDate.toEpochDay() - startDate.toEpochDay()
        return ((daysSinceStart / 7) + 1).toInt()
    }
    
    override suspend fun deleteOldAudioFiles(olderThan: LocalDateTime) {
        // Implementation to clean up old audio files
    }
    
    override suspend fun cleanupIncompleteSession(entryId: String) {
        // Implementation to clean up incomplete sessions
    }
}

// Extension functions for mapping between entities and domain models
private fun VerbalJournalEntryEntity.toDomainModel() = VerbalJournalEntry(
    id = id,
    userId = userId,
    sessionType = SessionType.valueOf(sessionType),
    topic = topic,
    promptId = promptId,
    targetDurationMinutes = targetDurationMinutes,
    actualDurationSeconds = actualDurationSeconds,
    totalTurns = totalTurns,
    overallScore = overallScore,
    status = SessionStatus.valueOf(status),
    createdAt = createdAt,
    completedAt = completedAt,
    language = language,
    difficulty = DifficultyLevel.valueOf(difficulty)
)

private fun VerbalJournalEntry.toEntity() = VerbalJournalEntryEntity(
    id = id,
    userId = userId,
    sessionType = sessionType.name,
    topic = topic,
    promptId = promptId,
    targetDurationMinutes = targetDurationMinutes,
    actualDurationSeconds = actualDurationSeconds,
    totalTurns = totalTurns,
    overallScore = overallScore,
    status = status.name,
    createdAt = createdAt,
    completedAt = completedAt,
    language = language,
    difficulty = difficulty.name
)

private fun ConversationTurnEntity.toDomainModel() = ConversationTurn(
    id = id,
    entryId = entryId,
    turnNumber = turnNumber,
    role = ConversationRole.valueOf(role),
    audioFilePath = audioFilePath,
    transcription = transcription,
    durationSeconds = durationSeconds,
    metrics = if (wordsPerMinute != null) SpeechMetrics(
        wordsPerMinute = wordsPerMinute,
        fluencyScore = fluencyScore ?: 0f,
        pronunciationScore = pronunciationScore ?: 0f,
        grammarScore = grammarScore ?: 0f,
        vocabularyScore = vocabularyScore ?: 0f,
        coherenceScore = coherenceScore ?: 0f
    ) else null,
    timestamp = timestamp
)

private fun SpeechErrorEntity.toDomainModel() = SpeechError(
    id = id,
    turnId = turnId,
    errorType = ErrorType.valueOf(errorType),
    errorCategory = errorCategory,
    originalText = originalText,
    correctedText = correctedText,
    explanation = explanation,
    severity = ErrorSeverity.valueOf(severity),
    position = if (startPosition != null && endPosition != null) {
        ErrorPosition(startPosition, endPosition)
    } else null,
    audioTimestamp = audioTimestamp
)

private fun SpeechError.toEntity() = SpeechErrorEntity(
    id = id,
    turnId = turnId,
    errorType = errorType.name,
    errorCategory = errorCategory,
    originalText = originalText,
    correctedText = correctedText,
    explanation = explanation,
    severity = severity.name,
    startPosition = position?.start,
    endPosition = position?.end,
    audioTimestamp = audioTimestamp
)

// Additional mapper functions would be implemented for all other entities...
// Abbreviated for space - similar pattern for all entity/domain conversions

private fun SessionAnalysisEntity.toDomainModel(): SessionAnalysis {
    // Parse JSON fields and create domain model
    throw NotImplementedError("JSON parsing for complex fields")
}

private fun SessionAnalysis.toEntity(): SessionAnalysisEntity {
    // Convert domain model to entity with JSON serialization
    throw NotImplementedError("JSON serialization for complex fields")
}

private fun VerbalJournalProfileEntity.toDomainModel(): VerbalJournalProfile {
    // Parse JSON fields and create domain model
    return VerbalJournalProfile(
        id = id,
        userId = userId,
        proficiencyLevel = ProficiencyLevel.valueOf(proficiencyLevel),
        nativeLanguage = nativeLanguage,
        targetLanguage = targetLanguage,
        preferences = UserPreferences(
            preferredTopics = emptyList(), // Parse from JSON
            preferredSessionTime = preferredSessionTime,
            enableReminders = enableReminders,
            enableBreakInPeriod = enableBreakInPeriod
        ),
        goals = UserGoals(
            dailyGoalMinutes = dailyGoalMinutes,
            weeklyGoalSessions = weeklyGoalSessions
        ),
        statistics = UserStatistics(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalSessions = totalSessions,
            totalMinutesSpoken = totalMinutesSpoken,
            lastSessionDate = lastSessionDate
        ),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun VerbalJournalProfile.toEntity(): VerbalJournalProfileEntity {
    // Convert domain model to entity
    return VerbalJournalProfileEntity(
        id = id,
        userId = userId,
        proficiencyLevel = proficiencyLevel.name,
        nativeLanguage = nativeLanguage,
        targetLanguage = targetLanguage,
        preferredTopics = "", // Serialize to JSON
        dailyGoalMinutes = goals.dailyGoalMinutes,
        weeklyGoalSessions = goals.weeklyGoalSessions,
        preferredSessionTime = preferences.preferredSessionTime,
        enableReminders = preferences.enableReminders,
        enableBreakInPeriod = preferences.enableBreakInPeriod,
        currentStreak = statistics.currentStreak,
        longestStreak = statistics.longestStreak,
        totalSessions = statistics.totalSessions,
        totalMinutesSpoken = statistics.totalMinutesSpoken,
        lastSessionDate = statistics.lastSessionDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Similar mappers for remaining entities...
private fun ImprovementAreaEntity.toDomainModel(): ImprovementArea {
    throw NotImplementedError("JSON parsing for complex fields")
}

private fun ImprovementArea.toEntity(): ImprovementAreaEntity {
    throw NotImplementedError("JSON serialization for complex fields")
}

private fun SessionRecommendationEntity.toDomainModel(): SessionRecommendation {
    throw NotImplementedError("JSON parsing for complex fields")
}

private fun SessionRecommendation.toEntity(): SessionRecommendationEntity {
    throw NotImplementedError("JSON serialization for complex fields")
}

private fun DailyJournalPromptEntity.toDomainModel(): DailyPrompt {
    throw NotImplementedError("JSON parsing for complex fields")
}

private fun DailyPrompt.toEntity(): DailyJournalPromptEntity {
    throw NotImplementedError("JSON serialization for complex fields")
}

private fun VerbalJournalAchievementEntity.toDomainModel(): Achievement {
    throw NotImplementedError("JSON parsing for complex fields")
}

private fun Achievement.toEntity(): VerbalJournalAchievementEntity {
    throw NotImplementedError("JSON serialization for complex fields")
}

private fun SpeechProgressSnapshotEntity.toDomainModel(): SpeechProgressSnapshot {
    throw NotImplementedError("JSON parsing for complex fields")
}

private fun SpeechProgressSnapshot.toEntity(): SpeechProgressSnapshotEntity {
    throw NotImplementedError("JSON serialization for complex fields")
}