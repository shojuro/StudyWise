package com.studywise.ai.data.service

import android.content.Context
import android.util.Log
import com.studywise.ai.domain.model.AnalyticsEvent
import com.studywise.ai.domain.service.AnalyticsService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AnalyticsService {
    
    private val analyticsScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val analyticsDir = File(context.filesDir, "analytics")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    
    private var currentUserId: String? = null
    private val userProperties = mutableMapOf<String, String>()
    private var sessionStartTime: Long = 0
    
    init {
        // Ensure analytics directory exists
        analyticsDir.mkdirs()
    }
    
    override fun logEvent(event: AnalyticsEvent) {
        analyticsScope.launch {
            try {
                val eventData = JSONObject().apply {
                    put("event_name", event.eventName)
                    put("timestamp", timestampFormat.format(Date()))
                    put("user_id", currentUserId ?: "anonymous")
                    put("session_duration", if (sessionStartTime > 0) System.currentTimeMillis() - sessionStartTime else 0)
                    
                    // Add event parameters
                    val params = JSONObject()
                    event.parameters.forEach { (key, value) ->
                        params.put(key, value)
                    }
                    put("parameters", params)
                    
                    // Add user properties
                    val props = JSONObject()
                    userProperties.forEach { (key, value) ->
                        props.put(key, value)
                    }
                    put("user_properties", props)
                }
                
                // Write to daily log file
                val logFile = File(analyticsDir, "analytics_${dateFormat.format(Date())}.json")
                logFile.appendText(eventData.toString() + "\n")
                
                // Also log to Logcat for debugging
                Log.d("Analytics", "Event logged: ${event.eventName}")
                
                // In production, you would send this to a real analytics service like:
                // - Firebase Analytics
                // - Google Analytics
                // - Mixpanel
                // - Custom analytics backend
                
            } catch (e: Exception) {
                Log.e("Analytics", "Failed to log event: ${event.eventName}", e)
            }
        }
    }
    
    override fun setUserId(userId: String?) {
        currentUserId = userId
        userId?.let {
            userProperties["user_id"] = it
        }
    }
    
    override fun setUserProperty(key: String, value: String) {
        userProperties[key] = value
    }
    
    override fun logSessionStart() {
        sessionStartTime = System.currentTimeMillis()
        logEvent(AnalyticsEvent.AppOpened(currentUserId, null))
    }
    
    override fun logSessionEnd() {
        if (sessionStartTime > 0) {
            val duration = System.currentTimeMillis() - sessionStartTime
            logEvent(AnalyticsEvent.FeatureUsed("session_ended", currentUserId))
            sessionStartTime = 0
        }
    }
    
    // Helper function to get analytics data for display
    fun getAnalyticsSummary(): Map<String, Any> {
        return try {
            val today = dateFormat.format(Date())
            val todayFile = File(analyticsDir, "analytics_$today.json")
            
            var eventCount = 0
            val eventTypes = mutableMapOf<String, Int>()
            
            if (todayFile.exists()) {
                todayFile.forEachLine { line ->
                    try {
                        val json = JSONObject(line)
                        eventCount++
                        val eventName = json.getString("event_name")
                        eventTypes[eventName] = eventTypes.getOrDefault(eventName, 0) + 1
                    } catch (e: Exception) {
                        // Skip malformed lines
                    }
                }
            }
            
            mapOf(
                "total_events_today" to eventCount,
                "event_types" to eventTypes,
                "active_user_id" to (currentUserId ?: "anonymous")
            )
        } catch (e: Exception) {
            Log.e("Analytics", "Failed to get analytics summary", e)
            emptyMap()
        }
    }
    
    // Clean up old analytics files (keep last 30 days)
    fun cleanupOldAnalytics() {
        analyticsScope.launch {
            try {
                val cutoffDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
                analyticsDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < cutoffDate) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                Log.e("Analytics", "Failed to cleanup old analytics", e)
            }
        }
    }
}