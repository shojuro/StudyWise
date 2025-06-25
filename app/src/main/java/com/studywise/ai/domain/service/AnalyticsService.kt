package com.studywise.ai.domain.service

import com.studywise.ai.domain.model.AnalyticsEvent

interface AnalyticsService {
    fun logEvent(event: AnalyticsEvent)
    fun setUserId(userId: String?)
    fun setUserProperty(key: String, value: String)
    fun logSessionStart()
    fun logSessionEnd()
}