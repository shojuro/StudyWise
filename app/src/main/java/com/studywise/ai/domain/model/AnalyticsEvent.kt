package com.studywise.ai.domain.model

sealed class AnalyticsEvent(
    val eventName: String,
    val parameters: Map<String, Any> = emptyMap()
) {
    // Learning Events
    class SessionStarted(
        val subject: String,
        val userId: String
    ) : AnalyticsEvent(
        "learning_session_started",
        mapOf("subject" to subject, "user_id" to userId)
    )
    
    class SessionCompleted(
        val subject: String,
        val userId: String,
        val duration: Long,
        val questionsAnswered: Int,
        val masteryScore: Float
    ) : AnalyticsEvent(
        "learning_session_completed",
        mapOf(
            "subject" to subject,
            "user_id" to userId,
            "duration_seconds" to duration,
            "questions_answered" to questionsAnswered,
            "mastery_score" to masteryScore
        )
    )
    
    class QuestionAnswered(
        val subject: String,
        val skillId: String,
        val isCorrect: Boolean,
        val responseTime: Long
    ) : AnalyticsEvent(
        "question_answered",
        mapOf(
            "subject" to subject,
            "skill_id" to skillId,
            "is_correct" to isCorrect,
            "response_time_ms" to responseTime
        )
    )
    
    // Photo Learning Events
    class PhotoAnalyzed(
        val objectName: String,
        val confidence: Float
    ) : AnalyticsEvent(
        "photo_analyzed",
        mapOf(
            "object_name" to objectName,
            "confidence" to confidence
        )
    )
    
    class SocraticLessonStarted(
        val objectName: String,
        val grade: Int
    ) : AnalyticsEvent(
        "socratic_lesson_started",
        mapOf(
            "object_name" to objectName,
            "grade_level" to grade
        )
    )
    
    class VoiceInputUsed(
        val duration: Long
    ) : AnalyticsEvent(
        "voice_input_used",
        mapOf("duration_ms" to duration)
    )
    
    // User Progress Events
    class SkillMastered(
        val userId: String,
        val skillId: String,
        val subject: String,
        val masteryLevel: Float
    ) : AnalyticsEvent(
        "skill_mastered",
        mapOf(
            "user_id" to userId,
            "skill_id" to skillId,
            "subject" to subject,
            "mastery_level" to masteryLevel
        )
    )
    
    class AchievementUnlocked(
        val userId: String,
        val achievementId: String,
        val achievementName: String
    ) : AnalyticsEvent(
        "achievement_unlocked",
        mapOf(
            "user_id" to userId,
            "achievement_id" to achievementId,
            "achievement_name" to achievementName
        )
    )
    
    // App Usage Events
    class AppOpened(
        val userId: String?,
        val userRole: String?
    ) : AnalyticsEvent(
        "app_opened",
        buildMap {
            userId?.let { put("user_id", it) }
            userRole?.let { put("user_role", it) }
        }
    )
    
    class FeatureUsed(
        val featureName: String,
        val userId: String?
    ) : AnalyticsEvent(
        "feature_used",
        buildMap {
            put("feature_name", featureName)
            userId?.let { put("user_id", it) }
        }
    )
    
    // Error Events
    class ErrorOccurred(
        val errorType: String,
        val errorMessage: String,
        val screen: String
    ) : AnalyticsEvent(
        "error_occurred",
        mapOf(
            "error_type" to errorType,
            "error_message" to errorMessage,
            "screen" to screen
        )
    )
}