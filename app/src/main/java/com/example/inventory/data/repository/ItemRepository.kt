package com.example.inventory.data.repository

import com.example.inventory.data.database.Item
import com.example.inventory.data.database.ItemDao
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for managing Item data operations
 */
class ItemRepository(private val itemDao: ItemDao) {
    
    /**
     * Get all items as a Flow
     */
    fun getAllItems(): Flow<List<Item>> = itemDao.getAllItems()
    
    /**
     * Get items by status as a Flow
     */
    fun getItemsByStatus(status: String): Flow<List<Item>> = itemDao.getItemsByStatus(status)
    
    /**
     * Get items by type as a Flow
     */
    fun getItemsByType(type: String): Flow<List<Item>> = itemDao.getItemsByType(type)
    
    /**
     * Get items by category as a Flow
     */
    fun getItemsByCategory(category: String): Flow<List<Item>> = itemDao.getItemsByCategory(category)
    
    /**
     * Get all custom categories as a Flow
     */
    fun getAllCategories(): Flow<List<String>> = itemDao.getAllCategories()
    
    /**
     * Get item by its barcode
     */
    suspend fun getItemByBarcode(barcode: String): Item? = itemDao.getItemByBarcode(barcode)
    
    /**
     * Get item by ID
     */
    suspend fun getItemById(id: UUID): Item? = itemDao.getItemById(id)
    
    /**
     * Insert a new item
     */
    suspend fun insertItem(item: Item) = itemDao.insert(item)
    
    /**
     * Update an existing item
     */
    suspend fun updateItem(item: Item) {
        // Ensure the lastModified timestamp is updated
        val updatedItem = item.copy(lastModified = System.currentTimeMillis())
        itemDao.update(updatedItem)
    }
    
    /**
     * Delete an item
     */
    suspend fun deleteItem(item: Item) = itemDao.delete(item)
} 