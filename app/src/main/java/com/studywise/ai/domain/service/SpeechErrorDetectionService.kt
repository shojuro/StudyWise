package com.studywise.ai.domain.service

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.AIRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for detecting errors in speech transcripts
 * Combines rule-based detection with AI-powered analysis
 */
@Singleton
class SpeechErrorDetectionService @Inject constructor(
    private val aiRepository: AIRepository
) {
    
    /**
     * Analyze a transcript for errors based on user's profile and correction tier
     */
    suspend fun analyzeTranscript(
        transcript: String,
        userProfile: VerbalJournalProfile,
        audioFeatures: AudioFeatures? = null
    ): List<ErrorInstance> = coroutineScope {
        if (transcript.isBlank()) return@coroutineScope emptyList()
        
        // Get errors to check based on correction tier
        val errorTypesToCheck = ErrorTypes.getErrorsForTier(userProfile.currentTier)
        
        // Run detection in parallel
        val detectionTasks = listOf(
            async { detectRuleBasedErrors(transcript, errorTypesToCheck) },
            async { detectAIErrors(transcript, userProfile) }
        )
        
        val allErrors = detectionTasks.awaitAll().flatten()
        
        // Apply L1 interference weighting
        val weightedErrors = applyL1Weighting(allErrors, userProfile.nativeLanguage)
        
        // Filter by correction tier
        val filteredErrors = filterByCorrectionTier(weightedErrors, userProfile.currentTier)
        
        // Sort by position in transcript
        filteredErrors.sortedBy { it.timestamp }
    }
    
    /**
     * Rule-based error detection for common patterns
     */
    private fun detectRuleBasedErrors(
        transcript: String,
        errorTypes: List<ErrorType>
    ): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        val sentences = splitIntoSentences(transcript)
        
        sentences.forEach { sentence ->
            // Check each error type
            errorTypes.forEach { errorType ->
                when (errorType.id) {
                    "article_usage" -> errors.addAll(detectArticleErrors(sentence))
                    "subject_verb_agreement" -> errors.addAll(detectSubjectVerbAgreement(sentence))
                    "plural_singular" -> errors.addAll(detectPluralErrors(sentence))
                    "preposition_semantic" -> errors.addAll(detectPrepositionErrors(sentence))
                    "word_order_basic" -> errors.addAll(detectWordOrderErrors(sentence))
                    "verb_missing" -> errors.addAll(detectMissingVerbs(sentence))
                    "tense_time_mismatch" -> errors.addAll(detectTenseErrors(sentence))
                    "modal_verb_usage" -> errors.addAll(detectModalVerbErrors(sentence))
                    "question_formation" -> errors.addAll(detectQuestionFormationErrors(sentence))
                }
            }
        }
        
        return errors
    }
    
    /**
     * AI-powered error detection for complex patterns
     */
    private suspend fun detectAIErrors(
        transcript: String,
        userProfile: VerbalJournalProfile
    ): List<ErrorInstance> {
        val prompt = buildErrorDetectionPrompt(transcript, userProfile)
        
        return try {
            val result = aiRepository.generateText(prompt, temperature = 0.3f)
            parseAIErrorResponse(result.getOrNull() ?: "", transcript)
        } catch (e: Exception) {
            // Fallback to empty list if AI fails
            emptyList()
        }
    }
    
    /**
     * Article error detection (a/an/the)
     */
    private fun detectArticleErrors(sentence: SentenceInfo): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        val words = sentence.text.split(" ")
        
        // Pattern: "I am [profession]" without article
        val professionPattern = Regex("\\b(I am|She is|He is|They are)\\s+(teacher|doctor|student|engineer|lawyer|nurse)\\b", RegexOption.IGNORE_CASE)
        professionPattern.findAll(sentence.text).forEach { match ->
            errors.add(
                ErrorInstance(
                    id = UUID.randomUUID().toString(),
                    errorType = ErrorTypes.getErrorTypeById("article_usage")!!,
                    severity = ErrorSeverity.HELPFUL,
                    originalText = match.value,
                    correctedText = match.value.replace(
                        Regex("(I am|She is|He is|They are)\\s+"),
                        "$1 a "
                    ),
                    explanation = "Professions need an article: 'a teacher', 'a doctor'",
                    timestamp = sentence.startPosition + match.range.first.toLong(),
                    confidence = 0.9f,
                    l1AdjustedWeight = 1.0f,
                    contextWindow = sentence.text
                )
            )
        }
        
        // Pattern: "the" before general concepts
        val theGeneralPattern = Regex("\\bthe\\s+(life|music|love|happiness|nature|society)\\b", RegexOption.IGNORE_CASE)
        theGeneralPattern.findAll(sentence.text).forEach { match ->
            errors.add(
                ErrorInstance(
                    id = UUID.randomUUID().toString(),
                    errorType = ErrorTypes.getErrorTypeById("article_usage")!!,
                    severity = ErrorSeverity.HELPFUL,
                    originalText = match.value,
                    correctedText = match.value.removePrefix("the "),
                    explanation = "General concepts don't need 'the': just 'life', 'music', etc.",
                    timestamp = sentence.startPosition + match.range.first.toLong(),
                    confidence = 0.85f,
                    l1AdjustedWeight = 1.0f,
                    contextWindow = sentence.text
                )
            )
        }
        
        return errors
    }
    
    /**
     * Subject-verb agreement detection
     */
    private fun detectSubjectVerbAgreement(sentence: SentenceInfo): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        
        // Pattern: Third person singular
        val thirdPersonPattern = Regex("\\b(he|she|it)\\s+(have|do|go|want|need|like)\\b", RegexOption.IGNORE_CASE)
        thirdPersonPattern.findAll(sentence.text).forEach { match ->
            val subject = match.groupValues[1]
            val verb = match.groupValues[2]
            val correctedVerb = when (verb.lowercase()) {
                "have" -> "has"
                "do" -> "does"
                else -> "${verb}s"
            }
            
            errors.add(
                ErrorInstance(
                    id = UUID.randomUUID().toString(),
                    errorType = ErrorTypes.getErrorTypeById("subject_verb_agreement")!!,
                    severity = ErrorSeverity.POLISH,
                    originalText = match.value,
                    correctedText = "$subject $correctedVerb",
                    explanation = "Third person singular (he/she/it) needs -s: '$correctedVerb'",
                    timestamp = sentence.startPosition + match.range.first.toLong(),
                    confidence = 0.95f,
                    l1AdjustedWeight = 1.0f,
                    contextWindow = sentence.text
                )
            )
        }
        
        return errors
    }
    
    /**
     * Plural/singular error detection
     */
    private fun detectPluralErrors(sentence: SentenceInfo): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        
        // Pattern: Number + singular noun
        val numberPattern = Regex("\\b(two|three|four|five|many|several|few|\\d+)\\s+(book|student|car|house|child|person)\\b", RegexOption.IGNORE_CASE)
        numberPattern.findAll(sentence.text).forEach { match ->
            val number = match.groupValues[1]
            val noun = match.groupValues[2]
            val pluralNoun = when (noun.lowercase()) {
                "child" -> "children"
                "person" -> "people"
                else -> "${noun}s"
            }
            
            errors.add(
                ErrorInstance(
                    id = UUID.randomUUID().toString(),
                    errorType = ErrorTypes.getErrorTypeById("plural_singular")!!,
                    severity = ErrorSeverity.HELPFUL,
                    originalText = match.value,
                    correctedText = "$number $pluralNoun",
                    explanation = "Use plural form after numbers: '$pluralNoun'",
                    timestamp = sentence.startPosition + match.range.first.toLong(),
                    confidence = 0.9f,
                    l1AdjustedWeight = 1.0f,
                    contextWindow = sentence.text
                )
            )
        }
        
        return errors
    }
    
    /**
     * Preposition error detection
     */
    private fun detectPrepositionErrors(sentence: SentenceInfo): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        
        // Common preposition collocations
        val prepositionPatterns = mapOf(
            "interested\\s+for" to "interested in",
            "depend\\s+of" to "depend on",
            "arrive\\s+to" to "arrive at",
            "married\\s+with" to "married to",
            "good\\s+in" to "good at",
            "afraid\\s+from" to "afraid of"
        )
        
        prepositionPatterns.forEach { (pattern, correction) ->
            val regex = Regex("\\b$pattern\\b", RegexOption.IGNORE_CASE)
            regex.findAll(sentence.text).forEach { match ->
                errors.add(
                    ErrorInstance(
                        id = UUID.randomUUID().toString(),
                        errorType = ErrorTypes.getErrorTypeById("preposition_semantic")!!,
                        severity = ErrorSeverity.IMPORTANT,
                        originalText = match.value,
                        correctedText = correction,
                        explanation = "The correct preposition is: '$correction'",
                        timestamp = sentence.startPosition + match.range.first.toLong(),
                        confidence = 0.95f,
                        l1AdjustedWeight = 1.0f,
                        contextWindow = sentence.text
                    )
                )
            }
        }
        
        return errors
    }
    
    /**
     * Word order error detection
     */
    private fun detectWordOrderErrors(sentence: SentenceInfo): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        
        // Pattern: Adjective after noun
        val adjAfterNounPattern = Regex("\\b(car|house|book|person|day)\\s+(beautiful|big|small|good|bad|new|old)\\b", RegexOption.IGNORE_CASE)
        adjAfterNounPattern.findAll(sentence.text).forEach { match ->
            val noun = match.groupValues[1]
            val adjective = match.groupValues[2]
            
            errors.add(
                ErrorInstance(
                    id = UUID.randomUUID().toString(),
                    errorType = ErrorTypes.getErrorTypeById("word_order_basic")!!,
                    severity = ErrorSeverity.CRITICAL,
                    originalText = match.value,
                    correctedText = "$adjective $noun",
                    explanation = "In English, adjectives come before nouns: '$adjective $noun'",
                    timestamp = sentence.startPosition + match.range.first.toLong(),
                    confidence = 0.9f,
                    l1AdjustedWeight = 1.0f,
                    contextWindow = sentence.text
                )
            )
        }
        
        return errors
    }
    
    /**
     * Missing verb detection
     */
    private fun detectMissingVerbs(sentence: SentenceInfo): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        
        // Pattern: Subject + adjective without "be" verb
        val missingBePattern = Regex("\\b(I|you|he|she|it|we|they)\\s+(happy|sad|tired|hungry|beautiful|smart)\\b", RegexOption.IGNORE_CASE)
        missingBePattern.findAll(sentence.text).forEach { match ->
            val subject = match.groupValues[1]
            val adjective = match.groupValues[2]
            val beVerb = when (subject.lowercase()) {
                "i" -> "am"
                "he", "she", "it" -> "is"
                else -> "are"
            }
            
            errors.add(
                ErrorInstance(
                    id = UUID.randomUUID().toString(),
                    errorType = ErrorTypes.getErrorTypeById("verb_missing")!!,
                    severity = ErrorSeverity.CRITICAL,
                    originalText = match.value,
                    correctedText = "$subject $beVerb $adjective",
                    explanation = "Need a verb 'to be': '$subject $beVerb $adjective'",
                    timestamp = sentence.startPosition + match.range.first.toLong(),
                    confidence = 0.95f,
                    l1AdjustedWeight = 1.0f,
                    contextWindow = sentence.text
                )
            )
        }
        
        return errors
    }
    
    /**
     * Tense error detection
     */
    private fun detectTenseErrors(sentence: SentenceInfo): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        
        // Pattern: Time marker + wrong tense
        val pastTimePattern = Regex("\\b(yesterday|last week|last year|ago)\\b.*\\b(go|come|eat|see|do)\\b", RegexOption.IGNORE_CASE)
        pastTimePattern.findAll(sentence.text).forEach { match ->
            val verb = match.groupValues[2]
            val pastForm = when (verb.lowercase()) {
                "go" -> "went"
                "come" -> "came"
                "eat" -> "ate"
                "see" -> "saw"
                "do" -> "did"
                else -> "${verb}ed"
            }
            
            errors.add(
                ErrorInstance(
                    id = UUID.randomUUID().toString(),
                    errorType = ErrorTypes.getErrorTypeById("tense_time_mismatch")!!,
                    severity = ErrorSeverity.CRITICAL,
                    originalText = verb,
                    correctedText = pastForm,
                    explanation = "Use past tense with past time markers: '$pastForm'",
                    timestamp = sentence.startPosition + match.range.first.toLong(),
                    confidence = 0.85f,
                    l1AdjustedWeight = 1.0f,
                    contextWindow = sentence.text
                )
            )
        }
        
        return errors
    }
    
    /**
     * Modal verb error detection
     */
    private fun detectModalVerbErrors(sentence: SentenceInfo): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        
        // Pattern: Modal + "to" + verb
        val modalToPattern = Regex("\\b(can|could|will|would|should|must|may|might)\\s+to\\s+(\\w+)\\b", RegexOption.IGNORE_CASE)
        modalToPattern.findAll(sentence.text).forEach { match ->
            val modal = match.groupValues[1]
            val verb = match.groupValues[2]
            
            errors.add(
                ErrorInstance(
                    id = UUID.randomUUID().toString(),
                    errorType = ErrorTypes.getErrorTypeById("modal_verb_usage")!!,
                    severity = ErrorSeverity.IMPORTANT,
                    originalText = match.value,
                    correctedText = "$modal $verb",
                    explanation = "Modal verbs don't use 'to': '$modal $verb'",
                    timestamp = sentence.startPosition + match.range.first.toLong(),
                    confidence = 0.95f,
                    l1AdjustedWeight = 1.0f,
                    contextWindow = sentence.text
                )
            )
        }
        
        return errors
    }
    
    /**
     * Question formation error detection
     */
    private fun detectQuestionFormationErrors(sentence: SentenceInfo): List<ErrorInstance> {
        val errors = mutableListOf<ErrorInstance>()
        
        // Pattern: Statement word order in question
        if (sentence.text.trim().endsWith("?")) {
            val statementPattern = Regex("^(You are|He is|She is|They are|You have|I can)\\s+", RegexOption.IGNORE_CASE)
            statementPattern.find(sentence.text)?.let { match ->
                val parts = match.value.trim().split(" ")
                if (parts.size == 2) {
                    val subject = parts[0]
                    val verb = parts[1]
                    val corrected = "$verb $subject"
                    
                    errors.add(
                        ErrorInstance(
                            id = UUID.randomUUID().toString(),
                            errorType = ErrorTypes.getErrorTypeById("question_formation")!!,
                            severity = ErrorSeverity.CRITICAL,
                            originalText = match.value.trim(),
                            correctedText = corrected,
                            explanation = "Questions need inverted word order: '$corrected ...?'",
                            timestamp = sentence.startPosition,
                            confidence = 0.85f,
                            l1AdjustedWeight = 1.0f,
                            contextWindow = sentence.text
                        )
                    )
                }
            }
        }
        
        return errors
    }
    
    /**
     * Apply L1 interference weighting to errors
     */
    private fun applyL1Weighting(
        errors: List<ErrorInstance>,
        nativeLanguage: String
    ): List<ErrorInstance> {
        val l1Pattern = L1InterferencePatterns.getPattern(nativeLanguage)
            ?: return errors
        
        return errors.map { error ->
            val weight = l1Pattern.weightingAdjustments[error.errorType.id]
                ?: 1.0f
            error.copy(l1AdjustedWeight = weight)
        }
    }
    
    /**
     * Filter errors by correction tier
     */
    private fun filterByCorrectionTier(
        errors: List<ErrorInstance>,
        tier: CorrectionTier
    ): List<ErrorInstance> {
        val allowedSeverities = when (tier) {
            CorrectionTier.CRITICAL -> setOf(ErrorSeverity.CRITICAL)
            CorrectionTier.IMPORTANT -> setOf(ErrorSeverity.CRITICAL, ErrorSeverity.IMPORTANT)
            CorrectionTier.HELPFUL -> setOf(ErrorSeverity.CRITICAL, ErrorSeverity.IMPORTANT, ErrorSeverity.HELPFUL)
            CorrectionTier.COMPREHENSIVE -> ErrorSeverity.values().toSet()
        }
        
        return errors.filter { it.severity in allowedSeverities }
    }
    
    /**
     * Build prompt for AI error detection
     */
    private fun buildErrorDetectionPrompt(
        transcript: String,
        userProfile: VerbalJournalProfile
    ): String {
        return """
            You are an expert ESL teacher analyzing a speech transcript.
            
            Student profile:
            - Native language: ${userProfile.nativeLanguage}
            - Proficiency level: ${userProfile.currentLevel}
            - Correction tier: ${userProfile.currentTier}
            
            Transcript to analyze:
            "$transcript"
            
            Identify grammar and usage errors appropriate for the student's correction tier:
            ${when (userProfile.currentTier) {
                CorrectionTier.CRITICAL -> "Only identify errors that completely break communication"
                CorrectionTier.IMPORTANT -> "Identify errors that break communication or cause confusion"
                CorrectionTier.HELPFUL -> "Identify errors that are noticeable to native speakers"
                CorrectionTier.COMPREHENSIVE -> "Identify all errors including style and idiom"
            }}
            
            For each error found, provide:
            1. The original text
            2. The corrected version
            3. Error type (grammar, vocabulary, etc.)
            4. Brief explanation
            5. Severity (critical/important/helpful/polish)
            
            Format as JSON array:
            [
                {
                    "original": "error text",
                    "corrected": "fixed text",
                    "type": "error_type",
                    "explanation": "why it's wrong",
                    "severity": "severity_level"
                }
            ]
            
            If no errors found, return empty array: []
        """.trimIndent()
    }
    
    /**
     * Parse AI error detection response
     */
    private fun parseAIErrorResponse(
        response: String,
        originalTranscript: String
    ): List<ErrorInstance> {
        // In production, would parse JSON response
        // For now, return empty list
        return emptyList()
    }
    
    /**
     * Split text into sentences with position tracking
     */
    private fun splitIntoSentences(text: String): List<SentenceInfo> {
        val sentences = mutableListOf<SentenceInfo>()
        var currentPosition = 0L
        
        // Simple sentence splitting - in production would be more sophisticated
        val sentenceEndings = Regex("[.!?]+\\s*")
        var lastEnd = 0
        
        sentenceEndings.findAll(text).forEach { match ->
            val sentence = text.substring(lastEnd, match.range.last + 1).trim()
            if (sentence.isNotEmpty()) {
                sentences.add(
                    SentenceInfo(
                        text = sentence,
                        startPosition = lastEnd.toLong()
                    )
                )
            }
            lastEnd = match.range.last + 1
        }
        
        // Add any remaining text
        if (lastEnd < text.length) {
            val remaining = text.substring(lastEnd).trim()
            if (remaining.isNotEmpty()) {
                sentences.add(
                    SentenceInfo(
                        text = remaining,
                        startPosition = lastEnd.toLong()
                    )
                )
            }
        }
        
        return sentences
    }
    
    data class SentenceInfo(
        val text: String,
        val startPosition: Long
    )
}