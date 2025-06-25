package com.studywise.ai.di

import com.studywise.ai.data.repository.AuthRepositoryImpl
import com.studywise.ai.data.repository.QuestionRepositoryImpl
import com.studywise.ai.domain.repository.AuthRepository
import com.studywise.ai.domain.repository.QuestionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(
        questionRepositoryImpl: QuestionRepositoryImpl
    ): QuestionRepository
}