package com.example.inventory.data.model

import java.util.UUID

/**
 * Represents an operation that is pending syncing with the remote server
 */
data class PendingOperation(
    /**
     * Unique identifier for the operation
     */
    val id: UUID = UUID.randomUUID(),
    
    /**
     * Type of operation (CREATE, UPDATE, DELETE)
     */
    val operationType: OperationType,
    
    /**
     * The name of the collection this operation affects
     */
    val collectionName: String,
    
    /**
     * The ID of the document this operation affects
     */
    val documentId: String,
    
    /**
     * JSON data for the operation
     */
    val data: String,
    
    /**
     * Timestamp when the operation was created
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * Number of retry attempts made
     */
    val retryCount: Int = 0
)

/**
 * Types of operations that can be performed on data
 */
enum class OperationType {
    /**
     * Create a new document
     */
    CREATE,
    
    /**
     * Update an existing document
     */
    UPDATE,
    
    /**
     * Delete a document
     */
    DELETE
} 