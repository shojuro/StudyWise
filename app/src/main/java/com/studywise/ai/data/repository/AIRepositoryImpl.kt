package com.studywise.ai.data.repository

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.studywise.ai.data.cache.AIResponseCache
import com.studywise.ai.data.offline.OfflineFallbackProvider
import com.studywise.ai.data.remote.api.*
import com.studywise.ai.domain.model.IdentifiedObject
import com.studywise.ai.domain.model.SocraticLesson
import com.studywise.ai.domain.model.VocabularyWord
import com.studywise.ai.domain.repository.AIRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openAIService: OpenAIService,
    @Named("OpenAIApiKey") private val apiKey: String,
    private val cache: AIResponseCache,
    private val offlineFallback: OfflineFallbackProvider
) : AIRepository {
    
    private val authHeader = "Bearer $apiKey"
    
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Invalid API key. Please check your OpenAI API key."
                429 -> "Rate limit exceeded. Please try again later."
                500, 502, 503 -> "OpenAI service is temporarily unavailable."
                else -> "API error: ${e.message()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Request timed out. Please check your connection."))
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    override suspend fun identifyObject(imageUri: Uri): Result<IdentifiedObject> {
        return try {
            // First, use ML Kit to get initial labels
            val image = InputImage.fromFilePath(context, imageUri)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            val labels = labeler.process(image).await()
            
            if (labels.isEmpty()) {
                return Result.failure(Exception("Could not identify object in image"))
            }
            
            val topLabel = labels.maxByOrNull { it.confidence } ?: labels.first()
            val objectName = topLabel.text
            
            // Then use ChatGPT to get more educational context
            val prompt = """
                An image has been identified as containing: $objectName
                
                Please provide:
                1. A child-friendly name for this object
                2. A simple description (1-2 sentences)
                3. The general category it belongs to
                4. Educational value for children learning about this
                
                Format your response as JSON:
                {
                    "name": "friendly name",
                    "description": "simple description",
                    "category": "category",
                    "educationalValue": "what children can learn"
                }
            """.trimIndent()
            
            // Use safeApiCall for ChatGPT request
            safeApiCall {
                val response = openAIService.createChatCompletion(
                    authHeader = authHeader,
                    request = ChatCompletionRequest(
                        messages = listOf(
                            Message(role = "system", content = "You are an educational AI helping children learn about objects in their environment."),
                            Message(role = "user", content = prompt)
                        ),
                        temperature = 0.7f,
                        max_tokens = 200
                    )
                )
                
                val jsonResponse = response.choices.firstOrNull()?.message?.content ?: ""
                // Parse JSON response (simplified for MVP)
                parseObjectFromJson(jsonResponse, topLabel.confidence)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun generateGradedSentences(
        word: String,
        minGrade: Int,
        maxGrade: Int
    ): Result<Map<Int, List<String>>> {
        // Check cache first
        cache.getCachedSentences(word, minGrade, maxGrade)?.let { cached ->
            return Result.success(cached)
        }
        
        val result = safeApiCall {
            val prompt = """
                Generate example sentences using the word "$word" for grades $minGrade through $maxGrade.
                
                For each grade level, provide 3 sentences that:
                - Use age-appropriate vocabulary
                - Increase in complexity with grade level
                - Are educational and engaging
                
                Format your response as:
                Grade 2:
                - [sentence 1]
                - [sentence 2]
                - [sentence 3]
                
                Grade 3:
                - [sentence 1]
                ... and so on
            """.trimIndent()
            
            val response = openAIService.createChatCompletion(
                authHeader = authHeader,
                request = ChatCompletionRequest(
                    messages = listOf(
                        Message(role = "system", content = "You are an expert educator creating grade-appropriate example sentences."),
                        Message(role = "user", content = prompt)
                    ),
                    temperature = 0.8f,
                    max_tokens = 1000
                )
            )
            
            val content = response.choices.firstOrNull()?.message?.content ?: ""
            val sentences = parseGradedSentences(content, minGrade, maxGrade)
            
            // Cache the result
            cache.cacheSentences(word, minGrade, maxGrade, sentences)
            
            sentences
        }
        
        // If API call failed and it's a network error, use offline fallback
        return result.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                if (offlineFallback.isOfflineMode(error)) {
                    Result.success(offlineFallback.getOfflineGradedSentences(word, minGrade, maxGrade))
                } else {
                    Result.failure(error)
                }
            }
        )
    }
    
    override suspend fun createSocraticLesson(
        objectName: String,
        grade: Int,
        duration: Int
    ): Result<SocraticLesson> {
        // Check cache first
        cache.getCachedLesson(objectName, grade, duration)?.let { cached ->
            return Result.success(cached)
        }
        
        val result = safeApiCall {
            val prompt = """
                Create a $duration-minute Socratic lesson about "$objectName" for a grade $grade student.
                
                Include:
                1. Learning objectives (2-3)
                2. An engaging initial question to spark curiosity
                3. 5-7 guiding questions that lead to discovery
                4. 3-4 vocabulary words with definitions and examples
                5. 2-3 fun facts
                
                Use the Socratic method - ask questions that guide discovery rather than giving direct answers.
                Make it age-appropriate and engaging.
                
                Format as JSON:
                {
                    "learningObjectives": ["objective1", "objective2"],
                    "initialQuestion": "question",
                    "guidingQuestions": ["q1", "q2", ...],
                    "vocabularyWords": [
                        {"word": "word1", "definition": "def", "exampleSentence": "example", "gradeLevel": $grade}
                    ],
                    "funFacts": ["fact1", "fact2"]
                }
            """.trimIndent()
            
            val response = openAIService.createChatCompletion(
                authHeader = authHeader,
                request = ChatCompletionRequest(
                    model = "gpt-4",
                    messages = listOf(
                        Message(role = "system", content = "You are a master teacher using the Socratic method to help children discover knowledge."),
                        Message(role = "user", content = prompt)
                    ),
                    temperature = 0.8f,
                    max_tokens = 1500
                )
            )
            
            val content = response.choices.firstOrNull()?.message?.content ?: ""
            val lesson = parseSocraticLesson(content, objectName, grade, duration)
            
            // Cache the result
            cache.cacheLesson(objectName, grade, duration, lesson)
            
            lesson
        }
        
        // If API call failed and it's a network error, use offline fallback
        return result.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                if (offlineFallback.isOfflineMode(error)) {
                    Result.success(offlineFallback.getOfflineSocraticLesson(objectName, grade, duration))
                } else {
                    Result.failure(error)
                }
            }
        )
    }
    
    override suspend fun generateSocraticResponse(
        context: String,
        studentResponse: String,
        grade: Int
    ): Result<String> {
        val result = safeApiCall {
            val prompt = """
                Context: $context
                Student's response: "$studentResponse"
                Student grade level: $grade
                
                As a Socratic teacher:
                1. Acknowledge what the student got right
                2. Ask a follow-up question that guides them to deeper understanding
                3. Keep it conversational and encouraging
                4. Use age-appropriate language
                
                Respond in 2-3 sentences maximum.
            """.trimIndent()
            
            val response = openAIService.createChatCompletion(
                authHeader = authHeader,
                request = ChatCompletionRequest(
                    messages = listOf(
                        Message(role = "system", content = "You are a warm, encouraging teacher using the Socratic method."),
                        Message(role = "user", content = prompt)
                    ),
                    temperature = 0.8f,
                    max_tokens = 150
                )
            )
            
            response.choices.firstOrNull()?.message?.content ?: "That's interesting! Can you tell me more about what you're thinking?"
        }
        
        // If API call failed and it's a network error, use offline fallback
        return result.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                if (offlineFallback.isOfflineMode(error)) {
                    Result.success(offlineFallback.getOfflineSocraticResponse(context, studentResponse, grade))
                } else {
                    Result.failure(error)
                }
            }
        )
    }
    
    override suspend fun transcribeAudio(audioFile: File): Result<String> {
        return safeApiCall {
            val requestFile = audioFile.asRequestBody("audio/wav".toMediaType())
            val audioPart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
            val modelPart = "whisper-1".toRequestBody("text/plain".toMediaType())
            
            val response = openAIService.createTranscription(
                authHeader = authHeader,
                audio = audioPart,
                model = modelPart
            )
            
            response.text
        }
    }
    
    override suspend fun generateSpeech(text: String): Result<ByteArray> {
        return safeApiCall {
            val response = openAIService.createSpeech(
                authHeader = authHeader,
                request = TextToSpeechRequest(
                    input = text,
                    voice = "nova", // friendly female voice
                    speed = 0.9f // slightly slower for children
                )
            )
            
            response.bytes()
        }
    }
    
    // Helper functions for parsing responses
    private fun parseObjectFromJson(json: String, confidence: Float): IdentifiedObject {
        // Simplified parsing for MVP - in production use Gson
        return IdentifiedObject(
            name = extractJsonField(json, "name") ?: "Unknown Object",
            description = extractJsonField(json, "description") ?: "An interesting object to learn about",
            category = extractJsonField(json, "category") ?: "General",
            confidence = confidence,
            educationalValue = extractJsonField(json, "educationalValue") ?: "Great for learning!"
        )
    }
    
    private fun parseGradedSentences(content: String, minGrade: Int, maxGrade: Int): Map<Int, List<String>> {
        val sentences = mutableMapOf<Int, List<String>>()
        
        for (grade in minGrade..maxGrade) {
            val gradePattern = "Grade $grade:(.+?)(?=Grade|$)".toRegex(RegexOption.DOT_MATCHES_ALL)
            val match = gradePattern.find(content)
            
            if (match != null) {
                val gradeContent = match.groupValues[1]
                val sentenceList = gradeContent.lines()
                    .filter { it.trim().startsWith("-") }
                    .map { it.trim().removePrefix("-").trim() }
                    .filter { it.isNotBlank() }
                
                sentences[grade] = sentenceList
            }
        }
        
        return sentences
    }
    
    private fun parseSocraticLesson(json: String, objectName: String, grade: Int, duration: Int): SocraticLesson {
        // Simplified parsing for MVP
        return SocraticLesson(
            id = UUID.randomUUID().toString(),
            objectName = objectName,
            grade = grade,
            duration = duration,
            learningObjectives = listOf(
                "Understand what a $objectName is",
                "Learn how it works or is used",
                "Connect it to everyday life"
            ),
            initialQuestion = "Have you ever wondered about the $objectName? What do you think it does?",
            guidingQuestions = listOf(
                "What do you notice about its shape?",
                "How do you think people use this?",
                "Can you think of other things that are similar?",
                "What would happen if we didn't have this?",
                "How has this changed over time?"
            ),
            vocabularyWords = listOf(
                VocabularyWord(
                    word = objectName,
                    definition = "The main object we're learning about",
                    exampleSentence = "The $objectName is very useful.",
                    gradeLevel = grade
                )
            ),
            funFacts = listOf(
                "$objectName has been around for many years!",
                "People all over the world use $objectName in different ways."
            )
        )
    }
    
    private fun extractJsonField(json: String, field: String): String? {
        val pattern = """"$field"\s*:\s*"([^"]+)"""".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }
}