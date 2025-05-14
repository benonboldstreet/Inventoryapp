package com.inventory.data

import com.inventory.model.Item
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the data repository operations.
 * This will be implemented by both real and mock repositories.
 */
interface Repository {
    /**
     * Get all inventory items
     */
    suspend fun getAllItems(): Flow<List<Item>>
    
    /**
     * Get a specific item by ID
     */
    suspend fun getItemById(id: String): Flow<Item?>
    
    /**
     * Add a new item to the inventory
     */
    suspend fun addItem(item: Item): Flow<Boolean>
    
    /**
     * Update an existing item
     */
    suspend fun updateItem(item: Item): Flow<Boolean>
    
    /**
     * Delete an item from the inventory
     */
    suspend fun deleteItem(id: String): Flow<Boolean>
} 