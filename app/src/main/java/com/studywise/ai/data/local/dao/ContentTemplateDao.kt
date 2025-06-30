package com.studywise.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.studywise.ai.data.local.entity.ContentTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentTemplateDao {
    
    @Insert
    suspend fun insertTemplate(template: ContentTemplateEntity): Long
    
    @Insert
    suspend fun insertTemplates(templates: List<ContentTemplateEntity>)
    
    @Update
    suspend fun updateTemplate(template: ContentTemplateEntity)
    
    @Delete
    suspend fun deleteTemplate(template: ContentTemplateEntity)
    
    @Query("""
        SELECT * FROM content_templates 
        WHERE skillId = :skillId 
        AND gradeLevel = :gradeLevel
        ORDER BY templateType, difficultyLevel
    """)
    suspend fun getTemplatesForSkillAndGrade(
        skillId: Long,
        gradeLevel: Int
    ): List<ContentTemplateEntity>
    
    @Query("""
        SELECT * FROM content_templates 
        WHERE skillId = :skillId 
        AND gradeLevel = :gradeLevel
        AND templateType = :templateType
    """)
    suspend fun getTemplatesByType(
        skillId: Long,
        gradeLevel: Int,
        templateType: String
    ): List<ContentTemplateEntity>
    
    @Query("""
        SELECT * FROM content_templates 
        WHERE skillId = :skillId 
        AND gradeLevel = :gradeLevel
        AND difficultyLevel = :difficultyLevel
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomTemplates(
        skillId: Long,
        gradeLevel: Int,
        difficultyLevel: String,
        limit: Int
    ): List<ContentTemplateEntity>
    
    @Query("""
        UPDATE content_templates 
        SET usageCount = usageCount + 1,
            lastUsed = :timestamp
        WHERE id = :templateId
    """)
    suspend fun incrementUsage(templateId: Long, timestamp: Long)
    
    @Query("""
        UPDATE content_templates 
        SET effectivenessScore = :score
        WHERE id = :templateId
    """)
    suspend fun updateEffectivenessScore(templateId: Long, score: Float)
    
    @Query("""
        SELECT * FROM content_templates 
        WHERE effectivenessScore >= :minScore
        ORDER BY effectivenessScore DESC
        LIMIT :limit
    """)
    suspend fun getTopEffectiveTemplates(minScore: Float, limit: Int): List<ContentTemplateEntity>
    
    @Query("""
        SELECT * FROM content_templates 
        WHERE contextType = :contextType
        AND gradeLevel = :gradeLevel
    """)
    fun observeTemplatesByContext(
        contextType: String,
        gradeLevel: Int
    ): Flow<List<ContentTemplateEntity>>
    
    @Query("SELECT * FROM content_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Long): ContentTemplateEntity?
    
    @Query("SELECT * FROM content_templates")
    suspend fun getAllTemplates(): List<ContentTemplateEntity>
    
    @Query("DELETE FROM content_templates")
    suspend fun deleteAllTemplates()
}