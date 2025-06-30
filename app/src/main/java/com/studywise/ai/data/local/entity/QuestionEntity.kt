package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.studywise.ai.domain.model.Question
import java.util.Date

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skillId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("skillId"), Index("gradeLevel")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val skillId: Long,
    val gradeLevel: Int,
    val prompt: String, // The Socratic question
    val hints: String, // Pipe-separated list of progressive hints
    val type: String = "practice", // diagnostic, practice, challenge, assessment
    val difficulty: Float = 0.5f, // 0.0 to 1.0
    val correctAnswer: String? = null,
    val explanation: String? = null,
    val options: String? = null, // Pipe-separated list for multiple choice
    val followUpQuestions: String? = null, // Pipe-separated list of follow-up questions
    val skillSubCategory: String? = null, // For more granular categorization
    val metadata: String? = null, // JSON string for additional data
    val isActive: Boolean = true,
    val createdAt: Date = Date()
) {
    fun toDomainModel(): Question {
        return Question(
            id = id.toString(),
            skillId = skillId.toString(),
            gradeLevel = gradeLevel,
            prompt = prompt,
            hints = hints.split("|").filter { it.isNotBlank() },
            type = type,
            difficulty = difficulty,
            correctAnswer = correctAnswer,
            explanation = explanation,
            options = options?.split("|")?.filter { it.isNotBlank() },
            followUpQuestions = followUpQuestions?.split("|")?.filter { it.isNotBlank() },
            skillSubCategory = skillSubCategory,
            metadata = metadata,
            createdAt = createdAt
        )
    }
}