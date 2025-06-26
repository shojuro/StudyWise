package com.studywise.ai.data.repository

import android.content.Context
import android.net.Uri
import com.studywise.ai.data.cache.AIResponseCache
import com.studywise.ai.data.offline.OfflineFallbackProvider
import com.studywise.ai.data.remote.api.*
import com.studywise.ai.domain.model.IdentifiedObject
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AIRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var openAIService: OpenAIService
    private lateinit var cache: AIResponseCache
    private lateinit var offlineFallback: OfflineFallbackProvider
    private lateinit var repository: AIRepositoryImpl
    
    private val testApiKey = "test-api-key"

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        openAIService = mockk()
        cache = mockk(relaxed = true)
        offlineFallback = mockk()
        
        repository = AIRepositoryImpl(
            context = context,
            openAIService = openAIService,
            apiKey = testApiKey,
            cache = cache,
            offlineFallback = offlineFallback
        )
    }

    @Test
    fun `generateGradedSentences should return cached data when available`() = runTest {
        // Given
        val word = "apple"
        val minGrade = 2
        val maxGrade = 4
        val cachedSentences = mapOf(
            2 to listOf("I see an apple.", "The apple is red."),
            3 to listOf("The apple grows on a tree.", "Apples are healthy fruits."),
            4 to listOf("The orchard contains many apple trees.", "Apples provide essential vitamins.")
        )
        
        every { cache.getCachedSentences(word, minGrade, maxGrade) } returns cachedSentences

        // When
        val result = repository.generateGradedSentences(word, minGrade, maxGrade)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cachedSentences, result.getOrNull())
        
        // Verify API was not called
        coVerify(exactly = 0) { openAIService.createChatCompletion(any(), any()) }
    }

    @Test
    fun `generateGradedSentences should call API and cache result when no cache`() = runTest {
        // Given
        val word = "apple"
        val minGrade = 2
        val maxGrade = 3
        val apiResponse = ChatCompletionResponse(
            id = "completion-123",
            choices = listOf(
                Choice(
                    index = 0,
                    message = Message(
                        role = "assistant",
                        content = """
                            Grade 2:
                            - I see an apple.
                            - The apple is red.
                            
                            Grade 3:
                            - The apple grows on a tree.
                            - Apples are healthy fruits.
                        """.trimIndent()
                    ),
                    finish_reason = "stop"
                )
            ),
            usage = Usage(prompt_tokens = 50, completion_tokens = 100, total_tokens = 150)
        )
        
        every { cache.getCachedSentences(word, minGrade, maxGrade) } returns null
        coEvery { openAIService.createChatCompletion(any(), any()) } returns apiResponse

        // When
        val result = repository.generateGradedSentences(word, minGrade, maxGrade)

        // Then
        assertTrue(result.isSuccess)
        val sentences = result.getOrNull()!!
        assertEquals(2, sentences.size)
        assertEquals(listOf("I see an apple.", "The apple is red."), sentences[2])
        assertEquals(listOf("The apple grows on a tree.", "Apples are healthy fruits."), sentences[3])
        
        // Verify caching
        verify { cache.cacheSentences(word, minGrade, maxGrade, sentences) }
    }

    @Test
    fun `generateGradedSentences should use offline fallback on network error`() = runTest {
        // Given
        val word = "apple"
        val minGrade = 2
        val maxGrade = 3
        val networkError = IOException("Network error")
        val offlineSentences = mapOf(
            2 to listOf("I see a apple.", "The apple is here."),
            3 to listOf("The apple is very interesting.", "I found a apple today.")
        )
        
        every { cache.getCachedSentences(word, minGrade, maxGrade) } returns null
        coEvery { openAIService.createChatCompletion(any(), any()) } throws networkError
        every { offlineFallback.isOfflineMode(networkError) } returns true
        every { offlineFallback.getOfflineGradedSentences(word, minGrade, maxGrade) } returns offlineSentences

        // When
        val result = repository.generateGradedSentences(word, minGrade, maxGrade)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(offlineSentences, result.getOrNull())
    }

    @Test
    fun `generateGradedSentences should return error for 401 unauthorized`() = runTest {
        // Given
        val word = "apple"
        val httpException = mockk<HttpException> {
            every { code() } returns 401
        }
        
        every { cache.getCachedSentences(any(), any(), any()) } returns null
        coEvery { openAIService.createChatCompletion(any(), any()) } throws httpException
        every { offlineFallback.isOfflineMode(any()) } returns false

        // When
        val result = repository.generateGradedSentences(word, 2, 4)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid API key") == true)
    }

    @Test
    fun `createSocraticLesson should use cache when available`() = runTest {
        // Given
        val objectName = "apple"
        val grade = 4
        val duration = 5
        val cachedLesson = mockk<com.studywise.ai.domain.model.SocraticLesson>()
        
        every { cache.getCachedLesson(objectName, grade, duration) } returns cachedLesson

        // When
        val result = repository.createSocraticLesson(objectName, grade, duration)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cachedLesson, result.getOrNull())
        
        // Verify API was not called
        coVerify(exactly = 0) { openAIService.createChatCompletion(any(), any()) }
    }

    @Test
    fun `generateSocraticResponse should handle rate limiting`() = runTest {
        // Given
        val httpException = mockk<HttpException> {
            every { code() } returns 429
        }
        
        coEvery { openAIService.createChatCompletion(any(), any()) } throws httpException
        every { offlineFallback.isOfflineMode(any()) } returns false

        // When
        val result = repository.generateSocraticResponse("context", "response", 5)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Rate limit") == true)
    }

    @Test
    fun `transcribeAudio should handle timeout`() = runTest {
        // Given
        val audioFile = mockk<java.io.File> {
            every { name } returns "audio.wav"
            every { asRequestBody(any()) } returns mockk()
        }
        val timeoutError = java.net.SocketTimeoutException("Request timed out")
        
        coEvery { openAIService.createTranscription(any(), any(), any(), any()) } throws timeoutError

        // When
        val result = repository.transcribeAudio(audioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("timed out") == true)
    }
}