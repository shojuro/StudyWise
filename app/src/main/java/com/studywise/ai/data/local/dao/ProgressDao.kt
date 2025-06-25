package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Update
    suspend fun updateProgress(progress: ProgressEntity)

    @Query("SELECT * FROM progress WHERE userId = :userId AND skillId = :skillId")
    suspend fun getProgress(userId: String, skillId: String): ProgressEntity?

    @Query("SELECT * FROM progress WHERE userId = :userId")
    fun getUserProgress(userId: String): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE userId = :userId AND gradeLevel = :gradeLevel")
    fun getUserProgressByGrade(userId: String, gradeLevel: Int): Flow<List<ProgressEntity>>

    @Query("""
        SELECT p.*, s.name as skillName, s.category 
        FROM progress p 
        INNER JOIN skills s ON p.skillId = s.id 
        WHERE p.userId = :userId 
        ORDER BY p.masteryLevel DESC 
        LIMIT :limit
    """)
    fun getTopMasteredSkills(userId: String, limit: Int): Flow<List<ProgressEntity>>

    @Query("""
        SELECT p.*, s.name as skillName, s.category 
        FROM progress p 
        INNER JOIN skills s ON p.skillId = s.id 
        WHERE p.userId = :userId 
        AND p.masteryLevel < 0.7
        ORDER BY p.lastPracticedAt ASC 
        LIMIT :limit
    """)
    fun getSkillsNeedingPractice(userId: String, limit: Int): Flow<List<ProgressEntity>>

    @Query("SELECT AVG(masteryLevel) FROM progress WHERE userId = :userId")
    suspend fun getAverageMastery(userId: String): Float?

    @Query("SELECT SUM(totalPointsEarned) FROM progress WHERE userId = :userId")
    suspend fun getTotalPoints(userId: String): Int?

    @Query("UPDATE progress SET streakDays = 0 WHERE userId = :userId")
    suspend fun resetAllStreaks(userId: String)
}