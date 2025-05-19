package com.example.inventory.api

import android.util.Log
import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Network Retry Utility
 * 
 * Provides retry functionality with exponential backoff for network operations.
 * Helps handle transient network issues gracefully.
 */
object NetworkRetry {
    private const val TAG = "NetworkRetry"
    
    /**
     * Default retry configuration
     */
    private val DEFAULT_CONFIG = RetryConfig(
        maxAttempts = 3,
        initialDelayMs = 1000,
        maxDelayMs = 10000,
        factor = 2.0
    )
    
    /**
     * Retry configuration
     * 
     * @param maxAttempts Maximum number of retry attempts
     * @param initialDelayMs Initial delay in milliseconds
     * @param maxDelayMs Maximum delay in milliseconds
     * @param factor Backoff factor (exponential)
     */
    data class RetryConfig(
        val maxAttempts: Int,
        val initialDelayMs: Long,
        val maxDelayMs: Long,
        val factor: Double
    )
    
    /**
     * Execute a suspending operation with retry logic
     * 
     * @param config Retry configuration
     * @param operation The suspending operation to execute
     */
    suspend fun <T> executeWithRetry(
        config: RetryConfig = DEFAULT_CONFIG,
        operation: suspend () -> T
    ): T {
        var currentDelay = config.initialDelayMs
        var attempt = 1
        
        while (true) {
            try {
                // Attempt the operation
                return operation()
            } catch (e: Exception) {
                // If we've reached max attempts, throw the exception
                if (attempt >= config.maxAttempts) {
                    Log.e(TAG, "Max retry attempts reached after ${attempt} tries", e)
                    throw e
                }
                
                // Log the retry
                Log.d(TAG, "Retry attempt ${attempt}/${config.maxAttempts} after ${currentDelay}ms due to: ${e.message}")
                
                // Wait before next attempt
                delay(currentDelay)
                
                // Increase the delay for next time (with max cap)
                currentDelay = (currentDelay * config.factor).toLong().coerceAtMost(config.maxDelayMs)
                attempt++
            }
        }
    }
    
    /**
     * Extension function to retry a suspending operation
     */
    suspend fun <T> (suspend () -> T).withRetry(
        config: RetryConfig = DEFAULT_CONFIG
    ): T {
        return executeWithRetry(config, this)
    }
} 