package com.studywise.ai.di

import com.studywise.ai.data.repository.AIRepositoryImpl
import com.studywise.ai.data.repository.AuthRepositoryImpl
import com.studywise.ai.data.repository.HybridAIRepository
import com.studywise.ai.data.repository.ProgressRepositoryImpl
import com.studywise.ai.data.repository.QuestionRepositoryImpl
import com.studywise.ai.data.repository.SchoolRepositoryImpl
import com.studywise.ai.data.repository.UserRepositoryImpl
import com.studywise.ai.domain.repository.AIRepository
import com.studywise.ai.domain.repository.AuthRepository
import com.studywise.ai.domain.repository.ProgressRepository
import com.studywise.ai.domain.repository.QuestionRepository
import com.studywise.ai.domain.repository.SchoolRepository
import com.studywise.ai.domain.repository.UserRepository
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

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindSchoolRepository(
        schoolRepositoryImpl: SchoolRepositoryImpl
    ): SchoolRepository

    @Binds
    @Singleton
    abstract fun bindAIRepository(
        hybridAIRepository: HybridAIRepository
    ): AIRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        progressRepositoryImpl: ProgressRepositoryImpl
    ): ProgressRepository
}