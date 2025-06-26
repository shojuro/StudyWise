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
    
    override suspend fun generateContentForManualObject(objectName: String): Result<IdentifiedObject> {
        // For AIRepositoryImpl (non-hybrid), generate content directly
        // Use the same approach as identifyObject but with manual input
        return generateEducationalContentForObject(objectName, 1.0f)
    }
    
    private suspend fun generateEducationalContentForObject(objectName: String, confidence: Float): Result<IdentifiedObject> {
        val prompt = """
            You are an expert English teacher and educational content creator. Always use perfect grammar, proper punctuation, and age-appropriate language.
            
            Please provide educational content about: $objectName
            
            IMPORTANT: Use grammatically perfect English.
            
            1. A child-friendly name for this object
            2. A simple, grammatically correct description (1-2 sentences)
            3. The general category it belongs to
            4. Educational value for children learning about this
            
            Format your response as JSON:
            {
                "name": "friendly name",
                "description": "simple description with perfect grammar",
                "category": "category",
                "educationalValue": "what children can learn"
            }
            
            Double-check your response for any spelling or grammar errors before responding.
        """.trimIndent()
        
        return safeApiCall {
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
            parseObjectFromJson(jsonResponse, confidence)
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
            
            // Add confidence threshold - reject low confidence detections
            if (topLabel.confidence < 0.7f) {
                return Result.failure(Exception("Unable to identify object with high confidence (${(topLabel.confidence * 100).toInt()}%). Please try another photo or enter the object manually."))
            }
            
            val objectName = topLabel.text
            
            // Then use ChatGPT to get more educational context
            val prompt = """
                You are an expert English teacher and educational content creator. Always use perfect grammar, proper punctuation, and age-appropriate language.
                
                An image has been identified as containing: $objectName (confidence: ${(topLabel.confidence * 100).toInt()}%)
                
                Please provide educational content about this object. IMPORTANT: Use grammatically perfect English.
                
                1. A child-friendly name for this object
                2. A simple, grammatically correct description (1-2 sentences)
                3. The general category it belongs to
                4. Educational value for children learning about this
                
                Format your response as JSON:
                {
                    "name": "friendly name",
                    "description": "simple description with perfect grammar",
                    "category": "category",
                    "educationalValue": "what children can learn"
                }
                
                Double-check your response for any spelling or grammar errors before responding.
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
                You are an expert English teacher. Generate grammatically perfect example sentences.
                
                Generate example sentences using the word "$word" for grades $minGrade through $maxGrade.
                
                CRITICAL: Every sentence must have perfect grammar, spelling, and punctuation.
                
                For each grade level, provide 3 sentences that:
                - Use age-appropriate vocabulary
                - Increase in complexity with grade level
                - Are educational and engaging
                - Have absolutely perfect grammar
                - Use proper capitalization and punctuation
                
                Common mistakes to avoid:
                - Double letters where they don't belong (wondereed → wondered)
                - Incorrect articles (the space → space, a apple → an apple)
                - Missing punctuation
                - Incomplete sentences
                
                Format your response as:
                Grade 2:
                - [grammatically perfect sentence 1]
                - [grammatically perfect sentence 2]
                - [grammatically perfect sentence 3]
                
                Grade 3:
                - [grammatically perfect sentence 1]
                ... and so on
                
                Double-check each sentence for errors before including it.
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
                You are an expert educator and English teacher. Create grammatically perfect educational content.
                
                Create a $duration-minute Socratic lesson about "$objectName" for a grade $grade student.
                
                CRITICAL REQUIREMENTS:
                - All questions must have perfect grammar and punctuation
                - Check for proper verb conjugation (wondered, not wondereed)
                - Use correct articles (a, an, the) appropriately
                - Ensure all sentences are complete and clear
                
                Include:
                1. Learning objectives (2-3) - grammatically correct
                2. An engaging initial question to spark curiosity (with perfect grammar)
                3. 5-7 guiding questions that lead to discovery (all grammatically correct)
                4. 3-4 vocabulary words with proper definitions and example sentences
                5. 2-3 fun facts written in proper English
                
                Use the Socratic method - ask questions that guide discovery rather than giving direct answers.
                Make it age-appropriate and engaging while maintaining perfect grammar.
                
                Example of good grammar: "Have you ever wondered about outer space?"
                Example of bad grammar: "Have you ever wondereed about the outer space?"
                
                Format as JSON:
                {
                    "learningObjectives": ["objective1", "objective2"],
                    "initialQuestion": "grammatically perfect question",
                    "guidingQuestions": ["grammatically correct q1", "grammatically correct q2", ...],
                    "vocabularyWords": [
                        {"word": "word1", "definition": "proper definition", "exampleSentence": "grammatically correct example", "gradeLevel": $grade}
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