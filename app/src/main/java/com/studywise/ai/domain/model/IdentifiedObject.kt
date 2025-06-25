package com.studywise.ai.domain.model

data class IdentifiedObject(
    val name: String,
    val description: String,
    val category: String,
    val confidence: Float,
    val educationalValue: String
)