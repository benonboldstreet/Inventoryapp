package com.example.inventory.api

// This file is now deprecated.
// Use com.example.inventory.data.model.SyncStatus instead
// 
// To avoid breaking existing code, this import statement provides backward compatibility
import com.example.inventory.data.model.SyncStatus

/**
 * Represents the current state of data synchronization with the cloud
 */
enum class SyncStatus {
    /**
     * No sync operation is in progress
     */
    IDLE,
    
    /**
     * A sync operation is currently in progress
     */
    IN_PROGRESS,
    
    /**
     * The last sync operation completed successfully
     */
    COMPLETE,
    
    /**
     * The last sync operation completed with some items still pending
     */
    PARTIAL,
    
    /**
     * The last sync operation failed
     */
    FAILED,
    
    /**
     * The device is offline and no sync is possible
     */
    OFFLINE
} 