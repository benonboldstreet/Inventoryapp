package com.example.inventory.data.repository

import com.example.inventory.data.model.Item
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for Item operations
 */
interface ItemRepository {
    
    /**
     * Get all items
     */
    fun getAllItems(): Flow<List<Item>>
    
    /**
     * Get an item by ID
     */
    fun getItemById(id: UUID): Flow<Item?>
    
    /**
     * Get an item by barcode
     */
    fun getItemByBarcode(barcode: String): Flow<Item?>
    
    /**
     * Get items by category
     */
    fun getItemsByCategory(category: String): Flow<List<Item>>
    
    /**
     * Get all categories
     */
    fun getAllCategories(): Flow<List<String>>
    
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
    
    /**
     * Refresh data from Firebase
     */
    suspend fun refreshFromFirebase()
} 