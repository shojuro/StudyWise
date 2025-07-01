package com.studywise.ai.domain.service

import com.studywise.ai.domain.model.verbaljournal.*
import java.io.File

interface SpeechAnalysisService {
    suspend fun analyzeSpeech(
        audioFile: File,
        transcription: String,
        durationSeconds: Float
    ): SpeechMetrics
    
    suspend fun detectErrors(
        transcription: String,
        userProfile: VerbalJournalProfile,
        conversationContext: ConversationContext
    ): List<SpeechError>
    
    suspend fun generateSessionAnalysis(
        entryId: String,
        turns: List<ConversationTurn>,
        errors: List<SpeechError>
    ): SessionAnalysis
    
    suspend fun calculateFluencyScore(
        audioFile: File,
        transcription: String,
        durationSeconds: Float
    ): Float
    
    suspend fun calculatePronunciationScore(
        audioFile: File,
        transcription: String
    ): Float
    
    suspend fun detectFillerWords(transcription: String): List<String>
    
    suspend fun analyzeVocabularyComplexity(transcription: String): VocabularyLevel
}