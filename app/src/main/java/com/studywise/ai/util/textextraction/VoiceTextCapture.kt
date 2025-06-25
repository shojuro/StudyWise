package com.studywise.ai.util.textextraction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceTextCapture @Inject constructor(
    private val context: Context
) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private val fullTranscript = StringBuilder()
    
    sealed class VoiceState {
        object Idle : VoiceState()
        object Listening : VoiceState()
        data class Transcribing(val partialText: String) : VoiceState()
        data class Success(val text: String) : VoiceState()
        data class Error(val message: String) : VoiceState()
    }
    
    fun startListening(): Flow<VoiceState> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            trySend(VoiceState.Error("Speech recognition not available"))
            close()
            return@callbackFlow
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    trySend(VoiceState.Listening)
                }
                
                override fun onBeginningOfSpeech() {
                    // Speech input started
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Volume change
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // Audio buffer received
                }
                
                override fun onEndOfSpeech() {
                    // User stopped speaking
                }
                
                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Unknown error"
                    }
                    trySend(VoiceState.Error(message))
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        fullTranscript.append(text).append(" ")
                        trySend(VoiceState.Success(fullTranscript.toString().trim()))
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!partialMatches.isNullOrEmpty()) {
                        trySend(VoiceState.Transcribing(fullTranscript.toString() + partialMatches[0]))
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Handle events
                }
            })
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer?.startListening(intent)
        
        awaitClose {
            stopListening()
        }
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    fun clearTranscript() {
        fullTranscript.clear()
    }
}