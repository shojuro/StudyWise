package com.studywise.ai.domain.service

import com.studywise.ai.domain.repository.AIRepository
import com.studywise.ai.test.fixtures.EducationalContentFixtures
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for AIEvaluationService
 */
class AIEvaluationServiceTest {
    
    private lateinit var aiRepository: AIRepository
    private lateinit var aiEvaluationService: AIEvaluationService
    
    @Before
    fun setup() {
        aiRepository = mockk()
        aiEvaluationService = AIEvaluationService(aiRepository)
    }
    
    @Test
    fun `evaluateAnswer should return correct evaluation for good answer`() = runTest {
        // Given
        val question = EducationalContentFixtures.createTestQuestion(
            prompt = "What is the main character's motivation?"
        )
        val skill = EducationalContentFixtures.createTestSkill()
        val userAnswer = "The main character is motivated by their desire to protect their family from danger."
        
        val mockAIResponse = """
        {
            "isCorrect": true,
            "scores": {
                "accuracy": 0.9,
                "comprehension": 0.85,
                "reasoning": 0.8,
                "expression": 0.9
            },
            "overallScore": 0.86,
            "strengths": ["Clear understanding", "Good use of evidence"],
            "areasForImprovement": ["Could elaborate more"],
            "feedback": "Excellent analysis! You clearly understood the character's core motivation.",
            "misconceptions": [],
            "suggestedFocus": "Try exploring how this motivation affects other characters"
        }
        """.trimIndent()
        
        coEvery { 
            aiRepository.generateResponse(any(), any(), any(), any()) 
        } returns Result.success(mockAIResponse)
        
        // When
        val result = aiEvaluationService.evaluateAnswer(
            question = question,
            userAnswer = userAnswer,
            skill = skill,
            gradeLevel = 4
        )
        
        // Then
        assertTrue(result.isCorrect)
        assertEquals(0.86f, result.overallScore)
        assertEquals(2, result.strengths.size)
        assertTrue(result.feedback.contains("Excellent"))
        assertTrue(result.misconceptions.isEmpty())
    }
    
    @Test
    fun `evaluateAnswer should return incorrect evaluation for poor answer`() = runTest {
        // Given
        val question = EducationalContentFixtures.createTestQuestion(
            prompt = "What is the theme of the story?"
        )
        val skill = EducationalContentFixtures.createTestSkill()
        val userAnswer = "I don't know"
        
        val mockAIResponse = """
        {
            "isCorrect": false,
            "scores": {
                "accuracy": 0.1,
                "comprehension": 0.2,
                "reasoning": 0.1,
                "expression": 0.3
            },
            "overallScore": 0.175,
            "strengths": ["Attempted the question"],
            "areasForImprovement": ["Provide more detail", "Reference the text"],
            "feedback": "Let's think about this together. What events in the story might give us clues about the theme?",
            "misconceptions": ["Theme confusion"],
            "suggestedFocus": "Review what theme means in literature"
        }
        """.trimIndent()
        
        coEvery { 
            aiRepository.generateResponse(any(), any(), any(), any()) 
        } returns Result.success(mockAIResponse)
        
        // When
        val result = aiEvaluationService.evaluateAnswer(
            question = question,
            userAnswer = userAnswer,
            skill = skill,
            gradeLevel = 4
        )
        
        // Then
        assertFalse(result.isCorrect)
        assertEquals(0.175f, result.overallScore)
        assertEquals(2, result.areasForImprovement.size)
        assertTrue(result.feedback.contains("think about this together"))
        assertEquals(1, result.misconceptions.size)
    }
    
    @Test
    fun `evaluateAnswer should fall back to rule-based evaluation on AI error`() = runTest {
        // Given
        val question = EducationalContentFixtures.createTestQuestion()
        val skill = EducationalContentFixtures.createTestSkill()
        val userAnswer = "The character learns that friendship is important because they help each other."
        
        coEvery { 
            aiRepository.generateResponse(any(), any(), any(), any()) 
        } returns Result.failure(Exception("AI service unavailable"))
        
        // When
        val result = aiEvaluationService.evaluateAnswer(
            question = question,
            userAnswer = userAnswer,
            skill = skill,
            gradeLevel = 4
        )
        
        // Then
        // Should use rule-based evaluation
        assertTrue(result.isCorrect) // Answer has good length and keywords
        assertTrue(result.feedback.isNotEmpty())
        assertTrue(result.strengths.isNotEmpty())
    }
    
    @Test
    fun `generateFollowUpQuestions should return Socratic questions`() = runTest {
        // Given
        val question = EducationalContentFixtures.createTestQuestion()
        val evaluation = DetailedEvaluation(
            isCorrect = true,
            scores = EvaluationScores(0.8f, 0.7f, 0.6f, 0.8f),
            overallScore = 0.725f,
            strengths = listOf("Good understanding"),
            areasForImprovement = listOf("Deeper analysis needed"),
            feedback = "Good work!",
            misconceptions = emptyList(),
            suggestedFocus = "Character development"
        )
        val skill = EducationalContentFixtures.createTestSkill()
        
        val mockAIResponse = """
        {
            "followUps": [
                {
                    "question": "What specific actions did the character take that showed this?",
                    "purpose": "Encourage evidence-based thinking",
                    "type": "clarification"
                },
                {
                    "question": "How might the story be different if the character made a different choice?",
                    "purpose": "Develop critical thinking",
                    "type": "extension"
                }
            ]
        }
        """.trimIndent()
        
        coEvery { 
            aiRepository.generateResponse(any(), any(), any(), any()) 
        } returns Result.success(mockAIResponse)
        
        // When
        val result = aiEvaluationService.generateFollowUpQuestions(
            question = question,
            userAnswer = "Test answer",
            evaluation = evaluation,
            skill = skill,
            gradeLevel = 4
        )
        
        // Then
        assertEquals(2, result.size)
        assertEquals(FollowUpType.CLARIFICATION, result[0].type)
        assertEquals(FollowUpType.EXTENSION, result[1].type)
        assertTrue(result[0].question.contains("specific actions"))
    }
    
    @Test
    fun `generateFollowUpQuestions should use defaults on AI error`() = runTest {
        // Given
        val question = EducationalContentFixtures.createTestQuestion()
        val evaluation = DetailedEvaluation(
            isCorrect = false,
            scores = EvaluationScores(0.4f, 0.5f, 0.3f, 0.6f),
            overallScore = 0.45f,
            strengths = emptyList(),
            areasForImprovement = listOf("Needs evidence"),
            feedback = "Try again",
            misconceptions = emptyList(),
            suggestedFocus = "Text evidence"
        )
        val skill = EducationalContentFixtures.createTestSkill()
        
        coEvery { 
            aiRepository.generateResponse(any(), any(), any(), any()) 
        } returns Result.failure(Exception("AI error"))
        
        // When
        val result = aiEvaluationService.generateFollowUpQuestions(
            question = question,
            userAnswer = "Short answer",
            evaluation = evaluation,
            skill = skill,
            gradeLevel = 4
        )
        
        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.size <= 3)
        // Should have reasoning and comprehension follow-ups due to low scores
        assertTrue(result.any { it.type == FollowUpType.CLARIFICATION })
    }
    
    @Test
    fun `rule-based evaluation should check answer length`() = runTest {
        // Given
        val question = EducationalContentFixtures.createTestQuestion()
        val skill = EducationalContentFixtures.createTestSkill()
        val shortAnswer = "Yes"
        val longAnswer = "The character demonstrates courage by standing up to the bully even though they were scared."
        
        coEvery { 
            aiRepository.generateResponse(any(), any(), any(), any()) 
        } returns Result.failure(Exception("Force rule-based"))
        
        // When
        val shortResult = aiEvaluationService.evaluateAnswer(
            question = question,
            userAnswer = shortAnswer,
            skill = skill,
            gradeLevel = 4
        )
        
        val longResult = aiEvaluationService.evaluateAnswer(
            question = question,
            userAnswer = longAnswer,
            skill = skill,
            gradeLevel = 4
        )
        
        // Then
        assertFalse(shortResult.isCorrect) // Too short
        assertTrue(longResult.isCorrect) // Good length with evidence
        assertTrue(shortResult.areasForImprovement.any { it.contains("elaborate") })
    }
    
    @Test
    fun `rule-based evaluation should check for evidence keywords`() = runTest {
        // Given
        val question = EducationalContentFixtures.createTestQuestion()
        val skill = EducationalContentFixtures.createTestSkill()
        val answerWithEvidence = "I think this because the text says that the character was brave."
        val answerWithoutEvidence = "The character was brave and strong."
        
        coEvery { 
            aiRepository.generateResponse(any(), any(), any(), any()) 
        } returns Result.failure(Exception("Force rule-based"))
        
        // When
        val withEvidenceResult = aiEvaluationService.evaluateAnswer(
            question = question,
            userAnswer = answerWithEvidence,
            skill = skill,
            gradeLevel = 4
        )
        
        val withoutEvidenceResult = aiEvaluationService.evaluateAnswer(
            question = question,
            userAnswer = answerWithoutEvidence,
            skill = skill,
            gradeLevel = 4
        )
        
        // Then
        assertTrue(withEvidenceResult.scores.reasoning > withoutEvidenceResult.scores.reasoning)
        assertTrue(withEvidenceResult.strengths.any { it.contains("evidence") })
        assertTrue(withoutEvidenceResult.areasForImprovement.any { it.contains("examples") })
    }
    
    @Test
    fun `toAnswerEvaluation should convert detailed to simple evaluation`() {
        // Given
        val detailed = DetailedEvaluation(
            isCorrect = true,
            scores = EvaluationScores(0.8f, 0.8f, 0.8f, 0.8f),
            overallScore = 0.8f,
            strengths = listOf("Good work"),
            areasForImprovement = emptyList(),
            feedback = "Excellent answer!",
            misconceptions = emptyList(),
            suggestedFocus = "Keep practicing"
        )
        
        // When
        val simple = aiEvaluationService.toAnswerEvaluation(detailed)
        
        // Then
        assertEquals(detailed.isCorrect, simple.isCorrect)
        assertEquals(detailed.feedback, simple.feedback)
        assertEquals(detailed.suggestedFocus, simple.suggestedFollowUp)
    }
}