package com.studywise.ai.data.local.entity.verbaljournal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.time.LocalDate

@Entity(
    tableName = "daily_journal_prompts",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["difficulty"]),
        Index(value = ["category"])
    ]
)
data class DailyJournalPromptEntity(
    @PrimaryKey
    val id: String,
    val date: LocalDate,
    val promptText: String,
    val category: String, // PERSONAL, OPINION, STORYTELLING, HYPOTHETICAL, DESCRIPTIVE
    val difficulty: String, // BEGINNER, INTERMEDIATE, ADVANCED
    val followUpQuestions: String, // JSON array
    val vocabularySuggestions: String, // JSON array
    val grammarFocus: String? = null,
    val culturalNotes: String? = null,
    val estimatedDurationMinutes: Int = 5,
    val isActive: Boolean = true
)