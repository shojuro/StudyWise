package com.studywise.ai.util.textextraction

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentTextExtractor @Inject constructor(
    private val contentResolver: ContentResolver,
    private val photoTextExtractor: PhotoTextExtractor
) : TextExtractor {
    
    override suspend fun extractText(source: Any): Result<String> = withContext(Dispatchers.IO) {
        try {
            when (source) {
                is Uri -> extractFromUri(source)
                is String -> extractFromUri(Uri.parse(source))
                else -> Result.failure(TextExtractionException("Invalid source type for document extraction"))
            }
        } catch (e: Exception) {
            Result.failure(TextExtractionException("Failed to extract text from document: ${e.message}"))
        }
    }
    
    private suspend fun extractFromUri(uri: Uri): Result<String> {
        val mimeType = contentResolver.getType(uri)
        
        return when {
            // For now, use OCR for all document types as a fallback
            mimeType?.contains("image") == true -> {
                photoTextExtractor.extractText(uri)
            }
            mimeType?.contains("pdf") == true -> {
                Result.failure(TextExtractionException(
                    "PDF files are not yet supported. Please take a photo of the document instead."
                ))
            }
            mimeType?.contains("wordprocessingml") == true || 
            uri.toString().endsWith(".docx") -> {
                Result.failure(TextExtractionException(
                    "Word documents are not yet supported. Please take a photo of the document instead."
                ))
            }
            else -> {
                // Try OCR as fallback for unknown types
                photoTextExtractor.extractText(uri)
            }
        }
    }
}