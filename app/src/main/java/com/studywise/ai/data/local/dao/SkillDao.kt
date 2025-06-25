package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.SkillCategory
import com.studywise.ai.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Query("SELECT * FROM skills WHERE id = :skillId")
    suspend fun getSkillById(skillId: String): SkillEntity?

    @Query("SELECT * FROM skills WHERE category = :category ORDER BY orderIndex")
    fun getSkillsByCategory(category: SkillCategory): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills ORDER BY category, orderIndex")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT COUNT(*) FROM skills")
    suspend fun getSkillCount(): Int

    @Query("DELETE FROM skills")
    suspend fun deleteAllSkills()
}