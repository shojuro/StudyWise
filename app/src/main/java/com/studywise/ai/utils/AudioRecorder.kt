package com.studywise.ai.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    
    fun startRecording(): Result<Unit> {
        return try {
            if (isRecording) {
                return Result.failure(IllegalStateException("Already recording"))
            }
            
            // Create output file
            outputFile = File(context.cacheDir, "audio_recording_${System.currentTimeMillis()}.m4a")
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile?.absolutePath)
                
                prepare()
                start()
            }
            
            isRecording = true
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalStateException) {
            Result.failure(e)
        }
    }
    
    fun stopRecording(): Result<File> {
        return try {
            if (!isRecording) {
                return Result.failure(IllegalStateException("Not recording"))
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            outputFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    Result.success(file)
                } else {
                    Result.failure(IOException("Recording file is empty or doesn't exist"))
                }
            } ?: Result.failure(IOException("No output file"))
        } catch (e: RuntimeException) {
            // Handle stop() called immediately after start()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            Result.failure(e)
        }
    }
    
    fun cancelRecording() {
        if (isRecording) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                // Ignore errors during cancellation
            }
            mediaRecorder = null
            isRecording = false
            
            // Delete the incomplete file
            outputFile?.delete()
            outputFile = null
        }
    }
    
    fun isRecording(): Boolean = isRecording
    
    fun release() {
        cancelRecording()
    }
}