package com.studywise.ai.domain.model.verbaljournal

/**
 * Language-specific interference patterns for common ESL learner backgrounds
 */
data class L1InterferencePattern(
    val nativeLanguage: String,
    val interferenceType: InterferenceType,
    val pattern: String,
    val description: String,
    val examples: List<Example>,
    val correctionStrategy: String
)

data class Example(
    val incorrect: String,
    val correct: String,
    val explanation: String
)

enum class InterferenceType {
    PHONOLOGICAL,
    GRAMMATICAL,
    LEXICAL,
    SYNTACTIC,
    PRAGMATIC
}

object CommonL1Patterns {
    val SPANISH = listOf(
        L1InterferencePattern(
            nativeLanguage = "Spanish",
            interferenceType = InterferenceType.PHONOLOGICAL,
            pattern = "Initial /s/ cluster",
            description = "Adding 'e' before words starting with s+consonant",
            examples = listOf(
                Example("I am from Espain", "I am from Spain", "Spanish doesn't have initial /sp/ clusters"),
                Example("The eschool is big", "The school is big", "Hypercorrection of s+consonant rule")
            ),
            correctionStrategy = "Practice minimal pairs: Spain/Espain, school/eschool"
        ),
        L1InterferencePattern(
            nativeLanguage = "Spanish",
            interferenceType = InterferenceType.GRAMMATICAL,
            pattern = "Article usage",
            description = "Using definite articles where English doesn't",
            examples = listOf(
                Example("The life is beautiful", "Life is beautiful", "Abstract nouns don't need articles"),
                Example("I like the coffee", "I like coffee", "General statements don't use articles")
            ),
            correctionStrategy = "Focus on abstract vs. concrete noun distinction"
        )
    )
    
    val MANDARIN = listOf(
        L1InterferencePattern(
            nativeLanguage = "Mandarin",
            interferenceType = InterferenceType.PHONOLOGICAL,
            pattern = "Final consonants",
            description = "Dropping or weakening final consonants",
            examples = listOf(
                Example("I like to rea(d)", "I like to read", "Final /d/ often dropped"),
                Example("The ca(t) is cute", "The cat is cute", "Final /t/ often dropped")
            ),
            correctionStrategy = "Exaggerate final consonants during practice"
        ),
        L1InterferencePattern(
            nativeLanguage = "Mandarin",
            interferenceType = InterferenceType.GRAMMATICAL,
            pattern = "Tense marking",
            description = "Omitting tense markers or using time adverbs instead",
            examples = listOf(
                Example("Yesterday I go to store", "Yesterday I went to the store", "Missing past tense"),
                Example("He already eat", "He has already eaten", "Missing perfect aspect")
            ),
            correctionStrategy = "Explicit focus on verb conjugation patterns"
        )
    )
    
    val ARABIC = listOf(
        L1InterferencePattern(
            nativeLanguage = "Arabic",
            interferenceType = InterferenceType.PHONOLOGICAL,
            pattern = "/p/ and /b/ confusion",
            description = "Substituting /b/ for /p/ as Arabic lacks /p/",
            examples = listOf(
                Example("I need a ben", "I need a pen", "/p/ pronounced as /b/"),
                Example("The broblem is big", "The problem is big", "/p/ pronounced as /b/")
            ),
            correctionStrategy = "Minimal pair drills: pen/ben, pat/bat"
        ),
        L1InterferencePattern(
            nativeLanguage = "Arabic",
            interferenceType = InterferenceType.SYNTACTIC,
            pattern = "Verb-initial sentences",
            description = "Starting sentences with verbs as in Arabic VSO order",
            examples = listOf(
                Example("Came the teacher late", "The teacher came late", "VSO to SVO conversion"),
                Example("Wants he coffee", "He wants coffee", "VSO to SVO conversion")
            ),
            correctionStrategy = "Practice subject-first sentence patterns"
        )
    )
    
    val JAPANESE = listOf(
        L1InterferencePattern(
            nativeLanguage = "Japanese",
            interferenceType = InterferenceType.PHONOLOGICAL,
            pattern = "/l/ and /r/ confusion",
            description = "Difficulty distinguishing and producing /l/ and /r/",
            examples = listOf(
                Example("I leally like it", "I really like it", "/r/ pronounced as /l/"),
                Example("The light is red", "The right is red", "/r/ and /l/ confusion")
            ),
            correctionStrategy = "Tongue position exercises and minimal pairs"
        ),
        L1InterferencePattern(
            nativeLanguage = "Japanese",
            interferenceType = InterferenceType.GRAMMATICAL,
            pattern = "Article omission",
            description = "Omitting articles as Japanese lacks them",
            examples = listOf(
                Example("I have cat", "I have a cat", "Missing indefinite article"),
                Example("Sun is bright", "The sun is bright", "Missing definite article")
            ),
            correctionStrategy = "Explicit article rules and contextual practice"
        )
    )
}

/**
 * Common error patterns across all ESL learners
 */
object UniversalESLPatterns {
    val PRONUNCIATION = listOf(
        ErrorPattern(
            type = ErrorType.PRONUNCIATION,
            category = "Word stress",
            frequency = 0,
            examples = listOf(
                "proGRESS (noun) vs. proGRESS (verb)",
                "PHOtograph vs. phoTOGraphy"
            )
        ),
        ErrorPattern(
            type = ErrorType.PRONUNCIATION,
            category = "Sentence stress",
            frequency = 0,
            examples = listOf(
                "I DIDN'T say that (emphasis on negation)",
                "I didn't SAY that (emphasis on action)"
            )
        )
    )
    
    val GRAMMAR = listOf(
        ErrorPattern(
            type = ErrorType.GRAMMAR,
            category = "Present perfect vs. Simple past",
            frequency = 0,
            examples = listOf(
                "I have seen him yesterday" → "I saw him yesterday",
                "I lived here for 5 years" → "I have lived here for 5 years"
            )
        ),
        ErrorPattern(
            type = ErrorType.GRAMMAR,
            category = "Phrasal verbs",
            frequency = 0,
            examples = listOf(
                "look after", "give up", "put off", "take on"
            )
        )
    )
    
    val FLUENCY = listOf(
        ErrorPattern(
            type = ErrorType.FLUENCY,
            category = "Filler overuse",
            frequency = 0,
            examples = listOf(
                "um", "uh", "you know", "like"
            )
        ),
        ErrorPattern(
            type = ErrorType.FLUENCY,
            category = "False starts",
            frequency = 0,
            examples = listOf(
                "I think... no, I mean...",
                "The... the... the book"
            )
        )
    )
}