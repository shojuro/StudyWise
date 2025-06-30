package com.studywise.ai.data.local.content

import com.studywise.ai.domain.model.ErrorCategory
import com.studywise.ai.domain.model.L1InterferencePattern

/**
 * L1 Interference patterns for common language backgrounds
 * Based on linguistic research and ESL pedagogy
 */
object L1InterferencePatterns {
    
    val patterns = listOf(
        // ===== SPANISH L1 PATTERNS =====
        L1InterferencePattern(
            id = "es_article_omission",
            l1Language = "Spanish",
            category = ErrorCategory.ARTICLE_USAGE,
            pattern = "Omission of articles with professions and abstract nouns",
            description = "Spanish speakers often omit articles where English requires them",
            commonErrors = listOf(
                "I am teacher" → "I am a teacher",
                "Life is beautiful" → "Life is beautiful" (correct),
                "He is in hospital" → "He is in the hospital"
            ),
            correctionStrategy = "Highlight contexts where English requires articles differently than Spanish",
            linguisticExplanation = "Spanish uses articles differently with professions and institutions"
        ),
        
        L1InterferencePattern(
            id = "es_subject_pronoun",
            l1Language = "Spanish",
            category = ErrorCategory.PRONOUN_REFERENCE,
            pattern = "Subject pronoun omission",
            description = "Spanish allows null subjects, leading to pronoun dropping",
            commonErrors = listOf(
                "Is raining" → "It is raining",
                "Think is important" → "I think it is important",
                "Was very difficult" → "It was very difficult"
            ),
            correctionStrategy = "Emphasize that English always requires explicit subjects",
            linguisticExplanation = "Spanish is a pro-drop language; English is not"
        ),
        
        L1InterferencePattern(
            id = "es_adjective_placement",
            l1Language = "Spanish",
            category = ErrorCategory.WORD_ORDER,
            pattern = "Post-nominal adjective placement",
            description = "Placing adjectives after nouns following Spanish syntax",
            commonErrors = listOf(
                "The car red" → "The red car",
                "A day special" → "A special day",
                "Something important" → "Something important" (correct in this case)
            ),
            correctionStrategy = "Practice adjective placement rules and exceptions",
            linguisticExplanation = "Spanish typically places adjectives after nouns"
        ),
        
        // ===== CHINESE (MANDARIN) L1 PATTERNS =====
        L1InterferencePattern(
            id = "zh_tense_marking",
            l1Language = "Chinese",
            category = ErrorCategory.VERB_TENSE,
            pattern = "Absence of tense marking",
            description = "Chinese doesn't inflect verbs for tense, leading to unmarked verbs",
            commonErrors = listOf(
                "Yesterday I go to store" → "Yesterday I went to the store",
                "He already eat" → "He has already eaten",
                "Tomorrow she come" → "Tomorrow she will come"
            ),
            correctionStrategy = "Focus on time markers and their corresponding tenses",
            linguisticExplanation = "Chinese uses aspect markers and context rather than verb inflection"
        ),
        
        L1InterferencePattern(
            id = "zh_article_system",
            l1Language = "Chinese",
            category = ErrorCategory.ARTICLE_USAGE,
            pattern = "Article omission or overuse",
            description = "Chinese lacks articles, causing systematic article errors",
            commonErrors = listOf(
                "I have book" → "I have a book",
                "The love is important" → "Love is important",
                "Go to the home" → "Go home"
            ),
            correctionStrategy = "Teach article rules systematically with count/non-count distinctions",
            linguisticExplanation = "Chinese uses classifiers and demonstratives instead of articles"
        ),
        
        L1InterferencePattern(
            id = "zh_plural_marking",
            l1Language = "Chinese",
            category = ErrorCategory.L1_SYNTAX_TRANSFER,
            pattern = "Plural marking omission",
            description = "Chinese doesn't mark plural on nouns systematically",
            commonErrors = listOf(
                "Three book" → "Three books",
                "Many student" → "Many students",
                "All the teacher" → "All the teachers"
            ),
            correctionStrategy = "Practice number agreement and plural formation rules",
            linguisticExplanation = "Chinese nouns don't inflect for number"
        ),
        
        // ===== ARABIC L1 PATTERNS =====
        L1InterferencePattern(
            id = "ar_verb_be_omission",
            l1Language = "Arabic",
            category = ErrorCategory.VERB_TENSE,
            pattern = "Copula 'be' omission in present tense",
            description = "Arabic doesn't use copula in present tense nominal sentences",
            commonErrors = listOf(
                "She teacher" → "She is a teacher",
                "They happy" → "They are happy",
                "The weather cold" → "The weather is cold"
            ),
            correctionStrategy = "Emphasize 'be' verb usage in all tenses",
            linguisticExplanation = "Arabic nominal sentences don't require a copula in present tense"
        ),
        
        L1InterferencePattern(
            id = "ar_p_b_confusion",
            l1Language = "Arabic",
            category = ErrorCategory.L1_PHONOLOGICAL_TRANSFER,
            pattern = "/p/ and /b/ confusion",
            description = "Many Arabic dialects lack /p/, leading to p/b substitution",
            commonErrors = listOf(
                "beoble" → "people",
                "haby" → "happy",
                "bark" → "park"
            ),
            correctionStrategy = "Explicit phonological training for /p/ vs /b/ distinction",
            linguisticExplanation = "Standard Arabic lacks the phoneme /p/"
        ),
        
        // ===== JAPANESE L1 PATTERNS =====
        L1InterferencePattern(
            id = "ja_l_r_confusion",
            l1Language = "Japanese",
            category = ErrorCategory.L1_PHONOLOGICAL_TRANSFER,
            pattern = "/l/ and /r/ confusion",
            description = "Japanese has one liquid phoneme covering both English /l/ and /r/",
            commonErrors = listOf(
                "light" ↔ "right",
                "collect" ↔ "correct",
                "glass" ↔ "grass"
            ),
            correctionStrategy = "Targeted pronunciation practice with minimal pairs",
            linguisticExplanation = "Japanese has a single liquid phoneme /ɾ/"
        ),
        
        L1InterferencePattern(
            id = "ja_article_overuse",
            l1Language = "Japanese",
            category = ErrorCategory.ARTICLE_USAGE,
            pattern = "Overuse of definite article 'the'",
            description = "Japanese speakers often overuse 'the' as a default article",
            commonErrors = listOf(
                "I like the coffee" → "I like coffee" (in general),
                "The Japanese people" → "Japanese people",
                "In the Japan" → "In Japan"
            ),
            correctionStrategy = "Teach generic vs specific reference distinctions",
            linguisticExplanation = "Japanese uses topic markers differently than English articles"
        ),
        
        // ===== KOREAN L1 PATTERNS =====
        L1InterferencePattern(
            id = "ko_word_order_sov",
            l1Language = "Korean",
            category = ErrorCategory.WORD_ORDER,
            pattern = "SOV word order transfer",
            description = "Korean SOV order influences English sentence structure",
            commonErrors = listOf(
                "I pizza ate" → "I ate pizza",
                "She to school went" → "She went to school",
                "He Korean speaks" → "He speaks Korean"
            ),
            correctionStrategy = "Practice SVO word order with various sentence types",
            linguisticExplanation = "Korean follows Subject-Object-Verb order"
        ),
        
        // ===== FRENCH L1 PATTERNS =====
        L1InterferencePattern(
            id = "fr_false_friends",
            l1Language = "French",
            category = ErrorCategory.FALSE_COGNATE,
            pattern = "False cognate usage",
            description = "French-English false friends cause semantic errors",
            commonErrors = listOf(
                "actually" (vraiment) used for "currently" (actuellement),
                "eventually" (finalement) used for "possibly" (éventuellement),
                "sensible" (raisonnable) used for "sensitive" (sensible)"
            ),
            correctionStrategy = "Create awareness of common false cognates",
            linguisticExplanation = "Similar forms have diverged in meaning between languages"
        ),
        
        // ===== RUSSIAN L1 PATTERNS =====
        L1InterferencePattern(
            id = "ru_article_absence",
            l1Language = "Russian",
            category = ErrorCategory.ARTICLE_USAGE,
            pattern = "Systematic article omission",
            description = "Russian lacks articles entirely",
            commonErrors = listOf(
                "I am student" → "I am a student",
                "Book is on table" → "The book is on the table",
                "She is doctor" → "She is a doctor"
            ),
            correctionStrategy = "Systematic introduction of article concepts",
            linguisticExplanation = "Russian has no article system"
        ),
        
        // ===== HINDI L1 PATTERNS =====
        L1InterferencePattern(
            id = "hi_progressive_overuse",
            l1Language = "Hindi",
            category = ErrorCategory.VERB_TENSE,
            pattern = "Overuse of progressive aspect",
            description = "Hindi speakers often overuse continuous tenses",
            commonErrors = listOf(
                "I am knowing" → "I know",
                "She is having a car" → "She has a car",
                "They are believing" → "They believe"
            ),
            correctionStrategy = "Teach stative vs dynamic verb distinctions",
            linguisticExplanation = "Hindi uses progressive aspect more broadly than English"
        ),
        
        // ===== PORTUGUESE L1 PATTERNS =====
        L1InterferencePattern(
            id = "pt_gerund_infinitive",
            l1Language = "Portuguese",
            category = ErrorCategory.VERB_TENSE,
            pattern = "Gerund vs infinitive confusion",
            description = "Portuguese gerund usage differs from English",
            commonErrors = listOf(
                "I stopped to smoke" ↔ "I stopped smoking",
                "I like to swimming" → "I like swimming",
                "Before to go" → "Before going"
            ),
            correctionStrategy = "Practice gerund vs infinitive patterns systematically",
            linguisticExplanation = "Portuguese uses infinitives where English uses gerunds"
        )
    )
    
    /**
     * Get patterns for a specific L1 language
     */
    fun getPatternsForL1(language: String): List<L1InterferencePattern> {
        return patterns.filter { it.l1Language.equals(language, ignoreCase = true) }
    }
    
    /**
     * Get patterns by error category
     */
    fun getPatternsByCategory(category: ErrorCategory): List<L1InterferencePattern> {
        return patterns.filter { it.category == category }
    }
    
    /**
     * Get all unique L1 languages
     */
    fun getSupportedL1Languages(): List<String> {
        return patterns.map { it.l1Language }.distinct().sorted()
    }
}