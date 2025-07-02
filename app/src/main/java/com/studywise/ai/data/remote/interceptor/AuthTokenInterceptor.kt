package com.studywise.ai.data.remote.interceptor

import com.studywise.ai.domain.service.security.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds authentication token to requests
 */
@Singleton
class AuthTokenInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val TOKEN_TYPE = "Bearer"
        
        // Endpoints that don't require authentication
        private val PUBLIC_ENDPOINTS = listOf(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/auth/forgot-password",
            "/public/"
        )
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.url.encodedPath)) {
            return chain.proceed(request)
        }
        
        // Get access token
        val accessToken = runBlocking {
            tokenManager.getAccessToken()
        }
        
        return if (accessToken != null) {
            // Add token to request
            val authenticatedRequest = request.newBuilder()
                .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $accessToken")
                .build()
            
            chain.proceed(authenticatedRequest)
        } else {
            // No token available, proceed without authentication
            Timber.w("No access token available for authenticated request: ${request.url}")
            chain.proceed(request)
        }
    }
    
    private fun isPublicEndpoint(path: String): Boolean {
        return PUBLIC_ENDPOINTS.any { publicPath ->
            path.contains(publicPath, ignoreCase = true)
        }
    }
}

/**
 * OkHttp authenticator that handles 401 responses by refreshing tokens
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager
) : okhttp3.Authenticator {
    
    companion object {
        private const val MAX_RETRY_COUNT = 3
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val TOKEN_TYPE = "Bearer"
    }
    
    override fun authenticate(route: okhttp3.Route?, response: Response): okhttp3.Request? {
        // Check if we've already tried to authenticate this request
        val retryCount = response.retryCount()
        if (retryCount >= MAX_RETRY_COUNT) {
            Timber.e("Max retry count reached for token refresh")
            return null
        }
        
        // Check if the request already has a token
        val currentToken = response.request.header(HEADER_AUTHORIZATION)
        if (currentToken == null) {
            Timber.w("No authorization header in failed request")
            return null
        }
        
        // Synchronously refresh the token
        val refreshResult = runBlocking {
            tokenManager.refreshAccessToken()
        }
        
        return if (refreshResult.isSuccess) {
            val newToken = runBlocking {
                tokenManager.getAccessToken()
            }
            
            if (newToken != null) {
                // Retry the request with new token
                response.request.newBuilder()
                    .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $newToken")
                    .build()
            } else {
                Timber.e("Failed to get new access token after refresh")
                null
            }
        } else {
            Timber.e("Token refresh failed: ${refreshResult.exceptionOrNull()?.message}")
            null
        }
    }
    
    private fun Response.retryCount(): Int {
        var currentResponse: Response? = this
        var count = 0
        
        while (currentResponse?.priorResponse != null) {
            count++
            currentResponse = currentResponse.priorResponse
        }
        
        return count
    }
}