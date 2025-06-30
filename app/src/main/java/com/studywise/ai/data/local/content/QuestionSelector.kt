package com.studywise.ai.data.local.content

import com.studywise.ai.data.local.dao.QuestionDao
import com.studywise.ai.data.local.dao.StudentSkillMasteryDao
import com.studywise.ai.data.local.entity.QuestionEntity
import com.studywise.ai.data.local.entity.SkillEntity
import com.studywise.ai.domain.model.Question
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Intelligent question selection based on student performance and skill progression
 */
@Singleton
class QuestionSelector @Inject constructor(
    private val questionDao: QuestionDao,
    private val studentSkillMasteryDao: StudentSkillMasteryDao
) {
    
    /**
     * Select optimal questions for a skill based on student mastery
     */
    suspend fun selectQuestionsForSkill(
        studentId: String,
        skill: SkillEntity,
        count: Int = 5,
        sessionType: SessionType = SessionType.PRACTICE
    ): List<Question> {
        // Get student's current mastery level
        val mastery = studentSkillMasteryDao.getMastery(studentId, skill.id)
        val masteryLevel = mastery?.masteryLevel ?: 0f
        
        // Get all available questions for this skill
        val availableQuestions = questionDao.getQuestionsBySkill(skill.id)
        
        // Select questions based on session type and mastery
        return when (sessionType) {
            SessionType.DIAGNOSTIC -> selectDiagnosticQuestions(availableQuestions, count)
            SessionType.PRACTICE -> selectPracticeQuestions(availableQuestions, masteryLevel, count)
            SessionType.CHALLENGE -> selectChallengeQuestions(availableQuestions, count)
            SessionType.REVIEW -> selectReviewQuestions(availableQuestions, masteryLevel, count)
        }
    }
    
    /**
     * Select questions for assessment (covers range of difficulties)
     */
    private fun selectDiagnosticQuestions(
        questions: List<QuestionEntity>,
        count: Int
    ): List<Question> {
        val sorted = questions.sortedBy { it.difficulty }
        val selected = mutableListOf<QuestionEntity>()
        
        // Select questions across difficulty spectrum
        val step = questions.size / count.coerceAtLeast(1)
        for (i in 0 until count.coerceAtMost(questions.size)) {
            val index = (i * step).coerceAtMost(questions.size - 1)
            selected.add(sorted[index])
        }
        
        return selected.map { it.toDomainModel() }
    }
    
    /**
     * Select practice questions based on current mastery
     */
    private fun selectPracticeQuestions(
        questions: List<QuestionEntity>,
        masteryLevel: Float,
        count: Int
    ): List<Question> {
        // Target difficulty range based on mastery
        val targetDifficulty = when {
            masteryLevel < 0.3f -> 0.2f to 0.4f
            masteryLevel < 0.6f -> 0.4f to 0.6f
            masteryLevel < 0.8f -> 0.5f to 0.8f
            else -> 0.6f to 0.9f
        }
        
        // Filter questions in target range
        val targetQuestions = questions.filter { 
            it.difficulty in targetDifficulty.first..targetDifficulty.second 
        }
        
        // If not enough in range, expand range
        val selectedQuestions = if (targetQuestions.size >= count) {
            targetQuestions.shuffled().take(count)
        } else {
            val additional = questions
                .filter { it !in targetQuestions }
                .sortedBy { kotlin.math.abs(it.difficulty - masteryLevel) }
                .take(count - targetQuestions.size)
            (targetQuestions + additional).shuffled()
        }
        
        return selectedQuestions.map { it.toDomainModel() }
    }
    
    /**
     * Select challenging questions for advanced practice
     */
    private fun selectChallengeQuestions(
        questions: List<QuestionEntity>,
        count: Int
    ): List<Question> {
        return questions
            .filter { it.difficulty >= 0.7f }
            .shuffled()
            .take(count)
            .map { it.toDomainModel() }
    }
    
    /**
     * Select review questions focusing on previously struggled areas
     */
    private fun selectReviewQuestions(
        questions: List<QuestionEntity>,
        masteryLevel: Float,
        count: Int
    ): List<Question> {
        // For review, select slightly easier questions than current mastery
        val targetDifficulty = (masteryLevel - 0.2f).coerceAtLeast(0.1f)
        
        return questions
            .sortedBy { kotlin.math.abs(it.difficulty - targetDifficulty) }
            .take(count)
            .map { it.toDomainModel() }
    }
    
    enum class SessionType {
        DIAGNOSTIC,
        PRACTICE,
        CHALLENGE,
        REVIEW
    }
    
    /**
     * Convenience method for selecting questions
     */
    suspend fun selectQuestions(
        skill: SkillEntity,
        gradeLevel: Int,
        sessionType: String,
        count: Int,
        studentId: String
    ): Result<List<Question>> {
        return try {
            val type = when (sessionType.lowercase()) {
                "diagnostic" -> SessionType.DIAGNOSTIC
                "challenge" -> SessionType.CHALLENGE
                "review" -> SessionType.REVIEW
                else -> SessionType.PRACTICE
            }
            val questions = selectQuestionsForSkill(studentId, skill, count, type)
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension function to convert entity to domain model
private fun QuestionEntity.toDomainModel(): Question {
    return Question(
        id = this.id.toString(),
        skillId = this.skillId.toString(),
        prompt = this.prompt,
        difficulty = this.difficulty,
        gradeLevel = this.gradeLevel,
        hints = this.hints?.split("|") ?: emptyList(),
        followUpQuestions = this.followUpQuestions?.split("|") ?: emptyList(),
        metadata = this.metadata
    )
}