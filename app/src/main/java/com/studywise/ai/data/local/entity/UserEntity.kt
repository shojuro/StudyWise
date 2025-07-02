package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val password: String, // Will be hashed
    val role: UserRole,
    val grade: Int? = null, // Only for students
    val birthDate: LocalDate? = null, // For age verification and COPPA compliance
    val createdAt: Date,
    val lastLoginAt: Date? = null,
    val isActive: Boolean = true,
    val parentId: String? = null, // For linking students to parents
    val hasParentalConsent: Boolean = false, // For users under 13
    val consentVerifiedAt: Long? = null
)

enum class UserRole {
    STUDENT,
    PARENT,
    TEACHER
}