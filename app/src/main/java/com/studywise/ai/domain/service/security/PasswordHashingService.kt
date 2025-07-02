package com.studywise.ai.domain.service.security

/**
 * Service for secure password hashing and verification
 */
interface PasswordHashingService {
    /**
     * Hash a password using a secure algorithm
     * @param password The plain text password to hash
     * @return The hashed password string
     */
    suspend fun hashPassword(password: String): String
    
    /**
     * Verify a password against a hash
     * @param password The plain text password to verify
     * @param hash The hash to verify against
     * @return true if the password matches the hash, false otherwise
     */
    suspend fun verifyPassword(password: String, hash: String): Boolean
    
    /**
     * Check if a hash needs to be upgraded (e.g., from SHA-256 to BCrypt)
     * @param hash The hash to check
     * @return true if the hash should be upgraded, false otherwise
     */
    fun needsUpgrade(hash: String): Boolean
    
    /**
     * Get the current hash type/algorithm
     * @return The hash type identifier
     */
    fun getHashType(): HashType
}

/**
 * Types of password hashing algorithms
 */
enum class HashType {
    SHA256,     // Legacy, insecure
    BCRYPT      // Current standard
}

/**
 * Password strength validation result
 */
data class PasswordStrength(
    val isValid: Boolean,
    val score: Int, // 0-100
    val feedback: List<String>
)

/**
 * Extension functions for password validation
 */
fun String.validatePasswordStrength(): PasswordStrength {
    val feedback = mutableListOf<String>()
    var score = 0
    
    // Length check
    when {
        length < 8 -> feedback.add("Password must be at least 8 characters long")
        length >= 8 -> score += 20
        length >= 12 -> score += 10
        length >= 16 -> score += 10
    }
    
    // Character variety checks
    if (contains(Regex("[A-Z]"))) {
        score += 15
    } else {
        feedback.add("Add uppercase letters for better security")
    }
    
    if (contains(Regex("[a-z]"))) {
        score += 15
    } else {
        feedback.add("Add lowercase letters for better security")
    }
    
    if (contains(Regex("[0-9]"))) {
        score += 15
    } else {
        feedback.add("Add numbers for better security")
    }
    
    if (contains(Regex("[!@#$%^&*(),.?\":{}|<>]"))) {
        score += 15
    } else {
        feedback.add("Add special characters for better security")
    }
    
    // Common patterns to avoid
    val commonPatterns = listOf(
        "password", "123456", "qwerty", "abc123", "111111",
        "admin", "letmein", "welcome", "monkey", "dragon"
    )
    
    if (commonPatterns.any { lowercase().contains(it) }) {
        score -= 20
        feedback.add("Avoid common passwords and patterns")
    }
    
    // Repetitive characters
    if (contains(Regex("(.)\\1{2,}"))) {
        score -= 10
        feedback.add("Avoid repeating characters")
    }
    
    // Sequential characters
    if (contains(Regex("(012|123|234|345|456|567|678|789|890|abc|bcd|cde|def|efg|fgh)"))) {
        score -= 10
        feedback.add("Avoid sequential characters")
    }
    
    return PasswordStrength(
        isValid = length >= 8 && score >= 50,
        score = score.coerceIn(0, 100),
        feedback = feedback
    )
}