package com.studywise.ai.di

import com.studywise.ai.data.service.AnalyticsServiceImpl
import com.studywise.ai.domain.service.AnalyticsService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    
    @Binds
    @Singleton
    abstract fun bindAnalyticsService(
        analyticsServiceImpl: AnalyticsServiceImpl
    ): AnalyticsService
}