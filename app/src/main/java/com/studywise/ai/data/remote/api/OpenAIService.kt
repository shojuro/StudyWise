package com.studywise.ai.data.remote.api

import retrofit2.http.*

interface OpenAIService {
    
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
    
    @POST("v1/audio/speech")
    suspend fun createSpeech(
        @Header("Authorization") authHeader: String,
        @Body request: TextToSpeechRequest
    ): okhttp3.ResponseBody
    
    @POST("v1/audio/transcriptions")
    @Multipart
    suspend fun createTranscription(
        @Header("Authorization") authHeader: String,
        @Part audio: okhttp3.MultipartBody.Part,
        @Part("model") model: okhttp3.RequestBody,
        @Part("language") language: okhttp3.RequestBody? = null
    ): TranscriptionResponse
}

// Request/Response Models
data class ChatCompletionRequest(
    val model: String = "gpt-4",
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    val max_tokens: Int? = null,
    val stream: Boolean = false
)

data class Message(
    val role: String, // "system", "user", "assistant"
    val content: String
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String?
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class TextToSpeechRequest(
    val model: String = "tts-1",
    val input: String,
    val voice: String = "nova", // alloy, echo, fable, onyx, nova, shimmer
    val response_format: String = "mp3",
    val speed: Float = 1.0f
)

data class TranscriptionResponse(
    val text: String
)