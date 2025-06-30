package com.studywise.ai.test.fixtures

import com.studywise.ai.data.local.entity.*
import com.studywise.ai.domain.model.Question
import java.util.*

/**
 * Test fixtures for educational content testing
 */
object EducationalContentFixtures {
    
    // Test Skills
    fun createTestSkill(
        id: Long = 1L,
        code: String = "RL.4.1",
        name: String = "Key Ideas and Details",
        description: String = "Refer to details and examples in a text",
        category: SkillCategory = SkillCategory.READING_LITERATURE,
        gradeLevel: Int = 4
    ) = SkillEntity(
        id = id,
        code = code,
        name = name,
        description = description,
        category = category,
        gradeLevel = gradeLevel
    )
    
    fun createTestSkills(): List<SkillEntity> = listOf(
        createTestSkill(1L, "RL.4.1", "Key Ideas and Details", "Refer to details in text", SkillCategory.READING_LITERATURE, 4),
        createTestSkill(2L, "RL.4.2", "Theme Identification", "Determine theme from details", SkillCategory.READING_LITERATURE, 4),
        createTestSkill(3L, "RI.4.1", "Key Ideas in Informational", "Refer to details in informational text", SkillCategory.READING_INFORMATIONAL, 4),
        createTestSkill(4L, "W.4.1", "Opinion Writing", "Write opinion pieces", SkillCategory.WRITING, 4),
        createTestSkill(5L, "L.4.1", "Grammar Conventions", "Demonstrate command of grammar", SkillCategory.LANGUAGE_GRAMMAR, 4)
    )
    
    // Test Content Templates
    fun createTestTemplate(
        id: Long = 1L,
        skillId: Long = 1L,
        gradeLevel: Int = 4,
        promptTemplate: String = "Based on your reading, {question_type} about {topic}?",
        variables: Map<String, List<String>> = mapOf(
            "question_type" to listOf("what did you learn", "what can you infer"),
            "topic" to listOf("the main character", "the setting", "the plot")
        )
    ) = ContentTemplateEntity(
        id = id,
        skillId = skillId,
        gradeLevel = gradeLevel,
        promptTemplate = promptTemplate,
        templateVariables = variables,
        hintTemplates = listOf(
            "Think about {hint_focus} in your text.",
            "Look for clues about {hint_focus}."
        ),
        followUpTemplates = listOf(
            "Can you explain why you think that?",
            "What evidence from the text supports your answer?"
        ),
        metadata = mapOf(
            "cognitive_level" to "understand",
            "text_type" to "narrative"
        ),
        createdAt = Date(),
        lastUsed = Date()
    )
    
    fun createTestTemplates(): List<ContentTemplateEntity> = listOf(
        createTestTemplate(
            id = 1L,
            skillId = 1L,
            gradeLevel = 4,
            promptTemplate = "What details in your text tell you about {element}?",
            variables = mapOf(
                "element" to listOf("the character's feelings", "the setting", "the problem")
            )
        ),
        createTestTemplate(
            id = 2L,
            skillId = 2L,
            gradeLevel = 4,
            promptTemplate = "What do you think is the theme of your story? Use {evidence_type} to support your answer.",
            variables = mapOf(
                "evidence_type" to listOf("specific examples", "character actions", "story events")
            )
        ),
        createTestTemplate(
            id = 3L,
            skillId = 1L,
            gradeLevel = 5,
            promptTemplate = "Analyze how {character_aspect} affects {story_element} in your text.",
            variables = mapOf(
                "character_aspect" to listOf("the character's decisions", "the character's relationships"),
                "story_element" to listOf("the plot development", "the story's outcome")
            )
        )
    )
    
    // Test Questions
    fun createTestQuestion(
        id: String = "q1",
        skillId: String = "1",
        prompt: String = "What details tell you about the main character?",
        difficulty: Float = 0.5f,
        hints: List<String> = listOf("Look for descriptions", "Consider their actions"),
        followUpQuestions: List<String> = listOf("Why do you think that?")
    ) = Question(
        id = id,
        skillId = skillId,
        prompt = prompt,
        difficulty = difficulty,
        hints = hints,
        followUpQuestions = followUpQuestions,
        metadata = mapOf("source" to "template_generated")
    )
    
    // Test Student Mastery
    fun createTestMastery(
        studentId: String = "student123",
        skillId: Long = 1L,
        gradeLevel: Int = 4,
        masteryLevel: Float = 0.6f,
        practiceCount: Int = 10,
        correctCount: Int = 6
    ) = StudentSkillMasteryEntity(
        studentId = studentId,
        skillId = skillId,
        gradeLevel = gradeLevel,
        masteryLevel = masteryLevel,
        confidenceScore = 0.7f,
        practiceCount = practiceCount,
        correctCount = correctCount,
        accuracyRate = correctCount.toFloat() / practiceCount,
        firstPracticed = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000), // 7 days ago
        lastPracticed = Date(),
        questionsAnswered = practiceCount,
        correctAnswers = correctCount,
        incorrectAnswers = practiceCount - correctCount,
        hintsUsed = 3,
        avgResponseTimeMs = 45000,
        streakCount = 3,
        bestStreak = 5
    )
    
    fun createTestMasteryData(): List<StudentSkillMasteryEntity> = listOf(
        createTestMastery("student123", 1L, 4, 0.8f, 20, 16),
        createTestMastery("student123", 2L, 4, 0.6f, 15, 9),
        createTestMastery("student123", 3L, 4, 0.4f, 10, 4),
        createTestMastery("student123", 4L, 4, 0.9f, 25, 23),
        createTestMastery("student123", 5L, 4, 0.3f, 5, 2)
    )
    
    // Test Skill Progressions
    fun createTestProgression(
        fromSkillId: Long = 1L,
        toSkillId: Long = 2L,
        requiredMastery: Float = 0.8f
    ) = SkillProgressionEntity(
        fromSkillId = fromSkillId,
        toSkillId = toSkillId,
        requiredMastery = requiredMastery,
        progressionType = ProgressionType.SEQUENTIAL,
        recommendedGap = 2
    )
    
    fun createTestProgressions(): List<SkillProgressionEntity> = listOf(
        createTestProgression(1L, 2L, 0.8f),
        createTestProgression(2L, 3L, 0.7f),
        createTestProgression(3L, 4L, 0.75f),
        createTestProgression(4L, 5L, 0.8f)
    )
    
    // Mock book text samples
    fun getTestBookTexts() = mapOf(
        "narrative" to """
            Sarah walked through the old garden, her fingers trailing along the weathered fence. 
            The roses had grown wild, their thorns catching at her sleeve. She remembered when 
            her grandmother had planted them, each bush a memory, each bloom a story.
        """.trimIndent(),
        
        "informational" to """
            The water cycle is a continuous process that moves water throughout Earth's systems. 
            Evaporation occurs when water changes from liquid to vapor. This vapor rises into 
            the atmosphere where it cools and condenses to form clouds.
        """.trimIndent(),
        
        "poetry" to """
            The wind whispers secrets through ancient trees,
            While shadows dance on moonlit leaves.
            Night creatures sing their lullabies,
            Beneath the star-filled, endless skies.
        """.trimIndent()
    )
    
    // Test Session Data
    fun createTestSession(
        id: String = "session123",
        userId: String = "student123",
        subject: String = "Reading",
        questionsAnswered: Int = 5,
        correctAnswers: Int = 4,
        pointsEarned: Int = 45
    ) = LearningSessionEntity(
        id = id,
        userId = userId,
        subject = subject,
        bookTitle = "Test Book",
        startedAt = Date(System.currentTimeMillis() - 30 * 60 * 1000), // 30 minutes ago
        completedAt = Date(),
        questionsAnswered = questionsAnswered,
        correctAnswers = correctAnswers,
        pointsEarned = pointsEarned,
        status = SessionStatus.COMPLETED
    )
    
    // Gamification Test Data
    fun createTestAchievement(
        id: String = "first_perfect",
        name: String = "Perfect Start",
        description: String = "Answer your first question correctly"
    ) = com.studywise.ai.domain.gamification.Achievement(
        id = id,
        name = name,
        description = description,
        iconUrl = "achievement_icon",
        rarity = com.studywise.ai.domain.gamification.AchievementRarity.COMMON,
        unlockedAt = Date()
    )
    
    fun createTestLearningEvent(
        userId: String = "student123",
        skillId: String = "RL.4.1",
        isCorrect: Boolean = true,
        responseTimeMs: Long = 30000
    ) = com.studywise.ai.domain.gamification.LearningEvent(
        userId = userId,
        skillId = skillId,
        skillName = "Key Ideas and Details",
        isCorrect = isCorrect,
        accuracy = 0.8f,
        difficulty = 0.5f,
        responseTimeMs = responseTimeMs,
        usedHint = false,
        currentStreak = 3,
        questionsCompleted = 5,
        pointsEarned = 10,
        isFirstCorrect = false,
        choseOwnPath = true,
        skillMasteryLevel = 0.7f
    )
}