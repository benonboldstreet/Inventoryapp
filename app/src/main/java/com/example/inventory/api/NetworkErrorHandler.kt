package com.example.inventory.api

import android.util.Log
import androidx.compose.runtime.MutableState
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
    // Private mutable state
    private val _lastErrorMessage = mutableStateOf<String?>(null)
    private val _isLoading = mutableStateOf(false)
    
    // Public immutable access
    val lastErrorMessage: MutableState<String?> = _lastErrorMessage
    val isLoading: MutableState<Boolean> = _isLoading
    
    // Initialize by subscribing to SharedViewModel network state
    init {
        // When cloud connectivity changes, update our error state accordingly
        if (!SharedViewModel.isCloudConnected.value) {
            _lastErrorMessage.value = "Not connected to cloud services. Please check your internet connection."
        }
        
        // This is a callback to set our error message automatically when connectivity is lost
        SharedViewModel.addConnectivityListener { isConnected ->
            if (!isConnected) {
                _lastErrorMessage.value = "Lost connection to cloud services. Please check your internet connection."
            } else {
                // Only clear errors if they were connectivity related
                if (_lastErrorMessage.value?.contains("connect") == true || 
                    _lastErrorMessage.value?.contains("Connection") == true) {
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
        // Set loading state
        _isLoading.value = true
        _lastErrorMessage.value = null
        
        return try {
            val result = apiCall()
            // Clear loading state
            _isLoading.value = false
            result
        } catch (e: Exception) {
            // Create a local variable for the error message
            val errorMessageText = when (e) {
                is UnknownHostException -> "Cannot connect to cloud server. Please check your internet connection."
                is ConnectException -> "Connection to cloud server failed. Server may be down."
                is SocketTimeoutException -> "Connection to cloud server timed out. Please try again."
                else -> "Error connecting to cloud service: ${e.message}"
            }
            
            // Log the error
            Log.e("CloudInventory", "Error in $operationName: $errorMessageText", e)
            
            // Update the error message for UI display
            _lastErrorMessage.value = errorMessageText
            
            // Update SharedViewModel connectivity if this is a connection error
            if (e is UnknownHostException || e is ConnectException) {
                SharedViewModel.setCloudConnected(false)
            }
            
            // Clear loading state
            _isLoading.value = false
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
        // Set loading state
        _isLoading.value = true
        _lastErrorMessage.value = null
        
        try {
            val result = apiCall()
            // Clear loading state
            _isLoading.value = false
            emit(result)
        } catch (e: Exception) {
            // Create a local variable for the error message
            val errorMessageText = when (e) {
                is UnknownHostException -> "Cannot connect to cloud server. Please check your internet connection."
                is ConnectException -> "Connection to cloud server failed. Server may be down."
                is SocketTimeoutException -> "Connection to cloud server timed out. Please try again."
                else -> "Error connecting to cloud service: ${e.message}"
            }
            
            // Log the error
            Log.e("CloudInventory", "Error in $operationName: $errorMessageText", e)
            
            // Update the error message for UI display
            _lastErrorMessage.value = errorMessageText
            
            // Update SharedViewModel connectivity if this is a connection error
            if (e is UnknownHostException || e is ConnectException) {
                SharedViewModel.setCloudConnected(false)
            }
            
            // Clear loading state
            _isLoading.value = false
            emit(emptyValue)
        }
    }
    
    /**
     * Clear any previous error messages
     */
    fun clearErrors() {
        _lastErrorMessage.value = null
    }
} 