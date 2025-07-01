package com.studywise.ai.domain.model.verbaljournal

/**
 * Models for managing conversation flow and AI responses
 */
data class ConversationContext(
    val sessionId: String,
    val currentTopic: String?,
    val conversationHistory: List<ConversationTurn>,
    val userProfile: VerbalJournalProfile,
    val sessionGoals: SessionGoals,
    val currentPhase: ConversationPhase
)

data class SessionGoals(
    val targetDuration: Int,
    val focusAreas: List<ImprovementAreaType>,
    val specificObjectives: List<String>,
    val difficultyLevel: DifficultyLevel
)

enum class ConversationPhase {
    GREETING,
    WARM_UP,
    MAIN_TOPIC,
    DEEP_DIVE,
    WRAP_UP,
    FEEDBACK
}

data class AIResponse(
    val text: String,
    val audioUrl: String? = null,
    val responseType: ResponseType,
    val followUpPrompts: List<String>,
    val encouragementNote: String? = null,
    val grammarHint: String? = null,
    val vocabularySuggestions: List<String> = emptyList()
)

enum class ResponseType {
    GREETING,
    QUESTION,
    FOLLOW_UP,
    CLARIFICATION,
    ENCOURAGEMENT,
    CORRECTION,
    TOPIC_TRANSITION,
    SUMMARY,
    FAREWELL
}

data class ConversationStrategy(
    val userLevel: ProficiencyLevel,
    val isBreakInPeriod: Boolean,
    val weekInProgram: Int?,
    val strategies: List<StrategyRule>
)

data class StrategyRule(
    val condition: String,
    val action: String,
    val priority: Int
)

object ConversationStrategies {
    fun getStrategy(profile: VerbalJournalProfile, weekInProgram: Int?): ConversationStrategy {
        val strategies = when (profile.proficiencyLevel) {
            ProficiencyLevel.BEGINNER -> beginnerStrategies()
            ProficiencyLevel.ELEMENTARY -> elementaryStrategies()
            ProficiencyLevel.INTERMEDIATE -> intermediateStrategies()
            ProficiencyLevel.UPPER_INTERMEDIATE -> upperIntermediateStrategies()
            ProficiencyLevel.ADVANCED -> advancedStrategies()
            ProficiencyLevel.PROFICIENT -> proficientStrategies()
        }
        
        return ConversationStrategy(
            userLevel = profile.proficiencyLevel,
            isBreakInPeriod = profile.preferences.enableBreakInPeriod && weekInProgram != null && weekInProgram <= 12,
            weekInProgram = weekInProgram,
            strategies = strategies
        )
    }
    
    private fun beginnerStrategies() = listOf(
        StrategyRule(
            condition = "User pauses > 5 seconds",
            action = "Provide gentle prompt or rephrase question simply",
            priority = 1
        ),
        StrategyRule(
            condition = "User makes error",
            action = "Acknowledge meaning, provide gentle correction after conversation",
            priority = 2
        ),
        StrategyRule(
            condition = "User uses native language",
            action = "Acknowledge and provide English equivalent naturally",
            priority = 1
        ),
        StrategyRule(
            condition = "Conversation stalls",
            action = "Ask simple yes/no question to restart",
            priority = 1
        )
    )
    
    private fun elementaryStrategies() = listOf(
        StrategyRule(
            condition = "User pauses > 3 seconds",
            action = "Wait patiently, offer help if pause extends",
            priority = 2
        ),
        StrategyRule(
            condition = "User makes repeated error",
            action = "Address error through modeling correct form",
            priority = 2
        ),
        StrategyRule(
            condition = "User shows improvement",
            action = "Acknowledge progress explicitly",
            priority = 1
        ),
        StrategyRule(
            condition = "Topic exhausted",
            action = "Introduce related but slightly challenging topic",
            priority = 2
        )
    )
    
    private fun intermediateStrategies() = listOf(
        StrategyRule(
            condition = "User uses simple language only",
            action = "Model more sophisticated alternatives naturally",
            priority = 2
        ),
        StrategyRule(
            condition = "User makes minor errors",
            action = "Ignore unless impedes communication",
            priority = 3
        ),
        StrategyRule(
            condition = "User attempts complex structure",
            action = "Encourage and expand on their attempt",
            priority = 1
        ),
        StrategyRule(
            condition = "Conversation is superficial",
            action = "Ask thought-provoking follow-up questions",
            priority = 2
        )
    )
    
    private fun upperIntermediateStrategies() = listOf(
        StrategyRule(
            condition = "User avoids certain structures",
            action = "Create natural opportunities to practice them",
            priority = 2
        ),
        StrategyRule(
            condition = "User speech lacks nuance",
            action = "Introduce idioms and cultural expressions",
            priority = 3
        ),
        StrategyRule(
            condition = "User makes fossilized errors",
            action = "Address systematically through focused exercises",
            priority = 1
        )
    )
    
    private fun advancedStrategies() = listOf(
        StrategyRule(
            condition = "User speech is fluent but unnatural",
            action = "Focus on pragmatics and register",
            priority = 1
        ),
        StrategyRule(
            condition = "User lacks cultural references",
            action = "Introduce through authentic contexts",
            priority = 2
        ),
        StrategyRule(
            condition = "Conversation lacks depth",
            action = "Challenge with abstract or controversial topics",
            priority = 2
        )
    )
    
    private fun proficientStrategies() = listOf(
        StrategyRule(
            condition = "Any aspect needs polish",
            action = "Provide peer-like natural conversation",
            priority = 3
        ),
        StrategyRule(
            condition = "User seeks specific improvement",
            action = "Focus intensively on requested area",
            priority = 1
        )
    )
}

/**
 * Topic suggestion system
 */
object TopicSuggestionEngine {
    fun suggestTopic(
        profile: VerbalJournalProfile,
        recentTopics: List<String>,
        currentDate: java.time.LocalDate,
        sessionType: SessionType
    ): TopicSuggestion {
        return when (sessionType) {
            SessionType.FREE_CONVERSATION -> suggestFreeConversationTopic(profile, recentTopics)
            SessionType.TOPIC_BASED -> suggestStructuredTopic(profile, recentTopics)
            SessionType.DAILY_PROMPT -> getDailyPromptTopic(currentDate, profile.proficiencyLevel)
        }
    }
    
    private fun suggestFreeConversationTopic(
        profile: VerbalJournalProfile,
        recentTopics: List<String>
    ): TopicSuggestion {
        val preferredTopics = profile.preferences.preferredTopics
        val availableTopics = FREE_CONVERSATION_TOPICS[profile.proficiencyLevel] ?: emptyList()
        
        val filteredTopics = availableTopics.filter { it !in recentTopics }
        val topic = filteredTopics.firstOrNull { topic ->
            preferredTopics.any { pref -> topic.contains(pref, ignoreCase = true) }
        } ?: filteredTopics.randomOrNull() ?: "Tell me about your day"
        
        return TopicSuggestion(
            topic = topic,
            scaffolding = getScaffoldingForLevel(profile.proficiencyLevel),
            vocabulary = getVocabularyForTopic(topic, profile.proficiencyLevel)
        )
    }
    
    private fun suggestStructuredTopic(
        profile: VerbalJournalProfile,
        recentTopics: List<String>
    ): TopicSuggestion {
        // Implementation for structured topics
        return TopicSuggestion(
            topic = "Describing your ideal vacation",
            scaffolding = listOf(
                "Where would you go?",
                "What would you do there?",
                "Who would you go with?",
                "How long would you stay?"
            ),
            vocabulary = listOf("destination", "itinerary", "accommodation", "sightseeing", "relaxation")
        )
    }
    
    private fun getDailyPromptTopic(
        date: java.time.LocalDate,
        level: ProficiencyLevel
    ): TopicSuggestion {
        // Implementation for daily prompts
        return TopicSuggestion(
            topic = "If you could have dinner with anyone in history, who would it be and why?",
            scaffolding = listOf(
                "Introduce the person",
                "Explain why you chose them",
                "What would you ask them?",
                "What would you tell them about modern times?"
            ),
            vocabulary = listOf("historical figure", "influence", "curiosity", "perspective", "legacy")
        )
    }
    
    private fun getScaffoldingForLevel(level: ProficiencyLevel): List<String> {
        return when (level) {
            ProficiencyLevel.BEGINNER -> listOf(
                "Start with simple words",
                "Use gestures if needed",
                "Take your time"
            )
            ProficiencyLevel.INTERMEDIATE -> listOf(
                "Try to speak in complete sentences",
                "Use connecting words",
                "Express your opinions"
            )
            else -> listOf(
                "Elaborate on your ideas",
                "Use examples",
                "Consider different perspectives"
            )
        }
    }
    
    private fun getVocabularyForTopic(topic: String, level: ProficiencyLevel): List<String> {
        // Simplified implementation
        return listOf("example", "vocabulary", "words", "for", "topic")
    }
    
    private val FREE_CONVERSATION_TOPICS = mapOf(
        ProficiencyLevel.BEGINNER to listOf(
            "Your family",
            "Your daily routine",
            "Your favorite food",
            "The weather today",
            "Your hobbies"
        ),
        ProficiencyLevel.INTERMEDIATE to listOf(
            "A memorable trip",
            "Your career goals",
            "Technology in daily life",
            "Environmental concerns",
            "Cultural differences"
        ),
        ProficiencyLevel.ADVANCED to listOf(
            "The impact of AI on society",
            "Work-life balance in modern times",
            "The future of education",
            "Ethical dilemmas in medicine",
            "Space exploration priorities"
        )
    )
}

data class TopicSuggestion(
    val topic: String,
    val scaffolding: List<String>,
    val vocabulary: List<String>
)