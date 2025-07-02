package com.studywise.ai.di

import com.studywise.ai.data.service.MockSpeechAnalysisService
import com.studywise.ai.data.service.MockSpeechToTextService
import com.studywise.ai.data.service.MockVerbalJournalConversationService
import com.studywise.ai.data.service.NetworkStateMonitorImpl
import com.studywise.ai.data.service.SyncManagerImpl
import com.studywise.ai.data.service.SyncTransactionManagerImpl
import com.studywise.ai.domain.service.ConflictResolver
import com.studywise.ai.domain.service.DefaultConflictResolver
import com.studywise.ai.domain.service.DefaultEntityVersionManager
import com.studywise.ai.domain.service.EntityVersionManager
import com.studywise.ai.domain.service.NetworkStateMonitor
import com.studywise.ai.domain.service.SpeechAnalysisService
import com.studywise.ai.domain.service.SpeechToTextService
import com.studywise.ai.domain.service.SyncManager
import com.studywise.ai.domain.service.SyncTransactionManager
import com.studywise.ai.domain.service.VerbalJournalConversationService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    
    companion object {
        @Provides
        @Singleton
        fun provideConflictResolver(): ConflictResolver {
            return DefaultConflictResolver()
        }
        
        @Provides
        @Singleton
        fun provideEntityVersionManager(): EntityVersionManager {
            return DefaultEntityVersionManager()
        }
    }
    
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
    
    @Binds
    @Singleton
    abstract fun bindNetworkStateMonitor(
        networkStateMonitorImpl: NetworkStateMonitorImpl
    ): NetworkStateMonitor
    
    @Binds
    @Singleton
    abstract fun bindSyncManager(
        syncManagerImpl: SyncManagerImpl
    ): SyncManager
    
    @Binds
    @Singleton
    abstract fun bindSyncTransactionManager(
        syncTransactionManagerImpl: SyncTransactionManagerImpl
    ): SyncTransactionManager
}