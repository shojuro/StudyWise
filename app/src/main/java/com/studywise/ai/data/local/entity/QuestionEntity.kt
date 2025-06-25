package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
    @PrimaryKey
    val id: String,
    val skillId: String,
    val gradeLevel: Int,
    val prompt: String, // The Socratic question
    val hints: String, // Pipe-separated list of progressive hints
    val type: String, // multiple_choice, true_false, comprehension, reflection
    val difficulty: String, // EASY, MEDIUM, HARD
    val correctAnswer: String? = null,
    val explanation: String? = null,
    val options: String? = null, // Pipe-separated list for multiple choice
    val followUpQuestions: String? = null, // Pipe-separated list of follow-up questions
    val skillSubCategory: String? = null, // For more granular categorization
    val createdAt: Date
)