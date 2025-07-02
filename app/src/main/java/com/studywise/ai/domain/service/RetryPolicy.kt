package com.studywise.ai.domain.service

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

/**
 * Policy for retrying failed operations
 */
interface RetryPolicy {
    /**
     * Calculate delay before next retry attempt
     */
    fun getRetryDelay(attemptNumber: Int): Long
    
    /**
     * Check if operation should be retried
     */
    fun shouldRetry(attemptNumber: Int, error: Throwable): Boolean
    
    /**
     * Execute operation with retry logic
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T
    ): Result<T>
}

/**
 * Exponential backoff retry policy
 */
class ExponentialBackoffRetryPolicy(
    private val baseDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 60000L,
    private val maxRetries: Int = 3,
    private val factor: Double = 2.0
) : RetryPolicy {
    
    override fun getRetryDelay(attemptNumber: Int): Long {
        val exponentialDelay = baseDelayMs * factor.pow(attemptNumber - 1).toLong()
        return min(exponentialDelay, maxDelayMs)
    }
    
    override fun shouldRetry(attemptNumber: Int, error: Throwable): Boolean {
        if (attemptNumber >= maxRetries) return false
        
        // Don't retry for non-retryable errors
        return when (error) {
            is IllegalArgumentException -> false
            is SecurityException -> false
            is IllegalStateException -> false
            else -> true
        }
    }
    
    override suspend fun <T> executeWithRetry(operation: suspend () -> T): Result<T> {
        var lastError: Throwable? = null
        
        for (attempt in 1..maxRetries) {
            try {
                return Result.success(operation())
            } catch (e: Exception) {
                lastError = e
                
                if (!shouldRetry(attempt, e)) {
                    return Result.failure(e)
                }
                
                if (attempt < maxRetries) {
                    val delayMs = getRetryDelay(attempt)
                    delay(delayMs)
                }
            }
        }
        
        return Result.failure(lastError ?: Exception("Max retries exceeded"))
    }
}

/**
 * Linear backoff retry policy
 */
class LinearBackoffRetryPolicy(
    private val baseDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 30000L,
    private val maxRetries: Int = 3,
    private val incrementMs: Long = 1000L
) : RetryPolicy {
    
    override fun getRetryDelay(attemptNumber: Int): Long {
        val linearDelay = baseDelayMs + (incrementMs * (attemptNumber - 1))
        return min(linearDelay, maxDelayMs)
    }
    
    override fun shouldRetry(attemptNumber: Int, error: Throwable): Boolean {
        return attemptNumber < maxRetries && error !is IllegalArgumentException
    }
    
    override suspend fun <T> executeWithRetry(operation: suspend () -> T): Result<T> {
        var lastError: Throwable? = null
        
        for (attempt in 1..maxRetries) {
            try {
                return Result.success(operation())
            } catch (e: Exception) {
                lastError = e
                
                if (!shouldRetry(attempt, e)) {
                    return Result.failure(e)
                }
                
                if (attempt < maxRetries) {
                    val delayMs = getRetryDelay(attempt)
                    delay(delayMs)
                }
            }
        }
        
        return Result.failure(lastError ?: Exception("Max retries exceeded"))
    }
}

/**
 * No retry policy - operations are attempted only once
 */
class NoRetryPolicy : RetryPolicy {
    override fun getRetryDelay(attemptNumber: Int): Long = 0L
    
    override fun shouldRetry(attemptNumber: Int, error: Throwable): Boolean = false
    
    override suspend fun <T> executeWithRetry(operation: suspend () -> T): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}