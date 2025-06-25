package com.studywise.ai.data.local

import com.studywise.ai.data.local.dao.QuestionDao
import com.studywise.ai.data.local.dao.SkillDao
import com.studywise.ai.data.local.entity.QuestionEntity
import com.studywise.ai.data.local.entity.SkillCategory
import com.studywise.ai.data.local.entity.SkillEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val skillDao: SkillDao,
    private val questionDao: QuestionDao
) {
    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        // Check if database is already initialized
        if (skillDao.getSkillCount() > 0) {
            return@withContext
        }

        // Initialize skills
        val skills = createSkills()
        skillDao.insertSkills(skills)

        // Initialize questions for grades 4-8 (MVP)
        val questions = createQuestions()
        questionDao.insertQuestions(questions)
    }

    private fun createSkills(): List<SkillEntity> {
        val skills = mutableListOf<SkillEntity>()
        var orderIndex = 0

        // Reading Literature Skills
        val literatureSkills = listOf(
            "Character Traits", "Plot Sequence", "Theme Identification", "Setting Impact",
            "Point of View", "Conflict Identification", "Making Inferences", "Character Motivation",
            "Comparing Characters", "Figurative Language", "Mood and Tone", "Symbolism",
            "Foreshadowing", "Dialogue Analysis", "Story Structure", "Making Connections",
            "Character Relationships", "Author's Purpose", "Resolution Analysis", "Literary Devices"
        )

        literatureSkills.forEach { name ->
            skills.add(
                SkillEntity(
                    id = "lit_${name.lowercase().replace(" ", "_")}",
                    category = SkillCategory.READING_LITERATURE,
                    name = name,
                    description = "Analyze and understand $name in literary texts",
                    orderIndex = orderIndex++
                )
            )
        }

        // Add other skill categories similarly...
        // For MVP, we'll focus on Reading Literature skills

        return skills
    }

    private fun createQuestions(): List<QuestionEntity> {
        val questions = mutableListOf<QuestionEntity>()
        val gson = Gson()

        // Character Traits Questions
        val characterTraitsQuestions = listOf(
            QuestionData(
                skillId = "lit_character_traits",
                gradeLevel = 4,
                prompt = "Think about the main character in your story. What three words would you use to describe them? Can you find a part in the story that shows one of these traits?",
                hints = listOf(
                    "Look for what the character says or does",
                    "Think about how other characters react to them",
                    "Check the beginning and see how they've changed"
                )
            ),
            QuestionData(
                skillId = "lit_character_traits",
                gradeLevel = 5,
                prompt = "How would you describe the main character's personality? Find two examples from the story where their actions or words show us what they're really like.",
                hints = listOf(
                    "Actions often reveal more than words",
                    "Look for patterns in their behavior",
                    "Consider their choices in difficult moments"
                )
            ),
            QuestionData(
                skillId = "lit_character_traits",
                gradeLevel = 6,
                prompt = "What character traits define the protagonist? How do their actions, dialogue, and thoughts reveal these traits throughout the story?",
                hints = listOf(
                    "Create a list of traits with evidence",
                    "Look for contradictions in their character",
                    "Consider how traits affect the plot"
                )
            ),
            QuestionData(
                skillId = "lit_character_traits",
                gradeLevel = 7,
                prompt = "Analyze how the author develops the main character's personality. What techniques (actions, dialogue, thoughts, others' reactions) reveal their complex traits?",
                hints = listOf(
                    "Identify direct vs. indirect characterization",
                    "Track character development over time",
                    "Analyze the reliability of different sources"
                )
            ),
            QuestionData(
                skillId = "lit_character_traits",
                gradeLevel = 8,
                prompt = "Examine how the protagonist's character traits evolve throughout the narrative. How do internal and external forces shape their development?",
                hints = listOf(
                    "Map traits at beginning, middle, and end",
                    "Identify catalysts for change",
                    "Analyze the interplay of nature vs. nurture"
                )
            )
        )

        // Theme Identification Questions
        val themeQuestions = listOf(
            QuestionData(
                skillId = "lit_theme_identification",
                gradeLevel = 4,
                prompt = "What lesson do you think the main character learned in this story? How could this lesson help you in your own life?",
                hints = listOf(
                    "Think about what changed for the character",
                    "Look at the story's ending",
                    "Consider the title of your book"
                )
            ),
            QuestionData(
                skillId = "lit_theme_identification",
                gradeLevel = 5,
                prompt = "What message is the author trying to share through this story? Find one part that really shows this message.",
                hints = listOf(
                    "Themes are often about life lessons",
                    "Look for repeated ideas or symbols",
                    "Check character conversations about important topics"
                )
            ),
            QuestionData(
                skillId = "lit_theme_identification",
                gradeLevel = 6,
                prompt = "What universal theme emerges from this story? How do the character's experiences convey this theme?",
                hints = listOf(
                    "Universal themes apply to many people",
                    "Track how the theme develops",
                    "Connect events to the bigger message"
                )
            ),
            QuestionData(
                skillId = "lit_theme_identification",
                gradeLevel = 7,
                prompt = "Identify the central theme and explain how multiple story elements (plot, character, setting) work together to develop it.",
                hints = listOf(
                    "Themes can be complex and layered",
                    "Look for symbolic elements",
                    "Consider conflicting themes"
                )
            ),
            QuestionData(
                skillId = "lit_theme_identification",
                gradeLevel = 8,
                prompt = "Analyze how the author develops complex themes through literary techniques. How do conflicting ideas create thematic tension?",
                hints = listOf(
                    "Identify primary and secondary themes",
                    "Analyze thematic contradictions",
                    "Examine how structure reinforces theme"
                )
            )
        )

        // Convert to entities
        (characterTraitsQuestions + themeQuestions).forEach { data ->
            questions.add(
                QuestionEntity(
                    id = UUID.randomUUID().toString(),
                    skillId = data.skillId,
                    gradeLevel = data.gradeLevel,
                    prompt = data.prompt,
                    hints = data.hints.joinToString("|"),
                    type = "comprehension",
                    difficulty = "MEDIUM",
                    correctAnswer = "Student's thoughtful response",
                    explanation = "Good readers think about what they read and can explain their thinking.",
                    followUpQuestions = listOf(
                        "What made you think of that?",
                        "Can you find another example?",
                        "How does this connect to the story's message?"
                    ).joinToString("|"),
                    createdAt = Date()
                )
            )
        }

        return questions
    }

    private data class QuestionData(
        val skillId: String,
        val gradeLevel: Int,
        val prompt: String,
        val hints: List<String>
    )
}