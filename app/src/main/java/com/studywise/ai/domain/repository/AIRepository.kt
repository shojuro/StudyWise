package com.studywise.ai.domain.repository

import android.net.Uri
import com.studywise.ai.domain.model.IdentifiedObject
import com.studywise.ai.domain.model.SocraticLesson
import java.io.File

interface AIRepository {
    suspend fun identifyObject(imageUri: Uri): Result<IdentifiedObject>
    suspend fun generateContentForManualObject(objectName: String): Result<IdentifiedObject>
    suspend fun generateGradedSentences(word: String, minGrade: Int = 2, maxGrade: Int = 12): Result<Map<Int, List<String>>>
    suspend fun createSocraticLesson(objectName: String, grade: Int, duration: Int = 5): Result<SocraticLesson>
    suspend fun generateSocraticResponse(context: String, studentResponse: String, grade: Int): Result<String>
    suspend fun transcribeAudio(audioFile: File): Result<String>
    suspend fun generateSpeech(text: String): Result<ByteArray>
}