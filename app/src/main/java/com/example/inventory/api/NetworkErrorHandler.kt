package com.example.inventory.api

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.inventory.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Network Error Handler for Cloud Operations
 * 
 * This class provides standardized error handling for all cloud operations,
 * with clear error messages that can be displayed to users.
 */
object NetworkErrorHandler {
    // Tracks the last error that occurred for display in the UI
    val lastErrorMessage = mutableStateOf<String?>(null)
    
    // Tracks if a network operation is in progress
    val isLoading = mutableStateOf(false)
    
    // Initialize by subscribing to SharedViewModel network state
    init {
        // When cloud connectivity changes, update our error state accordingly
        if (!SharedViewModel.isCloudConnected.value) {
            lastErrorMessage.value = "Not connected to cloud services. Please check your internet connection."
        }
        
        // This is a callback to set our error message automatically when connectivity is lost
        SharedViewModel.addConnectivityListener { isConnected ->
            if (!isConnected) {
                lastErrorMessage.value = "Lost connection to cloud services. Please check your internet connection."
            } else {
                // Only clear errors if they were connectivity related
                if (lastErrorMessage.value?.contains("connect") == true || 
                    lastErrorMessage.value?.contains("Connection") == true) {
                    clearErrors()
                }
            }
        }
    }
    
    /**
     * Handle API call with proper error handling and logging
     * 
     * @param operationName A descriptive name of the operation (e.g., "Get Items")
     * @param apiCall The suspend function to execute that makes the API call
     * @return The result of the API call, or null if an error occurred
     */
    suspend fun <T> handleApiCall(operationName: String, apiCall: suspend () -> T): T? {
        isLoading.value = true
        lastErrorMessage.value = null
        
        return try {
            val result = apiCall()
            isLoading.value = false
            result
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> "Cannot connect to cloud server. Please check your internet connection."
                is ConnectException -> "Connection to cloud server failed. Server may be down."
                is SocketTimeoutException -> "Connection to cloud server timed out. Please try again."
                else -> "Error connecting to cloud service: ${e.message}"
            }
            
            // Log the error
            Log.e("CloudInventory", "Error in $operationName: $errorMessage", e)
            
            // Update the error message for UI display
            lastErrorMessage.value = errorMessage
            
            // Update SharedViewModel connectivity if this is a connection error
            if (e is UnknownHostException || e is ConnectException) {
                SharedViewModel.isCloudConnected.value = false
            }
            
            isLoading.value = false
            null
        }
    }
    
    /**
     * Handle API call that returns a Flow, with proper error handling
     * 
     * @param operationName A descriptive name of the operation (e.g., "Get Items")
     * @param apiCall The suspend function to execute that makes the API call
     * @param emptyValue The value to emit if an error occurs
     * @return A flow containing the result of the API call, or the empty value if an error occurred
     */
    fun <T> handleApiCallFlow(
        operationName: String,
        apiCall: suspend () -> T,
        emptyValue: T
    ): Flow<T> = flow {
        isLoading.value = true
        lastErrorMessage.value = null
        
        try {
            val result = apiCall()
            isLoading.value = false
            emit(result)
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> "Cannot connect to cloud server. Please check your internet connection."
                is ConnectException -> "Connection to cloud server failed. Server may be down."
                is SocketTimeoutException -> "Connection to cloud server timed out. Please try again."
                else -> "Error connecting to cloud service: ${e.message}"
            }
            
            // Log the error
            Log.e("CloudInventory", "Error in $operationName: $errorMessage", e)
            
            // Update the error message for UI display
            lastErrorMessage.value = errorMessage
            
            // Update SharedViewModel connectivity if this is a connection error
            if (e is UnknownHostException || e is ConnectException) {
                SharedViewModel.isCloudConnected.value = false
            }
            
            isLoading.value = false
            emit(emptyValue)
        }
    }
    
    /**
     * Clear any previous error messages
     */
    fun clearErrors() {
        lastErrorMessage.value = null
    }
} 