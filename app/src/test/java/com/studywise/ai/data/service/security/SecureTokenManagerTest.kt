package com.studywise.ai.data.service.security

import android.content.Context
import com.studywise.ai.data.local.preferences.EncryptedPreferencesManager
import com.studywise.ai.domain.model.security.*
import com.studywise.ai.domain.service.security.TokenValidator
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.*

/**
 * Unit tests for SecureTokenManager
 */
class SecureTokenManagerTest {
    
    @MockK
    private lateinit var context: Context
    
    @MockK
    private lateinit var tokenStorage: SecureTokenStorage
    
    @MockK
    private lateinit var tokenValidator: TokenValidator
    
    @MockK
    private lateinit var tokenGenerator: SimpleTokenGenerator
    
    @MockK
    private lateinit var encryptedPreferences: EncryptedPreferencesManager
    
    private lateinit var tokenManager: SecureTokenManager
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        tokenManager = SecureTokenManager(
            context = context,
            tokenStorage = tokenStorage,
            tokenValidator = tokenValidator,
            tokenGenerator = tokenGenerator,
            encryptedPreferences = encryptedPreferences
        )
    }
    
    @Test
    fun `test generateTokens creates and stores tokens successfully`() = runTest {
        // Given
        val userId = "user123"
        val role = "STUDENT"
        val deviceId = "device123"
        val accessToken = "access.token.here"
        val refreshToken = "refresh.token.here"
        
        coEvery { tokenStorage.getDeviceId() } returns deviceId
        coEvery { tokenGenerator.generateAccessToken(any(), any(), any(), any()) } returns accessToken
        coEvery { tokenGenerator.generateRefreshToken(any(), any()) } returns refreshToken
        coEvery { tokenStorage.saveTokens(any(), any(), any()) } just Runs
        
        // When
        val result = tokenManager.generateTokens(userId, role, deviceId)
        
        // Then
        assertTrue(result.isSuccess)
        val authToken = result.getOrNull()!!
        assertEquals(accessToken, authToken.accessToken)
        assertEquals(refreshToken, authToken.refreshToken)
        assertEquals(userId, authToken.userId)
        
        coVerify {
            tokenGenerator.generateAccessToken(userId, role, deviceId, any())
            tokenGenerator.generateRefreshToken(userId, deviceId)
            tokenStorage.saveTokens(accessToken, refreshToken, any())
        }
    }
    
    @Test
    fun `test getAccessToken returns valid token`() = runTest {
        // Given
        val accessToken = "valid.access.token"
        val claims = TokenClaims(
            sub = "user123",
            iat = System.currentTimeMillis() / 1000,
            exp = (System.currentTimeMillis() / 1000) + 3600,
            role = "STUDENT"
        )
        
        coEvery { tokenStorage.getAccessToken() } returns accessToken
        coEvery { tokenValidator.validateTokenStructure(accessToken) } returns true
        coEvery { tokenValidator.verifySignature(accessToken) } returns true
        coEvery { tokenValidator.extractClaims(accessToken) } returns claims
        coEvery { tokenValidator.isTokenExpired(claims) } returns false
        
        // When
        val result = tokenManager.getAccessToken()
        
        // Then
        assertEquals(accessToken, result)
    }
    
    @Test
    fun `test getAccessToken refreshes expired token`() = runTest {
        // Given
        val expiredToken = "expired.access.token"
        val refreshToken = "valid.refresh.token"
        val newAccessToken = "new.access.token"
        
        val expiredClaims = TokenClaims(
            sub = "user123",
            iat = System.currentTimeMillis() / 1000 - 7200,
            exp = System.currentTimeMillis() / 1000 - 3600,
            role = "STUDENT"
        )
        
        val refreshClaims = TokenClaims(
            sub = "user123",
            iat = System.currentTimeMillis() / 1000,
            exp = (System.currentTimeMillis() / 1000) + 86400,
            scope = "refresh"
        )
        
        coEvery { tokenStorage.getAccessToken() } returnsMany listOf(expiredToken, newAccessToken)
        coEvery { tokenStorage.getRefreshToken() } returns refreshToken
        coEvery { tokenValidator.validateTokenStructure(expiredToken) } returns true
        coEvery { tokenValidator.verifySignature(expiredToken) } returns true
        coEvery { tokenValidator.extractClaims(expiredToken) } returns expiredClaims
        coEvery { tokenValidator.isTokenExpired(expiredClaims) } returns true
        
        coEvery { tokenValidator.validateTokenStructure(refreshToken) } returns true
        coEvery { tokenValidator.verifySignature(refreshToken) } returns true
        coEvery { tokenValidator.extractClaims(refreshToken) } returns refreshClaims
        coEvery { tokenValidator.isTokenExpired(refreshClaims) } returns false
        
        coEvery { tokenStorage.getDeviceId() } returns "device123"
        coEvery { tokenGenerator.generateAccessToken(any(), any(), any(), any()) } returns newAccessToken
        coEvery { tokenGenerator.generateRefreshToken(any(), any()) } returns refreshToken
        coEvery { tokenStorage.saveTokens(any(), any(), any()) } just Runs
        
        // When
        val result = tokenManager.getAccessToken()
        
        // Then
        assertEquals(newAccessToken, result)
    }
    
    @Test
    fun `test validateToken returns Valid for valid token`() = runTest {
        // Given
        val token = "valid.token.here"
        val claims = TokenClaims(
            sub = "user123",
            iat = System.currentTimeMillis() / 1000,
            exp = (System.currentTimeMillis() / 1000) + 3600,
            role = "STUDENT"
        )
        
        coEvery { tokenValidator.validateTokenStructure(token) } returns true
        coEvery { tokenValidator.verifySignature(token) } returns true
        coEvery { tokenValidator.extractClaims(token) } returns claims
        coEvery { tokenValidator.isTokenExpired(claims) } returns false
        
        // When
        val result = tokenManager.validateToken(token)
        
        // Then
        assertTrue(result is TokenValidationResult.Valid)
    }
    
    @Test
    fun `test validateToken returns Expired for expired token`() = runTest {
        // Given
        val token = "expired.token.here"
        val claims = TokenClaims(
            sub = "user123",
            iat = System.currentTimeMillis() / 1000 - 7200,
            exp = System.currentTimeMillis() / 1000 - 3600,
            role = "STUDENT"
        )
        
        coEvery { tokenValidator.validateTokenStructure(token) } returns true
        coEvery { tokenValidator.verifySignature(token) } returns true
        coEvery { tokenValidator.extractClaims(token) } returns claims
        coEvery { tokenValidator.isTokenExpired(claims) } returns true
        
        // When
        val result = tokenManager.validateToken(token)
        
        // Then
        assertTrue(result is TokenValidationResult.Expired)
    }
    
    @Test
    fun `test validateToken returns Invalid for invalid structure`() = runTest {
        // Given
        val token = "invalid.token"
        
        coEvery { tokenValidator.validateTokenStructure(token) } returns false
        
        // When
        val result = tokenManager.validateToken(token)
        
        // Then
        assertTrue(result is TokenValidationResult.Invalid)
    }
    
    @Test
    fun `test clearTokens clears storage and updates state`() = runTest {
        // Given
        coEvery { tokenStorage.clearTokens() } just Runs
        
        // When
        tokenManager.clearTokens()
        val tokenState = tokenManager.observeTokenState().first()
        
        // Then
        assertEquals(TokenState.NoToken, tokenState)
        coVerify { tokenStorage.clearTokens() }
    }
    
    @Test
    fun `test hasValidTokens returns true for valid token`() = runTest {
        // Given
        val accessToken = "valid.access.token"
        val claims = TokenClaims(
            sub = "user123",
            iat = System.currentTimeMillis() / 1000,
            exp = (System.currentTimeMillis() / 1000) + 3600,
            role = "STUDENT"
        )
        
        coEvery { tokenStorage.getAccessToken() } returns accessToken
        coEvery { tokenValidator.validateTokenStructure(accessToken) } returns true
        coEvery { tokenValidator.verifySignature(accessToken) } returns true
        coEvery { tokenValidator.extractClaims(accessToken) } returns claims
        coEvery { tokenValidator.isTokenExpired(claims) } returns false
        
        // When
        val result = tokenManager.hasValidTokens()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `test hasValidTokens returns false when no token`() = runTest {
        // Given
        coEvery { tokenStorage.getAccessToken() } returns null
        
        // When
        val result = tokenManager.hasValidTokens()
        
        // Then
        assertFalse(result)
    }
}