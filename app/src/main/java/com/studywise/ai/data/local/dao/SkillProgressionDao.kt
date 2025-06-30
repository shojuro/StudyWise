package com.studywise.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.studywise.ai.data.local.entity.SkillProgressionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillProgressionDao {
    
    @Insert
    suspend fun insertProgression(progression: SkillProgressionEntity): Long
    
    @Insert
    suspend fun insertProgressions(progressions: List<SkillProgressionEntity>)
    
    @Update
    suspend fun updateProgression(progression: SkillProgressionEntity)
    
    @Delete
    suspend fun deleteProgression(progression: SkillProgressionEntity)
    
    @Query("SELECT * FROM skill_progressions WHERE skillId = :skillId")
    suspend fun getProgressionsForSkill(skillId: Long): List<SkillProgressionEntity>
    
    @Query("SELECT * FROM skill_progressions WHERE prerequisiteSkillId = :skillId")
    suspend fun getSkillsWithPrerequisite(skillId: Long): List<SkillProgressionEntity>
    
    @Query("SELECT * FROM skill_progressions WHERE gradeLevel = :gradeLevel ORDER BY recommendedOrder")
    suspend fun getProgressionsForGrade(gradeLevel: Int): List<SkillProgressionEntity>
    
    @Query("""
        SELECT * FROM skill_progressions 
        WHERE gradeLevel = :gradeLevel 
        AND cognitiveLevel = :cognitiveLevel 
        ORDER BY recommendedOrder
    """)
    suspend fun getProgressionsByGradeAndCognitive(
        gradeLevel: Int,
        cognitiveLevel: String
    ): List<SkillProgressionEntity>
    
    @Query("""
        SELECT sp.* FROM skill_progressions sp
        WHERE sp.prerequisiteSkillId IN (
            SELECT ssm.skillId FROM student_skill_mastery ssm
            WHERE ssm.studentId = :studentId 
            AND ssm.masteryLevel >= sp.minimumMasteryLevel
        )
        AND sp.skillId NOT IN (
            SELECT skillId FROM student_skill_mastery 
            WHERE studentId = :studentId
        )
        ORDER BY sp.gradeLevel, sp.recommendedOrder
        LIMIT :limit
    """)
    suspend fun getNextAvailableSkills(studentId: String, limit: Int = 5): List<SkillProgressionEntity>
    
    @Query("SELECT * FROM skill_progressions WHERE progressionType = :type")
    fun observeProgressionsByType(type: String): Flow<List<SkillProgressionEntity>>
    
    @Query("DELETE FROM skill_progressions")
    suspend fun deleteAllProgressions()
}