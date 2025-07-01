package com.studywise.ai.data.service

import com.studywise.ai.domain.model.verbaljournal.*
import com.studywise.ai.domain.service.VerbalJournalConversationService
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockVerbalJournalConversationService @Inject constructor() : VerbalJournalConversationService {
    
    private val greetings = listOf(
        "Hello! It's great to practice English with you today. How are you feeling?",
        "Hi there! Welcome to your verbal journal session. What would you like to talk about?",
        "Good to see you! I'm here to help you practice speaking. How has your day been?"
    )
    
    private val followUpQuestions = mapOf(
        ConversationPhase.WARM_UP to listOf(
            "That's interesting! Can you tell me more about that?",
            "How did that make you feel?",
            "What happened next?"
        ),
        ConversationPhase.MAIN_TOPIC to listOf(
            "Why do you think that is?",
            "Can you give me an example?",
            "How does this compare to your experience?"
        ),
        ConversationPhase.DEEP_DIVE to listOf(
            "What would you do differently?",
            "How has this affected your life?",
            "What are your thoughts on the future of this?"
        )
    )
    
    private val encouragements = listOf(
        "You're doing great! Keep going.",
        "I can see you're improving!",
        "Excellent use of vocabulary!",
        "Your pronunciation is getting better!"
    )
    
    private val hints = listOf(
        "Try to use connecting words like 'because', 'however', or 'therefore'",
        "Don't worry about making mistakes - focus on expressing your ideas",
        "Take a deep breath and speak at your own pace",
        "You can use simpler words if you can't think of the exact word"
    )
    
    override suspend fun generateResponse(
        context: ConversationContext,
        lastUserInput: String
    ): AIResponse {
        delay(Random.nextLong(500, 1500))
        
        val responseText = when (context.currentPhase) {
            ConversationPhase.GREETING -> greetings.random()
            ConversationPhase.WARM_UP -> generateWarmUpResponse(context, lastUserInput)
            ConversationPhase.MAIN_TOPIC -> generateMainTopicResponse(context, lastUserInput)
            ConversationPhase.DEEP_DIVE -> generateDeepDiveResponse(context, lastUserInput)
            ConversationPhase.WRAP_UP -> generateWrapUpResponse(context)
            ConversationPhase.FEEDBACK -> generateFeedbackResponse(context)
        }
        
        return AIResponse(
            text = responseText,
            audioUrl = null, // Mock - no audio generation
            responseType = determineResponseType(context.currentPhase),
            followUpPrompts = getFollowUpPrompts(context),
            encouragementNote = if (Random.nextBoolean()) encouragements.random() else null,
            grammarHint = null,
            vocabularySuggestions = emptyList()
        )
    }
    
    override suspend fun generateHint(
        context: ConversationContext,
        userLevel: ProficiencyLevel
    ): String {
        delay(500)
        return hints.random()
    }
    
    override suspend fun generateRecommendations(
        analysis: SessionAnalysis,
        userProfile: VerbalJournalProfile
    ): List<SessionRecommendation> {
        delay(1000)
        
        return listOf(
            SessionRecommendation(
                id = UUID.randomUUID().toString(),
                analysisId = analysis.id,
                type = RecommendationType.EXERCISE,
                title = "Pronunciation Practice: TH Sounds",
                description = "Practice distinguishing between 'th' sounds in words like 'think' and 'this'",
                reason = "We noticed some difficulty with these sounds in your session",
                priority = Priority.HIGH,
                targetArea = "Pronunciation",
                estimatedDurationMinutes = 10,
                resourceUrl = null,
                exerciseData = ExerciseData(
                    instructions = "Repeat after the audio examples",
                    examples = listOf("think", "this", "through", "that"),
                    targetSkills = listOf("Pronunciation")
                ),
                isCompleted = false,
                completedAt = null,
                createdAt = LocalDateTime.now()
            ),
            SessionRecommendation(
                id = UUID.randomUUID().toString(),
                analysisId = analysis.id,
                type = RecommendationType.TOPIC,
                title = "Daily Routine Vocabulary",
                description = "Practice talking about your daily activities with expanded vocabulary",
                reason = "Build confidence with everyday conversation topics",
                priority = Priority.MEDIUM,
                targetArea = "Vocabulary",
                estimatedDurationMinutes = 15,
                resourceUrl = null,
                exerciseData = null,
                isCompleted = false,
                completedAt = null,
                createdAt = LocalDateTime.now()
            )
        )
    }
    
    override suspend fun evaluateConversationFlow(turns: List<ConversationTurn>): Float {
        // Simple mock evaluation
        return 0.7f + Random.nextFloat() * 0.3f
    }
    
    override suspend fun suggestNextTopic(
        completedTopics: List<String>,
        userInterests: List<String>,
        proficiencyLevel: ProficiencyLevel
    ): String {
        val topics = when (proficiencyLevel) {
            ProficiencyLevel.BEGINNER, ProficiencyLevel.ELEMENTARY -> listOf(
                "Your favorite food",
                "Your family",
                "Your daily routine"
            )
            ProficiencyLevel.INTERMEDIATE, ProficiencyLevel.UPPER_INTERMEDIATE -> listOf(
                "A memorable trip",
                "Technology in daily life",
                "Your career goals"
            )
            else -> listOf(
                "Climate change solutions",
                "The future of education",
                "Cultural differences"
            )
        }
        
        return topics.filterNot { it in completedTopics }.randomOrNull() 
            ?: "Tell me about something interesting that happened recently"
    }
    
    private fun generateWarmUpResponse(context: ConversationContext, lastInput: String): String {
        return followUpQuestions[ConversationPhase.WARM_UP]?.random() 
            ?: "Tell me more about that."
    }
    
    private fun generateMainTopicResponse(context: ConversationContext, lastInput: String): String {
        val questions = followUpQuestions[ConversationPhase.MAIN_TOPIC] ?: emptyList()
        return questions.random()
    }
    
    private fun generateDeepDiveResponse(context: ConversationContext, lastInput: String): String {
        val questions = followUpQuestions[ConversationPhase.DEEP_DIVE] ?: emptyList()
        return questions.random()
    }
    
    private fun generateWrapUpResponse(context: ConversationContext): String {
        return "We've had a great conversation today! Before we finish, is there anything else you'd like to share or any questions you have about what we discussed?"
    }
    
    private fun generateFeedbackResponse(context: ConversationContext): String {
        return "Thank you for practicing with me today! You did really well. Remember to keep practicing regularly. See you next time!"
    }
    
    private fun determineResponseType(phase: ConversationPhase): ResponseType {
        return when (phase) {
            ConversationPhase.GREETING -> ResponseType.GREETING
            ConversationPhase.WARM_UP, ConversationPhase.MAIN_TOPIC, ConversationPhase.DEEP_DIVE -> ResponseType.QUESTION
            ConversationPhase.WRAP_UP -> ResponseType.TOPIC_TRANSITION
            ConversationPhase.FEEDBACK -> ResponseType.FAREWELL
        }
    }
    
    private fun getFollowUpPrompts(context: ConversationContext): List<String> {
        return when (context.currentPhase) {
            ConversationPhase.GREETING -> listOf(
                "How are you today?",
                "What's on your mind?",
                "Tell me about your day"
            )
            else -> emptyList()
        }
    }
}