package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val createdAt: Date,
    val lastLoginAt: Date? = null,
    val isActive: Boolean = true,
    val parentId: String? = null, // For linking students to parents
    val profileImageUrl: String? = null // Profile picture URL
)

enum class UserRole {
    STUDENT,
    PARENT,
    TEACHER,
    ADULT
}