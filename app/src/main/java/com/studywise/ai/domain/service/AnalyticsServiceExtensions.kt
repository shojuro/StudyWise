package com.studywise.ai.domain.service

import com.studywise.ai.domain.model.AnalyticsEvent

// Extension functions for common analytics events

fun AnalyticsService.trackSubjectProgressViewed(subject: String) {
    logEvent(
        AnalyticsEvent.CustomEvent(
            eventName = "subject_progress_viewed",
            parameters = mapOf(
                "subject" to subject,
                "screen" to "subject_progress_detail"
            )
        )
    )
}

fun AnalyticsService.trackProgressExported(format: String) {
    logEvent(
        AnalyticsEvent.CustomEvent(
            eventName = "progress_exported",
            parameters = mapOf(
                "format" to format,
                "source" to "subject_progress_detail"
            )
        )
    )
}

fun AnalyticsService.trackChartInteraction(chartType: String, action: String) {
    logEvent(
        AnalyticsEvent.CustomEvent(
            eventName = "chart_interaction",
            parameters = mapOf(
                "chart_type" to chartType,
                "action" to action
            )
        )
    )
}

fun AnalyticsService.trackLearningRecommendationViewed(recommendationType: String) {
    logEvent(
        AnalyticsEvent.CustomEvent(
            eventName = "recommendation_viewed",
            parameters = mapOf(
                "type" to recommendationType,
                "source" to "progress_screen"
            )
        )
    )
}

fun AnalyticsService.trackAchievementUnlocked(achievementId: String, achievementName: String) {
    logEvent(
        AnalyticsEvent.CustomEvent(
            eventName = "achievement_unlocked",
            parameters = mapOf(
                "achievement_id" to achievementId,
                "achievement_name" to achievementName
            )
        )
    )
}

fun AnalyticsService.trackProgressMilestone(milestone: String, value: Any) {
    logEvent(
        AnalyticsEvent.CustomEvent(
            eventName = "progress_milestone",
            parameters = mapOf(
                "milestone" to milestone,
                "value" to value.toString()
            )
        )
    )
}