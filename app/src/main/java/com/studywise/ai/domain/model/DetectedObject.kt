package com.studywise.ai.domain.model

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val category: String
)