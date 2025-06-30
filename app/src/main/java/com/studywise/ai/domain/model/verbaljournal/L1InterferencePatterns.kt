package com.studywise.ai.domain.model.verbaljournal

/**
 * L1 (Native Language) Interference Patterns for major languages
 * Based on linguistic research and ESL teaching experience
 */
object L1InterferencePatterns {
    
    val patterns = mapOf(
        "Spanish" to L1InterferencePattern(
            nativeLanguage = "Spanish",
            languageFamily = "Romance",
            commonErrors = listOf(
                "subject_pronoun_omission", // "Is good" instead of "It is good"
                "adjective_noun_order", // "car blue" instead of "blue car"
                "false_friends", // "embarazada" ≠ "embarrassed"
                "article_before_profession", // "I am a teacher" vs "Soy profesor"
                "present_perfect_confusion", // Using simple past instead
                "gerund_infinitive_confusion", // "I enjoy to swim"
                "double_negatives", // "I don't have nothing"
                "ser_estar_confusion" // Both translate to "be"
            ),
            pronunciationChallenges = listOf(
                "/v/ vs /b/", // "very" sounds like "berry"
                "/ʃ/ (sh)", // Initial "sh" difficult
                "/θ/ (th)", // "think" → "tink" or "sink"
                "/z/ endings", // "dogs" → "dogss"
                "initial /s/ clusters", // "espeak" for "speak"
                "vowel length distinction", // "ship" vs "sheep"
                "/dʒ/ (j)", // "job" → "yob"
            ),
            grammarInterference = listOf(
                "subjunctive_overuse",
                "reflexive_pronoun_errors",
                "continuous_tense_underuse",
                "preposition_confusion_por_para"
            ),
            falseFreinds = listOf(
                FalseFriend("éxito", "exit", "success", "salida", "El éxito del proyecto (The project's success)"),
                FalseFriend("embarazada", "embarrassed", "pregnant", "avergonzada", "Está embarazada (She is pregnant)"),
                FalseFriend("realizar", "realize", "to carry out", "darse cuenta", "Realizar un proyecto (Carry out a project)"),
                FalseFriend("actual", "actual", "current", "real/verdadero", "El presidente actual (The current president)")
            ),
            weightingAdjustments = mapOf(
                "article_errors" to 0.8f, // Common but less critical
                "subject_pronoun_omission" to 0.6f, // Very expected
                "word_order" to 1.2f, // More critical for Spanish speakers
                "false_friends" to 1.3f, // Can cause serious misunderstandings
                "pronunciation_v_b" to 0.5f // Expected, lower weight
            )
        ),
        
        "Chinese" to L1InterferencePattern(
            nativeLanguage = "Chinese (Mandarin/Cantonese)",
            languageFamily = "Sino-Tibetan",
            commonErrors = listOf(
                "article_omission", // No articles in Chinese
                "plural_omission", // "three book"
                "verb_tense_omission", // Context-based tense
                "pronoun_gender_confusion", // No he/she distinction in speech
                "countable_uncountable_confusion", // Different system
                "preposition_errors", // Different conceptual mapping
                "relative_clause_errors", // Different structure
                "question_formation" // No auxiliary inversion
            ),
            pronunciationChallenges = listOf(
                "/θ/ and /ð/", // No equivalent sounds
                "/r/ and /l/", // Often confused
                "final consonants", // Tendency to drop
                "consonant clusters", // Break up with vowels
                "/v/", // Often becomes /w/
                "word stress", // Tonal language background
                "sentence intonation" // Different patterns
            ),
            grammarInterference = listOf(
                "aspect_not_tense", // Chinese uses aspect markers
                "topic_comment_structure", // Different word order
                "measure_word_interference", // "a piece of paper"
                "serial_verb_construction" // Multiple verbs without connectors
            ),
            falseFreinds = listOf(), // Fewer due to different writing systems
            weightingAdjustments = mapOf(
                "articles" to 0.3f, // Very expected, minimal weight
                "plural_markers" to 0.4f, // Also very common
                "verb_tense" to 0.9f, // Important but expected
                "word_order" to 1.3f, // Critical for comprehension
                "pronunciation_r_l" to 0.6f // Common, less critical in context
            )
        ),
        
        "Arabic" to L1InterferencePattern(
            nativeLanguage = "Arabic",
            languageFamily = "Semitic",
            commonErrors = listOf(
                "indefinite_article_omission", // "I am teacher"
                "present_tense_be_omission", // "She happy"
                "word_order_VSO", // Verb-Subject-Object tendency
                "pronoun_redundancy", // "My father he is..."
                "genitive_construction", // "The book of the student"
                "comparative_superlative", // Different formation
                "time_expressions", // Different conceptualization
                "passive_voice_avoidance" // Less common in Arabic
            ),
            pronunciationChallenges = listOf(
                "/p/ vs /b/", // No /p/ in Arabic
                "/v/ vs /f/", // No /v/ in most dialects
                "/ŋ/ (ng)", // Difficult sound
                "vowel distinctions", // Fewer vowels in Arabic
                "stress patterns", // Different system
                "consonant clusters", // Insert vowels
                "/tʃ/ (ch)", // Not in standard Arabic
            ),
            grammarInterference = listOf(
                "dual_number_interference", // Arabic has singular/dual/plural
                "gender_in_verbs", // Verbs change by gender
                "verb_initial_sentences", // VSO order
                "nominal_sentences" // Sentences without verbs
            ),
            falseFreinds = listOf(
                FalseFriend("مكتبة", "maktaba", "library", "bookstore", "Different meaning despite similarity")
            ),
            weightingAdjustments = mapOf(
                "word_order" to 0.8f, // Some flexibility acceptable
                "article_omission" to 0.7f, // Common pattern
                "pronunciation_p_b" to 0.5f, // Very expected
                "present_be_omission" to 1.1f // Can impede communication
            )
        ),
        
        "Japanese" to L1InterferencePattern(
            nativeLanguage = "Japanese",
            languageFamily = "Japonic",
            commonErrors = listOf(
                "article_confusion", // Overuse or omission
                "singular_plural_confusion", // No obligatory plurals
                "pronoun_dropping", // Common in Japanese
                "word_order_SOV", // Subject-Object-Verb
                "relative_clause_prenominal", // Before the noun
                "auxiliary_verb_errors", // Different modal system
                "countable_uncountable", // Different categorization
                "preposition_confusion" // Particles work differently
            ),
            pronunciationChallenges = listOf(
                "/l/ vs /r/", // Single liquid phoneme
                "/θ/ and /ð/", // Use /s/ and /z/
                "/v/ vs /b/", // No /v/ distinction
                "/f/", // Only before /u/
                "consonant clusters", // Insert vowels
                "word stress", // Pitch accent language
                "/si/ vs /ʃi/", // "sit" vs "shit"
            ),
            grammarInterference = listOf(
                "topic_marker_interference", // は (wa) structure
                "politeness_levels", // Affects verb choice
                "passive_voice_frequency", // Used differently
                "double_subject_construction" // "Elephants, noses are long"
            ),
            falseFreinds = listOf(
                FalseFriend("マンション", "mansion", "apartment", "豪邸", "Manshon means apartment building"),
                FalseFriend("スマート", "smart", "slim/stylish", "頭が良い", "Sumaato means slim, not intelligent")
            ),
            weightingAdjustments = mapOf(
                "pronunciation_l_r" to 0.4f, // Very expected
                "articles" to 0.7f, // Common confusion
                "word_order" to 1.2f, // Important for clarity
                "consonant_clusters" to 0.6f // Expected vowel insertion
            )
        ),
        
        "Korean" to L1InterferencePattern(
            nativeLanguage = "Korean",
            languageFamily = "Koreanic",
            commonErrors = listOf(
                "article_system", // No articles in Korean
                "final_consonant_unreleased", // Different phonotactics
                "verb_final_word_order", // SOV structure
                "topic_particle_interference", // 은/는 influence
                "honorific_interference", // Complex politeness
                "progressive_stative_confusion", // "I am knowing"
                "preposition_postposition", // Korean uses postpositions
                "yes_no_confusion" // Answering negative questions
            ),
            pronunciationChallenges = listOf(
                "/f/ vs /p/", // No /f/ in Korean
                "/v/ vs /b/", // No /v/ sound
                "/θ/ and /ð/", // Difficult sounds
                "/z/", // Often devoiced
                "/r/ vs /l/", // Allophonic in Korean
                "final consonants", // Unreleased
                "vowel distinctions", // Different system
            ),
            grammarInterference = listOf(
                "agglutinative_influence", // Adding too many endings
                "null_subject_tendency", // Dropping subjects
                "numeral_classifier_system", // Measure words
                "verb_serialization" // Multiple verbs together
            ),
            falseFreinds = listOf(
                FalseFriend("컵", "cup", "disposable cup", "머그잔", "Keop usually means paper/plastic cup")
            ),
            weightingAdjustments = mapOf(
                "final_consonants" to 0.5f, // Very characteristic
                "articles" to 0.6f, // Expected difficulty
                "word_order" to 1.1f, // Important but manageable
                "pronunciation_f" to 0.4f // Very expected substitution
            )
        ),
        
        "French" to L1InterferencePattern(
            nativeLanguage = "French",
            languageFamily = "Romance",
            commonErrors = listOf(
                "false_friends_extensive", // Many false cognates
                "adjective_position", // After noun often
                "present_perfect_passé_composé", // Different usage
                "progressive_aspect", // Less used in French
                "phrasal_verb_avoidance", // Not in French
                "gender_interference", // Assigning gender
                "negative_construction", // "ne...pas" influence
                "subjunctive_overuse" // More common in French
            ),
            pronunciationChallenges = listOf(
                "/h/", // Silent in French
                "/θ/ and /ð/", // Use /s/ and /z/
                "final consonants", // Often silent
                "/ɪ/ vs /i:/", // "ship" vs "sheep"
                "stress patterns", // Final syllable stress
                "/r/", // Uvular in French
                "nasal vowels" // Carrying over
            ),
            grammarInterference = listOf(
                "partitive_article_overuse", // "some" overuse
                "reflexive_pronoun_overuse", // More reflexives
                "word_order_in_questions", // Different inversion
                "tense_concordance" // Sequence of tenses
            ),
            falseFreinds = listOf(
                FalseFriend("actuellement", "actually", "currently", "en fait", "Actuellement il travaille (Currently he works)"),
                FalseFriend("éventuellement", "eventually", "possibly", "finalement", "Éventuellement, on verra (Possibly, we'll see)"),
                FalseFriend("librairie", "library", "bookstore", "bibliothèque", "J'achète à la librairie (I buy at the bookstore)"),
                FalseFriend("preservatif", "preservative", "condom", "conservateur", "Medical/pharmaceutical context")
            ),
            weightingAdjustments = mapOf(
                "false_friends" to 1.4f, // High risk of misunderstanding
                "pronunciation_h" to 0.6f, // Common but understandable
                "adjective_position" to 0.7f, // Usually clear from context
                "progressive_aspect" to 0.9f // Can affect meaning
            )
        ),
        
        "Russian" to L1InterferencePattern(
            nativeLanguage = "Russian",
            languageFamily = "Slavic",
            commonErrors = listOf(
                "article_system", // No articles in Russian
                "present_tense_be", // Zero copula
                "aspect_not_tense", // Perfective/imperfective
                "word_order_flexible", // Free word order
                "preposition_case_confusion", // Case system
                "yes_no_agreement", // "Da" to negative questions
                "consonant_palatalization", // Soft consonants
                "motion_verb_complexity" // Different system
            ),
            pronunciationChallenges = listOf(
                "/θ/ and /ð/", // No equivalents
                "/w/ vs /v/", // Only /v/ in Russian
                "/h/", // Often /x/ sound
                "/ŋ/ (ng)", // Difficult ending
                "vowel reduction", // Different pattern
                "final devoicing", // "dog" → "dok"
                "consonant clusters", // Many in Russian
            ),
            grammarInterference = listOf(
                "case_ending_influence", // Expecting inflection
                "aspect_perfectivity", // Different from tense
                "verbal_prefixation", // Changing meaning
                "double_negation" // Standard in Russian
            ),
            falseFreinds = listOf(
                FalseFriend("магазин", "magazine", "store/shop", "журнал", "Magazin means store"),
                FalseFriend("фамилия", "family", "surname", "семья", "Familiya means last name")
            ),
            weightingAdjustments = mapOf(
                "articles" to 0.4f, // Very difficult concept
                "word_order" to 0.9f, // Flexible but important
                "present_be" to 1.2f, // Critical omission
                "final_devoicing" to 0.6f // Systematic pattern
            )
        ),
        
        "Hindi" to L1InterferencePattern(
            nativeLanguage = "Hindi",
            languageFamily = "Indo-Aryan",
            commonErrors = listOf(
                "progressive_overuse", // Present continuous for simple
                "article_the_overuse", // "The life is beautiful"
                "preposition_confusion", // Different mapping
                "question_tag_na", // Adding "na?" or "no?"
                "present_perfect_overuse", // For simple past
                "gerund_infinitive", // Different usage
                "word_order_SOV", // Subject-Object-Verb
                "reduplication" // Emphasis through repetition
            ),
            pronunciationChallenges = listOf(
                "/θ/ and /ð/", // Often /t̪/ and /d̪/
                "/ʒ/ (zh)", // Not in Hindi
                "/æ/ vs /ɛ/", // "bad" vs "bed"
                "aspirated consonants", // Transfer aspiration
                "/v/ vs /w/", // Single sound in Hindi
                "retroflex influence", // ṭ, ḍ sounds
                "stress patterns" // Different system
            ),
            grammarInterference = listOf(
                "postposition_influence", // Instead of prepositions
                "compound_verb_usage", // "do making" for "make"
                "honorific_complexity", // Tu/tum/aap influence
                "ergative_case_influence" // In perfect tenses
            ),
            falseFreinds = listOf(
                FalseFriend("कट", "cut", "to be cut", "काटना", "Different transitivity")
            ),
            weightingAdjustments = mapOf(
                "progressive_overuse" to 0.8f, // Common pattern
                "article_the" to 0.7f, // Frequent but understandable
                "pronunciation_th" to 0.6f, // Expected substitution
                "word_order" to 1.0f // Important for clarity
            )
        ),
        
        "Portuguese" to L1InterferencePattern(
            nativeLanguage = "Portuguese",
            languageFamily = "Romance",
            commonErrors = listOf(
                "false_friends_spanish", // Similar to Spanish
                "present_continuous_gerund", // Different usage
                "personal_infinitive", // Unique to Portuguese
                "subjunctive_future", // More complex
                "haver_ter_confusion", // Both mean "have"
                "preposition_contraction", // De+o = do
                "double_negative_informal", // Colloquial usage
                "reflexive_pronoun_placement" // Different rules
            ),
            pronunciationChallenges = listOf(
                "nasal vowels", // Unique to Portuguese
                "/ʃ/ for s", // In certain positions
                "/ʒ/ for g/j", // Different from Spanish
                "vowel reduction", // Unstressed vowels
                "/l/ velarization", // Dark L
                "diphthongs", // Complex system
                "/r/ variations" // Regional differences
            ),
            grammarInterference = listOf(
                "mesoclisis", // Pronoun in middle of verb
                "synthetic_future", // One word future
                "gerund_vs_infinitive", // Different from English
                "ser_estar_ficar" // Three "to be" verbs
            ),
            falseFreinds = listOf(
                FalseFriend("pretender", "pretend", "to intend", "fingir", "Pretendo ir (I intend to go)"),
                FalseFriend("esquisito", "exquisite", "strange/weird", "requintado", "Que esquisito! (How strange!)"),
                FalseFriend("puxar", "push", "to pull", "empurrar", "Puxe a porta (Pull the door)")
            ),
            weightingAdjustments = mapOf(
                "false_friends" to 1.3f, // Can cause confusion
                "nasal_vowels" to 0.5f, // Expected carryover
                "present_continuous" to 0.9f, // Different usage
                "reflexive_placement" to 0.7f // Grammar difference
            )
        ),
        
        "German" to L1InterferencePattern(
            nativeLanguage = "German",
            languageFamily = "Germanic",
            commonErrors = listOf(
                "verb_second_position", // V2 word order
                "separable_verbs", // Influence on phrasal verbs
                "case_system_influence", // Expecting cases
                "false_friends_germanic", // Many cognates
                "present_perfect_preference", // Over simple past
                "adjective_endings", // Expecting agreement
                "modal_verb_construction", // Different usage
                "time_manner_place" // Adverb order
            ),
            pronunciationChallenges = listOf(
                "/w/ vs /v/", // Hypercorrection
                "/θ/ and /ð/", // Often /s/ and /z/
                "final devoicing", // b→p, d→t, g→k
                "/ɜː/ (ur)", // "bird", "work"
                "vowel length", // Phonemic in German
                "/r/ pronunciation", // Different varieties
                "initial /sp/, /st/" // Often /ʃp/, /ʃt/
            ),
            grammarInterference = listOf(
                "verb_final_subordinate", // In dependent clauses
                "adjective_declension", // Expecting endings
                "compound_word_creation", // One long word
                "formal_informal_distinction" // Sie/du influence
            ),
            falseFreinds = listOf(
                FalseFriend("bekommen", "become", "to get/receive", "werden", "Ich bekomme ein Geschenk (I get a gift)"),
                FalseFriend("eventuell", "eventually", "possibly/perhaps", "schließlich", "Eventuell komme ich (Perhaps I'll come)"),
                FalseFriend("aktuell", "actual", "current/present", "tatsächlich", "Die aktuelle Situation (The current situation)")
            ),
            weightingAdjustments = mapOf(
                "word_order_v2" to 1.1f, // Can affect meaning
                "false_friends" to 1.2f, // Common confusion
                "final_devoicing" to 0.6f, // Systematic pattern
                "present_perfect" to 0.8f // Usage difference
            )
        )
    )
    
    /**
     * Get interference pattern for a specific native language
     */
    fun getPattern(nativeLanguage: String): L1InterferencePattern? {
        return patterns[nativeLanguage]
    }
    
    /**
     * Get error weight adjustment for a specific error type and native language
     */
    fun getErrorWeight(nativeLanguage: String, errorType: String): Float {
        val pattern = patterns[nativeLanguage] ?: return 1.0f
        return pattern.weightingAdjustments[errorType] ?: 1.0f
    }
    
    /**
     * Check if an error is expected based on L1 interference
     */
    fun isExpectedError(nativeLanguage: String, errorType: String): Boolean {
        val pattern = patterns[nativeLanguage] ?: return false
        return errorType in pattern.commonErrors || 
               errorType in pattern.pronunciationChallenges ||
               errorType in pattern.grammarInterference
    }
    
    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): List<String> {
        return patterns.keys.toList()
    }
}