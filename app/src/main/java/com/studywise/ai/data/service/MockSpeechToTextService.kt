package com.studywise.ai.data.service

import com.studywise.ai.domain.service.SpeechToTextService
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockSpeechToTextService @Inject constructor() : SpeechToTextService {
    
    private val sampleTranscriptions = listOf(
        "Hello, I'm excited to practice my English today. The weather is really nice.",
        "I went to the market yesterday and bought some fresh vegetables.",
        "My favorite hobby is reading books, especially science fiction novels.",
        "I think technology has changed our lives in many positive ways.",
        "Last weekend, I visited my family and we had a great time together.",
        "I'm learning English because I want to communicate with people from different countries.",
        "The most challenging part of learning English is pronunciation for me.",
        "I enjoy watching movies in English with subtitles to improve my listening skills."
    )
    
    override suspend fun transcribeAudio(audioFile: File): String {
        // Simulate processing time
        delay(Random.nextLong(1000, 3000))
        
        // Return a random sample transcription
        return sampleTranscriptions.random()
    }
    
    override suspend fun startRealtimeTranscription(onTranscription: (String) -> Unit) {
        // Mock implementation - would use actual speech recognition API
        val words = sampleTranscriptions.random().split(" ")
        words.forEach { word ->
            delay(300)
            onTranscription(word)
        }
    }
    
    override suspend fun stopRealtimeTranscription() {
        // Mock implementation
    }
    
    override fun isAvailable(): Boolean = true
}