package com.studywise.ai.data.repository

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.studywise.ai.BuildConfig
import com.studywise.ai.data.cache.AIResponseCache
import com.studywise.ai.data.offline.OfflineFallbackProvider
import com.studywise.ai.data.remote.api.*
import com.studywise.ai.domain.model.IdentifiedObject
import com.studywise.ai.domain.model.SocraticLesson
import com.studywise.ai.domain.model.VocabularyWord
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File

class HybridAIRepositoryTest {
    
    private lateinit var context: Context
    private lateinit var openAIService: OpenAIService
    private lateinit var mistralService: MistralApiService
    private lateinit var cache: AIResponseCache
    private lateinit var offlineFallback: OfflineFallbackProvider
    private lateinit var repository: HybridAIRepository
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        openAIService = mockk()
        mistralService = mockk()
        cache = mockk(relaxed = true)
        offlineFallback = mockk(relaxed = true)
        
        mockkStatic(BuildConfig::class)
        every { BuildConfig.OPENAI_API_KEY } returns "test-openai-key"
        every { BuildConfig.MISTRAL_API_KEY } returns "test-mistral-key"
        
        repository = HybridAIRepository(
            context = context,
            openAIService = openAIService,
            mistralService = mistralService,
            cache = cache,
            offlineFallback = offlineFallback
        )
    }
    
    @Test
    fun `identifyObject should return identified object with ML Kit`() = runTest {
        // This test would require mocking ML Kit, which is complex
        // For now, we'll focus on testing the AI service interactions
    }
    
    @Test
    fun `generateGradedSentences should use cache when available`() = runTest {
        // Given
        val word = "telescope"
        val minGrade = 4
        val maxGrade = 6
        val cachedSentences = mapOf(
            4 to listOf("I can see the moon with my telescope."),
            5 to listOf("The telescope helps astronomers study distant stars."),
            6 to listOf("Modern telescopes use advanced optics to capture light from galaxies.")
        )
        
        every { cache.getCachedSentences(word, minGrade, maxGrade) } returns cachedSentences
        
        // When
        val result = repository.generateGradedSentences(word, minGrade, maxGrade)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(cachedSentences)
        
        // Should not call any API
        coVerify(exactly = 0) { mistralService.createChatCompletion(any(), any()) }
        coVerify(exactly = 0) { openAIService.createChatCompletion(any(), any()) }
    }
    
    @Test
    fun `generateGradedSentences should use Mistral when no cache and API key is valid`() = runTest {
        // Given
        val word = "microscope"
        val minGrade = 3
        val maxGrade = 5
        
        every { cache.getCachedSentences(word, minGrade, maxGrade) } returns null
        every { BuildConfig.MISTRAL_API_KEY } returns "valid-mistral-key"
        
        val mistralResponse = MistralChatResponse(
            choices = listOf(
                MistralChoice(
                    message = MistralMessage(
                        role = "assistant",
                        content = """
                        Grade 3:
                        - I can see tiny things with a microscope.
                        - The microscope makes small objects look bigger.
                        - Scientists use microscopes in their labs.
                        
                        Grade 4:
                        - Microscopes help us study cells and bacteria.
                        - The microscope lens magnifies objects many times.
                        - We discovered germs using powerful microscopes.
                        
                        Grade 5:
                        - Compound microscopes use multiple lenses for magnification.
                        - Electron microscopes can see objects 2 million times larger.
                        - Microscopic organisms live in a drop of pond water.
                        """.trimIndent()
                    ),
                    index = 0,
                    finish_reason = "stop"
                )
            ),
            id = "test",
            objectType = "chat.completion",
            created = System.currentTimeMillis() / 1000,
            model = "mistral-7b",
            usage = MistralUsage(
                prompt_tokens = 100,
                completion_tokens = 150,
                total_tokens = 250
            )
        )
        
        coEvery { mistralService.createChatCompletion(any(), any()) } returns mistralResponse
        
        // When
        val result = repository.generateGradedSentences(word, minGrade, maxGrade)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val sentences = result.getOrNull()
        assertThat(sentences).isNotNull()
        assertThat(sentences!![3]).hasSize(3)
        assertThat(sentences[4]).hasSize(3)
        assertThat(sentences[5]).hasSize(3)
        
        coVerify(exactly = 1) { mistralService.createChatCompletion(any(), any()) }
    }
    
    @Test
    fun `createSocraticLesson should use cache when available`() = runTest {
        // Given
        val objectName = "compass"
        val grade = 4
        val duration = 15
        
        val cachedLesson = SocraticLesson(
            id = "test-id",
            objectName = objectName,
            grade = grade,
            duration = duration,
            learningObjectives = listOf("Understand how compasses work"),
            initialQuestion = "Have you ever wondered how explorers find their way?",
            guidingQuestions = listOf("What do you notice about the needle?"),
            vocabularyWords = listOf(
                VocabularyWord(
                    word = "magnetic",
                    definition = "Having the properties of a magnet",
                    exampleSentence = "The compass needle is magnetic.",
                    gradeLevel = grade
                )
            ),
            funFacts = listOf("The Earth acts like a giant magnet!")
        )
        
        every { cache.getCachedLesson(objectName, grade, duration) } returns cachedLesson
        
        // When
        val result = repository.createSocraticLesson(objectName, grade, duration)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(cachedLesson)
        
        coVerify(exactly = 0) { mistralService.createChatCompletion(any(), any()) }
        coVerify(exactly = 0) { openAIService.createChatCompletion(any(), any()) }
    }
    
    @Test
    fun `generateSocraticResponse should use Mistral for responses`() = runTest {
        // Given
        val context = "We're learning about plants and photosynthesis"
        val studentResponse = "Plants need sunlight to grow"
        val grade = 3
        
        val mistralResponse = MistralChatResponse(
            choices = listOf(
                MistralChoice(
                    message = MistralMessage(
                        role = "assistant",
                        content = "That's right! Plants do need sunlight. What do you think happens to the sunlight inside the plant's leaves?"
                    ),
                    index = 0,
                    finish_reason = "stop"
                )
            ),
            id = "test",
            objectType = "chat.completion",
            created = System.currentTimeMillis() / 1000,
            model = "mistral-7b",
            usage = MistralUsage(
                prompt_tokens = 50,
                completion_tokens = 30,
                total_tokens = 80
            )
        )
        
        coEvery { mistralService.createChatCompletion(any(), any()) } returns mistralResponse
        
        // When
        val result = repository.generateSocraticResponse(context, studentResponse, grade)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).contains("That's right!")
        assertThat(result.getOrNull()).contains("leaves")
        
        coVerify(exactly = 1) { mistralService.createChatCompletion(any(), any()) }
    }
    
    @Test
    fun `should fallback to OpenAI when Mistral fails`() = runTest {
        // Given
        val context = "Learning about gravity"
        val studentResponse = "Things fall down"
        val grade = 2
        
        // Mistral fails
        coEvery { mistralService.createChatCompletion(any(), any()) } throws Exception("Mistral API error")
        
        // OpenAI responds
        val openAIResponse = ChatCompletionResponse(
            choices = listOf(
                Choice(
                    message = Message(
                        role = "assistant",
                        content = "Yes! Things do fall down. Have you noticed that some things fall faster than others?"
                    ),
                    index = 0,
                    finish_reason = "stop"
                )
            ),
            id = "test",
            usage = Usage(
                prompt_tokens = 50,
                completion_tokens = 30,
                total_tokens = 80
            )
        )
        
        coEvery { openAIService.createChatCompletion(any(), any()) } returns openAIResponse
        
        // When
        val result = repository.generateSocraticResponse(context, studentResponse, grade)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).contains("Yes!")
        
        coVerify(exactly = 1) { mistralService.createChatCompletion(any(), any()) }
        coVerify(exactly = 1) { openAIService.createChatCompletion(any(), any()) }
    }
    
    @Test
    fun `generateSpeech should use OpenAI TTS`() = runTest {
        // Given
        val text = "Welcome to StudyWise!"
        val audioBytes = byteArrayOf(1, 2, 3, 4, 5)
        
        val responseBody = mockk<okhttp3.ResponseBody>()
        coEvery { responseBody.bytes() } returns audioBytes
        coEvery { openAIService.createSpeech(any(), any()) } returns responseBody
        
        // When
        val result = repository.generateSpeech(text)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(audioBytes)
        
        coVerify(exactly = 1) { openAIService.createSpeech(any(), any()) }
    }
    
    @Test
    fun `transcribeAudio should use OpenAI Whisper`() = runTest {
        // Given
        val audioFile = mockk<File>()
        every { audioFile.name } returns "test.wav"
        every { audioFile.exists() } returns true
        
        val transcriptionResponse = TranscriptionResponse(
            text = "Hello, this is a test transcription."
        )
        
        coEvery { openAIService.createTranscription(any(), any(), any(), any()) } returns transcriptionResponse
        
        // When
        val result = repository.transcribeAudio(audioFile)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("Hello, this is a test transcription.")
        
        coVerify(exactly = 1) { openAIService.createTranscription(any(), any(), any(), any()) }
    }
}