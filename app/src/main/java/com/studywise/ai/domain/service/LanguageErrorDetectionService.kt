package com.studywise.ai.domain.service

import com.studywise.ai.data.local.content.L1InterferencePatterns
import com.studywise.ai.domain.model.*
import com.studywise.ai.domain.repository.AIRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for detecting language errors using rule-based and AI approaches
 * Implements progressive correction tiers based on ESL pedagogy
 */
@Singleton
class LanguageErrorDetectionService @Inject constructor(
    private val aiRepository: AIRepository,
    private val studentProgressRepository: com.studywise.ai.domain.repository.ProgressRepository
) {
    
    companion object {
        // Confidence thresholds
        private const val HIGH_CONFIDENCE = 0.8f
        private const val MEDIUM_CONFIDENCE = 0.6f
        private const val LOW_CONFIDENCE = 0.4f
        
        // Error detection patterns
        private val ARTICLE_PATTERNS = mapOf(
            "missing_a_an" to Regex("""(?i)\b(am|is|are)\s+(teacher|student|doctor|engineer|artist)(?!\w)"""),
            "the_with_general" to Regex("""(?i)\bthe\s+(life|love|happiness|nature|music)(?!\s+of|\s+in|\s+that)"""),
            "missing_the" to Regex("""(?i)\b(go|went|going)\s+to\s+(hospital|school|church|prison)(?!\w)""")
        )
        
        private val VERB_PATTERNS = mapOf(
            "missing_s" to Regex("""(?i)\b(he|she|it)\s+(go|come|eat|sleep|work|study)(?!\w)"""),
            "wrong_tense" to Regex("""(?i)\b(yesterday|last\s+\w+)\s+\w+\s+(go|come|eat|is|are|have)(?!\w)"""),
            "missing_be" to Regex("""(?i)\b(I|you|he|she|it|they|we)\s+(happy|sad|tired|hungry|teacher|student)(?!\w)""")
        )
        
        private val PREPOSITION_PATTERNS = mapOf(
            "depend_of" to Regex("""(?i)\bdepend\s+of\b"""),
            "arrive_to" to Regex("""(?i)\barrive\s+to\b"""),
            "discuss_about" to Regex("""(?i)\bdiscuss\s+about\b""")
        )
    }
    
    /**
     * Analyze text for language errors
     */
    suspend fun analyzeText(
        text: String,
        studentId: String,
        studentL1: String? = null,
        gradeLevel: Int
    ): WritingSampleAnalysis = withContext(Dispatchers.IO) {
        
        coroutineScope {
            // Run detection methods in parallel
            val ruleBasedErrors = async { detectRuleBasedErrors(text, studentL1) }
            val aiDetectedErrors = async { detectAIErrors(text, studentL1, gradeLevel) }
            val coherenceAnalysis = async { analyzeCoherence(text) }
            
            // Combine results
            val allErrors = (ruleBasedErrors.await() + aiDetectedErrors.await())
                .distinctBy { it.error.startIndex to it.error.endIndex }
                .sortedBy { it.error.startIndex }
            
            // Calculate scores
            val writingScore = calculateWritingScore(text, allErrors, coherenceAnalysis.await())
            
            // Generate Socratic questions based on errors
            val socraticQuestions = generateSocraticQuestions(allErrors, gradeLevel)
            
            // Identify strengths and improvement areas
            val (strengths, improvementAreas) = analyzeStrengthsAndWeaknesses(allErrors)
            
            WritingSampleAnalysis(
                sampleId = UUID.randomUUID().toString(),
                studentId = studentId,
                text = text,
                errors = allErrors,
                overallScore = writingScore,
                strengthAreas = strengths,
                improvementAreas = improvementAreas,
                socraticQuestions = socraticQuestions
            )
        }
    }
    
    /**
     * Detect errors using rule-based patterns
     */
    private suspend fun detectRuleBasedErrors(
        text: String,
        studentL1: String?
    ): List<ErrorWithCorrections> = withContext(Dispatchers.Default) {
        
        val errors = mutableListOf<ErrorWithCorrections>()
        
        // Check article errors
        ARTICLE_PATTERNS.forEach { (errorType, pattern) ->
            pattern.findAll(text).forEach { match ->
                val error = when (errorType) {
                    "missing_a_an" -> createArticleError(match, text, "Missing article before profession")
                    "the_with_general" -> createArticleError(match, text, "Unnecessary 'the' with general concept")
                    "missing_the" -> createArticleError(match, text, "Missing 'the' with institution")
                    else -> null
                }
                error?.let { errors.add(it) }
            }
        }
        
        // Check verb errors
        VERB_PATTERNS.forEach { (errorType, pattern) ->
            pattern.findAll(text).forEach { match ->
                val error = when (errorType) {
                    "missing_s" -> createVerbError(match, text, "Missing -s in third person singular")
                    "wrong_tense" -> createVerbError(match, text, "Wrong tense with time marker")
                    "missing_be" -> createVerbError(match, text, "Missing 'be' verb")
                    else -> null
                }
                error?.let { errors.add(it) }
            }
        }
        
        // Check preposition errors
        PREPOSITION_PATTERNS.forEach { (errorType, pattern) ->
            pattern.findAll(text).forEach { match ->
                val error = createPrepositionError(match, text, errorType)
                errors.add(error)
            }
        }
        
        // Apply L1-specific patterns if available
        studentL1?.let {
            errors.addAll(checkL1SpecificPatterns(text, it))
        }
        
        errors
    }
    
    /**
     * Detect errors using AI
     */
    private suspend fun detectAIErrors(
        text: String,
        studentL1: String?,
        gradeLevel: Int
    ): List<ErrorWithCorrections> = withContext(Dispatchers.IO) {
        
        val prompt = buildErrorDetectionPrompt(text, studentL1, gradeLevel)
        
        try {
            val response = aiRepository.generateSocraticResponse(
                context = prompt,
                studentResponse = text,
                grade = gradeLevel
            )
            
            response.getOrNull()?.let { parseAIErrorResponse(it, text) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Create article error with corrections
     */
    private fun createArticleError(
        match: MatchResult,
        fullText: String,
        description: String
    ): ErrorWithCorrections {
        val errorText = match.value
        val corrections = when {
            description.contains("Missing article before profession") -> {
                val profession = errorText.substringAfterLast(" ")
                val article = if (profession.first().toLowerCase() in "aeiou") "an" else "a"
                listOf(
                    CorrectionOption(
                        suggestion = errorText.replace(profession, "$article $profession"),
                        explanation = "Use '$article' before singular countable nouns like professions",
                        tier = CorrectionTier.DIRECT,
                        examples = listOf("She is a teacher", "He is an engineer"),
                        confidence = 0.9f
                    )
                )
            }
            description.contains("Unnecessary 'the'") -> {
                listOf(
                    CorrectionOption(
                        suggestion = errorText.replace("the ", ""),
                        explanation = "Don't use 'the' with general concepts",
                        tier = CorrectionTier.GUIDED,
                        examples = listOf("Love is beautiful", "Music brings joy"),
                        confidence = 0.85f
                    )
                )
            }
            else -> emptyList()
        }
        
        return ErrorWithCorrections(
            error = LanguageError(
                id = UUID.randomUUID().toString(),
                text = errorText,
                startIndex = match.range.first,
                endIndex = match.range.last + 1,
                category = ErrorCategory.ARTICLE_USAGE,
                level = ErrorLevel.SIGNIFICANT,
                description = description,
                confidence = 0.8f
            ),
            corrections = corrections,
            recommendedTier = CorrectionTier.GUIDED,
            pedagogicalNote = "Common article error pattern"
        )
    }
    
    /**
     * Create verb error with corrections
     */
    private fun createVerbError(
        match: MatchResult,
        fullText: String,
        description: String
    ): ErrorWithCorrections {
        val errorText = match.value
        val corrections = when {
            description.contains("Missing -s") -> {
                val verb = errorText.substringAfterLast(" ")
                listOf(
                    CorrectionOption(
                        suggestion = errorText.replace(verb, verb + "s"),
                        explanation = "Add -s to verbs with he/she/it",
                        tier = CorrectionTier.SCAFFOLDED,
                        examples = listOf("He works", "She studies", "It rains"),
                        confidence = 0.9f
                    )
                )
            }
            description.contains("Wrong tense") -> {
                // Context-specific tense correction
                val timeMarker = if (errorText.contains("yesterday")) "past" else "past"
                listOf(
                    CorrectionOption(
                        suggestion = "Consider using past tense here",
                        explanation = "Match verb tense with time expressions like 'yesterday'",
                        tier = CorrectionTier.METALINGUISTIC,
                        ruleReference = "Past time markers require past tense verbs",
                        confidence = 0.85f
                    )
                )
            }
            description.contains("Missing 'be'") -> {
                val subject = errorText.substringBefore(" ")
                val beForm = when (subject.toLowerCase()) {
                    "i" -> "am"
                    "he", "she", "it" -> "is"
                    else -> "are"
                }
                listOf(
                    CorrectionOption(
                        suggestion = errorText.replace(" ", " $beForm ", 1),
                        explanation = "Add the correct form of 'be' between subject and adjective",
                        tier = CorrectionTier.DIRECT,
                        examples = listOf("I am happy", "She is tired", "They are students"),
                        confidence = 0.95f
                    )
                )
            }
            else -> emptyList()
        }
        
        return ErrorWithCorrections(
            error = LanguageError(
                id = UUID.randomUUID().toString(),
                text = errorText,
                startIndex = match.range.first,
                endIndex = match.range.last + 1,
                category = ErrorCategory.VERB_TENSE,
                level = ErrorLevel.CRITICAL,
                description = description,
                confidence = 0.85f
            ),
            corrections = corrections,
            recommendedTier = CorrectionTier.SCAFFOLDED,
            pedagogicalNote = "Focus on subject-verb agreement and tense consistency"
        )
    }
    
    /**
     * Create preposition error with corrections
     */
    private fun createPrepositionError(
        match: MatchResult,
        fullText: String,
        errorType: String
    ): ErrorWithCorrections {
        val errorText = match.value
        val (correctPrep, explanation) = when (errorType) {
            "depend_of" -> "on" to "Use 'depend on', not 'depend of'"
            "arrive_to" -> "at" to "Use 'arrive at' for specific places"
            "discuss_about" -> "" to "Use 'discuss' without 'about'"
            else -> "" to ""
        }
        
        return ErrorWithCorrections(
            error = LanguageError(
                id = UUID.randomUUID().toString(),
                text = errorText,
                startIndex = match.range.first,
                endIndex = match.range.last + 1,
                category = ErrorCategory.PREPOSITION,
                level = ErrorLevel.MINOR,
                description = "Incorrect preposition usage",
                confidence = 0.9f
            ),
            corrections = listOf(
                CorrectionOption(
                    suggestion = if (correctPrep.isEmpty()) {
                        errorText.replace(" about", "")
                    } else {
                        errorText.replace(errorText.substringAfterLast(" "), correctPrep)
                    },
                    explanation = explanation,
                    tier = CorrectionTier.DIRECT,
                    confidence = 0.9f
                )
            ),
            recommendedTier = CorrectionTier.DIRECT,
            pedagogicalNote = "Common prepositional collocation error"
        )
    }
    
    /**
     * Check L1-specific interference patterns
     */
    private fun checkL1SpecificPatterns(
        text: String,
        studentL1: String
    ): List<ErrorWithCorrections> {
        val errors = mutableListOf<ErrorWithCorrections>()
        val l1Patterns = L1InterferencePatterns.getPatternsForL1(studentL1)
        
        l1Patterns.forEach { pattern ->
            // Apply pattern-specific detection logic
            when (pattern.category) {
                ErrorCategory.WORD_ORDER -> {
                    // Check for SOV patterns if applicable
                    if (pattern.id == "ko_word_order_sov") {
                        checkSOVPattern(text)?.let { errors.add(it) }
                    }
                }
                ErrorCategory.L1_PHONOLOGICAL_TRANSFER -> {
                    // Check for phonological transfers in spelling
                    pattern.commonErrors.forEach { (error, _) ->
                        if (text.contains(error, ignoreCase = true)) {
                            errors.add(createL1InterferenceError(text, error, pattern))
                        }
                    }
                }
                else -> {
                    // Generic L1 pattern checking
                }
            }
        }
        
        return errors
    }
    
    /**
     * Create L1 interference error
     */
    private fun createL1InterferenceError(
        text: String,
        errorPattern: String,
        l1Pattern: L1InterferencePattern
    ): ErrorWithCorrections {
        val errorIndex = text.indexOf(errorPattern, ignoreCase = true)
        val correction = l1Pattern.commonErrors
            .find { it.first == errorPattern }
            ?.second ?: errorPattern
        
        return ErrorWithCorrections(
            error = LanguageError(
                id = UUID.randomUUID().toString(),
                text = errorPattern,
                startIndex = errorIndex,
                endIndex = errorIndex + errorPattern.length,
                category = l1Pattern.category,
                level = ErrorLevel.SIGNIFICANT,
                description = l1Pattern.description,
                l1Language = l1Pattern.l1Language,
                confidence = 0.75f
            ),
            corrections = listOf(
                CorrectionOption(
                    suggestion = correction,
                    explanation = l1Pattern.linguisticExplanation,
                    tier = CorrectionTier.METALINGUISTIC,
                    examples = l1Pattern.commonErrors.map { it.second },
                    confidence = 0.8f
                )
            ),
            recommendedTier = CorrectionTier.METALINGUISTIC,
            pedagogicalNote = "L1 interference: ${l1Pattern.correctionStrategy}"
        )
    }
    
    /**
     * Build AI prompt for error detection
     */
    private fun buildErrorDetectionPrompt(
        text: String,
        studentL1: String?,
        gradeLevel: Int
    ): String {
        return """
        You are an expert ESL teacher analyzing student writing for language errors.
        ${studentL1?.let { "The student's first language is $it." } ?: ""}
        Grade level: $gradeLevel
        
        Analyze this text for language errors:
        "$text"
        
        Identify errors in these categories:
        1. Grammar (verb tense, agreement, word order)
        2. Articles and determiners
        3. Prepositions and collocations
        4. Word choice and register
        5. Coherence and cohesion
        
        For each error, provide:
        - The specific error text
        - Error category and severity
        - A clear correction
        - An explanation suitable for grade $gradeLevel
        - Teaching strategy (awareness, guided, scaffolded, direct, or metalinguistic)
        
        Format as JSON:
        {
            "errors": [
                {
                    "text": "error text",
                    "category": "category",
                    "severity": "critical/significant/minor",
                    "correction": "corrected text",
                    "explanation": "why this is wrong",
                    "strategy": "correction approach"
                }
            ]
        }
        
        Focus on errors that impede comprehension or represent systematic patterns.
        Be encouraging and constructive in explanations.
        """.trimIndent()
    }
    
    /**
     * Analyze text coherence
     */
    private suspend fun analyzeCoherence(text: String): CoherenceAnalysis {
        // Simple coherence analysis based on text features
        val sentences = text.split(Regex("[.!?]+")).filter { it.isNotBlank() }
        val hasTransitions = text.contains(Regex(
            "\\b(however|therefore|moreover|furthermore|additionally|consequently|thus)\\b",
            RegexOption.IGNORE_CASE
        ))
        val avgSentenceLength = sentences.map { it.split(" ").size }.average()
        
        return CoherenceAnalysis(
            sentenceCount = sentences.size,
            hasTransitions = hasTransitions,
            avgSentenceLength = avgSentenceLength,
            coherenceScore = when {
                sentences.size < 3 -> 0.5f
                hasTransitions && avgSentenceLength in 10.0..20.0 -> 0.9f
                hasTransitions || avgSentenceLength in 8.0..25.0 -> 0.7f
                else -> 0.6f
            }
        )
    }
    
    /**
     * Calculate writing score
     */
    private fun calculateWritingScore(
        text: String,
        errors: List<ErrorWithCorrections>,
        coherence: CoherenceAnalysis
    ): WritingScore {
        val wordCount = text.split("\\s+".toRegex()).size
        val errorDensity = errors.size.toFloat() / wordCount
        
        // Grammar score based on error density and severity
        val grammarScore = when {
            errorDensity < 0.05f -> 0.9f
            errorDensity < 0.1f -> 0.7f
            errorDensity < 0.15f -> 0.5f
            else -> 0.3f
        } * (1f - errors.count { it.error.level == ErrorLevel.CRITICAL } * 0.1f)
        
        // Vocabulary score (simplified)
        val uniqueWords = text.toLowerCase().split("\\s+".toRegex()).toSet().size
        val lexicalDiversity = uniqueWords.toFloat() / wordCount
        val vocabularyScore = minOf(lexicalDiversity * 2, 0.9f)
        
        // Task achievement (simplified - checking for minimum length)
        val taskScore = when {
            wordCount < 50 -> 0.3f
            wordCount < 100 -> 0.6f
            wordCount < 200 -> 0.8f
            else -> 0.9f
        }
        
        return WritingScore(
            grammar = grammarScore.coerceIn(0.1f, 1f),
            vocabulary = vocabularyScore,
            coherence = coherence.coherenceScore,
            taskAchievement = taskScore,
            overall = (grammarScore * 0.3f + vocabularyScore * 0.2f + 
                      coherence.coherenceScore * 0.3f + taskScore * 0.2f).coerceIn(0.1f, 1f)
        )
    }
    
    /**
     * Generate Socratic questions based on errors
     */
    private fun generateSocraticQuestions(
        errors: List<ErrorWithCorrections>,
        gradeLevel: Int
    ): List<SocraticPrompt> {
        val questions = mutableListOf<SocraticPrompt>()
        
        // Group errors by category
        val errorsByCategory = errors.groupBy { it.error.category }
        
        errorsByCategory.forEach { (category, categoryErrors) ->
            val question = when (category) {
                ErrorCategory.VERB_TENSE -> when (gradeLevel) {
                    in 2..5 -> "Look at your action words. Do they match when things happened?"
                    in 6..8 -> "How can you tell if your verbs match the time in your story?"
                    else -> "Examine your verb tenses. How do they create temporal coherence?"
                }
                ErrorCategory.ARTICLE_USAGE -> when (gradeLevel) {
                    in 2..5 -> "When do we need 'a' or 'the' before a word?"
                    in 6..8 -> "What's the difference between 'a teacher' and 'the teacher'?"
                    else -> "How do articles specify or generalize noun references?"
                }
                ErrorCategory.SUBJECT_VERB_AGREEMENT -> when (gradeLevel) {
                    in 2..5 -> "Does your action word match who's doing it?"
                    in 6..8 -> "How do subjects and verbs need to agree in number?"
                    else -> "What principles govern subject-verb agreement in complex sentences?"
                }
                else -> "What pattern do you notice in this type of error?"
            }
            
            questions.add(
                SocraticPrompt(
                    question = question,
                    errorCategory = category,
                    scaffoldingLevel = when (gradeLevel) {
                        in 2..5 -> 1
                        in 6..8 -> 2
                        else -> 3
                    },
                    expectedInsight = "Recognition of systematic error pattern"
                )
            )
        }
        
        // Add reflection question
        questions.add(
            SocraticPrompt(
                question = when (gradeLevel) {
                    in 2..5 -> "What's one thing you can check next time you write?"
                    in 6..8 -> "What strategy could help you avoid these mistakes?"
                    else -> "How might awareness of these patterns improve your writing?"
                },
                errorCategory = ErrorCategory.VERB_TENSE, // Generic
                scaffoldingLevel = 1,
                expectedInsight = "Metacognitive awareness of writing process"
            )
        )
        
        return questions.take(3) // Limit to 3 questions
    }
    
    /**
     * Analyze strengths and weaknesses
     */
    private fun analyzeStrengthsAndWeaknesses(
        errors: List<ErrorWithCorrections>
    ): Pair<List<String>, List<ErrorCategory>> {
        val errorCategories = errors.map { it.error.category }
        val errorCounts = errorCategories.groupingBy { it }.eachCount()
        
        // Identify strengths (categories with few or no errors)
        val allCategories = ErrorCategory.values().toList()
        val strengths = allCategories
            .filter { errorCounts.getOrDefault(it, 0) == 0 }
            .take(3)
            .map { getCategoryStrengthDescription(it) }
        
        // Identify weaknesses (most frequent error categories)
        val weaknesses = errorCounts
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
        
        return strengths to weaknesses
    }
    
    /**
     * Get strength description for error-free categories
     */
    private fun getCategoryStrengthDescription(category: ErrorCategory): String {
        return when (category) {
            ErrorCategory.VERB_TENSE -> "Good control of verb tenses"
            ErrorCategory.SUBJECT_VERB_AGREEMENT -> "Consistent subject-verb agreement"
            ErrorCategory.ARTICLE_USAGE -> "Appropriate article usage"
            ErrorCategory.PREPOSITION -> "Accurate prepositional phrases"
            ErrorCategory.WORD_ORDER -> "Clear sentence structure"
            ErrorCategory.COHERENCE -> "Well-organized ideas"
            else -> "Strong ${category.name.toLowerCase().replace('_', ' ')}"
        }
    }
    
    /**
     * Parse AI error detection response
     */
    private fun parseAIErrorResponse(response: String, originalText: String): List<ErrorWithCorrections> {
        // Implementation would parse JSON response from AI
        // For now, return empty list
        return emptyList()
    }
    
    /**
     * Check for SOV word order patterns
     */
    private fun checkSOVPattern(text: String): ErrorWithCorrections? {
        // Simplified SOV detection
        val objectVerbPattern = Regex("""(?i)\b(the|a|an)\s+(\w+)\s+(ate|bought|saw|made|took)\b""")
        val match = objectVerbPattern.find(text)
        
        return match?.let {
            ErrorWithCorrections(
                error = LanguageError(
                    id = UUID.randomUUID().toString(),
                    text = it.value,
                    startIndex = it.range.first,
                    endIndex = it.range.last + 1,
                    category = ErrorCategory.WORD_ORDER,
                    level = ErrorLevel.SIGNIFICANT,
                    description = "Possible non-English word order",
                    confidence = 0.6f
                ),
                corrections = listOf(
                    CorrectionOption(
                        suggestion = "Consider Subject-Verb-Object order",
                        explanation = "English typically follows SVO word order",
                        tier = CorrectionTier.AWARENESS,
                        confidence = 0.6f
                    )
                ),
                recommendedTier = CorrectionTier.AWARENESS,
                pedagogicalNote = "Word order interference from L1"
            )
        }
    }
}

// Supporting data class
data class CoherenceAnalysis(
    val sentenceCount: Int,
    val hasTransitions: Boolean,
    val avgSentenceLength: Double,
    val coherenceScore: Float
)