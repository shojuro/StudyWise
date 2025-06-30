package com.studywise.ai.domain.usecase.verbaljournal

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.VerbalJournalRepository
import com.studywise.ai.domain.service.VerbalJournalConversationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for starting a new Verbal Journal session
 */
class StartVerbalJournalSessionUseCase @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository,
    private val conversationService: VerbalJournalConversationService
) {
    
    /**
     * Start a new Verbal Journal session for the user
     */
    suspend operator fun invoke(userId: String): Flow<StartSessionResult> = flow {
        try {
            // Get or create user profile
            val profile = verbalJournalRepository.getOrCreateProfile(
                userId = userId,
                nativeLanguage = "English" // Default, should come from user settings
            )
            
            // Check if user has completed a session today
            val todaysSessions = verbalJournalRepository.getUserJournalEntries(userId)
            
            // Determine session duration based on break-in period
            val sessionDuration = getSessionDuration(profile.breakInWeek)
            
            // Get today's prompt
            val prompt = conversationService.getJournalPrompt(
                date = java.util.Date(),
                userProfile = profile
            )
            
            // Create new journal entry
            val entry = verbalJournalRepository.createJournalEntry(
                userId = userId,
                targetDurationMinutes = sessionDuration,
                correctionTier = profile.currentTier
            )
            
            // Generate initial AI greeting
            val greeting = generateGreeting(profile, prompt)
            
            // Add AI greeting as first conversation turn
            val aiTurn = ConversationTurn(
                id = java.util.UUID.randomUUID().toString(),
                speaker = Speaker.AI,
                timestamp = java.util.Date(),
                transcript = greeting,
                transcriptionConfidence = 1.0f,
                errors = emptyList(),
                aiResponse = greeting,
                audioSegmentPath = null
            )
            
            verbalJournalRepository.addConversationTurn(entry.id, aiTurn)
            
            emit(
                StartSessionResult.Success(
                    sessionId = entry.id,
                    profile = profile,
                    prompt = prompt,
                    targetDuration = sessionDuration,
                    initialGreeting = greeting
                )
            )
            
        } catch (e: Exception) {
            emit(StartSessionResult.Error(e.message ?: "Failed to start session"))
        }
    }
    
    /**
     * Get session duration based on break-in week
     */
    private fun getSessionDuration(breakInWeek: Int): Int {
        return BreakInSchedules.getStandardSchedule()
            .find { it.week == breakInWeek }
            ?.sessionLengthMinutes
            ?: 30 // Default to 30 minutes if beyond break-in period
    }
    
    /**
     * Generate personalized greeting
     */
    private fun generateGreeting(
        profile: VerbalJournalProfile,
        prompt: JournalPrompt
    ): String {
        val greetings = when (profile.currentLevel) {
            ProficiencyLevel.BEGINNER -> listOf(
                "Hello! Welcome to today's speaking practice. ${prompt.promptText}",
                "Hi there! Let's practice English together. ${prompt.promptText}",
                "Good to see you! Ready for today's conversation? ${prompt.promptText}"
            )
            ProficiencyLevel.INTERMEDIATE -> listOf(
                "Hey! Great to have you back for another session. ${prompt.promptText}",
                "Welcome back! I'm excited to chat with you today. ${prompt.promptText}",
                "Hi! Let's dive into today's topic. ${prompt.promptText}"
            )
            ProficiencyLevel.ADVANCED -> listOf(
                "Welcome! I'm looking forward to our discussion today. ${prompt.promptText}",
                "Great to see you again! ${prompt.promptText}",
                "Hello! Let's explore today's thought-provoking topic. ${prompt.promptText}"
            )
            ProficiencyLevel.NATIVE_LEVEL -> listOf(
                "Welcome back! ${prompt.promptText}",
                "Hey there! ${prompt.promptText}",
                "Good to see you! ${prompt.promptText}"
            )
        }
        
        return greetings.random()
    }
}

/**
 * Result of starting a session
 */
sealed class StartSessionResult {
    data class Success(
        val sessionId: String,
        val profile: VerbalJournalProfile,
        val prompt: JournalPrompt,
        val targetDuration: Int,
        val initialGreeting: String
    ) : StartSessionResult()
    
    data class Error(val message: String) : StartSessionResult()
}