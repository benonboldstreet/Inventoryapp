package com.example.inventory.api

/**
 * Types of operations that can be pending sync with the cloud
 */
enum class OperationType {
    /**
     * Insert a new item
     */
    INSERT_ITEM,
    
    /**
     * Update an existing item
     */
    UPDATE_ITEM,
    
    /**
     * Delete an item
     */
    DELETE_ITEM,
    
    /**
     * Insert a new staff member
     */
    INSERT_STAFF,
    
    /**
     * Update an existing staff member
     */
    UPDATE_STAFF,
    
    /**
     * Delete a staff member
     */
    DELETE_STAFF,
    
    /**
     * Create a new checkout log
     */
    CREATE_CHECKOUT,
    
    /**
     * Update a checkout log (e.g., check in)
     */
    UPDATE_CHECKOUT,
    
    /**
     * Delete a checkout log
     */
    DELETE_CHECKOUT
} 