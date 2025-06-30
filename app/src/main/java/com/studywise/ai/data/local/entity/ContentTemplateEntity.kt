package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "content_templates",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skillId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["skillId"]),
        Index(value = ["templateType"]),
        Index(value = ["gradeLevel"])
    ]
)
data class ContentTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val skillId: Long,
    val templateType: String, // "diagnostic", "foundation", "practice", "application", "challenge", "test_aligned", "extension"
    val gradeLevel: Int,
    
    // Template structure
    val templatePattern: String, // The template with variable placeholders
    val variables: String, // JSON array of variable definitions
    val constraints: String? = null, // JSON object of constraints
    
    // Variation metadata
    val difficultyLevel: String, // "low", "medium", "high", "advanced"
    val contextType: String? = null, // "narrative", "informational", "persuasive", "technical", "poetry", "multimedia"
    val scaffoldingLevel: String? = null, // "high_support", "medium_support", "low_support", "independent"
    val timingVariation: String? = null, // "untimed", "generous", "standard", "speed_challenge", "rapid_fire"
    
    // Usage tracking
    val usageCount: Int = 0,
    val effectivenessScore: Float? = null,
    val lastUsed: Long? = null
)