package com.studywise.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.studywise.ai.data.local.entity.StudentSkillMasteryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface StudentSkillMasteryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMastery(mastery: StudentSkillMasteryEntity): Long
    
    @Insert
    suspend fun insertMasteries(masteries: List<StudentSkillMasteryEntity>)
    
    @Update
    suspend fun updateMastery(mastery: StudentSkillMasteryEntity)
    
    @Delete
    suspend fun deleteMastery(mastery: StudentSkillMasteryEntity)
    
    @Query("""
        SELECT * FROM student_skill_mastery 
        WHERE studentId = :studentId 
        AND skillId = :skillId
    """)
    suspend fun getMastery(studentId: String, skillId: Long): StudentSkillMasteryEntity?
    
    @Query("""
        SELECT * FROM student_skill_mastery 
        WHERE studentId = :studentId 
        ORDER BY masteryLevel DESC
    """)
    fun observeStudentMasteries(studentId: String): Flow<List<StudentSkillMasteryEntity>>
    
    @Query("""
        SELECT * FROM student_skill_mastery 
        WHERE studentId = :studentId 
        AND gradeLevel = :gradeLevel
        ORDER BY masteryLevel DESC
    """)
    suspend fun getMasteriesForGrade(
        studentId: String,
        gradeLevel: Int
    ): List<StudentSkillMasteryEntity>
    
    @Query("""
        SELECT * FROM student_skill_mastery 
        WHERE studentId = :studentId 
        AND masteryLevel >= :minMasteryLevel
    """)
    suspend fun getMasteredSkills(
        studentId: String,
        minMasteryLevel: Float = 0.8f
    ): List<StudentSkillMasteryEntity>
    
    @Query("""
        SELECT * FROM student_skill_mastery 
        WHERE studentId = :studentId 
        AND needsReview = 1
        ORDER BY lastPracticed ASC
    """)
    suspend fun getSkillsNeedingReview(studentId: String): List<StudentSkillMasteryEntity>
    
    @Query("""
        UPDATE student_skill_mastery 
        SET totalAttempts = totalAttempts + 1,
            successfulAttempts = successfulAttempts + :wasSuccessful,
            consecutiveSuccesses = CASE 
                WHEN :wasSuccessful = 1 THEN consecutiveSuccesses + 1 
                ELSE 0 
            END,
            lastPracticed = :timestamp,
            totalPracticeTime = totalPracticeTime + :practiceTimeMinutes
        WHERE studentId = :studentId AND skillId = :skillId
    """)
    suspend fun updatePracticeStats(
        studentId: String,
        skillId: Long,
        wasSuccessful: Int,
        timestamp: Date,
        practiceTimeMinutes: Int
    )
    
    @Query("""
        UPDATE student_skill_mastery 
        SET masteryLevel = :masteryLevel,
            accuracyRate = CAST(successfulAttempts AS FLOAT) / totalAttempts,
            currentDifficultyLevel = :difficultyLevel
        WHERE studentId = :studentId AND skillId = :skillId
    """)
    suspend fun updateMasteryLevel(
        studentId: String,
        skillId: Long,
        masteryLevel: Float,
        difficultyLevel: String
    )
    
    @Query("""
        SELECT AVG(masteryLevel) FROM student_skill_mastery 
        WHERE studentId = :studentId 
        AND gradeLevel = :gradeLevel
    """)
    suspend fun getAverageMasteryForGrade(studentId: String, gradeLevel: Int): Float?
    
    @Query("""
        SELECT COUNT(*) FROM student_skill_mastery 
        WHERE studentId = :studentId 
        AND masteryLevel >= :minMasteryLevel
    """)
    suspend fun countMasteredSkills(studentId: String, minMasteryLevel: Float = 0.8f): Int
    
    @Query("""
        SELECT * FROM student_skill_mastery 
        WHERE studentId = :studentId 
        AND isReadyForAssessment = 1
        ORDER BY masteryLevel DESC
    """)
    suspend fun getSkillsReadyForAssessment(studentId: String): List<StudentSkillMasteryEntity>
    
    @Query("DELETE FROM student_skill_mastery WHERE studentId = :studentId")
    suspend fun deleteAllForStudent(studentId: String)
}