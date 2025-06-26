package com.studywise.ai.data.remote.api

import com.studywise.ai.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface StudyWiseApiService {
    
    // User endpoints
    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<UserDto>
    
    @PUT("users/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Query("name") name: String,
        @Query("gradeLevel") gradeLevel: Int?,
        @Query("profileImage") profileImage: String?
    ): Response<Unit>
    
    // Learning session endpoints
    @POST("sessions")
    suspend fun uploadLearningSession(
        @Query("sessionId") sessionId: String,
        @Query("userId") userId: String,
        @Query("subject") subject: String,
        @Query("startedAt") startedAt: Long,
        @Query("completedAt") completedAt: Long?,
        @Query("durationMinutes") durationMinutes: Int,
        @Query("questionsAnswered") questionsAnswered: Int,
        @Query("correctAnswers") correctAnswers: Int,
        @Query("pointsEarned") pointsEarned: Int
    ): Response<Unit>
    
    @GET("sessions/user/{userId}")
    suspend fun getLearningSessionsSince(
        @Path("userId") userId: String,
        @Query("since") sinceTimestamp: Long
    ): Response<List<LearningSessionDto>>
    
    // Progress endpoints
    @PUT("progress")
    suspend fun updateProgress(
        @Query("userId") userId: String,
        @Query("skillId") skillId: String,
        @Query("masteryLevel") masteryLevel: Float,
        @Query("questionsAnswered") questionsAnswered: Int,
        @Query("correctAnswers") correctAnswers: Int,
        @Query("lastPracticedAt") lastPracticedAt: Long
    ): Response<Unit>
    
    @GET("progress/user/{userId}")
    suspend fun getUserProgress(@Path("userId") userId: String): Response<List<ProgressDto>>
    
    // Question endpoints
    @POST("questions/responses")
    suspend fun uploadQuestionResponse(
        @Query("userId") userId: String,
        @Query("questionId") questionId: String,
        @Query("userAnswer") userAnswer: String,
        @Query("isCorrect") isCorrect: Boolean,
        @Query("timeSpentSeconds") timeSpentSeconds: Int,
        @Query("timestamp") timestamp: Long
    ): Response<Unit>
    
    @GET("questions/grade/{gradeLevel}")
    suspend fun getQuestionsForGrade(@Path("gradeLevel") gradeLevel: Int): Response<List<QuestionDto>>
}