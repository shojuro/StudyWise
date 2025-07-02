package com.studywise.ai.data.service.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.studywise.ai.domain.service.security.TokenEncryption
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token encryption implementation using Android Keystore
 */
@Singleton
class AndroidKeystoreTokenEncryption @Inject constructor() : TokenEncryption {
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "StudyWiseTokenKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }
    
    override suspend fun encryptToken(token: String): String {
        try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            // Get IV from cipher
            val iv = cipher.iv
            
            // Encrypt the token
            val encryptedBytes = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            
            // Return Base64 encoded string
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Timber.e(e, "Failed to encrypt token")
            throw SecurityException("Token encryption failed", e)
        }
    }
    
    override suspend fun decryptToken(encryptedToken: String): String {
        try {
            val combined = Base64.decode(encryptedToken, Base64.NO_WRAP)
            
            // Extract IV and encrypted data
            val iv = ByteArray(GCM_IV_LENGTH)
            val encryptedBytes = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, iv.size)
            System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)
            
            // Decrypt
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Timber.e(e, "Failed to decrypt token")
            throw SecurityException("Token decryption failed", e)
        }
    }
    
    override suspend fun generateKey(): String {
        // Key is managed by Android Keystore, return alias
        getOrCreateSecretKey()
        return KEY_ALIAS
    }
    
    override suspend fun isInitialized(): Boolean {
        return try {
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            Timber.e(e, "Failed to check keystore initialization")
            false
        }
    }
    
    private fun getOrCreateSecretKey(): SecretKey {
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            // Key exists, retrieve it
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            // Generate new key
            generateSecretKey()
        }
    }
    
    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Set to true for biometric protection
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
}

/**
 * Secure token storage using encrypted shared preferences
 */
@Singleton
class SecureTokenStorage @Inject constructor(
    private val tokenEncryption: TokenEncryption,
    private val encryptedPreferences: EncryptedPreferencesManager
) {
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_METADATA = "token_metadata"
        private const val KEY_DEVICE_ID = "device_id"
    }
    
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        metadata: TokenMetadata
    ) {
        try {
            // Encrypt tokens
            val encryptedAccess = tokenEncryption.encryptToken(accessToken)
            val encryptedRefresh = tokenEncryption.encryptToken(refreshToken)
            
            // Save encrypted tokens
            encryptedPreferences.putString(KEY_ACCESS_TOKEN, encryptedAccess)
            encryptedPreferences.putString(KEY_REFRESH_TOKEN, encryptedRefresh)
            
            // Save metadata (can be stored unencrypted)
            encryptedPreferences.putString(KEY_TOKEN_METADATA, metadata.toJson())
            
            Timber.d("Tokens saved securely")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save tokens")
            throw e
        }
    }
    
    suspend fun getAccessToken(): String? {
        return try {
            encryptedPreferences.getString(KEY_ACCESS_TOKEN)?.let { encrypted ->
                tokenEncryption.decryptToken(encrypted)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve access token")
            null
        }
    }
    
    suspend fun getRefreshToken(): String? {
        return try {
            encryptedPreferences.getString(KEY_REFRESH_TOKEN)?.let { encrypted ->
                tokenEncryption.decryptToken(encrypted)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve refresh token")
            null
        }
    }
    
    suspend fun getTokenMetadata(): TokenMetadata? {
        return try {
            encryptedPreferences.getString(KEY_TOKEN_METADATA)?.let { json ->
                TokenMetadata.fromJson(json)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve token metadata")
            null
        }
    }
    
    suspend fun clearTokens() {
        encryptedPreferences.remove(KEY_ACCESS_TOKEN)
        encryptedPreferences.remove(KEY_REFRESH_TOKEN)
        encryptedPreferences.remove(KEY_TOKEN_METADATA)
        Timber.d("Tokens cleared")
    }
    
    suspend fun saveDeviceId(deviceId: String) {
        encryptedPreferences.putString(KEY_DEVICE_ID, deviceId)
    }
    
    suspend fun getDeviceId(): String? {
        return encryptedPreferences.getString(KEY_DEVICE_ID)
    }
}

// Extension functions for JSON serialization
private fun TokenMetadata.toJson(): String {
    return """
        {
            "userId": "$userId",
            "deviceId": "$deviceId",
            "issuedAt": $issuedAt,
            "expiresAt": $expiresAt,
            "lastUsed": $lastUsed,
            "tokenVersion": $tokenVersion
        }
    """.trimIndent()
}

private fun TokenMetadata.Companion.fromJson(json: String): TokenMetadata {
    // Simple JSON parsing - in production, use Gson or Moshi
    val map = json.trim()
        .removePrefix("{")
        .removeSuffix("}")
        .split(",")
        .map { it.trim() }
        .associate {
            val (key, value) = it.split(":")
            key.trim().trim('"') to value.trim().trim('"')
        }
    
    return TokenMetadata(
        userId = map["userId"] ?: "",
        deviceId = map["deviceId"] ?: "",
        issuedAt = map["issuedAt"]?.toLongOrNull() ?: 0L,
        expiresAt = map["expiresAt"]?.toLongOrNull() ?: 0L,
        lastUsed = map["lastUsed"]?.toLongOrNull() ?: System.currentTimeMillis(),
        tokenVersion = map["tokenVersion"]?.toIntOrNull() ?: 1
    )
}