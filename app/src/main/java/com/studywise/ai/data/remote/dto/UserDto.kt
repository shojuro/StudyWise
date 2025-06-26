package com.studywise.ai.data.remote.dto

import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.data.local.entity.UserRole
import java.util.Date

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val gradeLevel: Int?,
    val profileImage: String?,
    val lastLoginAt: Date?,
    val createdAt: Date
) {
    fun toEntity(): UserEntity {
        return UserEntity(
            id = id,
            name = name,
            email = email,
            password = "", // Password not synced from server
            role = UserRole.valueOf(role),
            grade = gradeLevel,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt,
            isActive = true,
            parentId = null // Parent ID not included in DTO
        )
    }
}