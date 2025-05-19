package com.example.inventory.data.repository

import com.example.inventory.data.model.Item
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for managing Item data operations
 * 
 * This defines the contract for item repository implementations,
 * whether they're using local storage or cloud storage.
 */
interface ItemRepository {
    /**
     * Get all items as a Flow
     */
    fun getAllItems(): Flow<List<Item>>
    
    /**
     * Get items by status as a Flow
     */
    fun getItemsByStatus(status: String): Flow<List<Item>>
    
    /**
     * Get items by type as a Flow
     */
    fun getItemsByType(type: String): Flow<List<Item>>
    
    /**
     * Get items by category as a Flow
     */
    fun getItemsByCategory(category: String): Flow<List<Item>>
    
    /**
     * Get all custom categories as a Flow
     */
    fun getAllCategories(): Flow<List<String>>
    
    /**
     * Get item by its barcode
     */
    suspend fun getItemByBarcode(barcode: String): Item?
    
    /**
     * Get item by ID
     */
    suspend fun getItemById(id: UUID): Item?
    
    /**
     * Insert a new item
     */
    suspend fun insertItem(item: Item)
    
    /**
     * Update an existing item
     */
    suspend fun updateItem(item: Item)
    
    /**
     * Delete an item
     */
    suspend fun deleteItem(item: Item)
} 