package com.studywise.ai.di

import android.content.Context
import androidx.room.Room
import com.studywise.ai.data.local.dao.*
import com.studywise.ai.data.local.database.StudyWiseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStudyWiseDatabase(
        @ApplicationContext context: Context
    ): StudyWiseDatabase {
        return Room.databaseBuilder(
            context,
            StudyWiseDatabase::class.java,
            StudyWiseDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // For development only
        .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: StudyWiseDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideSkillDao(database: StudyWiseDatabase): SkillDao {
        return database.skillDao()
    }

    @Provides
    @Singleton
    fun provideQuestionDao(database: StudyWiseDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun provideLearningSessionDao(database: StudyWiseDatabase): LearningSessionDao {
        return database.learningSessionDao()
    }

    @Provides
    @Singleton
    fun provideProgressDao(database: StudyWiseDatabase): ProgressDao {
        return database.progressDao()
    }

    @Provides
    @Singleton
    fun provideSessionQuestionDao(database: StudyWiseDatabase): SessionQuestionDao {
        return database.sessionQuestionDao()
    }
    
    @Provides
    @Singleton
    fun provideQuestionResponseDao(database: StudyWiseDatabase): QuestionResponseDao {
        return database.questionResponseDao()
    }
}