package com.studywise.ai.di

import com.studywise.ai.data.service.security.BCryptPasswordHashingService
import com.studywise.ai.data.service.security.PasswordHashMigrator
import com.studywise.ai.domain.service.security.PasswordHashingService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module for security-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {
    
    @Binds
    @Singleton
    abstract fun bindPasswordHashingService(
        bcryptService: BCryptPasswordHashingService
    ): PasswordHashingService
}

@Module
@InstallIn(SingletonComponent::class)
object SecurityProviderModule {
    
    @Provides
    @Singleton
    fun providePasswordHashMigrator(
        hashingService: PasswordHashingService
    ): PasswordHashMigrator {
        return PasswordHashMigrator(hashingService)
    }
}