package com.studywise.ai.domain.model.verbaljournal

/**
 * Comprehensive error type definitions for ESL speech analysis
 * Organized by correction tier for progressive feedback
 */
object ErrorTypes {
    
    /**
     * CRITICAL TIER - Communication-breaking errors
     * These errors prevent the listener from understanding the intended message
     */
    val criticalErrors = listOf(
        ErrorType(
            id = "word_order_basic",
            name = "Basic Word Order",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.CRITICAL,
            correctionTier = CorrectionTier.CRITICAL,
            description = "Incorrect sentence structure that prevents comprehension",
            examples = listOf(
                "*Beautiful very is she → She is very beautiful",
                "*Yesterday I to school went → Yesterday I went to school",
                "*Want I coffee → I want coffee",
                "*Book this interesting is → This book is interesting"
            )
        ),
        ErrorType(
            id = "verb_missing",
            name = "Missing Main Verb",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.CRITICAL,
            correctionTier = CorrectionTier.CRITICAL,
            description = "Sentences lacking a main verb",
            examples = listOf(
                "*I to the store → I went to the store",
                "*She very happy → She is very happy",
                "*They at home → They are at home",
                "*The food delicious → The food is delicious"
            )
        ),
        ErrorType(
            id = "tense_time_mismatch",
            name = "Tense-Time Mismatch",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.CRITICAL,
            correctionTier = CorrectionTier.CRITICAL,
            description = "Using wrong tense that changes temporal meaning",
            examples = listOf(
                "*Yesterday I go → Yesterday I went",
                "*Tomorrow I went → Tomorrow I will go",
                "*Right now I ate → Right now I am eating",
                "*Last year I will visit → Last year I visited"
            )
        ),
        ErrorType(
            id = "negation_placement",
            name = "Negation Placement",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.CRITICAL,
            correctionTier = CorrectionTier.CRITICAL,
            description = "Incorrect placement of negation changing meaning",
            examples = listOf(
                "*I no like → I don't like",
                "*He not can do → He cannot do",
                "*She doesn't can → She can't",
                "*No is good → It's not good"
            )
        ),
        ErrorType(
            id = "question_formation",
            name = "Question Formation",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.CRITICAL,
            correctionTier = CorrectionTier.CRITICAL,
            description = "Malformed questions that confuse the listener",
            examples = listOf(
                "*You are student? → Are you a student?",
                "*Where you go? → Where do you go?",
                "*What you want? → What do you want?",
                "*When he come? → When does he come?"
            )
        )
    )
    
    /**
     * IMPORTANT TIER - Confusion-causing errors
     * These errors may cause misunderstanding or require clarification
     */
    val importantErrors = listOf(
        ErrorType(
            id = "modal_verb_usage",
            name = "Modal Verb Errors",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.IMPORTANT,
            correctionTier = CorrectionTier.IMPORTANT,
            description = "Incorrect use of modal verbs affecting meaning",
            examples = listOf(
                "*I must to go → I must go",
                "*She can sings → She can sing",
                "*They should to study → They should study",
                "*He will can come → He will be able to come"
            )
        ),
        ErrorType(
            id = "preposition_semantic",
            name = "Semantic Preposition Errors",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.IMPORTANT,
            correctionTier = CorrectionTier.IMPORTANT,
            description = "Preposition errors that change meaning",
            examples = listOf(
                "*interested for → interested in",
                "*depend of → depend on",
                "*arrive to → arrive at",
                "*married with → married to"
            )
        ),
        ErrorType(
            id = "conditional_structure",
            name = "Conditional Errors",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.IMPORTANT,
            correctionTier = CorrectionTier.IMPORTANT,
            description = "Incorrect conditional structures",
            examples = listOf(
                "*If I will have time → If I have time",
                "*If I would know → If I knew",
                "*If he would have come → If he had come",
                "*I will go if will rain → I will go if it rains"
            )
        ),
        ErrorType(
            id = "reference_ambiguity",
            name = "Pronoun Reference Ambiguity",
            category = ErrorCategory.COHERENCE,
            severity = ErrorSeverity.IMPORTANT,
            correctionTier = CorrectionTier.IMPORTANT,
            description = "Unclear pronoun references",
            examples = listOf(
                "John told Mike that he was wrong (who?)",
                "The book is on the table. It is red. (book or table?)",
                "Sarah and Anna went, but she stayed (who?)"
            )
        ),
        ErrorType(
            id = "verb_pattern_error",
            name = "Verb Pattern Errors",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.IMPORTANT,
            correctionTier = CorrectionTier.IMPORTANT,
            description = "Wrong verb patterns affecting meaning",
            examples = listOf(
                "*suggest him to go → suggest that he go",
                "*want that he comes → want him to come",
                "*enjoy to swim → enjoy swimming",
                "*avoid to meet → avoid meeting"
            )
        )
    )
    
    /**
     * HELPFUL TIER - Noticeable but clear errors
     * These errors are obvious to native speakers but don't impede understanding
     */
    val helpfulErrors = listOf(
        ErrorType(
            id = "article_usage",
            name = "Article Errors",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.HELPFUL,
            correctionTier = CorrectionTier.HELPFUL,
            description = "Missing or incorrect articles",
            examples = listOf(
                "*I am student → I am a student",
                "*The life is beautiful → Life is beautiful",
                "*I love the music → I love music",
                "*She is doctor → She is a doctor"
            )
        ),
        ErrorType(
            id = "plural_singular",
            name = "Number Agreement",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.HELPFUL,
            correctionTier = CorrectionTier.HELPFUL,
            description = "Plural/singular errors",
            examples = listOf(
                "*Three book → Three books",
                "*Many information → Much information",
                "*Childrens → Children",
                "*Peoples → People"
            )
        ),
        ErrorType(
            id = "preposition_minor",
            name = "Minor Preposition Errors",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.HELPFUL,
            correctionTier = CorrectionTier.HELPFUL,
            description = "Preposition errors that don't change core meaning",
            examples = listOf(
                "*in Monday → on Monday",
                "*at the weekend → on the weekend",
                "*in the morning of Tuesday → on Tuesday morning",
                "*at December → in December"
            )
        ),
        ErrorType(
            id = "word_form",
            name = "Word Form Errors",
            category = ErrorCategory.VOCABULARY,
            severity = ErrorSeverity.HELPFUL,
            correctionTier = CorrectionTier.HELPFUL,
            description = "Using wrong form of a word",
            examples = listOf(
                "*I am happiness → I am happy",
                "*She speaks loud → She speaks loudly",
                "*It was a beauty day → It was a beautiful day",
                "*He runs quick → He runs quickly"
            )
        ),
        ErrorType(
            id = "redundancy",
            name = "Redundant Expressions",
            category = ErrorCategory.COHERENCE,
            severity = ErrorSeverity.HELPFUL,
            correctionTier = CorrectionTier.HELPFUL,
            description = "Unnecessary repetition or redundancy",
            examples = listOf(
                "*return back → return",
                "*repeat again → repeat",
                "*combine together → combine",
                "*rise up → rise"
            )
        )
    )
    
    /**
     * COMPREHENSIVE TIER - Polish-level improvements
     * These are minor errors that mark non-native speech but rarely cause confusion
     */
    val comprehensiveErrors = listOf(
        ErrorType(
            id = "subject_verb_agreement",
            name = "Subject-Verb Agreement",
            category = ErrorCategory.GRAMMAR,
            severity = ErrorSeverity.POLISH,
            correctionTier = CorrectionTier.COMPREHENSIVE,
            description = "Third person singular and other agreement issues",
            examples = listOf(
                "*She have → She has",
                "*He don't → He doesn't",
                "*The team are → The team is",
                "*Everyone have → Everyone has"
            )
        ),
        ErrorType(
            id = "collocation",
            name = "Collocation Errors",
            category = ErrorCategory.VOCABULARY,
            severity = ErrorSeverity.POLISH,
            correctionTier = CorrectionTier.COMPREHENSIVE,
            description = "Non-native word combinations",
            examples = listOf(
                "*make a photo → take a photo",
                "*do a mistake → make a mistake",
                "*say a joke → tell a joke",
                "*strong rain → heavy rain"
            )
        ),
        ErrorType(
            id = "idiom_usage",
            name = "Idiomatic Expression",
            category = ErrorCategory.VOCABULARY,
            severity = ErrorSeverity.POLISH,
            correctionTier = CorrectionTier.COMPREHENSIVE,
            description = "Non-idiomatic or incorrect idiom usage",
            examples = listOf(
                "*It's raining cats and puppies → cats and dogs",
                "*piece of a cake → piece of cake",
                "*hit the hay stack → hit the hay",
                "*break the ice cube → break the ice"
            )
        ),
        ErrorType(
            id = "register_formality",
            name = "Register/Formality Mismatch",
            category = ErrorCategory.PRAGMATICS,
            severity = ErrorSeverity.POLISH,
            correctionTier = CorrectionTier.COMPREHENSIVE,
            description = "Inappropriate level of formality",
            examples = listOf(
                "Using 'gonna' in formal presentation",
                "Using 'whom' in casual conversation",
                "Overly formal in friendly email",
                "Too casual in job interview"
            )
        ),
        ErrorType(
            id = "phrasal_verb_choice",
            name = "Phrasal Verb Selection",
            category = ErrorCategory.VOCABULARY,
            severity = ErrorSeverity.POLISH,
            correctionTier = CorrectionTier.COMPREHENSIVE,
            description = "Using single verbs instead of natural phrasal verbs",
            examples = listOf(
                "postpone → put off (in casual speech)",
                "continue → go on (in casual speech)",
                "surrender → give up (in casual speech)",
                "tolerate → put up with (in casual speech)"
            )
        )
    )
    
    /**
     * Get all error types organized by tier
     */
    fun getAllErrorTypes(): Map<CorrectionTier, List<ErrorType>> {
        return mapOf(
            CorrectionTier.CRITICAL to criticalErrors,
            CorrectionTier.IMPORTANT to importantErrors,
            CorrectionTier.HELPFUL to helpfulErrors,
            CorrectionTier.COMPREHENSIVE to comprehensiveErrors
        )
    }
    
    /**
     * Get error types for a specific tier and below
     */
    fun getErrorsForTier(tier: CorrectionTier): List<ErrorType> {
        return when (tier) {
            CorrectionTier.CRITICAL -> criticalErrors
            CorrectionTier.IMPORTANT -> criticalErrors + importantErrors
            CorrectionTier.HELPFUL -> criticalErrors + importantErrors + helpfulErrors
            CorrectionTier.COMPREHENSIVE -> criticalErrors + importantErrors + helpfulErrors + comprehensiveErrors
        }
    }
    
    /**
     * Find error type by ID
     */
    fun getErrorTypeById(id: String): ErrorType? {
        val allErrors = criticalErrors + importantErrors + helpfulErrors + comprehensiveErrors
        return allErrors.find { it.id == id }
    }
    
    /**
     * Get errors by category
     */
    fun getErrorsByCategory(category: ErrorCategory): List<ErrorType> {
        val allErrors = criticalErrors + importantErrors + helpfulErrors + comprehensiveErrors
        return allErrors.filter { it.category == category }
    }
    
    /**
     * Pronunciation-specific error types
     */
    val pronunciationErrors = listOf(
        ErrorType(
            id = "phoneme_substitution",
            name = "Sound Substitution",
            category = ErrorCategory.PRONUNCIATION,
            severity = ErrorSeverity.HELPFUL,
            correctionTier = CorrectionTier.HELPFUL,
            description = "Systematic sound substitutions based on L1",
            examples = listOf(
                "/θ/ → /s/ or /t/",
                "/v/ → /b/ or /w/",
                "/r/ → /l/",
                "/ð/ → /d/ or /z/"
            )
        ),
        ErrorType(
            id = "stress_pattern",
            name = "Word Stress",
            category = ErrorCategory.PRONUNCIATION,
            severity = ErrorSeverity.HELPFUL,
            correctionTier = CorrectionTier.HELPFUL,
            description = "Incorrect word stress patterns",
            examples = listOf(
                "preSENT (n) vs. preSENT (v)",
                "PHOtograph vs. phoTOGraphy",
                "reCORD (n) vs. reCORD (v)"
            )
        ),
        ErrorType(
            id = "intonation_pattern",
            name = "Sentence Intonation",
            category = ErrorCategory.PRONUNCIATION,
            severity = ErrorSeverity.POLISH,
            correctionTier = CorrectionTier.COMPREHENSIVE,
            description = "Non-native intonation patterns",
            examples = listOf(
                "Rising intonation in statements",
                "Flat intonation in questions",
                "Incorrect emphasis"
            )
        )
    )
    
    /**
     * Fluency-specific error types
     */
    val fluencyErrors = listOf(
        ErrorType(
            id = "hesitation_markers",
            name = "Excessive Hesitation",
            category = ErrorCategory.FLUENCY,
            severity = ErrorSeverity.HELPFUL,
            correctionTier = CorrectionTier.HELPFUL,
            description = "Overuse of fillers and hesitation markers",
            examples = listOf(
                "Excessive 'um', 'uh', 'er'",
                "Long pauses mid-sentence",
                "False starts and reformulations"
            )
        ),
        ErrorType(
            id = "speech_rate",
            name = "Speech Rate Issues",
            category = ErrorCategory.FLUENCY,
            severity = ErrorSeverity.HELPFUL,
            correctionTier = CorrectionTier.HELPFUL,
            description = "Speaking too fast or too slow",
            examples = listOf(
                "Under 100 WPM (too slow)",
                "Over 200 WPM (too fast)",
                "Inconsistent rate"
            )
        )
    )
}