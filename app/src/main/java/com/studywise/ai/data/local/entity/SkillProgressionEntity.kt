package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "skill_progressions",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skillId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["prerequisiteSkillId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["skillId"]),
        Index(value = ["prerequisiteSkillId"]),
        Index(value = ["gradeLevel"])
    ]
)
data class SkillProgressionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val skillId: Long,
    val prerequisiteSkillId: Long? = null,
    val gradeLevel: Int,
    
    // Progression metadata
    val progressionType: String, // "prerequisite", "corequisite", "follow-up"
    val minimumMasteryLevel: Float = 0.7f, // Required mastery of prerequisite
    val recommendedOrder: Int = 0, // For ordering within grade level
    
    // Cognitive complexity (Bloom's Taxonomy)
    val cognitiveLevel: String, // "remember", "understand", "apply", "analyze", "evaluate", "create"
    
    // Additional guidance
    val transitionGuidance: String? = null, // Tips for moving from prerequisite to this skill
    val scaffoldingSuggestions: String? = null // How to support struggling students
)