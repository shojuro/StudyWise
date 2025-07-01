package com.studywise.ai.domain.service

import com.studywise.ai.domain.model.verbaljournal.*

interface VerbalJournalConversationService {
    suspend fun generateResponse(
        context: ConversationContext,
        lastUserInput: String
    ): AIResponse
    
    suspend fun generateHint(
        context: ConversationContext,
        userLevel: ProficiencyLevel
    ): String
    
    suspend fun generateRecommendations(
        analysis: SessionAnalysis,
        userProfile: VerbalJournalProfile
    ): List<SessionRecommendation>
    
    suspend fun evaluateConversationFlow(
        turns: List<ConversationTurn>
    ): Float
    
    suspend fun suggestNextTopic(
        completedTopics: List<String>,
        userInterests: List<String>,
        proficiencyLevel: ProficiencyLevel
    ): String
}