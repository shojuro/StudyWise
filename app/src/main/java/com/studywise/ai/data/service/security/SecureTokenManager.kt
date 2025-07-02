package com.studywise.ai.data.service.security

import android.os.Build
import android.provider.Settings
import com.studywise.ai.domain.model.security.*
import com.studywise.ai.domain.service.security.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Secure implementation of TokenManager
 */
@Singleton
class SecureTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStorage: SecureTokenStorage,
    private val tokenValidator: TokenValidator,
    private val tokenGenerator: SimpleTokenGenerator,
    private val encryptedPreferences: EncryptedPreferencesManager
) : TokenManager {
    
    private val _tokenState = MutableStateFlow<TokenState>(TokenState.NoToken)
    
    override suspend fun generateTokens(
        userId: String,
        role: String?,
        deviceId: String?
    ): Result<AuthToken> {
        return try {
            val actualDeviceId = deviceId ?: getOrCreateDeviceId()
            val sessionId = UUID.randomUUID().toString()
            
            // Generate tokens
            val accessToken = tokenGenerator.generateAccessToken(
                userId = userId,
                role = role,
                deviceId = actualDeviceId,
                sessionId = sessionId
            )
            
            val refreshToken = tokenGenerator.generateRefreshToken(
                userId = userId,
                deviceId = actualDeviceId
            )
            
            val authToken = AuthToken(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = AuthToken.ACCESS_TOKEN_EXPIRY_SECONDS,
                userId = userId
            )
            
            // Store tokens
            storeTokens(authToken)
            
            Result.success(authToken)
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate tokens")
            Result.failure(e)
        }
    }
    
    override suspend fun storeTokens(authToken: AuthToken): Result<Unit> {
        return try {
            val metadata = TokenMetadata(
                userId = authToken.userId,
                deviceId = getOrCreateDeviceId(),
                issuedAt = authToken.issuedAt,
                expiresAt = authToken.expiresAt
            )
            
            tokenStorage.saveTokens(
                accessToken = authToken.accessToken,
                refreshToken = authToken.refreshToken,
                metadata = metadata
            )
            
            _tokenState.value = TokenState.Valid
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to store tokens")
            _tokenState.value = TokenState.Error("Storage failed")
            Result.failure(e)
        }
    }
    
    override suspend fun getAccessToken(): String? {
        val token = tokenStorage.getAccessToken()
        
        if (token != null) {
            // Validate token before returning
            when (validateToken(token)) {
                is TokenValidationResult.Valid -> return token
                is TokenValidationResult.Expired -> {
                    // Try to refresh
                    refreshAccessToken()
                    return tokenStorage.getAccessToken()
                }
                else -> {
                    Timber.w("Invalid access token found in storage")
                    return null
                }
            }
        }
        
        return null
    }
    
    override suspend fun getRefreshToken(): String? {
        return tokenStorage.getRefreshToken()
    }
    
    override suspend fun validateToken(token: String): TokenValidationResult {
        return try {
            // Validate structure
            if (!tokenValidator.validateTokenStructure(token)) {
                return TokenValidationResult.Invalid
            }
            
            // Verify signature
            if (!tokenValidator.verifySignature(token)) {
                return TokenValidationResult.Invalid
            }
            
            // Extract claims and check expiry
            val claims = tokenValidator.extractClaims(token)
            if (claims == null) {
                return TokenValidationResult.Invalid
            }
            
            if (tokenValidator.isTokenExpired(claims)) {
                return TokenValidationResult.Expired
            }
            
            TokenValidationResult.Valid
        } catch (e: Exception) {
            Timber.e(e, "Token validation error")
            TokenValidationResult.Error(e.message ?: "Validation failed")
        }
    }
    
    override suspend fun refreshAccessToken(): Result<AuthToken> {
        _tokenState.value = TokenState.Refreshing
        
        return try {
            val refreshToken = tokenStorage.getRefreshToken()
            if (refreshToken == null) {
                _tokenState.value = TokenState.NoToken
                return Result.failure(Exception("No refresh token available"))
            }
            
            // Validate refresh token
            val validation = validateToken(refreshToken)
            if (validation !is TokenValidationResult.Valid) {
                _tokenState.value = TokenState.NoToken
                return Result.failure(Exception("Invalid refresh token"))
            }
            
            // Extract user info from refresh token
            val claims = tokenValidator.extractClaims(refreshToken)
            if (claims == null) {
                _tokenState.value = TokenState.Error("Failed to extract claims")
                return Result.failure(Exception("Failed to extract token claims"))
            }
            
            // Generate new tokens
            val result = generateTokens(
                userId = claims.sub,
                role = claims.role,
                deviceId = claims.deviceId
            )
            
            if (result.isSuccess) {
                _tokenState.value = TokenState.Valid
            } else {
                _tokenState.value = TokenState.Error("Refresh failed")
            }
            
            result
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh access token")
            _tokenState.value = TokenState.Error(e.message ?: "Refresh failed")
            Result.failure(e)
        }
    }
    
    override suspend fun clearTokens() {
        tokenStorage.clearTokens()
        _tokenState.value = TokenState.NoToken
    }
    
    override suspend fun hasValidTokens(): Boolean {
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken == null) return false
        
        return validateToken(accessToken) is TokenValidationResult.Valid
    }
    
    override fun observeTokenState(): Flow<TokenState> {
        return _tokenState.asStateFlow()
    }
    
    override suspend fun getTokenMetadata(): TokenMetadata? {
        return tokenStorage.getTokenMetadata()
    }
    
    override suspend fun revokeTokens(): Result<Unit> {
        return try {
            // In a real implementation, this would call the server to revoke tokens
            // For now, we just clear local storage
            clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun getOrCreateDeviceId(): String {
        var deviceId = tokenStorage.getDeviceId()
        if (deviceId == null) {
            deviceId = generateDeviceId()
            tokenStorage.saveDeviceId(deviceId)
        }
        return deviceId
    }
    
    @Suppress("DEPRECATION")
    private fun generateDeviceId(): String {
        return try {
            // Try to get Android ID first
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                // Valid Android ID
                "AND-$androidId"
            } else {
                // Fallback to random UUID
                "RND-${UUID.randomUUID()}"
            }
        } catch (e: Exception) {
            // Ultimate fallback
            "FBK-${UUID.randomUUID()}"
        }
    }
}

/**
 * Session manager implementation
 */
@Singleton
class SecureSessionManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val encryptedPreferences: EncryptedPreferencesManager
) : SessionManager {
    
    companion object {
        private const val KEY_CURRENT_SESSION = "current_session_id"
        private const val KEY_SESSION_PREFIX = "session_"
    }
    
    override suspend fun createSession(userId: String, authToken: AuthToken): Result<String> {
        return try {
            val sessionId = UUID.randomUUID().toString()
            val sessionInfo = SessionInfo(
                sessionId = sessionId,
                userId = userId,
                deviceId = getDeviceInfo(),
                deviceName = getDeviceName(),
                createdAt = System.currentTimeMillis(),
                lastActiveAt = System.currentTimeMillis(),
                expiresAt = authToken.expiresAt,
                ipAddress = null, // Would need network info
                userAgent = "StudyWise Android ${Build.VERSION.RELEASE}"
            )
            
            // Store session info
            encryptedPreferences.putString(
                "$KEY_SESSION_PREFIX$sessionId",
                sessionInfo.toJson()
            )
            encryptedPreferences.putString(KEY_CURRENT_SESSION, sessionId)
            
            Result.success(sessionId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create session")
            Result.failure(e)
        }
    }
    
    override suspend fun validateSession(): Boolean {
        val sessionId = getCurrentSessionId() ?: return false
        val sessionJson = encryptedPreferences.getString("$KEY_SESSION_PREFIX$sessionId")
            ?: return false
        
        return try {
            val session = SessionInfo.fromJson(sessionJson)
            val now = System.currentTimeMillis()
            
            // Check if session is expired
            if (now > session.expiresAt) {
                return false
            }
            
            // Check if tokens are valid
            tokenManager.hasValidTokens()
        } catch (e: Exception) {
            Timber.e(e, "Failed to validate session")
            false
        }
    }
    
    override suspend fun endSession() {
        val sessionId = getCurrentSessionId()
        if (sessionId != null) {
            encryptedPreferences.remove("$KEY_SESSION_PREFIX$sessionId")
        }
        encryptedPreferences.remove(KEY_CURRENT_SESSION)
        tokenManager.clearTokens()
    }
    
    override suspend fun getCurrentSessionId(): String? {
        return encryptedPreferences.getString(KEY_CURRENT_SESSION)
    }
    
    override suspend fun extendSession(): Result<Unit> {
        return try {
            val sessionId = getCurrentSessionId()
                ?: return Result.failure(Exception("No active session"))
            
            val sessionJson = encryptedPreferences.getString("$KEY_SESSION_PREFIX$sessionId")
                ?: return Result.failure(Exception("Session not found"))
            
            val session = SessionInfo.fromJson(sessionJson)
            val updatedSession = session.copy(
                lastActiveAt = System.currentTimeMillis()
            )
            
            encryptedPreferences.putString(
                "$KEY_SESSION_PREFIX$sessionId",
                updatedSession.toJson()
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getActiveSessions(userId: String): List<SessionInfo> {
        // In a real implementation, this would query the server
        // For now, return current session if it matches the user
        val sessionId = getCurrentSessionId() ?: return emptyList()
        val sessionJson = encryptedPreferences.getString("$KEY_SESSION_PREFIX$sessionId")
            ?: return emptyList()
        
        return try {
            val session = SessionInfo.fromJson(sessionJson)
            if (session.userId == userId) listOf(session) else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun revokeSession(sessionId: String): Result<Unit> {
        return try {
            encryptedPreferences.remove("$KEY_SESSION_PREFIX$sessionId")
            
            // If revoking current session, clear everything
            if (sessionId == getCurrentSessionId()) {
                endSession()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
    
    private fun getDeviceName(): String {
        return Build.MODEL
    }
}

// Extension functions for JSON serialization
private fun SessionInfo.toJson(): String {
    return """
        {
            "sessionId": "$sessionId",
            "userId": "$userId",
            "deviceId": ${deviceId?.let { "\"$it\"" } ?: "null"},
            "deviceName": ${deviceName?.let { "\"$it\"" } ?: "null"},
            "createdAt": $createdAt,
            "lastActiveAt": $lastActiveAt,
            "expiresAt": $expiresAt,
            "ipAddress": ${ipAddress?.let { "\"$it\"" } ?: "null"},
            "userAgent": ${userAgent?.let { "\"$it\"" } ?: "null"}
        }
    """.trimIndent()
}

private fun SessionInfo.Companion.fromJson(json: String): SessionInfo {
    val map = json.trim()
        .removePrefix("{")
        .removeSuffix("}")
        .split(",")
        .map { it.trim() }
        .associate {
            val (key, value) = it.split(":", limit = 2)
            key.trim().trim('"') to value.trim()
        }
    
    return SessionInfo(
        sessionId = map["sessionId"]?.trim('"') ?: "",
        userId = map["userId"]?.trim('"') ?: "",
        deviceId = map["deviceId"]?.takeIf { it != "null" }?.trim('"'),
        deviceName = map["deviceName"]?.takeIf { it != "null" }?.trim('"'),
        createdAt = map["createdAt"]?.toLongOrNull() ?: 0L,
        lastActiveAt = map["lastActiveAt"]?.toLongOrNull() ?: 0L,
        expiresAt = map["expiresAt"]?.toLongOrNull() ?: 0L,
        ipAddress = map["ipAddress"]?.takeIf { it != "null" }?.trim('"'),
        userAgent = map["userAgent"]?.takeIf { it != "null" }?.trim('"')
    )
}