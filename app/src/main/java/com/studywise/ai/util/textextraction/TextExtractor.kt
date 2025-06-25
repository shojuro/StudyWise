package com.studywise.ai.util.textextraction

interface TextExtractor {
    suspend fun extractText(source: Any): Result<String>
    
    fun validateExtractedText(text: String): Result<String> {
        return when {
            text.isBlank() -> Result.failure(TextExtractionException("No text found"))
            text.length < 50 -> Result.failure(TextExtractionException("Text too short. Please provide at least 50 characters."))
            else -> Result.success(text.trim())
        }
    }
}

class TextExtractionException(message: String) : Exception(message)

enum class InputMethod {
    TEXT,
    CAMERA,
    DOCUMENT,
    VOICE
}