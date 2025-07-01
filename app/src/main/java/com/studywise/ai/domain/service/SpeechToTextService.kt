package com.studywise.ai.domain.service

import java.io.File

interface SpeechToTextService {
    suspend fun transcribeAudio(audioFile: File): String
    suspend fun startRealtimeTranscription(onTranscription: (String) -> Unit)
    suspend fun stopRealtimeTranscription()
    fun isAvailable(): Boolean
}