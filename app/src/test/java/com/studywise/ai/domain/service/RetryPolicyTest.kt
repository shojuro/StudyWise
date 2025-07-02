package com.studywise.ai.domain.service

import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for RetryPolicy implementations
 */
class RetryPolicyTest {

    @Test
    fun `test ExponentialBackoffRetryPolicy getRetryDelay`() {
        // Given
        val retryPolicy = ExponentialBackoffRetryPolicy(
            baseDelayMs = 1000L,
            maxDelayMs = 10000L,
            maxRetries = 3,
            factor = 2.0
        )

        // When & Then
        assertEquals(1000L, retryPolicy.getRetryDelay(1))
        assertEquals(2000L, retryPolicy.getRetryDelay(2))
        assertEquals(4000L, retryPolicy.getRetryDelay(3))
        assertEquals(8000L, retryPolicy.getRetryDelay(4))
    }

    @Test
    fun `test ExponentialBackoffRetryPolicy respects maxDelay`() {
        // Given
        val retryPolicy = ExponentialBackoffRetryPolicy(
            baseDelayMs = 1000L,
            maxDelayMs = 5000L,
            maxRetries = 5,
            factor = 2.0
        )

        // When & Then
        assertEquals(4000L, retryPolicy.getRetryDelay(3))
        assertEquals(5000L, retryPolicy.getRetryDelay(4)) // Capped at maxDelay
        assertEquals(5000L, retryPolicy.getRetryDelay(5)) // Still capped
    }

    @Test
    fun `test ExponentialBackoffRetryPolicy shouldRetry with retryable exception`() {
        // Given
        val retryPolicy = ExponentialBackoffRetryPolicy(maxRetries = 3)

        // When & Then
        assertTrue(retryPolicy.shouldRetry(1, IOException("Network error")))
        assertTrue(retryPolicy.shouldRetry(2, SocketTimeoutException("Timeout")))
        assertFalse(retryPolicy.shouldRetry(3, IOException("Network error"))) // Max retries reached
    }

    @Test
    fun `test ExponentialBackoffRetryPolicy shouldRetry with non-retryable exception`() {
        // Given
        val retryPolicy = ExponentialBackoffRetryPolicy(maxRetries = 3)

        // When & Then
        assertFalse(retryPolicy.shouldRetry(1, IllegalArgumentException("Invalid argument")))
        assertFalse(retryPolicy.shouldRetry(1, SecurityException("Security violation")))
        assertFalse(retryPolicy.shouldRetry(1, IllegalStateException("Invalid state")))
    }

    @Test
    fun `test ExponentialBackoffRetryPolicy executeWithRetry succeeds on first attempt`() = runTest {
        // Given
        val retryPolicy = ExponentialBackoffRetryPolicy(maxRetries = 3)
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry {
            attemptCount++
            "success"
        }

        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(1, attemptCount)
    }

    @Test
    fun `test ExponentialBackoffRetryPolicy executeWithRetry succeeds after retries`() = runTest {
        // Given
        val retryPolicy = ExponentialBackoffRetryPolicy(
            baseDelayMs = 10L, // Short delay for testing
            maxRetries = 3
        )
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry {
            attemptCount++
            if (attemptCount < 3) {
                throw IOException("Network error")
            }
            "success"
        }

        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(3, attemptCount)
    }

    @Test
    fun `test ExponentialBackoffRetryPolicy executeWithRetry fails after max retries`() = runTest {
        // Given
        val retryPolicy = ExponentialBackoffRetryPolicy(
            baseDelayMs = 10L, // Short delay for testing
            maxRetries = 2
        )
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry {
            attemptCount++
            throw IOException("Persistent network error")
        }

        // Then
        assertTrue(result.isFailure)
        assertEquals(2, attemptCount)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `test ExponentialBackoffRetryPolicy executeWithRetry fails immediately for non-retryable exception`() = runTest {
        // Given
        val retryPolicy = ExponentialBackoffRetryPolicy(maxRetries = 3)
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry {
            attemptCount++
            throw IllegalArgumentException("Invalid argument")
        }

        // Then
        assertTrue(result.isFailure)
        assertEquals(1, attemptCount)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `test LinearBackoffRetryPolicy getRetryDelay`() {
        // Given
        val retryPolicy = LinearBackoffRetryPolicy(
            baseDelayMs = 1000L,
            incrementMs = 500L,
            maxDelayMs = 5000L
        )

        // When & Then
        assertEquals(1000L, retryPolicy.getRetryDelay(1))
        assertEquals(1500L, retryPolicy.getRetryDelay(2))
        assertEquals(2000L, retryPolicy.getRetryDelay(3))
        assertEquals(2500L, retryPolicy.getRetryDelay(4))
    }

    @Test
    fun `test LinearBackoffRetryPolicy respects maxDelay`() {
        // Given
        val retryPolicy = LinearBackoffRetryPolicy(
            baseDelayMs = 1000L,
            incrementMs = 2000L,
            maxDelayMs = 4000L
        )

        // When & Then
        assertEquals(3000L, retryPolicy.getRetryDelay(2))
        assertEquals(4000L, retryPolicy.getRetryDelay(3)) // Capped at maxDelay
        assertEquals(4000L, retryPolicy.getRetryDelay(4)) // Still capped
    }

    @Test
    fun `test NoRetryPolicy never retries`() = runTest {
        // Given
        val retryPolicy = NoRetryPolicy()
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry {
            attemptCount++
            if (attemptCount == 1) {
                throw IOException("Network error")
            }
            "success"
        }

        // Then
        assertTrue(result.isFailure)
        assertEquals(1, attemptCount)
        assertFalse(retryPolicy.shouldRetry(1, IOException("Any error")))
        assertEquals(0L, retryPolicy.getRetryDelay(1))
    }

    @Test
    fun `test NoRetryPolicy succeeds on first attempt`() = runTest {
        // Given
        val retryPolicy = NoRetryPolicy()
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry {
            attemptCount++
            "success"
        }

        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(1, attemptCount)
    }
}