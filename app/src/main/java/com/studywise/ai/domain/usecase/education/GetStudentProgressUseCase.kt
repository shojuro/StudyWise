package com.studywise.ai.domain.usecase.education

import com.studywise.ai.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for fetching overall student progress
 */
class GetStudentProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(studentId: String): Result<StudentProgress> {
        return try {
            val allProgress = progressRepository.getProgressByStudent(studentId).first()
            val skillsMastered = allProgress.count { it.masteryLevel >= 0.8f }
            val overallCompletion = if (allProgress.isNotEmpty()) {
                allProgress.map { it.masteryLevel }.average().toFloat()
            } else 0f
            
            // Get streak from user stats (simplified)
            val currentStreak = progressRepository.getCurrentStreak(studentId)
            val totalPoints = progressRepository.getTotalPoints(studentId)
            
            Result.success(
                StudentProgress(
                    studentId = studentId,
                    overallCompletion = overallCompletion,
                    skillsMastered = skillsMastered,
                    currentStreak = currentStreak,
                    totalPoints = totalPoints,
                    lastActivityDate = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class StudentProgress(
    val studentId: String,
    val overallCompletion: Float,
    val skillsMastered: Int,
    val currentStreak: Int,
    val totalPoints: Int,
    val lastActivityDate: Long
)