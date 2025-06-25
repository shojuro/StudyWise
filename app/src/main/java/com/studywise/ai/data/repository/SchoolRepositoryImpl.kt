package com.studywise.ai.data.repository

import com.studywise.ai.data.local.preferences.PreferencesManager
import com.studywise.ai.domain.repository.SchoolRepository
import com.studywise.ai.domain.model.School
import com.studywise.ai.domain.model.SchoolType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchoolRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : SchoolRepository {

    override suspend fun searchSchools(query: String): Result<List<School>> {
        // For MVP, return mock data
        val allSchools = getMockSchools()
        val filteredSchools = allSchools.filter { school ->
            school.name.contains(query, ignoreCase = true) ||
            school.city.contains(query, ignoreCase = true) ||
            school.state.contains(query, ignoreCase = true)
        }
        return Result.success(filteredSchools)
    }

    override suspend fun getPopularSchools(): Result<List<School>> {
        return Result.success(getMockSchools().take(5))
    }

    override suspend fun saveSelectedSchool(schoolId: String): Result<Unit> {
        return try {
            preferencesManager.updateSchoolId(schoolId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCustomSchool(name: String, city: String): Result<School> {
        val school = School(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            city = city,
            state = "",
            type = SchoolType.PRIVATE
        )
        return Result.success(school)
    }

    private fun getMockSchools(): List<School> {
        return listOf(
            School("1", "Lincoln High School", "San Francisco", "CA", SchoolType.PUBLIC),
            School("2", "St. Mary's Academy", "Los Angeles", "CA", SchoolType.PRIVATE),
            School("3", "Washington Middle School", "Seattle", "WA", SchoolType.PUBLIC),
            School("4", "Homeschool Network", "", "", SchoolType.HOMESCHOOL),
            School("5", "Jefferson Elementary", "Portland", "OR", SchoolType.PUBLIC),
            School("6", "Private Learning Center", "Phoenix", "AZ", SchoolType.PRIVATE),
            School("7", "Roosevelt High School", "Chicago", "IL", SchoolType.PUBLIC),
            School("8", "Academy of Arts", "New York", "NY", SchoolType.PRIVATE),
            School("9", "Madison Middle School", "Denver", "CO", SchoolType.PUBLIC),
            School("10", "Online Learning Academy", "", "", SchoolType.HOMESCHOOL)
        )
    }
}