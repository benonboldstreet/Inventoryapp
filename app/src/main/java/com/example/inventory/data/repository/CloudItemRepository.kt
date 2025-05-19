package com.example.inventory.data.repository

import android.content.Context
import android.util.Log
import com.example.inventory.api.ItemApiService
import com.example.inventory.api.NetworkErrorHandler
import com.example.inventory.api.NetworkModule
import com.example.inventory.api.OfflineCache
import com.example.inventory.api.OperationType
import com.example.inventory.api.PendingOperation
import com.example.inventory.api.toDto
import com.example.inventory.api.toEntity
import com.example.inventory.data.model.Item
import com.example.inventory.ui.viewmodel.SharedViewModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Cloud repository implementation for managing Item data operations
 * This connects to Azure/cloud backend instead of local database
 * Supports offline operation with caching and synchronization
 */
class CloudItemRepository(
    private val apiService: ItemApiService = NetworkModule.itemApiService,
    private val appContext: Context
) : ItemRepository {
    
    // Gson for serializing items for offline storage
    private val gson = Gson()
    
    init {
        // Validate API service initialization
        if (apiService == null) {
            throw IllegalStateException("ItemApiService not properly initialized")
        }
    }
    
    /**
     * Get all items as a Flow
     */
    override fun getAllItems(): Flow<List<Item>> = flow {
        try {
            // First check if we're online
            if (SharedViewModel.isCloudConnected.value) {
                // Try to get from cloud
                val result = NetworkErrorHandler.handleApiCall(
                    operationName = "Get All Items",
                    apiCall = { 
                        val items = apiService.getAllItems().map { it.toEntity() }
                        // Cache the results for offline use
                        OfflineCache.cacheItems(items, appContext)
                        items
                    }
                )
                
                if (result != null) {
                    emit(result)
                } else {
                    // If cloud request failed, fall back to cache
                    emit(OfflineCache.getCachedItems())
                }
            } else {
                // If offline, use cached data
                emit(OfflineCache.getCachedItems())
            }
        } catch (e: Exception) {
            // Log error and fall back to cache
            Log.e("CloudItemRepository", "Error getting all items", e)
            emit(OfflineCache.getCachedItems())
        }
    }
    
    /**
     * Get items by status as a Flow
     */
    override fun getItemsByStatus(status: String): Flow<List<Item>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Items by Status ($status)",
                apiCall = { 
                    apiService.getAllItems()
                        .filter { it.status == status }
                        .map { it.toEntity() }
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache
                emit(OfflineCache.getCachedItems().filter { it.status == status })
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedItems().filter { it.status == status })
        }
    }
    
    /**
     * Get items by type as a Flow
     */
    override fun getItemsByType(type: String): Flow<List<Item>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Items by Type ($type)",
                apiCall = { 
                    apiService.getAllItems()
                        .filter { it.type == type }
                        .map { it.toEntity() }
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache
                emit(OfflineCache.getCachedItems().filter { it.type == type })
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedItems().filter { it.type == type })
        }
    }
    
    /**
     * Get items by category as a Flow
     */
    override fun getItemsByCategory(category: String): Flow<List<Item>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Items by Category ($category)",
                apiCall = { 
                    apiService.getAllItems()
                        .filter { it.category == category }
                        .map { it.toEntity() }
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache
                emit(OfflineCache.getCachedItems().filter { it.category == category })
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedItems().filter { it.category == category })
        }
    }
    
    /**
     * Get all custom categories as a Flow
     */
    override fun getAllCategories(): Flow<List<String>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get All Categories",
                apiCall = { 
                    apiService.getAllItems()
                        .map { it.category }
                        .distinct()
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache
                emit(OfflineCache.getCachedItems().map { it.category }.distinct())
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedItems().map { it.category }.distinct())
        }
    }
    
    /**
     * Get item by its barcode
     * 
     * [CLOUD ENDPOINT - READ] Retrieves an item from cloud storage by barcode
     */
    override suspend fun getItemByBarcode(barcode: String): Item? {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Item by Barcode ($barcode)",
                apiCall = { 
                    apiService.getItemByBarcode(barcode).toEntity()
                }
            )
            
            if (result != null) {
                // Cache the item
                OfflineCache.cacheItem(result, appContext)
                return result
            }
        }
        
        // If offline or cloud request failed, search in cache
        return OfflineCache.getCachedItems().find { it.barcode == barcode }
    }
    
    /**
     * Get item by ID
     * 
     * [CLOUD ENDPOINT - READ] Retrieves an item from cloud storage by ID
     */
    override suspend fun getItemById(id: UUID): Item? {
        // First check cache for immediate response
        val cachedItem = OfflineCache.getCachedItem(id)
        
        // If online, try to get fresh data
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Item by ID ($id)",
                apiCall = { 
                    apiService.getItemById(id.toString()).toEntity()
                }
            )
            
            if (result != null) {
                // Cache the item
                OfflineCache.cacheItem(result, appContext)
                return result
            }
        }
        
        // Return cached item if we have it
        return cachedItem
    }
    
    /**
     * Insert a new item
     * 
     * [CLOUD ENDPOINT - CREATE] Creates a new item in cloud storage via API
     */
    override suspend fun insertItem(item: Item) {
        // Add to the local cache immediately for responsiveness
        OfflineCache.cacheItem(item, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            NetworkErrorHandler.handleApiCall(
                operationName = "Insert Item",
                apiCall = { 
                    apiService.createItem(item.toDto())
                }
            )
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.INSERT_ITEM,
                data = gson.toJson(item)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
    }
    
    /**
     * Update an existing item
     * 
     * [CLOUD ENDPOINT - UPDATE] Updates an item in cloud storage via API
     */
    override suspend fun updateItem(item: Item) {
        // Update the local cache immediately for responsiveness
        val updatedItem = item.copy(lastModified = System.currentTimeMillis())
        OfflineCache.cacheItem(updatedItem, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            NetworkErrorHandler.handleApiCall(
                operationName = "Update Item",
                apiCall = { 
                    apiService.updateItem(updatedItem.id.toString(), updatedItem.toDto())
                }
            )
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.UPDATE_ITEM,
                data = gson.toJson(updatedItem)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
    }
    
    /**
     * Delete an item
     * 
     * [CLOUD ENDPOINT - DELETE] Deletes an item from cloud storage via API
     * Note: This could be implemented as a soft delete on the server
     */
    override suspend fun deleteItem(item: Item) {
        // Update the local cache immediately for responsiveness
        val deletedItem = item.copy(isActive = false, lastModified = System.currentTimeMillis())
        OfflineCache.cacheItem(deletedItem, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            NetworkErrorHandler.handleApiCall(
                operationName = "Delete Item",
                apiCall = { 
                    apiService.updateItemStatus(item.id.toString(), mapOf("isActive" to "false"))
                }
            )
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.DELETE_ITEM,
                data = gson.toJson(deletedItem)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
    }
} 