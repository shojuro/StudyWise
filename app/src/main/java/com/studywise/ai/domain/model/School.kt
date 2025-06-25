package com.studywise.ai.domain.model

data class School(
    val id: String,
    val name: String,
    val city: String = "",
    val state: String = "",
    val type: SchoolType = SchoolType.PUBLIC
)

enum class SchoolType {
    ALL,
    PUBLIC,
    PRIVATE,
    HOMESCHOOL
}