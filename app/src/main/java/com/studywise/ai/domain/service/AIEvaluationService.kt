package com.studywise.ai.domain.service

import com.studywise.ai.data.local.entity.SkillEntity
import com.studywise.ai.domain.model.Question
import com.studywise.ai.presentation.screens.session.AnswerEvaluation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.min

/**
 * AI-powered evaluation service for student answers using Socratic method
 */
@Singleton
class AIEvaluationService @Inject constructor(
    private val aiRepository: com.studywise.ai.domain.repository.AIRepository
) {
    
    companion object {
        private const val EVALUATION_MODEL = "gpt-4"
        private const val TEMPERATURE = 0.3f // Lower temperature for more consistent evaluation
        
        // Evaluation criteria weights
        private const val WEIGHT_ACCURACY = 0.4f
        private const val WEIGHT_COMPREHENSION = 0.3f
        private const val WEIGHT_REASONING = 0.2f
        private const val WEIGHT_EXPRESSION = 0.1f
    }
    
    /**
     * Evaluate a student's answer using AI
     */
    suspend fun evaluateAnswer(
        question: Question,
        userAnswer: String,
        skill: SkillEntity,
        gradeLevel: Int,
        contextText: String? = null
    ): DetailedEvaluation = withContext(Dispatchers.IO) {
        
        // Build evaluation prompt
        val evaluationPrompt = buildEvaluationPrompt(
            question = question,
            userAnswer = userAnswer,
            skill = skill,
            gradeLevel = gradeLevel,
            contextText = contextText
        )
        
        try {
            // Call AI service
            val aiResponse = aiRepository.generateSocraticResponse(
                context = evaluationPrompt,
                studentResponse = userAnswer,
                grade = gradeLevel
            )
            
            // Parse AI response
            parseAIEvaluation(aiResponse.getOrNull() ?: "")
            
        } catch (e: Exception) {
            // Fallback to rule-based evaluation
            performRuleBasedEvaluation(question, userAnswer, skill, gradeLevel)
        }
    }
    
    /**
     * Generate Socratic follow-up questions based on the answer
     */
    suspend fun generateFollowUpQuestions(
        question: Question,
        userAnswer: String,
        evaluation: DetailedEvaluation,
        skill: SkillEntity,
        gradeLevel: Int
    ): List<SocraticFollowUp> = withContext(Dispatchers.IO) {
        
        val followUpPrompt = buildFollowUpPrompt(
            question = question,
            userAnswer = userAnswer,
            evaluation = evaluation,
            skill = skill,
            gradeLevel = gradeLevel
        )
        
        try {
            val aiResponse = aiRepository.generateSocraticResponse(
                context = followUpPrompt,
                studentResponse = userAnswer,
                grade = gradeLevel
            )
            
            parseFollowUpQuestions(aiResponse.getOrNull() ?: "")
            
        } catch (e: Exception) {
            // Fallback to predefined follow-ups
            generateDefaultFollowUps(evaluation, skill, gradeLevel)
        }
    }
    
    /**
     * Build evaluation prompt for AI
     */
    private fun buildEvaluationPrompt(
        question: Question,
        userAnswer: String,
        skill: SkillEntity,
        gradeLevel: Int,
        contextText: String?
    ): String {
        return """
        You are an expert educational evaluator using the Socratic method. 
        Evaluate this grade $gradeLevel student's answer for the skill: ${skill.name}
        
        ${contextText?.let { "Context from their reading:\n$it\n" } ?: ""}
        
        Question: ${question.prompt}
        
        Student's Answer: $userAnswer
        
        Evaluate based on these criteria:
        1. Accuracy (40%) - Is the answer factually correct?
        2. Comprehension (30%) - Does the student understand the concept?
        3. Reasoning (20%) - Is there clear logical thinking?
        4. Expression (10%) - Is the answer well-articulated?
        
        Provide your evaluation in this JSON format:
        {
            "isCorrect": true/false,
            "scores": {
                "accuracy": 0.0-1.0,
                "comprehension": 0.0-1.0,
                "reasoning": 0.0-1.0,
                "expression": 0.0-1.0
            },
            "overallScore": 0.0-1.0,
            "strengths": ["strength1", "strength2"],
            "areasForImprovement": ["area1", "area2"],
            "feedback": "Encouraging feedback using Socratic method",
            "misconceptions": ["misconception1"],
            "suggestedFocus": "What the student should focus on next"
        }
        
        Be encouraging and constructive. Focus on guiding discovery rather than giving direct answers.
        """.trimIndent()
    }
    
    /**
     * Build follow-up prompt for Socratic questioning
     */
    private fun buildFollowUpPrompt(
        question: Question,
        userAnswer: String,
        evaluation: DetailedEvaluation,
        skill: SkillEntity,
        gradeLevel: Int
    ): String {
        return """
        Based on this grade $gradeLevel student's answer, generate 2-3 Socratic follow-up questions.
        
        Original Question: ${question.prompt}
        Student's Answer: $userAnswer
        
        Evaluation Summary:
        - Overall Score: ${evaluation.overallScore}
        - Strengths: ${evaluation.strengths.joinToString(", ")}
        - Areas for Improvement: ${evaluation.areasForImprovement.joinToString(", ")}
        
        Generate follow-up questions that:
        1. Build on what the student already knows
        2. Guide them to discover deeper understanding
        3. Are appropriate for grade $gradeLevel
        4. Use the Socratic method (don't give answers, ask guiding questions)
        
        Format as JSON:
        {
            "followUps": [
                {
                    "question": "The follow-up question",
                    "purpose": "What this question helps the student discover",
                    "type": "clarification|extension|connection|reflection"
                }
            ]
        }
        """.trimIndent()
    }
    
    /**
     * Parse AI evaluation response
     */
    private fun parseAIEvaluation(aiResponse: String): DetailedEvaluation {
        return try {
            val json = JSONObject(aiResponse)
            
            DetailedEvaluation(
                isCorrect = json.getBoolean("isCorrect"),
                scores = EvaluationScores(
                    accuracy = json.getJSONObject("scores").getDouble("accuracy").toFloat(),
                    comprehension = json.getJSONObject("scores").getDouble("comprehension").toFloat(),
                    reasoning = json.getJSONObject("scores").getDouble("reasoning").toFloat(),
                    expression = json.getJSONObject("scores").getDouble("expression").toFloat()
                ),
                overallScore = json.getDouble("overallScore").toFloat(),
                strengths = json.getJSONArray("strengths").let { array ->
                    (0 until array.length()).map { array.getString(it) }
                },
                areasForImprovement = json.getJSONArray("areasForImprovement").let { array ->
                    (0 until array.length()).map { array.getString(it) }
                },
                feedback = json.getString("feedback"),
                misconceptions = json.optJSONArray("misconceptions")?.let { array ->
                    (0 until array.length()).map { array.getString(it) }
                } ?: emptyList(),
                suggestedFocus = json.optString("suggestedFocus", "")
            )
        } catch (e: Exception) {
            // Return a default evaluation if parsing fails
            DetailedEvaluation(
                isCorrect = false,
                scores = EvaluationScores(0.5f, 0.5f, 0.5f, 0.5f),
                overallScore = 0.5f,
                strengths = listOf("Attempted the question"),
                areasForImprovement = listOf("Could provide more detail"),
                feedback = "Good effort! Let's think about this together.",
                misconceptions = emptyList(),
                suggestedFocus = "Review the key concepts"
            )
        }
    }
    
    /**
     * Parse follow-up questions from AI response
     */
    private fun parseFollowUpQuestions(aiResponse: String): List<SocraticFollowUp> {
        return try {
            val json = JSONObject(aiResponse)
            val followUpsArray = json.getJSONArray("followUps")
            
            (0 until followUpsArray.length()).map { index ->
                val followUp = followUpsArray.getJSONObject(index)
                SocraticFollowUp(
                    question = followUp.getString("question"),
                    purpose = followUp.getString("purpose"),
                    type = FollowUpType.valueOf(followUp.getString("type").uppercase())
                )
            }
        } catch (e: Exception) {
            // Return default follow-ups
            listOf(
                SocraticFollowUp(
                    question = "What made you think of that answer?",
                    purpose = "Understand student's reasoning",
                    type = FollowUpType.CLARIFICATION
                ),
                SocraticFollowUp(
                    question = "Can you give an example from your own experience?",
                    purpose = "Connect to personal knowledge",
                    type = FollowUpType.CONNECTION
                )
            )
        }
    }
    
    /**
     * Rule-based evaluation fallback
     */
    private fun performRuleBasedEvaluation(
        question: Question,
        userAnswer: String,
        skill: SkillEntity,
        gradeLevel: Int
    ): DetailedEvaluation {
        val answerLength = userAnswer.trim().split("\\s+".toRegex()).size
        val minLength = when (gradeLevel) {
            in 2..3 -> 5
            in 4..5 -> 10
            in 6..8 -> 15
            else -> 20
        }
        
        // Basic scoring based on answer characteristics
        val lengthScore = min(1.0f, answerLength.toFloat() / minLength)
        val hasEvidence = userAnswer.contains("because", ignoreCase = true) || 
                         userAnswer.contains("for example", ignoreCase = true)
        val evidenceScore = if (hasEvidence) 0.8f else 0.4f
        
        // Check for key concepts from the question
        val questionWords = question.prompt.lowercase().split("\\s+".toRegex())
        val answerWords = userAnswer.lowercase().split("\\s+".toRegex())
        val relevanceScore = questionWords.count { it in answerWords }.toFloat() / questionWords.size
        
        val overallScore = (lengthScore * 0.3f + evidenceScore * 0.4f + relevanceScore * 0.3f)
        val isCorrect = overallScore >= 0.6f
        
        return DetailedEvaluation(
            isCorrect = isCorrect,
            scores = EvaluationScores(
                accuracy = relevanceScore,
                comprehension = evidenceScore,
                reasoning = if (hasEvidence) 0.7f else 0.3f,
                expression = lengthScore
            ),
            overallScore = overallScore,
            strengths = mutableListOf<String>().apply {
                if (lengthScore >= 0.8f) add("Good detail in your answer")
                if (hasEvidence) add("You provided supporting evidence")
                if (relevanceScore >= 0.6f) add("Your answer addresses the question")
            },
            areasForImprovement = mutableListOf<String>().apply {
                if (lengthScore < 0.8f) add("Try to elaborate more on your ideas")
                if (!hasEvidence) add("Include examples or reasons to support your answer")
                if (relevanceScore < 0.6f) add("Make sure to address all parts of the question")
            },
            feedback = generateRuleBasedFeedback(isCorrect, overallScore, gradeLevel),
            misconceptions = emptyList(),
            suggestedFocus = "Think about specific examples from your reading"
        )
    }
    
    /**
     * Generate rule-based feedback
     */
    private fun generateRuleBasedFeedback(isCorrect: Boolean, score: Float, gradeLevel: Int): String {
        return when {
            isCorrect && score >= 0.8f -> {
                when (gradeLevel) {
                    in 2..5 -> "Excellent work! You really understood this question!"
                    in 6..8 -> "Great analysis! Your thinking is clear and well-supported."
                    else -> "Exceptional response! Your critical thinking skills are impressive."
                }
            }
            isCorrect -> {
                when (gradeLevel) {
                    in 2..5 -> "Good job! You're on the right track!"
                    in 6..8 -> "Nice work! Consider adding more specific examples."
                    else -> "Solid understanding. Push yourself to explore deeper connections."
                }
            }
            score >= 0.4f -> {
                when (gradeLevel) {
                    in 2..5 -> "You're getting there! Let's think about this together."
                    in 6..8 -> "You have some good ideas. How can we develop them further?"
                    else -> "Interesting perspective. Consider other angles to strengthen your analysis."
                }
            }
            else -> {
                when (gradeLevel) {
                    in 2..5 -> "Let's try again! Think about what the question is asking."
                    in 6..8 -> "Take your time to re-read and think through your answer."
                    else -> "Consider revisiting the text for additional insights."
                }
            }
        }
    }
    
    /**
     * Generate default follow-up questions
     */
    private fun generateDefaultFollowUps(
        evaluation: DetailedEvaluation,
        skill: SkillEntity,
        gradeLevel: Int
    ): List<SocraticFollowUp> {
        val followUps = mutableListOf<SocraticFollowUp>()
        
        // Based on evaluation results
        if (evaluation.scores.reasoning < 0.6f) {
            followUps.add(
                SocraticFollowUp(
                    question = when (gradeLevel) {
                        in 2..5 -> "What clues in the text helped you think of this?"
                        in 6..8 -> "What evidence from the text supports your thinking?"
                        else -> "How does the textual evidence substantiate your interpretation?"
                    },
                    purpose = "Develop evidence-based reasoning",
                    type = FollowUpType.CLARIFICATION
                )
            )
        }
        
        if (evaluation.scores.comprehension < 0.7f) {
            followUps.add(
                SocraticFollowUp(
                    question = when (gradeLevel) {
                        in 2..5 -> "Can you tell me more about what this means?"
                        in 6..8 -> "How would you explain this to a friend?"
                        else -> "What are the key concepts you're working with here?"
                    },
                    purpose = "Check and deepen understanding",
                    type = FollowUpType.EXTENSION
                )
            )
        }
        
        // Always add a connection question
        followUps.add(
            SocraticFollowUp(
                question = when (gradeLevel) {
                    in 2..5 -> "When have you seen something like this before?"
                    in 6..8 -> "How does this connect to other things you've learned?"
                    else -> "What broader implications or connections can you identify?"
                },
                purpose = "Build connections to prior knowledge",
                type = FollowUpType.CONNECTION
            )
        )
        
        return followUps.take(3) // Limit to 3 follow-ups
    }
    
    /**
     * Convert detailed evaluation to simple answer evaluation
     */
    fun toAnswerEvaluation(detailed: DetailedEvaluation): AnswerEvaluation {
        return AnswerEvaluation(
            isCorrect = detailed.isCorrect,
            feedback = detailed.feedback,
            suggestedFollowUp = detailed.suggestedFocus
        )
    }
}

// Data classes
data class DetailedEvaluation(
    val isCorrect: Boolean,
    val scores: EvaluationScores,
    val overallScore: Float,
    val strengths: List<String>,
    val areasForImprovement: List<String>,
    val feedback: String,
    val misconceptions: List<String>,
    val suggestedFocus: String
)

data class EvaluationScores(
    val accuracy: Float,
    val comprehension: Float,
    val reasoning: Float,
    val expression: Float
)

data class SocraticFollowUp(
    val question: String,
    val purpose: String,
    val type: FollowUpType
)

enum class FollowUpType {
    CLARIFICATION,  // Clarify thinking
    EXTENSION,      // Extend understanding
    CONNECTION,     // Connect to other knowledge
    REFLECTION      // Reflect on learning process
}

// Extension for AnalyticsEvent
sealed class AnalyticsEvent {
    data class GamificationApplied(
        val userId: String,
        val strategy: String,
        val hooks: List<String>
    )
    
    data class SessionStarted(
        val subject: String,
        val userId: String
    )
    
    data class QuestionAnswered(
        val subject: String,
        val skillId: String,
        val isCorrect: Boolean,
        val responseTime: Long
    )
    
    data class SessionCompleted(
        val subject: String,
        val userId: String,
        val questionsCompleted: Int,
        val pointsEarned: Int
    )
}