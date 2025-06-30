package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String, // Unique identifier like "character_traits"
    val category: SkillCategory,
    val name: String,
    val description: String,
    val orderIndex: Int = 0, // For displaying skills in a specific order
    val iconUrl: String? = null,
    val isActive: Boolean = true
)

enum class SkillCategory {
    READING_LITERATURE,
    READING_INFORMATIONAL,
    WRITING,
    LANGUAGE_GRAMMAR,
    VOCABULARY_SPEAKING
}