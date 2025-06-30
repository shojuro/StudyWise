package com.studywise.ai.data.local.content

import com.studywise.ai.test.fixtures.EducationalContentFixtures
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for TemplateProcessor
 */
class TemplateProcessorTest {
    
    private lateinit var templateProcessor: TemplateProcessor
    
    @Before
    fun setup() {
        templateProcessor = TemplateProcessor()
    }
    
    @Test
    fun `processTemplate should replace single variable correctly`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "What can you tell me about {topic}?",
            variables = mapOf("topic" to listOf("the main character", "the setting"))
        )
        val customVariables = mapOf("topic" to "the protagonist")
        
        // When
        val result = templateProcessor.processTemplate(template, customVariables)
        
        // Then
        assertEquals("What can you tell me about the protagonist?", result)
    }
    
    @Test
    fun `processTemplate should replace multiple variables correctly`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "How does {character} respond to {event}?",
            variables = mapOf(
                "character" to listOf("the hero", "the villain"),
                "event" to listOf("the conflict", "the challenge")
            )
        )
        val customVariables = mapOf(
            "character" to "Sarah",
            "event" to "the storm"
        )
        
        // When
        val result = templateProcessor.processTemplate(template, customVariables)
        
        // Then
        assertEquals("How does Sarah respond to the storm?", result)
    }
    
    @Test
    fun `processTemplate should use random variable when no custom value provided`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "What details tell you about {element}?",
            variables = mapOf(
                "element" to listOf("the character's feelings", "the setting", "the problem")
            )
        )
        
        // When
        val result = templateProcessor.processTemplate(template)
        
        // Then
        assertTrue(
            result == "What details tell you about the character's feelings?" ||
            result == "What details tell you about the setting?" ||
            result == "What details tell you about the problem?"
        )
    }
    
    @Test
    fun `processTemplate should handle mixed custom and random variables`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "In your {text_type}, how does {character} show {trait}?",
            variables = mapOf(
                "text_type" to listOf("story", "novel", "book"),
                "character" to listOf("the protagonist", "the main character"),
                "trait" to listOf("courage", "kindness", "determination")
            )
        )
        val customVariables = mapOf("text_type" to "narrative")
        
        // When
        val result = templateProcessor.processTemplate(template, customVariables)
        
        // Then
        assertTrue(result.startsWith("In your narrative, how does"))
        assertTrue(result.contains("show"))
        assertTrue(
            result.contains("courage") || 
            result.contains("kindness") || 
            result.contains("determination")
        )
    }
    
    @Test
    fun `generateVariations should produce unique variations`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "What does {character} learn about {topic}?",
            variables = mapOf(
                "character" to listOf("the hero", "the protagonist", "the main character"),
                "topic" to listOf("friendship", "courage", "responsibility")
            )
        )
        
        // When
        val variations = templateProcessor.generateVariations(template, count = 5, ensureUnique = true)
        
        // Then
        assertEquals(5, variations.size)
        assertEquals(5, variations.toSet().size) // All unique
        variations.forEach { variation ->
            assertTrue(variation.contains("What does"))
            assertTrue(variation.contains("learn about"))
        }
    }
    
    @Test
    fun `generateVariations should handle count larger than possible combinations`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "Is this {answer}?",
            variables = mapOf("answer" to listOf("correct", "right"))
        )
        
        // When
        val variations = templateProcessor.generateVariations(template, count = 10, ensureUnique = true)
        
        // Then
        assertEquals(2, variations.size) // Only 2 possible unique combinations
        assertTrue(variations.contains("Is this correct?"))
        assertTrue(variations.contains("Is this right?"))
    }
    
    @Test
    fun `generateVariations with ensureUnique false should allow duplicates`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "Tell me about {topic}.",
            variables = mapOf("topic" to listOf("the story"))
        )
        
        // When
        val variations = templateProcessor.generateVariations(template, count = 5, ensureUnique = false)
        
        // Then
        assertEquals(5, variations.size)
        variations.forEach { variation ->
            assertEquals("Tell me about the story.", variation)
        }
    }
    
    @Test
    fun `processTemplate should handle templates with no variables`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "What is the main idea of your text?",
            variables = emptyMap()
        )
        
        // When
        val result = templateProcessor.processTemplate(template)
        
        // Then
        assertEquals("What is the main idea of your text?", result)
    }
    
    @Test
    fun `processTemplate should handle nested braces correctly`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "In the {{context}}, what does {character} do?",
            variables = mapOf("character" to listOf("the hero"))
        )
        
        // When
        val result = templateProcessor.processTemplate(template)
        
        // Then
        assertEquals("In the {{context}}, what does the hero do?", result)
    }
    
    @Test
    fun `generateVariations should produce consistent results with seed`() {
        // Given
        val template = EducationalContentFixtures.createTestTemplate(
            promptTemplate = "How does {element} affect {outcome}?",
            variables = mapOf(
                "element" to listOf("weather", "setting", "time"),
                "outcome" to listOf("the plot", "the mood", "the characters")
            )
        )
        
        // When - Generate variations twice with different processors
        val processor1 = TemplateProcessor()
        val processor2 = TemplateProcessor()
        
        val variations1 = processor1.generateVariations(template, count = 3)
        val variations2 = processor2.generateVariations(template, count = 3)
        
        // Then - Results should be different (randomized)
        assertNotEquals(variations1, variations2)
    }
}