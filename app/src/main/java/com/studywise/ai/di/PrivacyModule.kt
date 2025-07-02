package com.studywise.ai.di

import com.studywise.ai.data.local.dao.ConsentDao
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.remote.api.StudyWiseApi
import com.studywise.ai.data.service.privacy.ConsentManagerImpl
import com.studywise.ai.domain.service.privacy.ConsentManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module for privacy and security dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object PrivacyModule {
    
    @Provides
    @Singleton
    fun provideConsentManager(
        consentDao: ConsentDao,
        userDao: UserDao,
        api: StudyWiseApi
    ): ConsentManager {
        return ConsentManagerImpl(consentDao, userDao, api)
    }
}