package com.studywise.ai.domain.service

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.repository.AIRepository
import com.studywise.ai.domain.repository.VerbalJournalRepository
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing AI conversations in Verbal Journal sessions
 */
@Singleton
class VerbalJournalConversationService @Inject constructor(
    private val aiRepository: AIRepository,
    private val verbalJournalRepository: VerbalJournalRepository
) {
    
    /**
     * Generate an AI response based on conversation context
     */
    suspend fun generateResponse(
        journalEntryId: String,
        userTranscript: String,
        userProfile: VerbalJournalProfile
    ): String {
        // Get conversation history
        val conversationTurns = verbalJournalRepository.getConversationTurns(journalEntryId)
        val entry = verbalJournalRepository.getJournalEntry(journalEntryId)
            ?: throw IllegalStateException("Journal entry not found")
        
        // Build conversation context
        val context = buildConversationContext(
            entry = entry,
            turns = conversationTurns,
            userProfile = userProfile,
            latestUserInput = userTranscript
        )
        
        // Generate appropriate response
        val prompt = buildConversationPrompt(context)
        
        val result = aiRepository.generateText(
            prompt = prompt,
            temperature = 0.7f,
            maxTokens = 150
        )
        
        return result.getOrDefault(getFallbackResponse(context))
    }
    
    /**
     * Get or generate a journal prompt for the user
     */
    suspend fun getJournalPrompt(
        date: Date,
        userProfile: VerbalJournalProfile
    ): JournalPrompt {
        return verbalJournalRepository.getDailyPrompt(date, userProfile.currentLevel)
    }
    
    /**
     * Determine engagement level based on conversation
     */
    fun determineEngagementLevel(
        conversationTurns: List<ConversationTurn>,
        sessionDuration: Int
    ): EngagementLevel {
        if (conversationTurns.isEmpty()) return EngagementLevel.MEDIUM
        
        // Calculate average response length
        val userTurns = conversationTurns.filter { it.speaker == Speaker.USER }
        val avgResponseLength = userTurns
            .map { it.transcript.split(" ").size }
            .average()
        
        // Calculate response frequency
        val responseFrequency = userTurns.size.toFloat() / sessionDuration
        
        return when {
            avgResponseLength > 20 && responseFrequency > 0.5 -> EngagementLevel.HIGH
            avgResponseLength < 5 || responseFrequency < 0.2 -> EngagementLevel.LOW
            avgResponseLength < 10 && responseFrequency < 0.3 -> EngagementLevel.STRUGGLING
            else -> EngagementLevel.MEDIUM
        }
    }
    
    /**
     * Build conversation context for AI
     */
    private fun buildConversationContext(
        entry: VerbalJournalEntry,
        turns: List<ConversationTurn>,
        userProfile: VerbalJournalProfile,
        latestUserInput: String
    ): ConversationContext {
        // Extract recent topics from conversation
        val recentTopics = extractTopics(turns)
        
        // Determine engagement level
        val engagementLevel = determineEngagementLevel(
            turns,
            entry.actualDurationMinutes
        )
        
        return ConversationContext(
            userLevel = userProfile.currentLevel,
            sessionMinutes = entry.actualDurationMinutes,
            recentTopics = recentTopics,
            userInterests = userProfile.preferredTopics,
            conversationHistory = turns,
            energyLevel = engagementLevel,
            nativeLanguage = userProfile.nativeLanguage,
            correctionStyle = userProfile.correctionStyle,
            latestUserInput = latestUserInput
        )
    }
    
    /**
     * Build conversation prompt for AI
     */
    private fun buildConversationPrompt(context: ConversationContext): String {
        val conversationHistory = context.conversationHistory
            .takeLast(6) // Keep last 6 turns for context
            .joinToString("\n") { turn ->
                when (turn.speaker) {
                    Speaker.USER -> "Student: ${turn.transcript}"
                    Speaker.AI -> "You: ${turn.aiResponse ?: turn.transcript}"
                }
            }
        
        val engagementStrategy = when (context.energyLevel) {
            EngagementLevel.HIGH -> "Continue with engaging follow-up questions and show enthusiasm"
            EngagementLevel.MEDIUM -> "Maintain steady conversation with balanced questions"
            EngagementLevel.LOW -> "Encourage participation with simpler questions and positive reinforcement"
            EngagementLevel.STRUGGLING -> "Simplify language, offer choices, and be extra supportive"
        }
        
        val levelGuidance = when (context.userLevel) {
            ProficiencyLevel.BEGINNER -> """
                - Use simple vocabulary (high-frequency words)
                - Short, clear sentences
                - Avoid idioms and complex structures
                - Repeat/rephrase key ideas
            """.trimIndent()
            
            ProficiencyLevel.INTERMEDIATE -> """
                - Use common vocabulary with some variety
                - Mix simple and compound sentences
                - Introduce common idioms gradually
                - Natural but clear speech patterns
            """.trimIndent()
            
            ProficiencyLevel.ADVANCED -> """
                - Use varied vocabulary including less common words
                - Complex sentence structures are fine
                - Natural idioms and expressions
                - Discuss abstract concepts
            """.trimIndent()
            
            ProficiencyLevel.NATIVE_LEVEL -> """
                - Full range of vocabulary and expressions
                - Natural native-like speech
                - Complex topics and nuanced discussion
                - Cultural references are appropriate
            """.trimIndent()
        }
        
        return """
            You are a friendly, supportive English conversation partner helping a ${context.userLevel} level student practice speaking.
            
            Student profile:
            - Native language: ${context.nativeLanguage}
            - Current session: ${context.sessionMinutes} minutes
            - Engagement level: ${context.energyLevel}
            - Topics of interest: ${context.userInterests.joinToString(", ")}
            
            Conversation history:
            $conversationHistory
            
            Student's latest response: "${context.latestUserInput}"
            
            Your conversation guidelines:
            $levelGuidance
            
            Engagement strategy: $engagementStrategy
            
            Important rules:
            1. DO NOT correct errors directly during conversation
            2. Keep responses concise (2-3 sentences max)
            3. Ask open-ended questions to encourage speaking
            4. Show genuine interest in what they're saying
            5. If they're struggling, offer gentle prompts or choices
            6. Build on their interests and experiences
            7. Use natural, conversational language appropriate for their level
            
            Generate a natural, encouraging response that keeps the conversation flowing.
        """.trimIndent()
    }
    
    /**
     * Extract topics from conversation
     */
    private fun extractTopics(turns: List<ConversationTurn>): List<String> {
        // Simple keyword extraction - in production would use NLP
        val allText = turns.joinToString(" ") { it.transcript }
        val words = allText.lowercase().split(Regex("\\W+"))
        
        // Common topics
        val topicKeywords = mapOf(
            "family" to listOf("family", "mother", "father", "sister", "brother", "parent"),
            "work" to listOf("work", "job", "office", "colleague", "boss", "career"),
            "hobbies" to listOf("hobby", "like", "enjoy", "fun", "free time", "weekend"),
            "travel" to listOf("travel", "trip", "vacation", "visit", "country", "city"),
            "food" to listOf("food", "eat", "restaurant", "cook", "meal", "dinner"),
            "education" to listOf("school", "study", "learn", "class", "teacher", "student")
        )
        
        val detectedTopics = mutableListOf<String>()
        topicKeywords.forEach { (topic, keywords) ->
            if (keywords.any { it in words }) {
                detectedTopics.add(topic)
            }
        }
        
        return detectedTopics.distinct()
    }
    
    /**
     * Get fallback response if AI fails
     */
    private fun getFallbackResponse(context: ConversationContext): String {
        return when (context.energyLevel) {
            EngagementLevel.HIGH -> "That's really interesting! Can you tell me more about that?"
            EngagementLevel.MEDIUM -> "I see. What else would you like to share?"
            EngagementLevel.LOW -> "That's good! Is there anything else you'd like to talk about?"
            EngagementLevel.STRUGGLING -> "Thank you for sharing. Would you like to talk about something else, or shall we take a short break?"
        }
    }
    
    /**
     * Generate follow-up questions based on topic
     */
    fun generateFollowUpQuestions(
        topic: String,
        userLevel: ProficiencyLevel
    ): List<String> {
        val questions = when (topic.lowercase()) {
            "family" -> when (userLevel) {
                ProficiencyLevel.BEGINNER -> listOf(
                    "How many people are in your family?",
                    "Do you have brothers or sisters?",
                    "Where does your family live?"
                )
                ProficiencyLevel.INTERMEDIATE -> listOf(
                    "What do you enjoy doing with your family?",
                    "How often do you see your extended family?",
                    "What family traditions do you have?"
                )
                else -> listOf(
                    "How has your relationship with your family evolved over the years?",
                    "What values did your family instill in you?",
                    "How do you balance family time with other commitments?"
                )
            }
            
            "work", "career" -> when (userLevel) {
                ProficiencyLevel.BEGINNER -> listOf(
                    "What is your job?",
                    "Do you like your work?",
                    "What time do you start work?"
                )
                ProficiencyLevel.INTERMEDIATE -> listOf(
                    "What does a typical day at work look like for you?",
                    "What do you enjoy most about your job?",
                    "What challenges do you face at work?"
                )
                else -> listOf(
                    "How do you see your career progressing in the next five years?",
                    "What skills are you developing for future opportunities?",
                    "How do you maintain work-life balance?"
                )
            }
            
            else -> when (userLevel) {
                ProficiencyLevel.BEGINNER -> listOf(
                    "Can you tell me more?",
                    "Why do you think that?",
                    "What happened next?"
                )
                ProficiencyLevel.INTERMEDIATE -> listOf(
                    "How did that make you feel?",
                    "What was the most interesting part?",
                    "Have you experienced something similar before?"
                )
                else -> listOf(
                    "What insights did you gain from that experience?",
                    "How has this shaped your perspective?",
                    "What would you do differently if given the chance?"
                )
            }
        }
        
        return questions
    }
}

/**
 * Extended conversation context
 */
data class ConversationContext(
    val userLevel: ProficiencyLevel,
    val sessionMinutes: Int,
    val recentTopics: List<String>,
    val userInterests: List<String>,
    val conversationHistory: List<ConversationTurn>,
    val energyLevel: EngagementLevel,
    val nativeLanguage: String,
    val correctionStyle: CorrectionStyle,
    val latestUserInput: String
)