package com.studywise.ai.di

import com.studywise.ai.data.service.MockSpeechAnalysisService
import com.studywise.ai.data.service.MockSpeechToTextService
import com.studywise.ai.data.service.MockVerbalJournalConversationService
import com.studywise.ai.domain.service.SpeechAnalysisService
import com.studywise.ai.domain.service.SpeechToTextService
import com.studywise.ai.domain.service.VerbalJournalConversationService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    
    @Binds
    @Singleton
    abstract fun bindSpeechToTextService(
        mockSpeechToTextService: MockSpeechToTextService
    ): SpeechToTextService
    
    @Binds
    @Singleton
    abstract fun bindVerbalJournalConversationService(
        mockVerbalJournalConversationService: MockVerbalJournalConversationService
    ): VerbalJournalConversationService
    
    @Binds
    @Singleton
    abstract fun bindSpeechAnalysisService(
        mockSpeechAnalysisService: MockSpeechAnalysisService
    ): SpeechAnalysisService
}