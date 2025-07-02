package com.studywise.ai.data.service.security

import com.studywise.ai.domain.service.security.HashType
import com.studywise.ai.domain.service.security.validatePasswordStrength
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.*

/**
 * Unit tests for BCryptPasswordHashingService
 */
class BCryptPasswordHashingServiceTest {

    private lateinit var passwordHashingService: BCryptPasswordHashingService
    private lateinit var passwordHashMigrator: PasswordHashMigrator

    @Before
    fun setup() {
        passwordHashingService = BCryptPasswordHashingService()
        passwordHashMigrator = PasswordHashMigrator(passwordHashingService)
    }

    @Test
    fun `test hashPassword creates BCrypt hash`() = runTest {
        // Given
        val password = "TestPassword123!"

        // When
        val hash = passwordHashingService.hashPassword(password)

        // Then
        assertTrue(hash.startsWith("$2"))
        assertTrue(hash.length >= 60)
        assertNotEquals(password, hash)
    }

    @Test
    fun `test hashPassword creates different hashes for same password`() = runTest {
        // Given
        val password = "TestPassword123!"

        // When
        val hash1 = passwordHashingService.hashPassword(password)
        val hash2 = passwordHashingService.hashPassword(password)

        // Then
        assertNotEquals(hash1, hash2) // Different salts produce different hashes
    }

    @Test
    fun `test verifyPassword with correct BCrypt password`() = runTest {
        // Given
        val password = "TestPassword123!"
        val hash = passwordHashingService.hashPassword(password)

        // When
        val isValid = passwordHashingService.verifyPassword(password, hash)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `test verifyPassword with incorrect BCrypt password`() = runTest {
        // Given
        val password = "TestPassword123!"
        val wrongPassword = "WrongPassword123!"
        val hash = passwordHashingService.hashPassword(password)

        // When
        val isValid = passwordHashingService.verifyPassword(wrongPassword, hash)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `test verifyPassword with SHA256 hash`() = runTest {
        // Given
        val password = "TestPassword123!"
        // This is the SHA-256 hash of "TestPassword123!"
        val sha256Hash = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069"

        // When
        val isValid = passwordHashingService.verifyPassword(password, sha256Hash)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `test verifyPassword with invalid hash format`() = runTest {
        // Given
        val password = "TestPassword123!"
        val invalidHash = "invalid-hash-format"

        // When
        val isValid = passwordHashingService.verifyPassword(password, invalidHash)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `test needsUpgrade returns true for SHA256 hash`() {
        // Given
        val sha256Hash = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069"

        // When
        val needsUpgrade = passwordHashingService.needsUpgrade(sha256Hash)

        // Then
        assertTrue(needsUpgrade)
    }

    @Test
    fun `test needsUpgrade returns false for current BCrypt hash`() = runTest {
        // Given
        val password = "TestPassword123!"
        val bcryptHash = passwordHashingService.hashPassword(password)

        // When
        val needsUpgrade = passwordHashingService.needsUpgrade(bcryptHash)

        // Then
        assertFalse(needsUpgrade)
    }

    @Test
    fun `test needsUpgrade returns true for old BCrypt cost factor`() {
        // Given
        // BCrypt hash with cost factor 10 (lower than our default 12)
        val oldBcryptHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

        // When
        val needsUpgrade = passwordHashingService.needsUpgrade(oldBcryptHash)

        // Then
        assertTrue(needsUpgrade)
    }

    @Test
    fun `test getHashType returns BCRYPT`() {
        // When
        val hashType = passwordHashingService.getHashType()

        // Then
        assertEquals(HashType.BCRYPT, hashType)
    }

    @Test
    fun `test migrateIfNeeded upgrades SHA256 hash`() = runTest {
        // Given
        val password = "TestPassword123!"
        val sha256Hash = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069"

        // When
        val (newHash, wasUpgraded) = passwordHashMigrator.migrateIfNeeded(sha256Hash, password)

        // Then
        assertTrue(wasUpgraded)
        assertTrue(newHash.startsWith("$2"))
        assertTrue(passwordHashingService.verifyPassword(password, newHash))
    }

    @Test
    fun `test migrateIfNeeded does not upgrade current BCrypt hash`() = runTest {
        // Given
        val password = "TestPassword123!"
        val bcryptHash = passwordHashingService.hashPassword(password)

        // When
        val (newHash, wasUpgraded) = passwordHashMigrator.migrateIfNeeded(bcryptHash, password)

        // Then
        assertFalse(wasUpgraded)
        assertEquals(bcryptHash, newHash)
    }

    @Test
    fun `test migrateIfNeeded fails with wrong password`() = runTest {
        // Given
        val correctPassword = "TestPassword123!"
        val wrongPassword = "WrongPassword123!"
        val sha256Hash = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069"

        // When
        val (newHash, wasUpgraded) = passwordHashMigrator.migrateIfNeeded(sha256Hash, wrongPassword)

        // Then
        assertFalse(wasUpgraded)
        assertEquals(sha256Hash, newHash)
    }

    @Test
    fun `test password strength validation`() {
        // Test weak passwords
        var strength = "weak".validatePasswordStrength()
        assertFalse(strength.isValid)
        assertTrue(strength.feedback.isNotEmpty())

        strength = "12345678".validatePasswordStrength()
        assertFalse(strength.isValid)
        assertTrue(strength.feedback.any { it.contains("uppercase") })

        // Test strong password
        strength = "StrongP@ssw0rd!".validatePasswordStrength()
        assertTrue(strength.isValid)
        assertTrue(strength.score >= 70)

        // Test password with common patterns
        strength = "Password123!".validatePasswordStrength()
        assertTrue(strength.feedback.any { it.contains("common") })

        // Test password with repetitive characters
        strength = "Paaassword123!".validatePasswordStrength()
        assertTrue(strength.feedback.any { it.contains("repeating") })

        // Test password with sequential characters
        strength = "Abc123!@#".validatePasswordStrength()
        assertTrue(strength.feedback.any { it.contains("sequential") })
    }

    @Test
    fun `test hashPassword handles special characters`() = runTest {
        // Given
        val passwordWithSpecialChars = "P@ssw0rd!#$%^&*()"

        // When
        val hash = passwordHashingService.hashPassword(passwordWithSpecialChars)
        val isValid = passwordHashingService.verifyPassword(passwordWithSpecialChars, hash)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `test hashPassword handles unicode characters`() = runTest {
        // Given
        val passwordWithUnicode = "Pássw0rd™€∞"

        // When
        val hash = passwordHashingService.hashPassword(passwordWithUnicode)
        val isValid = passwordHashingService.verifyPassword(passwordWithUnicode, hash)

        // Then
        assertTrue(isValid)
    }
}