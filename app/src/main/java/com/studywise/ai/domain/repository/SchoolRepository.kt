package com.studywise.ai.domain.repository

import com.studywise.ai.domain.model.School

interface SchoolRepository {
    suspend fun searchSchools(query: String): Result<List<School>>
    suspend fun getPopularSchools(): Result<List<School>>
    suspend fun saveSelectedSchool(schoolId: String): Result<Unit>
    suspend fun createCustomSchool(name: String, city: String): Result<School>
}