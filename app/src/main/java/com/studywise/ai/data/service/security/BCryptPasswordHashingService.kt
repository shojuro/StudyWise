package com.studywise.ai.data.service.security

import at.favre.lib.crypto.bcrypt.BCrypt
import com.studywise.ai.domain.service.security.HashType
import com.studywise.ai.domain.service.security.PasswordHashingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BCrypt implementation of password hashing service
 * Supports migration from SHA-256 hashes
 */
@Singleton
class BCryptPasswordHashingService @Inject constructor() : PasswordHashingService {
    
    companion object {
        // BCrypt work factor (cost parameter)
        // Higher = more secure but slower. 12 is a good balance for mobile
        private const val BCRYPT_COST = 12
        
        // Prefix to identify BCrypt hashes
        private const val BCRYPT_PREFIX = "$2"
        
        // SHA-256 hashes are 64 characters hex string
        private const val SHA256_HASH_LENGTH = 64
    }
    
    override suspend fun hashPassword(password: String): String = withContext(Dispatchers.Default) {
        try {
            BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
        } catch (e: Exception) {
            Timber.e(e, "Failed to hash password with BCrypt")
            throw SecurityException("Password hashing failed", e)
        }
    }
    
    override suspend fun verifyPassword(password: String, hash: String): Boolean = withContext(Dispatchers.Default) {
        try {
            when {
                isBCryptHash(hash) -> {
                    // Verify BCrypt hash
                    val result = BCrypt.verifyer().verify(password.toCharArray(), hash)
                    result.verified
                }
                isSHA256Hash(hash) -> {
                    // Legacy SHA-256 verification
                    val calculatedHash = hashPasswordSHA256(password)
                    calculatedHash.equals(hash, ignoreCase = true)
                }
                else -> {
                    Timber.w("Unknown hash format: ${hash.take(10)}...")
                    false
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to verify password")
            false
        }
    }
    
    override fun needsUpgrade(hash: String): Boolean {
        return when {
            isBCryptHash(hash) -> {
                // Check if cost factor needs upgrade
                try {
                    val currentCost = extractBCryptCost(hash)
                    currentCost < BCRYPT_COST
                } catch (e: Exception) {
                    // If we can't extract cost, assume it needs upgrade
                    true
                }
            }
            isSHA256Hash(hash) -> true // SHA-256 always needs upgrade
            else -> true // Unknown format needs upgrade
        }
    }
    
    override fun getHashType(): HashType = HashType.BCRYPT
    
    /**
     * Check if a hash is a BCrypt hash
     */
    private fun isBCryptHash(hash: String): Boolean {
        return hash.startsWith(BCRYPT_PREFIX) && hash.length >= 60
    }
    
    /**
     * Check if a hash is a SHA-256 hash
     */
    private fun isSHA256Hash(hash: String): Boolean {
        return hash.length == SHA256_HASH_LENGTH && hash.matches(Regex("[a-fA-F0-9]+"))
    }
    
    /**
     * Extract cost factor from BCrypt hash
     */
    private fun extractBCryptCost(hash: String): Int {
        // BCrypt format: $2a$12$... where 12 is the cost
        return try {
            val parts = hash.split('$')
            if (parts.size >= 3) {
                parts[2].toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Legacy SHA-256 hashing for migration support
     */
    private fun hashPasswordSHA256(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Migration helper for upgrading password hashes
 */
class PasswordHashMigrator @Inject constructor(
    private val hashingService: PasswordHashingService
) {
    /**
     * Migrate a password hash if needed
     * @param currentHash The current password hash
     * @param plainPassword The plain text password (only available during login)
     * @return Pair of (newHash, wasUpgraded)
     */
    suspend fun migrateIfNeeded(currentHash: String, plainPassword: String?): Pair<String, Boolean> {
        return if (hashingService.needsUpgrade(currentHash) && plainPassword != null) {
            // Verify the password is correct before upgrading
            if (hashingService.verifyPassword(plainPassword, currentHash)) {
                val newHash = hashingService.hashPassword(plainPassword)
                Pair(newHash, true)
            } else {
                Pair(currentHash, false)
            }
        } else {
            Pair(currentHash, false)
        }
    }
}