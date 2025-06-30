package com.studywise.ai.data.repository

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.studywise.ai.BuildConfig
import com.studywise.ai.data.cache.AIResponseCache
import com.studywise.ai.data.offline.OfflineFallbackProvider
import com.studywise.ai.data.remote.api.*
import com.studywise.ai.domain.model.IdentifiedObject
import com.studywise.ai.domain.model.SocraticLesson
import com.studywise.ai.domain.model.VocabularyWord
import com.studywise.ai.domain.repository.AIRepository
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class HybridAIRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openAIService: OpenAIService,
    private val mistralService: MistralApiService,
    private val cache: AIResponseCache,
    private val offlineFallback: OfflineFallbackProvider
) : AIRepository {
    
    private val openAIAuthHeader = "Bearer ${BuildConfig.OPENAI_API_KEY}"
    private val mistralAuthHeader = "Bearer ${BuildConfig.MISTRAL_API_KEY}"
    
    init {
        // Debug logging for API keys (only log first few characters for security)
        Log.d("HybridAIRepository", "OpenAI key configured: ${BuildConfig.OPENAI_API_KEY.take(10)}...")
        Log.d("HybridAIRepository", "Mistral key configured: ${BuildConfig.MISTRAL_API_KEY.take(10)}...")
        
        // Check if keys are placeholder values
        if (BuildConfig.MISTRAL_API_KEY == "YOUR_MISTRAL_API_KEY_HERE") {
            Log.w("HybridAIRepository", "Mistral API key is still placeholder!")
        }
        if (BuildConfig.OPENAI_API_KEY == "YOUR_API_KEY_HERE") {
            Log.w("HybridAIRepository", "OpenAI API key is still placeholder!")
        }
    }
    
    // Use Mistral for cost-effective content generation
    // IMPORTANT: OpenAI API key was compromised, only use Mistral
    private val useMistralForContent = true
    private val useOnlyMistral = true
    
    // Common objects that ML Kit struggles with
    private val commonSchoolObjects = listOf(
        "pen", "pencil", "paper", "notebook", "eraser", "ruler", 
        "scissors", "glue", "stapler", "tape", "marker", "crayon",
        "backpack", "folder", "binder", "calculator", "book"
    )
    
    override suspend fun identifyObject(imageUri: Uri): Result<IdentifiedObject> {
        return try {
            // Use ML Kit for object detection
            val image = InputImage.fromFilePath(context, imageUri)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            val labels = labeler.process(image).await()
            
            // Log all detected labels for debugging
            labels.forEach { label ->
                Log.d("HybridAIRepository", "Detected: ${label.text} with confidence ${label.confidence}")
            }
            
            if (labels.isEmpty()) {
                return Result.failure(Exception("Could not identify object in image"))
            }
            
            val topLabel = labels.maxByOrNull { it.confidence } ?: labels.first()
            
            // Enhanced confidence threshold
            if (topLabel.confidence < 0.7f) {
                // Check if any detected label matches common objects
                val detectedCommonObject = labels.find { label ->
                    commonSchoolObjects.any { common -> 
                        label.text.lowercase().contains(common) || common.contains(label.text.lowercase())
                    }
                }
                
                if (detectedCommonObject != null) {
                    Log.d("HybridAIRepository", "Found common object despite low confidence: ${detectedCommonObject.text}")
                    // Use the common object even with lower confidence
                    return generateContentForObject(detectedCommonObject.text, detectedCommonObject.confidence)
                }
                
                return Result.failure(Exception("Unable to identify object with high confidence (${(topLabel.confidence * 100).toInt()}%). Please try another photo or enter the object manually."))
            }
            
            val objectName = topLabel.text
            val confidence = topLabel.confidence
            
            return generateContentForObject(objectName, confidence)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun generateContentForObject(objectName: String, confidence: Float): Result<IdentifiedObject> {
        // Always use Mistral since OpenAI key was compromised
        return if (BuildConfig.MISTRAL_API_KEY != "YOUR_MISTRAL_API_KEY_HERE") {
            generateEducationalContentWithMistral(objectName, confidence)
        } else {
            // No API key available, use offline fallback
            Result.failure(Exception("AI service unavailable. Please configure Mistral API key."))
        }
    }
    
    override suspend fun generateContentForManualObject(objectName: String): Result<IdentifiedObject> {
        // For manually entered objects, use 100% confidence
        return generateContentForObject(objectName, 1.0f)
    }
    
    private suspend fun generateEducationalContentWithMistral(
        objectName: String, 
        confidence: Float
    ): Result<IdentifiedObject> {
        return safeApiCall {
            val prompt = """
                You are an expert English teacher and educational content creator. Always use perfect grammar, proper punctuation, and age-appropriate language.
                
                An image has been identified as containing: $objectName (confidence: ${(confidence * 100).toInt()}%)
                
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
            
            val response = mistralService.createChatCompletion(
                authHeader = mistralAuthHeader,
                request = MistralChatCompletionRequest(
                    messages = listOf(
                        MistralMessage(
                            role = "system", 
                            content = "You are an educational AI helping children learn about objects in their environment. Always use perfect grammar."
                        ),
                        MistralMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.7f,
                    max_tokens = 200
                )
            )
            
            val jsonResponse = response.choices.firstOrNull()?.message?.content ?: ""
            parseObjectFromJson(jsonResponse, confidence)
        }
    }
    
    private suspend fun generateEducationalContentWithOpenAI(
        objectName: String, 
        confidence: Float
    ): Result<IdentifiedObject> {
        // Existing OpenAI implementation
        val prompt = """
            You are an expert English teacher and educational content creator. Always use perfect grammar, proper punctuation, and age-appropriate language.
            
            An image has been identified as containing: $objectName (confidence: ${(confidence * 100).toInt()}%)
            
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
        
        return safeApiCall {
            val response = openAIService.createChatCompletion(
                authHeader = openAIAuthHeader,
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
    
    override suspend fun generateGradedSentences(
        word: String,
        minGrade: Int,
        maxGrade: Int
    ): Result<Map<Int, List<String>>> {
        // Check cache first
        cache.getCachedSentences(word, minGrade, maxGrade)?.let { cached ->
            return Result.success(cached)
        }
        
        // Always use Mistral since OpenAI key was compromised
        val result = if (BuildConfig.MISTRAL_API_KEY != "YOUR_MISTRAL_API_KEY_HERE") {
            generateSentencesWithMistral(word, minGrade, maxGrade)
        } else {
            Result.failure(Exception("AI service unavailable. Please configure Mistral API key."))
        }
        
        // Cache successful results
        result.getOrNull()?.let { sentences ->
            cache.cacheSentences(word, minGrade, maxGrade, sentences)
        }
        
        // Fallback to offline if API fails
        if (result.isFailure && offlineFallback.isOfflineMode(result.exceptionOrNull())) {
            return Result.success(
                offlineFallback.getOfflineGradedSentences(word, minGrade, maxGrade)
            )
        }
        
        return result
    }
    
    private suspend fun generateSentencesWithMistral(
        word: String,
        minGrade: Int,
        maxGrade: Int
    ): Result<Map<Int, List<String>>> {
        return safeApiCall {
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
            
            val response = mistralService.createChatCompletion(
                authHeader = mistralAuthHeader,
                request = MistralChatCompletionRequest(
                    messages = listOf(
                        MistralMessage(
                            role = "system", 
                            content = "You are an expert educator creating perfect educational content."
                        ),
                        MistralMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.7f,
                    max_tokens = 500
                )
            )
            
            val content = response.choices.firstOrNull()?.message?.content ?: ""
            parseGradedSentences(content, minGrade, maxGrade)
        }
    }
    
    private suspend fun generateSentencesWithOpenAI(
        word: String,
        minGrade: Int,
        maxGrade: Int
    ): Result<Map<Int, List<String>>> {
        // Existing OpenAI implementation
        return safeApiCall {
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
                authHeader = openAIAuthHeader,
                request = ChatCompletionRequest(
                    messages = listOf(
                        Message(role = "system", content = "You are an expert educator."),
                        Message(role = "user", content = prompt)
                    ),
                    temperature = 0.7f,
                    max_tokens = 500
                )
            )
            
            val content = response.choices.firstOrNull()?.message?.content ?: ""
            parseGradedSentences(content, minGrade, maxGrade)
        }
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
        
        // Always use Mistral since OpenAI key was compromised
        val result = if (BuildConfig.MISTRAL_API_KEY != "YOUR_MISTRAL_API_KEY_HERE") {
            createLessonWithMistral(objectName, grade, duration)
        } else {
            Result.failure(Exception("AI service unavailable. Please configure Mistral API key."))
        }
        
        // Cache successful results
        result.getOrNull()?.let { lesson ->
            cache.cacheLesson(objectName, grade, duration, lesson)
        }
        
        return result
    }
    
    private suspend fun createLessonWithMistral(
        objectName: String,
        grade: Int,
        duration: Int
    ): Result<SocraticLesson> {
        return safeApiCall {
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
            
            val response = mistralService.createChatCompletion(
                authHeader = mistralAuthHeader,
                request = MistralChatCompletionRequest(
                    messages = listOf(
                        MistralMessage(
                            role = "system", 
                            content = "You are an expert Socratic educator creating perfect educational content."
                        ),
                        MistralMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.7f,
                    max_tokens = 600
                )
            )
            
            val jsonResponse = response.choices.firstOrNull()?.message?.content ?: ""
            parseSocraticLesson(jsonResponse, objectName, grade, duration)
        }
    }
    
    private suspend fun createLessonWithOpenAI(
        objectName: String,
        grade: Int,
        duration: Int
    ): Result<SocraticLesson> {
        // Existing OpenAI implementation (already updated with grammar fixes)
        return safeApiCall {
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
                authHeader = openAIAuthHeader,
                request = ChatCompletionRequest(
                    messages = listOf(
                        Message(role = "system", content = "You are an expert Socratic educator."),
                        Message(role = "user", content = prompt)
                    ),
                    temperature = 0.7f,
                    max_tokens = 600
                )
            )
            
            val jsonResponse = response.choices.firstOrNull()?.message?.content ?: ""
            parseSocraticLesson(jsonResponse, objectName, grade, duration)
        }
    }
    
    // Implement remaining methods from AIRepository interface
    override suspend fun generateSocraticResponse(
        context: String,
        studentResponse: String,
        grade: Int
    ): Result<String> {
        // Always use Mistral since OpenAI key was compromised
        return if (BuildConfig.MISTRAL_API_KEY != "YOUR_MISTRAL_API_KEY_HERE") {
            generateResponseWithMistral(context, studentResponse, grade)
        } else {
            Result.failure(Exception("AI service unavailable. Please configure Mistral API key."))
        }
    }
    
    private suspend fun generateResponseWithMistral(
        context: String,
        studentResponse: String,
        grade: Int
    ): Result<String> {
        return safeApiCall {
            val prompt = """
                Context: $context
                Student's response: "$studentResponse"
                Grade level: $grade
                
                As a Socratic teacher with perfect English grammar:
                1. Acknowledge their response positively
                2. Guide them deeper with a grammatically perfect follow-up question
                3. Keep it age-appropriate and encouraging
                4. Use perfect grammar and punctuation
                
                Respond in 2-3 sentences maximum.
            """.trimIndent()
            
            val response = mistralService.createChatCompletion(
                authHeader = mistralAuthHeader,
                request = MistralChatCompletionRequest(
                    messages = listOf(
                        MistralMessage(
                            role = "system", 
                            content = "You are a Socratic teacher who always uses perfect grammar."
                        ),
                        MistralMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.8f,
                    max_tokens = 150
                )
            )
            
            response.choices.firstOrNull()?.message?.content ?: "That's interesting! Can you tell me more?"
        }
    }
    
    private suspend fun generateResponseWithOpenAI(
        context: String,
        studentResponse: String,
        grade: Int
    ): Result<String> {
        return safeApiCall {
            val prompt = """
                Context: $context
                Student's response: "$studentResponse"
                Grade level: $grade
                
                As a Socratic teacher with perfect English grammar:
                1. Acknowledge their response positively
                2. Guide them deeper with a grammatically perfect follow-up question
                3. Keep it age-appropriate and encouraging
                4. Use perfect grammar and punctuation
                
                Respond in 2-3 sentences maximum.
            """.trimIndent()
            
            val response = openAIService.createChatCompletion(
                authHeader = openAIAuthHeader,
                request = ChatCompletionRequest(
                    messages = listOf(
                        Message(role = "system", content = "You are a Socratic teacher."),
                        Message(role = "user", content = prompt)
                    ),
                    temperature = 0.8f,
                    max_tokens = 150
                )
            )
            
            response.choices.firstOrNull()?.message?.content ?: "That's interesting! Can you tell me more?"
        }
    }
    
    override suspend fun generateSpeech(text: String): Result<ByteArray> {
        // Speech synthesis disabled - OpenAI key was compromised and Mistral doesn't support it
        return Result.failure(Exception("Speech synthesis is temporarily unavailable."))
    }
    
    override suspend fun transcribeAudio(audioFile: File): Result<String> {
        // Audio transcription disabled - OpenAI key was compromised and Mistral doesn't support it
        return Result.failure(Exception("Audio transcription is temporarily unavailable."))
    }
    
    // Helper methods
    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("HybridAIRepository", "API Error ${e.code()}: $errorBody")
            
            val errorMessage = when (e.code()) {
                401 -> {
                    // Log which API is failing
                    val apiType = if (errorBody?.contains("mistral") == true) "Mistral" else "OpenAI"
                    Log.e("HybridAIRepository", "$apiType API authentication failed")
                    "Invalid $apiType API key. Please check your API configuration."
                }
                429 -> "Rate limit exceeded. Please try again later."
                500, 502, 503 -> "AI service is temporarily unavailable. Please try again."
                else -> "API error (${e.code()}): ${e.message()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Request timed out. Please check your connection."))
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Log.e("HybridAIRepository", "Unexpected error: ${e.message}", e)
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    private fun parseObjectFromJson(json: String, confidence: Float): IdentifiedObject {
        // Simplified parsing for MVP - in production use Gson
        val name = extractJsonField(json, "name") ?: "Unknown Object"
        val article = if (name.first().lowercaseChar() in listOf('a', 'e', 'i', 'o', 'u')) "an" else "a"
        
        return IdentifiedObject(
            name = name,
            description = extractJsonField(json, "description") ?: "This is $article interesting object to learn about.",
            category = extractJsonField(json, "category") ?: "General",
            confidence = confidence,
            educationalValue = extractJsonField(json, "educationalValue") ?: "Learning about this helps us understand our world better!"
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
        // Simplified parsing - in production use proper JSON parsing
        val objectives = extractJsonArray(json, "learningObjectives") ?: listOf("Learn about the object")
        val initialQuestion = extractJsonField(json, "initialQuestion") ?: "What do you notice about this?"
        val guidingQuestions = extractJsonArray(json, "guidingQuestions") ?: listOf()
        val funFacts = extractJsonArray(json, "funFacts") ?: listOf()
        
        return SocraticLesson(
            id = java.util.UUID.randomUUID().toString(),
            objectName = objectName,
            grade = grade,
            duration = duration,
            learningObjectives = objectives,
            initialQuestion = initialQuestion,
            guidingQuestions = guidingQuestions,
            vocabularyWords = parseVocabularyWords(json),
            funFacts = funFacts
        )
    }
    
    private fun extractJsonField(json: String, field: String): String? {
        val pattern = """"$field"\s*:\s*"([^"]+)"""".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }
    
    private fun extractJsonArray(json: String, field: String): List<String>? {
        val pattern = """"$field"\s*:\s*\[([^\]]+)\]""".toRegex()
        val match = pattern.find(json) ?: return null
        val arrayContent = match.groupValues[1]
        
        return arrayContent.split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    }
    
    private fun parseVocabularyWords(json: String): List<VocabularyWord> {
        // Simplified parsing
        return listOf() // Would parse vocabulary words from JSON
    }
}