package com.studywise.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.studywise.ai.data.local.dao.*
import com.studywise.ai.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        SkillEntity::class,
        QuestionEntity::class,
        LearningSessionEntity::class,
        ProgressEntity::class,
        SessionQuestionEntity::class,
        QuestionResponseEntity::class,
        SkillProgressionEntity::class,
        ContentTemplateEntity::class,
        StudentSkillMasteryEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyWiseDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun skillDao(): SkillDao
    abstract fun questionDao(): QuestionDao
    abstract fun learningSessionDao(): LearningSessionDao
    abstract fun progressDao(): ProgressDao
    abstract fun sessionQuestionDao(): SessionQuestionDao
    abstract fun questionResponseDao(): QuestionResponseDao
    abstract fun skillProgressionDao(): SkillProgressionDao
    abstract fun contentTemplateDao(): ContentTemplateDao
    abstract fun studentSkillMasteryDao(): StudentSkillMasteryDao
    
    companion object {
        const val DATABASE_NAME = "studywise_database"
    }
}