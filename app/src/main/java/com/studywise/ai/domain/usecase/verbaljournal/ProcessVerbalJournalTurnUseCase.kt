package com.studywise.ai.domain.usecase.verbaljournal

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.VerbalJournalRepository
import com.studywise.ai.domain.service.SpeechErrorDetectionService
import com.studywise.ai.domain.service.VerbalJournalConversationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for processing a user's speech turn in a Verbal Journal session
 */
class ProcessVerbalJournalTurnUseCase @Inject constructor(
    private val verbalJournalRepository: VerbalJournalRepository,
    private val errorDetectionService: SpeechErrorDetectionService,
    private val conversationService: VerbalJournalConversationService
) {
    
    /**
     * Process a user's speech turn
     */
    suspend operator fun invoke(
        sessionId: String,
        audioFile: File?,
        transcript: String,
        transcriptionConfidence: Float = 0.95f,
        audioFeatures: AudioFeatures? = null
    ): Flow<ProcessTurnResult> = flow {
        try {
            // Get session and profile
            val session = verbalJournalRepository.getJournalEntry(sessionId)
                ?: throw IllegalStateException("Session not found")
            
            val profile = verbalJournalRepository.getOrCreateProfile(
                userId = session.userId,
                nativeLanguage = "English" // Should come from user settings
            )
            
            // Save audio file if provided
            val audioPath = audioFile?.let {
                verbalJournalRepository.saveAudioSegment(
                    turnId = UUID.randomUUID().toString(),
                    audioFile = it
                )
            }
            
            // Detect errors in transcript
            val errors = errorDetectionService.analyzeTranscript(
                transcript = transcript,
                userProfile = profile,
                audioFeatures = audioFeatures
            )
            
            // Create user turn
            val userTurn = ConversationTurn(
                id = UUID.randomUUID().toString(),
                speaker = Speaker.USER,
                timestamp = Date(),
                audioSegmentPath = audioPath,
                transcript = transcript,
                transcriptionConfidence = transcriptionConfidence,
                errors = errors,
                aiResponse = null
            )
            
            // Add user turn to conversation
            verbalJournalRepository.addConversationTurn(sessionId, userTurn)
            
            // Generate AI response
            val aiResponse = conversationService.generateResponse(
                journalEntryId = sessionId,
                userTranscript = transcript,
                userProfile = profile
            )
            
            // Create AI turn
            val aiTurn = ConversationTurn(
                id = UUID.randomUUID().toString(),
                speaker = Speaker.AI,
                timestamp = Date(),
                audioSegmentPath = null,
                transcript = aiResponse,
                transcriptionConfidence = 1.0f,
                errors = emptyList(),
                aiResponse = aiResponse
            )
            
            // Add AI turn to conversation
            verbalJournalRepository.addConversationTurn(sessionId, aiTurn)
            
            // Update session transcript
            val updatedSession = session.copy(
                transcript = session.transcript + " " + transcript,
                endTime = Date()
            )
            verbalJournalRepository.updateJournalEntry(updatedSession)
            
            // Determine if any errors should be shown (based on correction tier)
            val errorsToShow = filterErrorsByTier(errors, profile.currentTier)
            
            emit(
                ProcessTurnResult.Success(
                    userTurn = userTurn,
                    aiResponse = aiResponse,
                    detectedErrors = errorsToShow,
                    shouldShowCorrections = errorsToShow.isNotEmpty() && 
                        shouldShowCorrections(session, profile)
                )
            )
            
        } catch (e: Exception) {
            emit(ProcessTurnResult.Error(e.message ?: "Failed to process turn"))
        }
    }
    
    /**
     * Filter errors based on correction tier
     */
    private fun filterErrorsByTier(
        errors: List<ErrorInstance>,
        tier: CorrectionTier
    ): List<ErrorInstance> {
        return when (tier) {
            CorrectionTier.CRITICAL -> 
                errors.filter { it.severity == ErrorSeverity.CRITICAL }
            CorrectionTier.IMPORTANT -> 
                errors.filter { it.severity in listOf(ErrorSeverity.CRITICAL, ErrorSeverity.IMPORTANT) }
            CorrectionTier.HELPFUL -> 
                errors.filter { it.severity != ErrorSeverity.POLISH }
            CorrectionTier.COMPREHENSIVE -> 
                errors
        }
    }
    
    /**
     * Determine if corrections should be shown mid-conversation
     */
    private fun shouldShowCorrections(
        session: VerbalJournalEntry,
        profile: VerbalJournalProfile
    ): Boolean {
        // During break-in period, limit corrections
        if (profile.breakInWeek <= 4) {
            return false // No mid-session corrections for first 4 weeks
        }
        
        // Based on correction style preference
        return when (profile.correctionStyle) {
            CorrectionStyle.GENTLE -> false // Save all for end
            CorrectionStyle.BALANCED -> session.conversationTurns.size % 5 == 0 // Every 5 turns
            CorrectionStyle.INTENSIVE -> true // Show immediately
        }
    }
}

/**
 * Result of processing a turn
 */
sealed class ProcessTurnResult {
    data class Success(
        val userTurn: ConversationTurn,
        val aiResponse: String,
        val detectedErrors: List<ErrorInstance>,
        val shouldShowCorrections: Boolean
    ) : ProcessTurnResult()
    
    data class Error(val message: String) : ProcessTurnResult()
}