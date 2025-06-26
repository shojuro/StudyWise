package com.studywise.ai.util.textextraction

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) : TextExtractor {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    override suspend fun extractText(source: Any): Result<String> = withContext(Dispatchers.IO) {
        try {
            when (source) {
                is Uri -> extractFromUri(source)
                is String -> extractFromUri(Uri.parse(source))
                else -> Result.failure(TextExtractionException("Invalid source type for photo extraction"))
            }
        } catch (e: Exception) {
            Result.failure(TextExtractionException("Failed to extract text from image: ${e.message}"))
        }
    }
    
    private suspend fun extractFromUri(uri: Uri): Result<String> {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val result = recognizer.process(image).await()
            
            val extractedText = result.textBlocks.joinToString("\n") { block ->
                block.text
            }
            
            validateExtractedText(extractedText)
        } catch (e: Exception) {
            Result.failure(TextExtractionException("OCR failed: ${e.message}"))
        }
    }
    
    fun close() {
        recognizer.close()
    }
}