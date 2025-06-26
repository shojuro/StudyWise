package com.studywise.ai.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.studywise.ai.data.remote.api.MistralApiService
import com.studywise.ai.data.remote.dto.MistralChatCompletionRequest
import com.studywise.ai.data.remote.dto.MistralMessage
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MistralApiIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var mistralApiService: MistralApiService
    private lateinit var gson: Gson
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        mistralApiService = retrofit.create(MistralApiService::class.java)
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun testMistralChatCompletion_Success() = runTest {
        // Given
        val mockResponse = """
        {
            "id": "cmpl-mistral-123",
            "object": "chat.completion",
            "created": 1677652288,
            "model": "mistral-7b-instruct",
            "choices": [{
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": "Keys are small metal tools used to open locks. They have unique shapes that match specific locks."
                },
                "finish_reason": "stop"
            }],
            "usage": {
                "prompt_tokens": 25,
                "completion_tokens": 20,
                "total_tokens": 45
            }
        }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        val request = MistralChatCompletionRequest(
            model = "mistral-7b-instruct",
            messages = listOf(
                MistralMessage(
                    role = "system",
                    content = "You are an educational assistant for young students."
                ),
                MistralMessage(
                    role = "user",
                    content = "Explain what keys are to a 3rd grade student."
                )
            ),
            temperature = 0.7f,
            max_tokens = 150
        )
        
        // When
        val response = mistralApiService.createChatCompletion(request)
        
        // Then
        assertThat(response.isSuccessful).isTrue()
        assertThat(response.code()).isEqualTo(200)
        
        val chatResponse = response.body()
        assertThat(chatResponse).isNotNull()
        assertThat(chatResponse?.choices).hasSize(1)
        assertThat(chatResponse?.choices?.first()?.message?.content)
            .contains("Keys")
        assertThat(chatResponse?.model).isEqualTo("mistral-7b-instruct")
        assertThat(chatResponse?.usage?.totalTokens).isEqualTo(45)
        
        // Verify request
        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.method).isEqualTo("POST")
        assertThat(recordedRequest.path).isEqualTo("/v1/chat/completions")
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer test-mistral-key")
    }
    
    @Test
    fun testMistralChatCompletion_StreamingNotSupported() = runTest {
        // Given
        val errorResponse = """
        {
            "error": {
                "message": "Streaming is not supported in this endpoint",
                "type": "invalid_request_error",
                "code": "streaming_not_supported"
            }
        }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        val request = MistralChatCompletionRequest(
            model = "mistral-7b-instruct",
            messages = listOf(
                MistralMessage(role = "user", content = "Test message")
            ),
            stream = true // Try to enable streaming
        )
        
        // When
        val response = mistralApiService.createChatCompletion(request)
        
        // Then
        assertThat(response.isSuccessful).isFalse()
        assertThat(response.code()).isEqualTo(400)
        
        val errorBody = response.errorBody()?.string()
        assertThat(errorBody).contains("streaming_not_supported")
    }
    
    @Test
    fun testMistralChatCompletion_CostEffectiveEducationalContent() = runTest {
        // Given - Educational content generation response
        val mockResponse = """
        {
            "id": "cmpl-edu-456",
            "object": "chat.completion",
            "created": 1677652290,
            "model": "mistral-7b-instruct",
            "choices": [{
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": "{\"objectName\":\"Calculator\",\"gradeLevel\":4,\"subject\":\"Math\",\"mainContent\":\"A calculator is a device that helps us solve math problems quickly. It can add, subtract, multiply, and divide numbers.\",\"funFact\":\"The first pocket calculator was invented in 1970 and cost ${"$"}395!\",\"questions\":[\"What operations can a calculator do?\",\"When might you use a calculator?\",\"What's the difference between mental math and using a calculator?\"],\"suggestedActivities\":[\"Practice checking your math homework with a calculator\",\"Learn to use the memory functions on a calculator\"]}"
                },
                "finish_reason": "stop"
            }],
            "usage": {
                "prompt_tokens": 80,
                "completion_tokens": 120,
                "total_tokens": 200
            }
        }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        val systemPrompt = """
        You are an educational content creator for elementary school students.
        Generate educational content about the given object in JSON format.
        Include: objectName, gradeLevel, subject, mainContent, funFact, questions (array of 3), and suggestedActivities (array of 2).
        Use age-appropriate language and ensure content is engaging and accurate.
        """.trimIndent()
        
        val request = MistralChatCompletionRequest(
            model = "mistral-7b-instruct",
            messages = listOf(
                MistralMessage(role = "system", content = systemPrompt),
                MistralMessage(role = "user", content = "Create educational content about 'Calculator' for grade 4 Math")
            ),
            temperature = 0.7f,
            max_tokens = 300,
        )
        
        // When
        val response = mistralApiService.createChatCompletion(request)
        
        // Then
        assertThat(response.isSuccessful).isTrue()
        
        val chatResponse = response.body()
        val content = chatResponse?.choices?.first()?.message?.content
        assertThat(content).isNotNull()
        assertThat(content).contains("Calculator")
        assertThat(content).contains("gradeLevel")
        assertThat(content).contains("funFact")
        
        // Verify cost effectiveness (tokens used)
        val tokensUsed = chatResponse?.usage?.totalTokens ?: 0
        assertThat(tokensUsed).isLessThan(500) // Efficient token usage
        
        // At Mistral's pricing ($0.001 per 1K tokens), this request would cost ~$0.0002
    }
    
    @Test
    fun testMistralChatCompletion_MultipleMessages() = runTest {
        // Given - Conversation with multiple turns
        val mockResponse = """
        {
            "id": "cmpl-conv-789",
            "object": "chat.completion",
            "created": 1677652295,
            "model": "mistral-7b-instruct",
            "choices": [{
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": "Great question! Yes, apples can be different colors. The most common colors are red, green, and yellow. Some apples can even be a mix of colors!"
                },
                "finish_reason": "stop"
            }],
            "usage": {
                "prompt_tokens": 60,
                "completion_tokens": 35,
                "total_tokens": 95
            }
        }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )
        
        val request = MistralChatCompletionRequest(
            model = "mistral-7b-instruct",
            messages = listOf(
                MistralMessage(
                    role = "system",
                    content = "You are a friendly teacher for elementary students."
                ),
                MistralMessage(
                    role = "user",
                    content = "What are apples?"
                ),
                MistralMessage(
                    role = "assistant",
                    content = "Apples are delicious fruits that grow on trees. They are round and crunchy!"
                ),
                MistralMessage(
                    role = "user",
                    content = "Can apples be different colors?"
                )
            ),
            temperature = 0.7f,
            max_tokens = 100,
        )
        
        // When
        val response = mistralApiService.createChatCompletion(request)
        
        // Then
        assertThat(response.isSuccessful).isTrue()
        
        val chatResponse = response.body()
        assertThat(chatResponse?.choices?.first()?.message?.content)
            .contains("colors")
        
        // Verify the conversation context was sent
        val recordedRequest = mockWebServer.takeRequest()
        val requestBody = recordedRequest.body.readUtf8()
        assertThat(requestBody).contains("What are apples?")
        assertThat(requestBody).contains("delicious fruits")
        assertThat(requestBody).contains("Can apples be different colors?")
    }
    
    @Test
    fun testMistralChatCompletion_QuotaExceeded() = runTest {
        // Given
        val errorResponse = """
        {
            "error": {
                "message": "Monthly quota exceeded",
                "type": "quota_exceeded",
                "code": "monthly_quota_exceeded"
            }
        }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json")
                .addHeader("Retry-After", "3600")
        )
        
        val request = MistralChatCompletionRequest(
            model = "mistral-7b-instruct",
            messages = listOf(
                MistralMessage(role = "user", content = "Test")
            ),
        )
        
        // When
        val response = mistralApiService.createChatCompletion(request)
        
        // Then
        assertThat(response.isSuccessful).isFalse()
        assertThat(response.code()).isEqualTo(429)
        assertThat(response.headers()["Retry-After"]).isEqualTo("3600")
    }
}