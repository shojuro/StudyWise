package com.studywise.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.studywise.ai.data.local.converter.Converters
import com.studywise.ai.data.local.dao.*
import com.studywise.ai.data.local.entity.*
import com.studywise.ai.data.local.entity.privacy.*
import com.studywise.ai.data.local.entity.sync.*
import com.studywise.ai.data.local.entity.verbaljournal.*

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
        StudentSkillMasteryEntity::class,
        // Verbal Journal entities
        VerbalJournalEntryEntity::class,
        ConversationTurnEntity::class,
        SpeechErrorEntity::class,
        SessionAnalysisEntity::class,
        VerbalJournalProfileEntity::class,
        ImprovementAreaEntity::class,
        SessionRecommendationEntity::class,
        DailyJournalPromptEntity::class,
        VerbalJournalAchievementEntity::class,
        SpeechProgressSnapshotEntity::class,
        SyncOperationEntity::class,
        SyncConflictEntity::class,
        // Privacy & Consent entities
        ParentalConsentEntity::class,
        ConsentAuditEntity::class
    ],
    version = 10,
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
    abstract fun verbalJournalDao(): VerbalJournalDao
    abstract fun syncDao(): SyncDao
    abstract fun consentDao(): ConsentDao
    
    companion object {
        const val DATABASE_NAME = "studywise_database"
    }
}