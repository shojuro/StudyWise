package com.studywise.ai.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MistralApiService {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @retrofit2.http.Header("Authorization") authHeader: String,
        @Body request: MistralChatCompletionRequest
    ): MistralChatResponse
}

data class MistralChatCompletionRequest(
    val model: String = "mistral-small-latest", // Cost-effective model
    val messages: List<MistralMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 500,
    val top_p: Float = 1f,
    val stream: Boolean = false
)

data class MistralMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

data class MistralChatResponse(
    val id: String,
    @SerializedName("object")
    val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<MistralChoice>,
    val usage: MistralUsage
)

data class MistralChoice(
    val index: Int,
    val message: MistralMessage,
    val finish_reason: String?
)

data class MistralUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)