package com.studywise.ai.domain.usecase.education

import com.studywise.ai.data.local.entity.SkillEntity
import com.studywise.ai.data.local.entity.SkillProgressionEntity
import com.studywise.ai.data.local.entity.StudentSkillMasteryEntity
import com.studywise.ai.domain.repository.EducationalContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillProgressionManager @Inject constructor(
    private val educationalContentRepository: EducationalContentRepository
) {
    
    data class SkillProgressionStatus(
        val skill: SkillEntity,
        val mastery: StudentSkillMasteryEntity?,
        val prerequisites: List<SkillEntity>,
        val prerequisiteMastery: Map<Long, Float>,
        val isUnlocked: Boolean,
        val recommendedNext: Boolean,
        val estimatedTimeToMastery: Int // in minutes
    )
    
    data class LearningPath(
        val currentSkills: List<SkillProgressionStatus>,
        val nextRecommendedSkills: List<SkillEntity>,
        val masteredSkills: List<SkillEntity>,
        val overallProgress: Float,
        val currentGradeProgress: Float
    )
    
    /**
     * Get the complete learning path for a student
     */
    suspend fun getStudentLearningPath(
        studentId: String,
        gradeLevel: Int
    ): Result<LearningPath> {
        return try {
            // Get all skills for the grade
            val gradeSkills = educationalContentRepository.getSkillsForGrade(gradeLevel).getOrThrow()
            
            // Get student's progress
            val studentProgress = educationalContentRepository.getStudentProgress(studentId, gradeLevel).getOrThrow()
            val progressMap = studentProgress.associateBy { it.skillId }
            
            // Build skill progression status for each skill
            val skillStatuses = gradeSkills.map { skill ->
                buildSkillProgressionStatus(skill, studentId, progressMap)
            }
            
            // Categorize skills
            val masteredSkills = skillStatuses
                .filter { it.mastery?.masteryLevel ?: 0f >= 0.8f }
                .map { it.skill }
            
            val currentSkills = skillStatuses
                .filter { it.isUnlocked && (it.mastery?.masteryLevel ?: 0f) < 0.8f }
            
            val nextRecommended = educationalContentRepository
                .getRecommendedSkills(studentId, 5)
                .getOrThrow()
            
            // Calculate progress
            val overallProgress = if (gradeSkills.isNotEmpty()) {
                masteredSkills.size.toFloat() / gradeSkills.size
            } else 0f
            
            val currentGradeProgress = calculateGradeProgress(studentProgress)
            
            Result.success(
                LearningPath(
                    currentSkills = currentSkills,
                    nextRecommendedSkills = nextRecommended,
                    masteredSkills = masteredSkills,
                    overallProgress = overallProgress,
                    currentGradeProgress = currentGradeProgress
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get detailed progression status for a specific skill
     */
    suspend fun getSkillProgressionDetails(
        skillId: Long,
        studentId: String
    ): Result<SkillProgressionStatus> {
        return try {
            val skill = educationalContentRepository.getSkillById(skillId).getOrThrow()
            val mastery = educationalContentRepository.getStudentMastery(studentId, skillId).getOrThrow()
            val studentProgress = educationalContentRepository.getStudentProgress(studentId).getOrThrow()
            val progressMap = studentProgress.associateBy { it.skillId }
            
            val status = buildSkillProgressionStatus(skill, studentId, progressMap)
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a student is ready for a specific skill
     */
    suspend fun isStudentReadyForSkill(
        studentId: String,
        skillId: Long
    ): Result<Boolean> {
        return try {
            val progressions = educationalContentRepository.getSkillProgressions(skillId).getOrThrow()
            val prerequisites = educationalContentRepository.getPrerequisiteSkills(skillId).getOrThrow()
            
            if (prerequisites.isEmpty()) {
                return Result.success(true)
            }
            
            val studentProgress = educationalContentRepository.getStudentProgress(studentId).getOrThrow()
            val masteryMap = studentProgress.associateBy { it.skillId }
            
            val isReady = prerequisites.all { prereq ->
                val mastery = masteryMap[prereq.id]
                val requiredMastery = progressions
                    .find { it.prerequisiteSkillId == prereq.id }
                    ?.minimumMasteryLevel ?: 0.7f
                
                (mastery?.masteryLevel ?: 0f) >= requiredMastery
            }
            
            Result.success(isReady)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get next recommended skills based on current progress
     */
    suspend fun getNextRecommendedSkills(
        studentId: String,
        count: Int = 3
    ): Result<List<SkillEntity>> {
        return educationalContentRepository.getRecommendedSkills(studentId, count)
    }
    
    /**
     * Update student's skill mastery after practice
     */
    suspend fun updateSkillMastery(
        studentId: String,
        skillId: Long,
        successRate: Float,
        practiceTimeMinutes: Int
    ): Result<Unit> {
        return try {
            val currentMastery = educationalContentRepository
                .getStudentMastery(studentId, skillId)
                .getOrThrow()
            
            val updatedMastery = if (currentMastery != null) {
                updateExistingMastery(currentMastery, successRate, practiceTimeMinutes)
            } else {
                createNewMastery(studentId, skillId, successRate, practiceTimeMinutes)
            }
            
            educationalContentRepository.updateStudentMastery(updatedMastery)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get skills that need review based on forgetting curve
     */
    suspend fun getSkillsNeedingReview(
        studentId: String,
        limit: Int = 5
    ): Result<List<SkillEntity>> {
        return try {
            val progress = educationalContentRepository.getStudentProgress(studentId).getOrThrow()
            
            val needsReview = progress
                .filter { shouldReviewSkill(it) }
                .sortedBy { it.lastPracticed }
                .take(limit)
            
            val skills = needsReview.mapNotNull { mastery ->
                educationalContentRepository.getSkillById(mastery.skillId).getOrNull()
            }
            
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe student's progress in real-time
     */
    fun observeStudentProgress(studentId: String): Flow<List<StudentSkillMasteryEntity>> {
        return educationalContentRepository.observeStudentProgress(studentId)
    }
    
    /**
     * Observe specific skill mastery
     */
    fun observeSkillMastery(
        studentId: String,
        skillId: Long
    ): Flow<SkillProgressionStatus?> {
        return educationalContentRepository.observeSkillMastery(studentId, skillId)
            .map { mastery ->
                if (mastery != null) {
                    val skill = educationalContentRepository.getSkillById(skillId).getOrNull()
                    skill?.let {
                        buildSkillProgressionStatus(
                            skill = it,
                            studentId = studentId,
                            progressMap = mapOf(skillId to mastery)
                        )
                    }
                } else null
            }
    }
    
    // Helper methods
    
    private suspend fun buildSkillProgressionStatus(
        skill: SkillEntity,
        studentId: String,
        progressMap: Map<Long, StudentSkillMasteryEntity>
    ): SkillProgressionStatus {
        val mastery = progressMap[skill.id]
        val prerequisites = educationalContentRepository.getPrerequisiteSkills(skill.id).getOrThrow()
        
        val prerequisiteMastery = prerequisites.associate { prereq ->
            prereq.id to (progressMap[prereq.id]?.masteryLevel ?: 0f)
        }
        
        val isUnlocked = isStudentReadyForSkill(studentId, skill.id).getOrDefault(false)
        
        val recommendedNext = educationalContentRepository
            .getRecommendedSkills(studentId, 10)
            .getOrThrow()
            .any { it.id == skill.id }
        
        val estimatedTime = estimateTimeToMastery(mastery)
        
        return SkillProgressionStatus(
            skill = skill,
            mastery = mastery,
            prerequisites = prerequisites,
            prerequisiteMastery = prerequisiteMastery,
            isUnlocked = isUnlocked,
            recommendedNext = recommendedNext,
            estimatedTimeToMastery = estimatedTime
        )
    }
    
    private fun calculateGradeProgress(progress: List<StudentSkillMasteryEntity>): Float {
        if (progress.isEmpty()) return 0f
        
        val totalMastery = progress.sumOf { it.masteryLevel.toDouble() }
        return (totalMastery / progress.size).toFloat()
    }
    
    private fun updateExistingMastery(
        currentMastery: StudentSkillMasteryEntity,
        successRate: Float,
        practiceTimeMinutes: Int
    ): StudentSkillMasteryEntity {
        val totalAttempts = currentMastery.totalAttempts + 1
        val successfulAttempts = currentMastery.successfulAttempts + if (successRate >= 0.7f) 1 else 0
        val newAccuracyRate = successfulAttempts.toFloat() / totalAttempts
        
        // Update mastery level using weighted average
        val masteryWeight = 0.7f
        val newMasteryLevel = (currentMastery.masteryLevel * masteryWeight + 
                              successRate * (1 - masteryWeight)).coerceIn(0f, 1f)
        
        // Determine difficulty progression
        val currentDifficulty = currentMastery.currentDifficultyLevel
        val nextDifficulty = when {
            newMasteryLevel >= 0.9f && currentDifficulty == "advanced" -> "mastery"
            newMasteryLevel >= 0.8f && currentDifficulty == "intermediate" -> "advanced"
            newMasteryLevel >= 0.7f && currentDifficulty == "foundation" -> "intermediate"
            else -> currentDifficulty
        }
        
        return currentMastery.copy(
            masteryLevel = newMasteryLevel,
            accuracyRate = newAccuracyRate,
            totalAttempts = totalAttempts,
            successfulAttempts = successfulAttempts,
            consecutiveSuccesses = if (successRate >= 0.7f) currentMastery.consecutiveSuccesses + 1 else 0,
            currentDifficultyLevel = nextDifficulty,
            lastPracticed = java.util.Date(),
            totalPracticeTime = currentMastery.totalPracticeTime + practiceTimeMinutes,
            needsReview = false,
            isReadyForAssessment = newMasteryLevel >= 0.85f
        )
    }
    
    private suspend fun createNewMastery(
        studentId: String,
        skillId: Long,
        successRate: Float,
        practiceTimeMinutes: Int
    ): StudentSkillMasteryEntity {
        val skill = educationalContentRepository.getSkillById(skillId).getOrThrow()
        
        return StudentSkillMasteryEntity(
            studentId = studentId,
            skillId = skillId,
            gradeLevel = 5, // Default - should be determined by context
            masteryLevel = successRate * 0.3f, // Start conservatively
            confidenceScore = successRate,
            accuracyRate = successRate,
            completionRate = 1f,
            totalAttempts = 1,
            successfulAttempts = if (successRate >= 0.7f) 1 else 0,
            consecutiveSuccesses = if (successRate >= 0.7f) 1 else 0,
            averageResponseTime = practiceTimeMinutes * 60,
            currentDifficultyLevel = "foundation",
            firstPracticed = java.util.Date(),
            lastPracticed = java.util.Date(),
            totalPracticeTime = practiceTimeMinutes
        )
    }
    
    private fun shouldReviewSkill(mastery: StudentSkillMasteryEntity): Boolean {
        if (mastery.masteryLevel < 0.8f) return false
        
        val daysSinceLastPractice = mastery.lastPracticed?.let {
            (System.currentTimeMillis() - it.time) / (1000 * 60 * 60 * 24)
        } ?: 0
        
        // Simple forgetting curve - review after certain days based on mastery
        val reviewThreshold = when {
            mastery.masteryLevel >= 0.95f -> 30 // Review after 30 days
            mastery.masteryLevel >= 0.9f -> 21  // Review after 21 days
            mastery.masteryLevel >= 0.85f -> 14 // Review after 14 days
            else -> 7 // Review after 7 days
        }
        
        return daysSinceLastPractice >= reviewThreshold || mastery.needsReview
    }
    
    private fun estimateTimeToMastery(mastery: StudentSkillMasteryEntity?): Int {
        if (mastery == null) return 60 // Default 60 minutes for new skills
        
        val remainingMastery = 0.85f - mastery.masteryLevel
        if (remainingMastery <= 0) return 0
        
        // Estimate based on current progress rate
        val averagePracticeTime = if (mastery.totalAttempts > 0) {
            mastery.totalPracticeTime / mastery.totalAttempts
        } else 15 // Default 15 minutes per session
        
        val estimatedSessionsNeeded = (remainingMastery / 0.1f).toInt() // 0.1 mastery gain per session
        
        return estimatedSessionsNeeded * averagePracticeTime
    }
}