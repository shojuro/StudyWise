package com.studywise.ai.domain.service.security

import com.studywise.ai.domain.model.security.*
import kotlinx.coroutines.flow.Flow

/**
 * Service for managing authentication tokens securely
 */
interface TokenManager {
    /**
     * Generate new authentication tokens for a user
     */
    suspend fun generateTokens(
        userId: String,
        role: String? = null,
        deviceId: String? = null
    ): Result<AuthToken>
    
    /**
     * Store tokens securely
     */
    suspend fun storeTokens(authToken: AuthToken): Result<Unit>
    
    /**
     * Retrieve current access token
     */
    suspend fun getAccessToken(): String?
    
    /**
     * Retrieve current refresh token
     */
    suspend fun getRefreshToken(): String?
    
    /**
     * Validate a token
     */
    suspend fun validateToken(token: String): TokenValidationResult
    
    /**
     * Refresh access token using refresh token
     */
    suspend fun refreshAccessToken(): Result<AuthToken>
    
    /**
     * Clear all stored tokens (logout)
     */
    suspend fun clearTokens()
    
    /**
     * Check if user has valid tokens
     */
    suspend fun hasValidTokens(): Boolean
    
    /**
     * Observe token state changes
     */
    fun observeTokenState(): Flow<TokenState>
    
    /**
     * Get token metadata without decrypting tokens
     */
    suspend fun getTokenMetadata(): TokenMetadata?
    
    /**
     * Revoke tokens on server (if applicable)
     */
    suspend fun revokeTokens(): Result<Unit>
}

/**
 * Token state for observing changes
 */
sealed class TokenState {
    data object NoToken : TokenState()
    data object Valid : TokenState()
    data object Expired : TokenState()
    data object Refreshing : TokenState()
    data class Error(val message: String) : TokenState()
}

/**
 * Token encryption service
 */
interface TokenEncryption {
    /**
     * Encrypt a token for secure storage
     */
    suspend fun encryptToken(token: String): String
    
    /**
     * Decrypt a stored token
     */
    suspend fun decryptToken(encryptedToken: String): String
    
    /**
     * Generate a secure key for token encryption
     */
    suspend fun generateKey(): String
    
    /**
     * Check if encryption keys are properly initialized
     */
    suspend fun isInitialized(): Boolean
}

/**
 * Token validation service
 */
interface TokenValidator {
    /**
     * Validate token structure and signature
     */
    suspend fun validateTokenStructure(token: String): Boolean
    
    /**
     * Extract claims from token
     */
    suspend fun extractClaims(token: String): TokenClaims?
    
    /**
     * Verify token signature (if using JWT)
     */
    suspend fun verifySignature(token: String): Boolean
    
    /**
     * Check if token is expired
     */
    fun isTokenExpired(claims: TokenClaims): Boolean
}

/**
 * Session manager for token-based sessions
 */
interface SessionManager {
    /**
     * Create a new session
     */
    suspend fun createSession(userId: String, authToken: AuthToken): Result<String>
    
    /**
     * Validate current session
     */
    suspend fun validateSession(): Boolean
    
    /**
     * End current session
     */
    suspend fun endSession()
    
    /**
     * Get current session ID
     */
    suspend fun getCurrentSessionId(): String?
    
    /**
     * Extend session expiry
     */
    suspend fun extendSession(): Result<Unit>
    
    /**
     * Get all active sessions for a user
     */
    suspend fun getActiveSessions(userId: String): List<SessionInfo>
    
    /**
     * Revoke a specific session
     */
    suspend fun revokeSession(sessionId: String): Result<Unit>
}

/**
 * Session information
 */
data class SessionInfo(
    val sessionId: String,
    val userId: String,
    val deviceId: String?,
    val deviceName: String?,
    val createdAt: Long,
    val lastActiveAt: Long,
    val expiresAt: Long,
    val ipAddress: String?,
    val userAgent: String?
)