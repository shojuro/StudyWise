package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey
    val id: String,
    val category: SkillCategory,
    val name: String,
    val description: String,
    val orderIndex: Int // For displaying skills in a specific order
)

enum class SkillCategory {
    READING_LITERATURE,
    READING_INFORMATIONAL,
    WRITING,
    LANGUAGE_GRAMMAR,
    VOCABULARY_SPEAKING
}