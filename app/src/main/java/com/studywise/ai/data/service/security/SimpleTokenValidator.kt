package com.studywise.ai.data.service.security

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.studywise.ai.domain.model.security.TokenClaims
import com.studywise.ai.domain.service.security.TokenValidator
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple token validator implementation
 * Note: This is a simplified JWT-like implementation. 
 * For production, consider using a proper JWT library.
 */
@Singleton
class SimpleTokenValidator @Inject constructor(
    private val gson: Gson
) : TokenValidator {
    
    companion object {
        // In production, this should be stored securely and rotated
        private const val SECRET_KEY = "StudyWise-Secret-Key-2024-Secure-Token"
        private const val ALGORITHM = "HmacSHA256"
    }
    
    override suspend fun validateTokenStructure(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            
            // Try to decode each part
            parts.forEach { part ->
                Base64.decode(part, Base64.URL_SAFE or Base64.NO_PADDING)
            }
            
            true
        } catch (e: Exception) {
            Timber.e(e, "Invalid token structure")
            false
        }
    }
    
    override suspend fun extractClaims(token: String): TokenClaims? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            // Decode payload (second part)
            val payloadJson = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING),
                StandardCharsets.UTF_8
            )
            
            gson.fromJson(payloadJson, TokenClaims::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract claims")
            null
        }
    }
    
    override suspend fun verifySignature(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            
            val headerAndPayload = "${parts[0]}.${parts[1]}"
            val signature = parts[2]
            
            // Calculate signature
            val calculatedSignature = calculateSignature(headerAndPayload)
            
            // Compare signatures
            signature == calculatedSignature
        } catch (e: Exception) {
            Timber.e(e, "Failed to verify signature")
            false
        }
    }
    
    override fun isTokenExpired(claims: TokenClaims): Boolean {
        return System.currentTimeMillis() / 1000 > claims.exp
    }
    
    private fun calculateSignature(data: String): String {
        val mac = Mac.getInstance(ALGORITHM)
        val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
        mac.init(secretKey)
        
        val signature = mac.doFinal(data.toByteArray())
        return Base64.encodeToString(
            signature,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
    }
}

/**
 * Token generator for creating JWT-like tokens
 */
@Singleton
class SimpleTokenGenerator @Inject constructor(
    private val gson: Gson
) {
    
    companion object {
        private const val SECRET_KEY = "StudyWise-Secret-Key-2024-Secure-Token"
        private const val ALGORITHM = "HmacSHA256"
    }
    
    fun generateToken(claims: TokenClaims): String {
        // Create header
        val header = mapOf(
            "alg" to "HS256",
            "typ" to "JWT"
        )
        
        // Encode header
        val encodedHeader = Base64.encodeToString(
            gson.toJson(header).toByteArray(),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        
        // Encode payload
        val encodedPayload = Base64.encodeToString(
            gson.toJson(claims).toByteArray(),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        
        // Create signature
        val headerAndPayload = "$encodedHeader.$encodedPayload"
        val signature = calculateSignature(headerAndPayload)
        
        return "$headerAndPayload.$signature"
    }
    
    fun generateAccessToken(
        userId: String,
        role: String? = null,
        deviceId: String? = null,
        sessionId: String? = null
    ): String {
        val now = System.currentTimeMillis() / 1000
        val claims = TokenClaims(
            sub = userId,
            iat = now,
            exp = now + 3600, // 1 hour
            role = role,
            deviceId = deviceId,
            sessionId = sessionId
        )
        
        return generateToken(claims)
    }
    
    fun generateRefreshToken(
        userId: String,
        deviceId: String? = null
    ): String {
        val now = System.currentTimeMillis() / 1000
        val claims = TokenClaims(
            sub = userId,
            iat = now,
            exp = now + 2592000, // 30 days
            scope = "refresh",
            deviceId = deviceId
        )
        
        return generateToken(claims)
    }
    
    private fun calculateSignature(data: String): String {
        val mac = Mac.getInstance(ALGORITHM)
        val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
        mac.init(secretKey)
        
        val signature = mac.doFinal(data.toByteArray())
        return Base64.encodeToString(
            signature,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
    }
}