package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val hints: String, // JSON array of progressive hints
    val followUpQuestions: String? = null, // JSON array of follow-up questions
    val skillSubCategory: String? = null // For more granular categorization
)