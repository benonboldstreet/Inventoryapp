package com.example.inventory.data.model

/**
 * Status of synchronization operations
 * Used for tracking the state of data synchronization with the cloud
 */
enum class SyncStatus {
    /**
     * No synchronization in progress, waiting for changes
     */
    IDLE,
    
    /**
     * Synchronization is currently in progress
     */
    IN_PROGRESS,
    
    /**
     * Synchronization completed successfully for all items
     */
    COMPLETE,
    
    /**
     * Synchronization completed partially (some items failed)
     */
    PARTIAL,
    
    /**
     * Synchronization failed completely
     */
    FAILED,
    
    /**
     * Device is offline, synchronization not possible
     */
    OFFLINE
} 