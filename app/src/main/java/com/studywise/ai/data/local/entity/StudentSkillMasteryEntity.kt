package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "student_skill_mastery",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skillId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId", "skillId"], unique = true),
        Index(value = ["skillId"]),
        Index(value = ["masteryLevel"]),
        Index(value = ["lastPracticed"])
    ]
)
data class StudentSkillMasteryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val studentId: String,
    val skillId: Long,
    val gradeLevel: Int,
    
    // Mastery metrics
    val masteryLevel: Float = 0.0f, // 0.0 to 1.0
    val confidenceScore: Float = 0.0f, // Student's self-reported confidence
    val accuracyRate: Float = 0.0f, // Correct responses / total attempts
    val completionRate: Float = 0.0f, // Completed prompts / assigned prompts
    
    // Practice statistics
    val totalAttempts: Int = 0,
    val successfulAttempts: Int = 0,
    val consecutiveSuccesses: Int = 0,
    val averageResponseTime: Int? = null, // in seconds
    
    // Progression tracking
    val currentDifficultyLevel: String = "foundation", // "foundation", "intermediate", "advanced", "mastery"
    val recommendedNextLevel: String? = null,
    val isReadyForAssessment: Boolean = false,
    
    // Time tracking
    val firstPracticed: Date? = null,
    val lastPracticed: Date? = null,
    val totalPracticeTime: Int = 0, // in minutes
    
    // Learning patterns
    val preferredScaffoldingLevel: String? = null,
    val bestPerformanceTimeOfDay: String? = null, // "morning", "afternoon", "evening"
    val strugglingAreas: String? = null, // JSON array of specific sub-skills
    
    // Adaptive learning
    val needsReview: Boolean = false,
    val lastReviewDate: Date? = null,
    val reviewCount: Int = 0
)