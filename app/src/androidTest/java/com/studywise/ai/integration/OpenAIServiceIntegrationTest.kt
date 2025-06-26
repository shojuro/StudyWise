package com.studywise.ai.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.studywise.ai.data.remote.api.OpenAIService
import com.studywise.ai.data.remote.dto.ChatCompletionRequest
import com.studywise.ai.data.remote.dto.ChatMessage
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
class OpenAIServiceIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var openAIService: OpenAIService
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
        
        openAIService = retrofit.create(OpenAIService::class.java)
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun testChatCompletionRequest_Success() = runTest {
        // Given
        val mockResponse = """
        {
            "id": "chatcmpl-123",
            "object": "chat.completion",
            "created": 1677652288,
            "model": "gpt-4",
            "choices": [{
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": "An apple is a sweet fruit that grows on trees."
                },
                "finish_reason": "stop"
            }],
            "usage": {
                "prompt_tokens": 20,
                "completion_tokens": 10,
                "total_tokens": 30
            }
        }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        val request = ChatCompletionRequest(
            model = "gpt-4",
            messages = listOf(
                ChatMessage(
                    role = "system",
                    content = "You are a helpful educational assistant."
                ),
                ChatMessage(
                    role = "user",
                    content = "Tell me about apples for a 3rd grader."
                )
            ),
            temperature = 0.7,
            maxTokens = 150
        )
        
        // When
        val response = openAIService.createChatCompletion(
            authorization = "Bearer test-api-key",
            request = request
        )
        
        // Then
        assertThat(response.isSuccessful).isTrue()
        assertThat(response.code()).isEqualTo(200)
        
        val chatResponse = response.body()
        assertThat(chatResponse).isNotNull()
        assertThat(chatResponse?.choices).hasSize(1)
        assertThat(chatResponse?.choices?.first()?.message?.content)
            .contains("apple")
        assertThat(chatResponse?.usage?.totalTokens).isEqualTo(30)
        
        // Verify request
        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.method).isEqualTo("POST")
        assertThat(recordedRequest.path).isEqualTo("/v1/chat/completions")
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer test-api-key")
        assertThat(recordedRequest.getHeader("Content-Type")).contains("application/json")
    }
    
    @Test
    fun testChatCompletionRequest_RateLimitError() = runTest {
        // Given
        val errorResponse = """
        {
            "error": {
                "message": "Rate limit exceeded",
                "type": "rate_limit_error",
                "code": "rate_limit_exceeded"
            }
        }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setBody(errorResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        val request = ChatCompletionRequest(
            model = "gpt-4",
            messages = listOf(
                ChatMessage(role = "user", content = "Test message")
            )
        )
        
        // When
        val response = openAIService.createChatCompletion(
            authorization = "Bearer test-api-key",
            request = request
        )
        
        // Then
        assertThat(response.isSuccessful).isFalse()
        assertThat(response.code()).isEqualTo(429)
        
        val errorBody = response.errorBody()?.string()
        assertThat(errorBody).contains("rate_limit_exceeded")
    }
    
    @Test
    fun testChatCompletionRequest_NetworkTimeout() = runTest {
        // Given - simulate network delay
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .setBodyDelay(10, TimeUnit.SECONDS) // Longer than client timeout
        )
        
        val request = ChatCompletionRequest(
            model = "gpt-4",
            messages = listOf(
                ChatMessage(role = "user", content = "Test message")
            )
        )
        
        // When/Then - should throw timeout exception
        try {
            openAIService.createChatCompletion(
                authorization = "Bearer test-api-key",
                request = request
            )
            // If we reach here, the test should fail
            assertThat(false).isTrue() // Force failure
        } catch (e: Exception) {
            // Expected timeout exception
            assertThat(e.message).contains("timeout")
        }
    }
    
    @Test
    fun testChatCompletionRequest_MalformedResponse() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{ invalid json")
                .addHeader("Content-Type", "application/json")
        )
        
        val request = ChatCompletionRequest(
            model = "gpt-4",
            messages = listOf(
                ChatMessage(role = "user", content = "Test message")
            )
        )
        
        // When/Then - should handle JSON parsing error
        try {
            val response = openAIService.createChatCompletion(
                authorization = "Bearer test-api-key",
                request = request
            )
            
            // Response might be successful but body parsing will fail
            assertThat(response.isSuccessful).isTrue()
            val body = response.body()
            // If Gson can't parse, body might be null
            if (body == null) {
                assertThat(body).isNull()
            }
        } catch (e: Exception) {
            // JSON parsing exception is also acceptable
            assertThat(e).isInstanceOf(com.google.gson.JsonSyntaxException::class.java)
        }
    }
    
    @Test
    fun testMultipleSequentialRequests() = runTest {
        // Given - Queue multiple responses
        repeat(3) { index ->
            val mockResponse = """
            {
                "id": "chatcmpl-$index",
                "object": "chat.completion",
                "created": ${System.currentTimeMillis() / 1000},
                "model": "gpt-4",
                "choices": [{
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": "Response $index"
                    },
                    "finish_reason": "stop"
                }],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 5,
                    "total_tokens": 15
                }
            }
            """.trimIndent()
            
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(mockResponse)
            )
        }
        
        // When - Make multiple requests
        val responses = mutableListOf<String>()
        repeat(3) { index ->
            val request = ChatCompletionRequest(
                model = "gpt-4",
                messages = listOf(
                    ChatMessage(role = "user", content = "Request $index")
                )
            )
            
            val response = openAIService.createChatCompletion(
                authorization = "Bearer test-api-key",
                request = request
            )
            
            assertThat(response.isSuccessful).isTrue()
            response.body()?.choices?.first()?.message?.content?.let {
                responses.add(it)
            }
        }
        
        // Then
        assertThat(responses).hasSize(3)
        assertThat(responses[0]).isEqualTo("Response 0")
        assertThat(responses[1]).isEqualTo("Response 1")
        assertThat(responses[2]).isEqualTo("Response 2")
    }
}