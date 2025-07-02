package com.studywise.ai.domain.model.security

import java.util.UUID

/**
 * Authentication token model
 */
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long, // seconds
    val scope: String = "default",
    val issuedAt: Long = System.currentTimeMillis(),
    val userId: String
) {
    val expiresAt: Long
        get() = issuedAt + (expiresIn * 1000)
    
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt
    
    val isExpiringSoon: Boolean
        get() = System.currentTimeMillis() > (expiresAt - EXPIRY_BUFFER_MS)
    
    companion object {
        // Buffer time before token expiry to refresh (5 minutes)
        const val EXPIRY_BUFFER_MS = 5 * 60 * 1000L
        
        // Token expiry times
        const val ACCESS_TOKEN_EXPIRY_SECONDS = 3600L // 1 hour
        const val REFRESH_TOKEN_EXPIRY_SECONDS = 2592000L // 30 days
    }
}

/**
 * Token validation result
 */
sealed class TokenValidationResult {
    data object Valid : TokenValidationResult()
    data object Expired : TokenValidationResult()
    data object Invalid : TokenValidationResult()
    data class Error(val message: String) : TokenValidationResult()
}

/**
 * Token claims for JWT-like structure
 */
data class TokenClaims(
    val sub: String, // Subject (user ID)
    val iat: Long, // Issued at
    val exp: Long, // Expiration
    val iss: String = "StudyWise", // Issuer
    val aud: String = "StudyWise-App", // Audience
    val jti: String = UUID.randomUUID().toString(), // JWT ID
    val scope: String = "default",
    val role: String? = null,
    val deviceId: String? = null,
    val sessionId: String? = null
)

/**
 * Secure token storage data
 */
data class SecureTokenData(
    val encryptedAccessToken: String,
    val encryptedRefreshToken: String,
    val tokenMetadata: TokenMetadata
)

/**
 * Token metadata (stored unencrypted)
 */
data class TokenMetadata(
    val userId: String,
    val deviceId: String,
    val issuedAt: Long,
    val expiresAt: Long,
    val lastUsed: Long = System.currentTimeMillis(),
    val tokenVersion: Int = 1
)

/**
 * Token refresh request
 */
data class TokenRefreshRequest(
    val refreshToken: String,
    val deviceId: String? = null
)

/**
 * Token refresh response
 */
data class TokenRefreshResponse(
    val newAccessToken: String,
    val newRefreshToken: String? = null, // Optional: rotate refresh tokens
    val expiresIn: Long
)