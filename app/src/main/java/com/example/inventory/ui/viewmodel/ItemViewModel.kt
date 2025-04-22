package com.example.inventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.database.Item
import com.example.inventory.data.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {

    // Get all items as a Flow
    val allItems: Flow<List<Item>> = repository.getAllItems()
    
    // Get active items only
    val activeItems: Flow<List<Item>> = repository.getAllItems().map { items ->
        items.filter { it.isActive }
    }
    
    // Get archived items only
    val archivedItems: Flow<List<Item>> = repository.getAllItems().map { items ->
        items.filter { !it.isActive }
    }
    
    // Get items with "Available" status
    val availableItems: Flow<List<Item>> = repository.getItemsByStatus("Available")
        .map { items -> items.filter { it.isActive } }
    
    // Get items with "Checked Out" status
    val checkedOutItems: Flow<List<Item>> = repository.getItemsByStatus("Checked Out")
        .map { items -> items.filter { it.isActive } }
    
    /**
     * Get items by category
     */
    fun getItemsByCategory(category: String, activeOnly: Boolean = true): Flow<List<Item>> = 
        repository.getItemsByCategory(category).map { items ->
            if (activeOnly) items.filter { it.isActive } else items
        }
    
    /**
     * Get items by type
     */
    fun getItemsByType(type: String, activeOnly: Boolean = true): Flow<List<Item>> = 
        repository.getItemsByType(type).map { items ->
            if (activeOnly) items.filter { it.isActive } else items
        }
    
    /**
     * Add a new item
     */
    fun addItem(
        name: String,
        type: String,
        barcode: String,
        condition: String,
        status: String,
        category: String = "Other",
        photoPath: String? = null
    ) {
        val newItem = Item(
            name = name,
            type = type,
            barcode = barcode,
            condition = condition,
            status = status,
            category = category,
            photoPath = photoPath
        )
        viewModelScope.launch {
            repository.insertItem(newItem)
        }
    }
    
    /**
     * Update an existing item
     */
    fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }
    
    /**
     * Delete an item
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
    
    /**
     * Archive an item (mark as inactive) instead of deleting
     */
    fun archiveItem(item: Item) {
        val archivedItem = item.copy(isActive = false, lastModified = System.currentTimeMillis())
        viewModelScope.launch {
            repository.updateItem(archivedItem)
        }
    }
    
    /**
     * Unarchive an item (restore to active)
     */
    fun unarchiveItem(item: Item) {
        val activeItem = item.copy(isActive = true, lastModified = System.currentTimeMillis())
        viewModelScope.launch {
            repository.updateItem(activeItem)
        }
    }
    
    /**
     * Get item by barcode
     */
    suspend fun getItemByBarcode(barcode: String): Item? = repository.getItemByBarcode(barcode)
    
    /**
     * Get item by ID
     */
    suspend fun getItemById(id: UUID): Item? = repository.getItemById(id)
    
    /**
     * Update item status
     */
    fun updateItemStatus(item: Item, newStatus: String) {
        val updatedItem = item.copy(status = newStatus)
        viewModelScope.launch {
            repository.updateItem(updatedItem)
        }
    }
    
    /**
     * Update item condition
     */
    fun updateItemCondition(item: Item, newCondition: String) {
        val updatedItem = item.copy(condition = newCondition)
        viewModelScope.launch {
            repository.updateItem(updatedItem)
        }
    }
    
    /**
     * Factory for creating ItemViewModel with dependency injection
     */
    class Factory(private val repository: ItemRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
                return ItemViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 