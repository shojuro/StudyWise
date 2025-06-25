package com.studywise.ai.domain.model

import com.studywise.ai.data.local.entity.UserRole
import java.util.Date

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val grade: Int? = null,
    val createdAt: Date,
    val lastLoginAt: Date? = null,
    val isActive: Boolean = true,
    val parentId: String? = null,
    val children: List<User> = emptyList() // For parent users
)