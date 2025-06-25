package com.studywise.ai.domain.usecase

import com.studywise.ai.data.local.dao.QuestionDao
import com.studywise.ai.data.local.dao.SkillDao
import com.studywise.ai.domain.model.Question
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val questionDao: QuestionDao,
    private val skillDao: SkillDao
) {
    suspend fun getQuestionsForSkill(
        userId: String,
        skillId: String,
        gradeLevel: Int,
        count: Int = 5
    ): List<Question> {
        // Get unasked questions first
        val questions = questionDao.getUnaskedQuestions(userId, skillId, gradeLevel, count)
        
        // If not enough unasked questions, get any questions for the skill
        val allQuestions = if (questions.size < count) {
            questionDao.getQuestionsBySkillAndGrade(skillId, gradeLevel)
        } else {
            questions
        }

        // Get skill name
        val skill = skillDao.getSkillById(skillId)
        val skillName = skill?.name ?: "Unknown Skill"

        return allQuestions.take(count).map { entity ->
            Question(
                id = entity.id,
                skillId = entity.skillId,
                skillName = skillName,
                gradeLevel = entity.gradeLevel,
                prompt = entity.prompt,
                hints = parseJsonList(entity.hints),
                followUpQuestions = entity.followUpQuestions?.let { parseJsonList(it) } ?: emptyList(),
                skillSubCategory = entity.skillSubCategory
            )
        }
    }

    suspend fun getQuestionsForSubject(
        userId: String,
        subject: String,
        gradeLevel: Int,
        count: Int = 10
    ): List<Question> {
        // For MVP, we'll map subjects to skill categories
        val skillCategories = when (subject.lowercase()) {
            "english" -> listOf(
                "reading_literature",
                "reading_informational",
                "writing",
                "language_grammar",
                "vocabulary_speaking"
            )
            else -> emptyList()
        }

        val allQuestions = mutableListOf<Question>()
        
        // Get questions from each category
        for (category in skillCategories) {
            val skills = skillDao.getSkillsByCategory(
                com.studywise.ai.data.local.entity.SkillCategory.valueOf(category.uppercase())
            )
            
            skills.collect { skillList ->
                for (skill in skillList.take(2)) { // Take 2 skills per category for variety
                    val questions = getQuestionsForSkill(userId, skill.id, gradeLevel, 2)
                    allQuestions.addAll(questions)
                }
            }
        }

        return allQuestions.shuffled().take(count)
    }

    private fun parseJsonList(json: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}