package com.example.inventory.api

// This file is now deprecated.
// Use com.example.inventory.data.model.PendingOperation instead
// 
// To avoid breaking existing code, this import statement provides backward compatibility
import com.example.inventory.data.model.PendingOperation

/**
 * Represents an operation that needs to be synced with the cloud
 * 
 * This class is used to track operations that were performed while offline
 * and need to be synchronized when the connection is restored.
 */
data class PendingOperation(
    /**
     * The type of operation that needs to be performed
     */
    val type: OperationType,
    
    /**
     * The serialized data for the operation
     * This is a JSON string that can be deserialized into the appropriate model
     */
    val data: String,
    
    /**
     * When the operation was created
     */
    val timestamp: Long = System.currentTimeMillis()
) 