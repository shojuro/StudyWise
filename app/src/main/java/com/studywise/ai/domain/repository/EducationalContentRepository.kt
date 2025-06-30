package com.studywise.ai.domain.repository

import com.studywise.ai.data.local.content.EducationalContentData
import com.studywise.ai.data.local.entity.ContentTemplateEntity
import com.studywise.ai.data.local.entity.QuestionEntity
import com.studywise.ai.data.local.entity.SkillEntity
import com.studywise.ai.data.local.entity.SkillProgressionEntity
import com.studywise.ai.data.local.entity.StudentSkillMasteryEntity
import com.studywise.ai.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface EducationalContentRepository {
    
    // Skill Management
    suspend fun getAllSkills(): Result<List<SkillEntity>>
    suspend fun getSkillsByCategory(category: String): Result<List<SkillEntity>>
    suspend fun getSkillById(skillId: Long): Result<SkillEntity>
    suspend fun getSkillByCode(skillCode: String): Result<SkillEntity>
    
    // Skill Progressions
    suspend fun getSkillProgressions(skillId: Long): Result<List<SkillProgressionEntity>>
    suspend fun getPrerequisiteSkills(skillId: Long): Result<List<SkillEntity>>
    suspend fun getNextSkills(skillId: Long, studentId: String): Result<List<SkillEntity>>
    suspend fun getSkillsForGrade(gradeLevel: Int): Result<List<SkillEntity>>
    
    // Question Management
    suspend fun getQuestionsForSkill(skillId: Long, gradeLevel: Int): Result<List<Question>>
    suspend fun getQuestionById(questionId: Long): Result<Question>
    suspend fun generateQuestionsFromTemplates(
        skillId: Long,
        gradeLevel: Int,
        count: Int,
        difficultyLevel: String? = null
    ): Result<List<Question>>
    
    // Content Templates
    suspend fun getContentTemplates(
        skillId: Long,
        gradeLevel: Int,
        templateType: String? = null
    ): Result<List<ContentTemplateEntity>>
    suspend fun createContentFromTemplate(
        templateId: Long,
        variables: Map<String, String>
    ): Result<Question>
    
    // Student Progress
    suspend fun getStudentMastery(studentId: String, skillId: Long): Result<StudentSkillMasteryEntity?>
    suspend fun updateStudentMastery(mastery: StudentSkillMasteryEntity): Result<Unit>
    suspend fun getStudentProgress(studentId: String, gradeLevel: Int? = null): Result<List<StudentSkillMasteryEntity>>
    suspend fun getRecommendedSkills(studentId: String, limit: Int = 5): Result<List<SkillEntity>>
    
    // Content Data Access
    suspend fun getSkillPrompt(skillCode: String, gradeLevel: Int): Result<String>
    suspend fun getAllPromptsForGrade(gradeLevel: Int): Result<Map<String, String>>
    suspend fun getSkillDescription(skillCode: String): Result<String>
    
    // Analytics
    suspend fun trackQuestionAttempt(
        studentId: String,
        questionId: Long,
        wasCorrect: Boolean,
        responseTime: Int
    ): Result<Unit>
    suspend fun getSkillEffectiveness(skillId: Long): Result<Float>
    suspend fun getMostEffectiveTemplates(limit: Int = 10): Result<List<ContentTemplateEntity>>
    
    // Initialization
    suspend fun initializeEducationalContent(): Result<Unit>
    suspend fun isContentInitialized(): Result<Boolean>
    
    // Observables
    fun observeStudentProgress(studentId: String): Flow<List<StudentSkillMasteryEntity>>
    fun observeSkillMastery(studentId: String, skillId: Long): Flow<StudentSkillMasteryEntity?>
}