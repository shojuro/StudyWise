package com.studywise.ai.di

import android.content.Context
import com.google.gson.Gson
import com.studywise.ai.data.local.preferences.EncryptedPreferencesManager
import com.studywise.ai.data.service.security.*
import com.studywise.ai.domain.service.security.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    
    @Binds
    @Singleton
    abstract fun bindTokenEncryption(
        androidKeystoreTokenEncryption: AndroidKeystoreTokenEncryption
    ): TokenEncryption
    
    @Binds
    @Singleton
    abstract fun bindTokenValidator(
        simpleTokenValidator: SimpleTokenValidator
    ): TokenValidator
    
    @Binds
    @Singleton
    abstract fun bindTokenManager(
        secureTokenManager: SecureTokenManager
    ): TokenManager
    
    @Binds
    @Singleton
    abstract fun bindSessionManager(
        secureSessionManager: SecureSessionManager
    ): SessionManager
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
    
    @Provides
    @Singleton
    fun provideSecureTokenStorage(
        tokenEncryption: TokenEncryption,
        encryptedPreferencesManager: EncryptedPreferencesManager
    ): SecureTokenStorage {
        return SecureTokenStorage(tokenEncryption, encryptedPreferencesManager)
    }
    
    @Provides
    @Singleton
    fun provideSimpleTokenGenerator(
        gson: Gson
    ): SimpleTokenGenerator {
        return SimpleTokenGenerator(gson)
    }
}