package com.studywise.ai.data.service

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.service.SpeechAnalysisService
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockSpeechAnalysisService @Inject constructor() : SpeechAnalysisService {
    
    override suspend fun analyzeSpeech(
        audioFile: File,
        transcription: String,
        durationSeconds: Float
    ): SpeechMetrics {
        delay(Random.nextLong(500, 1000))
        
        val wordCount = transcription.split("\\s+".toRegex()).size
        val wordsPerMinute = (wordCount / (durationSeconds / 60)).toFloat()
        
        return SpeechMetrics(
            wordsPerMinute = wordsPerMinute,
            fluencyScore = Random.nextFloat() * 0.3f + 0.6f, // 0.6-0.9
            pronunciationScore = Random.nextFloat() * 0.3f + 0.6f,
            grammarScore = Random.nextFloat() * 0.3f + 0.6f,
            vocabularyScore = Random.nextFloat() * 0.3f + 0.6f,
            coherenceScore = Random.nextFloat() * 0.3f + 0.6f
        )
    }
    
    override suspend fun detectErrors(
        transcription: String,
        userProfile: VerbalJournalProfile,
        conversationContext: ConversationContext
    ): List<SpeechError> {
        delay(Random.nextLong(300, 800))
        
        val errors = mutableListOf<SpeechError>()
        
        // Mock error detection based on proficiency level
        when (userProfile.proficiencyLevel) {
            ProficiencyLevel.BEGINNER, ProficiencyLevel.ELEMENTARY -> {
                // Common beginner errors
                if (transcription.contains("I am go", ignoreCase = true)) {
                    errors.add(createGrammarError(
                        "I am go",
                        "I am going",
                        "Use 'am going' for present continuous tense"
                    ))
                }
                if (transcription.contains("yesterday I go", ignoreCase = true)) {
                    errors.add(createGrammarError(
                        "yesterday I go",
                        "yesterday I went",
                        "Use past tense 'went' with 'yesterday'"
                    ))
                }
            }
            ProficiencyLevel.INTERMEDIATE -> {
                // Intermediate errors
                if (transcription.contains("have went", ignoreCase = true)) {
                    errors.add(createGrammarError(
                        "have went",
                        "have gone",
                        "Use past participle 'gone' with 'have'"
                    ))
                }
            }
            else -> {
                // Advanced - fewer errors
                if (Random.nextFloat() < 0.2f) {
                    errors.add(createVocabularyError(
                        "very good",
                        "excellent",
                        "Consider using more sophisticated vocabulary"
                    ))
                }
            }
        }
        
        // Add pronunciation errors based on native language
        if (userProfile.nativeLanguage == "Spanish" && transcription.contains("school", ignoreCase = true)) {
            if (Random.nextFloat() < 0.3f) {
                errors.add(createPronunciationError(
                    "school",
                    "school",
                    "Remember not to add 'e' before 's' sound"
                ))
            }
        }
        
        return errors
    }
    
    override suspend fun generateSessionAnalysis(
        entryId: String,
        turns: List<ConversationTurn>,
        errors: List<SpeechError>
    ): SessionAnalysis {
        delay(Random.nextLong(1000, 2000))
        
        val userTurns = turns.filter { it.role == ConversationRole.USER }
        val allWords = userTurns.flatMap { it.transcription.split("\\s+".toRegex()) }
        val uniqueWords = allWords.toSet()
        
        val metrics = AggregateMetrics(
            averageFluencyScore = userTurns.mapNotNull { it.metrics?.fluencyScore }.average().toFloat(),
            averagePronunciationScore = userTurns.mapNotNull { it.metrics?.pronunciationScore }.average().toFloat(),
            averageGrammarScore = userTurns.mapNotNull { it.metrics?.grammarScore }.average().toFloat(),
            averageVocabularyScore = userTurns.mapNotNull { it.metrics?.vocabularyScore }.average().toFloat(),
            averageCoherenceScore = userTurns.mapNotNull { it.metrics?.coherenceScore }.average().toFloat()
        )
        
        val stats = SpeechStatistics(
            totalWords = allWords.size,
            uniqueWords = uniqueWords.size,
            averageWordsPerMinute = userTurns.mapNotNull { it.metrics?.wordsPerMinute }.average().toFloat(),
            totalPauses = Random.nextInt(5, 15),
            averagePauseDuration = Random.nextFloat() * 2 + 0.5f,
            longestUtterance = userTurns.maxOfOrNull { it.transcription.split("\\s+".toRegex()).size } ?: 0,
            totalFillerWords = allWords.count { it.lowercase() in listOf("um", "uh", "like", "you know") },
            totalSelfCorrections = Random.nextInt(0, 5)
        )
        
        val errorAnalysis = ErrorAnalysis(
            mostCommonErrors = errors.groupBy { it.errorType }
                .map { (type, errors) ->
                    ErrorPattern(
                        type = type,
                        category = errors.first().errorCategory,
                        frequency = errors.size,
                        examples = errors.take(3).map { it.originalText }
                    )
                }
                .sortedByDescending { it.frequency }
                .take(3),
            errorDistribution = errors.groupBy { it.errorType }.mapValues { it.value.size }
        )
        
        val insights = PerformanceInsights(
            vocabularyLevel = analyzeVocabularyLevel(uniqueWords),
            topicRelevanceScore = Random.nextFloat() * 0.3f + 0.7f,
            conversationFlowScore = Random.nextFloat() * 0.3f + 0.7f,
            strengths = generateStrengths(metrics),
            weaknesses = generateWeaknesses(metrics)
        )
        
        return SessionAnalysis(
            id = UUID.randomUUID().toString(),
            entryId = entryId,
            metrics = metrics,
            speechStats = stats,
            errorAnalysis = errorAnalysis,
            performanceInsights = insights,
            generatedAt = LocalDateTime.now()
        )
    }
    
    override suspend fun calculateFluencyScore(
        audioFile: File,
        transcription: String,
        durationSeconds: Float
    ): Float {
        // Mock calculation
        return Random.nextFloat() * 0.3f + 0.6f
    }
    
    override suspend fun calculatePronunciationScore(
        audioFile: File,
        transcription: String
    ): Float {
        // Mock calculation
        return Random.nextFloat() * 0.3f + 0.6f
    }
    
    override suspend fun detectFillerWords(transcription: String): List<String> {
        val fillerWords = listOf("um", "uh", "like", "you know", "basically", "actually")
        return transcription.split("\\s+".toRegex())
            .filter { it.lowercase() in fillerWords }
    }
    
    override suspend fun analyzeVocabularyComplexity(transcription: String): VocabularyLevel {
        val words = transcription.split("\\s+".toRegex())
        val averageWordLength = words.map { it.length }.average()
        
        return when {
            averageWordLength < 4 -> VocabularyLevel.BEGINNER
            averageWordLength < 6 -> VocabularyLevel.INTERMEDIATE
            else -> VocabularyLevel.ADVANCED
        }
    }
    
    private fun createGrammarError(
        original: String,
        corrected: String,
        explanation: String
    ): SpeechError {
        return SpeechError(
            id = UUID.randomUUID().toString(),
            turnId = UUID.randomUUID().toString(),
            errorType = ErrorType.GRAMMAR,
            errorCategory = "Tense",
            originalText = original,
            correctedText = corrected,
            explanation = explanation,
            severity = ErrorSeverity.MEDIUM,
            position = null,
            audioTimestamp = null
        )
    }
    
    private fun createPronunciationError(
        original: String,
        corrected: String,
        explanation: String
    ): SpeechError {
        return SpeechError(
            id = UUID.randomUUID().toString(),
            turnId = UUID.randomUUID().toString(),
            errorType = ErrorType.PRONUNCIATION,
            errorCategory = "L1 Interference",
            originalText = original,
            correctedText = corrected,
            explanation = explanation,
            severity = ErrorSeverity.LOW,
            position = null,
            audioTimestamp = null
        )
    }
    
    private fun createVocabularyError(
        original: String,
        corrected: String,
        explanation: String
    ): SpeechError {
        return SpeechError(
            id = UUID.randomUUID().toString(),
            turnId = UUID.randomUUID().toString(),
            errorType = ErrorType.VOCABULARY,
            errorCategory = "Word Choice",
            originalText = original,
            correctedText = corrected,
            explanation = explanation,
            severity = ErrorSeverity.LOW,
            position = null,
            audioTimestamp = null
        )
    }
    
    private fun analyzeVocabularyLevel(uniqueWords: Set<String>): VocabularyLevel {
        val averageLength = uniqueWords.map { it.length }.average()
        return when {
            averageLength < 5 -> VocabularyLevel.BEGINNER
            averageLength < 7 -> VocabularyLevel.INTERMEDIATE
            else -> VocabularyLevel.ADVANCED
        }
    }
    
    private fun generateStrengths(metrics: AggregateMetrics): List<String> {
        val strengths = mutableListOf<String>()
        
        if (metrics.averageFluencyScore > 0.8f) {
            strengths.add("Excellent fluency and natural speech flow")
        }
        if (metrics.averagePronunciationScore > 0.8f) {
            strengths.add("Clear pronunciation")
        }
        if (metrics.averageGrammarScore > 0.8f) {
            strengths.add("Strong grammar usage")
        }
        if (metrics.averageVocabularyScore > 0.8f) {
            strengths.add("Rich vocabulary")
        }
        if (metrics.averageCoherenceScore > 0.8f) {
            strengths.add("Well-organized ideas")
        }
        
        return strengths.ifEmpty { listOf("Good effort and participation") }
    }
    
    private fun generateWeaknesses(metrics: AggregateMetrics): List<String> {
        val weaknesses = mutableListOf<String>()
        
        if (metrics.averageFluencyScore < 0.7f) {
            weaknesses.add("Work on speaking more fluently with fewer pauses")
        }
        if (metrics.averagePronunciationScore < 0.7f) {
            weaknesses.add("Practice pronunciation of difficult sounds")
        }
        if (metrics.averageGrammarScore < 0.7f) {
            weaknesses.add("Review grammar rules for tense usage")
        }
        if (metrics.averageVocabularyScore < 0.7f) {
            weaknesses.add("Expand vocabulary with synonyms and phrases")
        }
        if (metrics.averageCoherenceScore < 0.7f) {
            weaknesses.add("Focus on organizing thoughts before speaking")
        }
        
        return weaknesses.ifEmpty { listOf("Continue practicing regularly") }
    }
}