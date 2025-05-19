package com.example.inventory.api

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