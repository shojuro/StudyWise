package com.studywise.ai.domain.usecase.education

import com.studywise.ai.data.local.dao.StudentSkillMasteryDao
import com.studywise.ai.data.local.dao.SkillDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for fetching skill mastery data
 */
class GetSkillMasteryUseCase @Inject constructor(
    private val studentSkillMasteryDao: StudentSkillMasteryDao,
    private val skillDao: SkillDao
) {
    suspend operator fun invoke(studentId: String): Result<List<SkillMasteryInfo>> {
        return try {
            val masteryData = studentSkillMasteryDao.observeStudentMasteries(studentId).first()
            val skills = skillDao.getAllSkills()
            
            val skillMasteryInfo = masteryData.map { mastery ->
                val skill = skills.find { it.id == mastery.skillId }
                SkillMasteryInfo(
                    skillId = mastery.skillId,
                    skillName = skill?.name ?: "Unknown Skill",
                    skillCode = skill?.code ?: "",
                    masteryLevel = mastery.masteryLevel,
                    questionsAnswered = mastery.totalAttempts,
                    correctAnswers = mastery.successfulAttempts,
                    lastPracticed = mastery.lastPracticed?.time ?: 0L,
                    recentImprovement = calculateRecentImprovement(mastery)
                )
            }
            
            Result.success(skillMasteryInfo.sortedByDescending { it.masteryLevel })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateRecentImprovement(mastery: com.studywise.ai.data.local.entity.StudentSkillMasteryEntity): Float {
        // In production, this would compare with historical data
        val recentAccuracy = if (mastery.totalAttempts > 0) {
            mastery.successfulAttempts.toFloat() / mastery.totalAttempts
        } else 0f
        
        // Simplified calculation - would track actual improvement over time
        return when {
            recentAccuracy > mastery.accuracyRate -> (recentAccuracy - mastery.accuracyRate).coerceIn(0f, 1f)
            else -> 0f
        }
    }
}

data class SkillMasteryInfo(
    val skillId: Long,
    val skillName: String,
    val skillCode: String,
    val masteryLevel: Float,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val lastPracticed: Long,
    val recentImprovement: Float
)