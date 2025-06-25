package com.studywise.ai.data.cache

import android.content.Context
import androidx.collection.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIResponseCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Cache for graded sentences - key: "sentences_word_minGrade_maxGrade"
    private val sentenceCache = LruCache<String, CachedResponse<Map<Int, List<String>>>>(20)
    
    // Cache for Socratic lessons - key: "lesson_object_grade_duration"
    private val lessonCache = LruCache<String, CachedResponse<com.studywise.ai.domain.model.SocraticLesson>>(10)
    
    // Cache for object identification - key: "object_imageHash"
    private val objectCache = LruCache<String, CachedResponse<com.studywise.ai.domain.model.IdentifiedObject>>(30)
    
    private val cacheExpirationMs = TimeUnit.MINUTES.toMillis(30) // 30 minutes expiration
    
    data class CachedResponse<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
        val expirationMs: Long = TimeUnit.MINUTES.toMillis(30)
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > expirationMs
        }
    }
    
    fun getCachedSentences(word: String, minGrade: Int, maxGrade: Int): Map<Int, List<String>>? {
        val key = "sentences_${word}_${minGrade}_${maxGrade}"
        val cached = sentenceCache.get(key)
        return if (cached != null && !cached.isExpired()) {
            cached.data
        } else {
            sentenceCache.remove(key)
            null
        }
    }
    
    fun cacheSentences(word: String, minGrade: Int, maxGrade: Int, sentences: Map<Int, List<String>>) {
        val key = "sentences_${word}_${minGrade}_${maxGrade}"
        sentenceCache.put(key, CachedResponse(sentences))
    }
    
    fun getCachedLesson(objectName: String, grade: Int, duration: Int): com.studywise.ai.domain.model.SocraticLesson? {
        val key = "lesson_${objectName}_${grade}_${duration}"
        val cached = lessonCache.get(key)
        return if (cached != null && !cached.isExpired()) {
            cached.data
        } else {
            lessonCache.remove(key)
            null
        }
    }
    
    fun cacheLesson(objectName: String, grade: Int, duration: Int, lesson: com.studywise.ai.domain.model.SocraticLesson) {
        val key = "lesson_${objectName}_${grade}_${duration}"
        lessonCache.put(key, CachedResponse(lesson))
    }
    
    fun getCachedObject(imageHash: String): com.studywise.ai.domain.model.IdentifiedObject? {
        val key = "object_$imageHash"
        val cached = objectCache.get(key)
        return if (cached != null && !cached.isExpired()) {
            cached.data
        } else {
            objectCache.remove(key)
            null
        }
    }
    
    fun cacheObject(imageHash: String, identifiedObject: com.studywise.ai.domain.model.IdentifiedObject) {
        val key = "object_$imageHash"
        objectCache.put(key, CachedResponse(identifiedObject))
    }
    
    fun clearAllCaches() {
        sentenceCache.evictAll()
        lessonCache.evictAll()
        objectCache.evictAll()
    }
    
    fun clearExpiredEntries() {
        // This could be called periodically to clean up expired entries
        val sentenceKeys = sentenceCache.snapshot().keys
        sentenceKeys.forEach { key ->
            val cached = sentenceCache.get(key)
            if (cached?.isExpired() == true) {
                sentenceCache.remove(key)
            }
        }
        
        val lessonKeys = lessonCache.snapshot().keys
        lessonKeys.forEach { key ->
            val cached = lessonCache.get(key)
            if (cached?.isExpired() == true) {
                lessonCache.remove(key)
            }
        }
        
        val objectKeys = objectCache.snapshot().keys
        objectKeys.forEach { key ->
            val cached = objectCache.get(key)
            if (cached?.isExpired() == true) {
                objectCache.remove(key)
            }
        }
    }
}