package com.studywise.ai.data.repository

import com.studywise.ai.data.local.dao.QuestionDao
import com.studywise.ai.data.local.entity.QuestionEntity
import com.studywise.ai.domain.model.Question
import com.studywise.ai.domain.model.QuestionDifficulty
import com.studywise.ai.domain.repository.QuestionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao
) : QuestionRepository {

    override suspend fun generateQuestion(
        subject: String,
        gradeLevel: Int,
        difficulty: QuestionDifficulty,
        bookContext: String
    ): Result<Question> = withContext(Dispatchers.IO) {
        try {
            // For MVP, generate questions based on templates
            val question = when (subject.lowercase()) {
                "english" -> generateEnglishQuestion(gradeLevel, difficulty, bookContext)
                "mathematics" -> generateMathQuestion(gradeLevel, difficulty)
                "science" -> generateScienceQuestion(gradeLevel, difficulty)
                "history" -> generateHistoryQuestion(gradeLevel, difficulty)
                else -> generateGeneralQuestion(subject, gradeLevel, difficulty)
            }
            
            // Save to database
            saveQuestionToDatabase(question)
            
            Result.success(question)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateEnglishQuestion(
        gradeLevel: Int,
        difficulty: QuestionDifficulty,
        bookContext: String
    ): Question {
        val questionTemplates = when (difficulty) {
            QuestionDifficulty.EASY -> listOf(
                "What is the main idea of this text?",
                "Who is the main character in this passage?",
                "What happened first in the story?"
            )
            QuestionDifficulty.MEDIUM -> listOf(
                "Why do you think the author wrote this?",
                "What does this word mean in the context of the passage?",
                "How does the character feel and why?"
            )
            QuestionDifficulty.HARD -> listOf(
                "What theme is the author exploring in this text?",
                "How does the author use language to create mood?",
                "Compare this text to something you've read before. How are they similar?"
            )
        }

        return Question(
            id = UUID.randomUUID().toString(),
            skillId = "reading_comprehension",
            gradeLevel = gradeLevel,
            prompt = questionTemplates.random(),
            type = "comprehension",
            difficulty = when(difficulty) {
                QuestionDifficulty.EASY -> 0.3f
                QuestionDifficulty.MEDIUM -> 0.5f
                QuestionDifficulty.HARD -> 0.7f
            },
            hints = listOf(
                "Look for repeated ideas or words",
                "Think about the author's purpose",
                "Consider the context"
            ),
            correctAnswer = "Student's thoughtful interpretation",
            explanation = "Good readers think deeply about what they read and can support their ideas with evidence from the text."
        )
    }

    private fun generateMathQuestion(
        gradeLevel: Int,
        difficulty: QuestionDifficulty
    ): Question {
        return when (gradeLevel) {
            in 4..5 -> generateElementaryMathQuestion(gradeLevel, difficulty)
            in 6..8 -> generateMiddleSchoolMathQuestion(gradeLevel, difficulty)
            else -> generateHighSchoolMathQuestion(gradeLevel, difficulty)
        }
    }

    private fun generateElementaryMathQuestion(gradeLevel: Int, difficulty: QuestionDifficulty): Question {
        val (num1, num2, operation) = when (difficulty) {
            QuestionDifficulty.EASY -> {
                val n1 = (1..10).random()
                val n2 = (1..10).random()
                Triple(n1, n2, "+")
            }
            QuestionDifficulty.MEDIUM -> {
                val n1 = (10..50).random()
                val n2 = (1..20).random()
                Triple(n1, n2, "-")
            }
            QuestionDifficulty.HARD -> {
                val n1 = (2..12).random()
                val n2 = (2..12).random()
                Triple(n1, n2, "×")
            }
        }
        
        val answer = when (operation) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "×" -> num1 * num2
            else -> 0
        }

        return Question(
            id = UUID.randomUUID().toString(),
            skillId = "basic_arithmetic",
            gradeLevel = gradeLevel,
            prompt = "What is $num1 $operation $num2?",
            type = "calculation",
            difficulty = when(difficulty) {
                QuestionDifficulty.EASY -> 0.3f
                QuestionDifficulty.MEDIUM -> 0.5f
                QuestionDifficulty.HARD -> 0.7f
            },
            hints = listOf(
                when (operation) {
                    "+" -> "Try counting up from $num1"
                    "-" -> "Start at $num1 and count backwards"
                    else -> "Think of it as adding $num1 together $num2 times"
                }
            ),
            correctAnswer = answer.toString(),
            explanation = "The answer is $answer because $num1 $operation $num2 = $answer"
        )
    }

    private fun generateMiddleSchoolMathQuestion(gradeLevel: Int, difficulty: QuestionDifficulty): Question {
        // Simplified for MVP
        return generateElementaryMathQuestion(gradeLevel, difficulty)
    }

    private fun generateHighSchoolMathQuestion(gradeLevel: Int, difficulty: QuestionDifficulty): Question {
        // Simplified for MVP
        return generateElementaryMathQuestion(gradeLevel, difficulty)
    }

    private fun generateScienceQuestion(gradeLevel: Int, difficulty: QuestionDifficulty): Question {
        val topics = listOf(
            "Why do we see the moon at night?",
            "What makes plants grow?",
            "How does water turn into ice?",
            "Why do we need to eat food?",
            "What causes the seasons to change?"
        )

        return Question(
            id = UUID.randomUUID().toString(),
            skillId = "science_comprehension",
            gradeLevel = gradeLevel,
            prompt = topics.random(),
            type = "comprehension",
            difficulty = when(difficulty) {
                QuestionDifficulty.EASY -> 0.3f
                QuestionDifficulty.MEDIUM -> 0.5f
                QuestionDifficulty.HARD -> 0.7f
            },
            hints = listOf(
                "Think about what you observe in nature",
                "Consider cause and effect",
                "Use what you know from your experience"
            ),
            correctAnswer = "Varies based on scientific understanding",
            explanation = "Science helps us understand the world around us through observation and reasoning."
        )
    }

    private fun generateHistoryQuestion(gradeLevel: Int, difficulty: QuestionDifficulty): Question {
        return Question(
            id = UUID.randomUUID().toString(),
            skillId = "history_reflection",
            gradeLevel = gradeLevel,
            prompt = "Why is it important to learn about history?",
            type = "reflection",
            difficulty = when(difficulty) {
                QuestionDifficulty.EASY -> 0.3f
                QuestionDifficulty.MEDIUM -> 0.5f
                QuestionDifficulty.HARD -> 0.7f
            },
            hints = listOf(
                "Think about how the past affects the present",
                "Consider what we can learn from others' experiences"
            ),
            correctAnswer = "Student's thoughtful response",
            explanation = "History helps us understand how we got here and make better decisions for the future."
        )
    }

    private fun generateGeneralQuestion(
        subject: String,
        gradeLevel: Int,
        difficulty: QuestionDifficulty
    ): Question {
        return Question(
            id = UUID.randomUUID().toString(),
            skillId = "general_reflection",
            gradeLevel = gradeLevel,
            prompt = "What interests you most about $subject?",
            type = "reflection",
            difficulty = when(difficulty) {
                QuestionDifficulty.EASY -> 0.3f
                QuestionDifficulty.MEDIUM -> 0.5f
                QuestionDifficulty.HARD -> 0.7f
            },
            hints = listOf(
                "Think about what you enjoy learning",
                "Consider how this subject relates to your life"
            ),
            correctAnswer = "Student's personal reflection",
            explanation = "Learning is most effective when we connect it to our interests and experiences."
        )
    }

    private suspend fun saveQuestionToDatabase(question: Question) {
        val entity = QuestionEntity(
            skillId = question.skillId.toLongOrNull() ?: 1L,
            gradeLevel = question.gradeLevel,
            prompt = question.prompt,
            type = question.type,
            difficulty = question.difficulty,
            hints = question.hints.joinToString("|"),
            correctAnswer = question.correctAnswer,
            explanation = question.explanation,
            options = question.options?.joinToString("|"),
            followUpQuestions = question.followUpQuestions?.joinToString("|"),
            createdAt = Date()
        )
        questionDao.insertQuestion(entity)
    }

    override suspend fun getQuestionById(questionId: String): Result<Question> = withContext(Dispatchers.IO) {
        try {
            val entity = questionDao.getQuestionById(questionId.toLongOrNull() ?: 0L)
            if (entity != null) {
                Result.success(entity.toDomainModel())
            } else {
                Result.failure(NoSuchElementException("Question not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveQuestion(question: Question): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            saveQuestionToDatabase(question)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun QuestionEntity.toDomainModel(): Question {
        return Question(
            id = id.toString(),
            skillId = skillId.toString(), 
            gradeLevel = gradeLevel,
            prompt = prompt,
            type = type,
            difficulty = difficulty,
            hints = hints.split("|").filter { it.isNotBlank() },
            correctAnswer = correctAnswer,
            explanation = explanation,
            options = options?.split("|")?.filter { it.isNotBlank() },
            followUpQuestions = followUpQuestions?.split("|")?.filter { it.isNotBlank() },
            createdAt = createdAt
        )
    }
}