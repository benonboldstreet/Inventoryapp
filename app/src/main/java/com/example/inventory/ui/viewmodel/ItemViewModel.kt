package com.example.inventory.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ItemViewModel"
    }

    private val _uiState = MutableStateFlow<ItemUiState>(ItemUiState.Loading)
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    // Direct access to repository - no mapping needed since we're using model objects
    val allItems: Flow<List<Item>> = itemRepository.getAllItems()
    
    // Filter for active items in the ViewModel
    val activeItems: Flow<List<Item>> = itemRepository.getAllItems()
        .map { items -> items.filter { it.isActive } }
    
    // Filter for archived items in the ViewModel
    val archivedItems: Flow<List<Item>> = itemRepository.getAllItems()
        .map { items -> items.filter { !it.isActive } }
    
    // Items that have "Available" status
    val availableItems: Flow<List<Item>> = itemRepository.getAllItems()
        .map { items -> items.filter { it.status == "Available" } }
    
    // Items that have "Checked Out" status
    val checkedOutItems: Flow<List<Item>> = itemRepository.getAllItems()
        .map { items -> items.filter { it.status == "Checked Out" } }
    
    // Group items by category
    val itemsByCategory: Flow<Map<String, List<Item>>> = itemRepository.getAllItems()
        .map { items -> items.groupBy { it.category } }
    
    // Get items by type
    fun getItemsByType(type: String, activeOnly: Boolean = true): Flow<List<Item>> {
        return itemRepository.getAllItems()
            .map { items -> 
                items.filter { it.type == type }
                    .filter { if (activeOnly) it.isActive else true }
            }
    }
    
    // Get item by ID
    fun getItemById(id: UUID): Flow<Item?> {
        return itemRepository.getItemById(id)
    }
    
    // Get item by barcode
    suspend fun getItemByBarcode(barcode: String): Result<Item?> {
        return try {
            val item = itemRepository.getItemByBarcode(barcode).first()
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting item by barcode: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Add a new item
    suspend fun addItem(item: Item): Result<Item> {
        return try {
            _uiState.value = ItemUiState.Loading
                itemRepository.insertItem(item)
            _uiState.value = ItemUiState.Success
            Result.success(item)
            } catch (e: Exception) {
            Log.e(TAG, "Error adding item: ${e.message}", e)
            _uiState.value = ItemUiState.Error("Failed to add item: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Update an existing item
    suspend fun updateItem(item: Item): Result<Item> {
        return try {
            _uiState.value = ItemUiState.Loading
                itemRepository.updateItem(item)
            _uiState.value = ItemUiState.Success
            Result.success(item)
            } catch (e: Exception) {
            Log.e(TAG, "Error updating item: ${e.message}", e)
            _uiState.value = ItemUiState.Error("Failed to update item: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Delete an item
    suspend fun deleteItem(item: Item): Result<Unit> {
        return try {
            _uiState.value = ItemUiState.Loading
                itemRepository.deleteItem(item)
            _uiState.value = ItemUiState.Success
            Result.success(Unit)
            } catch (e: Exception) {
            Log.e(TAG, "Error deleting item: ${e.message}", e)
            _uiState.value = ItemUiState.Error("Failed to delete item: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Archive an item (update to mark as inactive)
    suspend fun archiveItem(item: Item): Result<Item> {
        return try {
            _uiState.value = ItemUiState.Loading
            val archivedItem = item.copy(isActive = false)
            itemRepository.updateItem(archivedItem)
            _uiState.value = ItemUiState.Success
            Result.success(archivedItem)
            } catch (e: Exception) {
            Log.e(TAG, "Error archiving item: ${e.message}", e)
            _uiState.value = ItemUiState.Error("Failed to archive item: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Restore an archived item (update to mark as active)
    suspend fun unarchiveItem(item: Item): Result<Item> {
        return try {
            _uiState.value = ItemUiState.Loading
            val unarchivedItem = item.copy(isActive = true)
            itemRepository.updateItem(unarchivedItem)
            _uiState.value = ItemUiState.Success
            Result.success(unarchivedItem)
            } catch (e: Exception) {
            Log.e(TAG, "Error unarchiving item: ${e.message}", e)
            _uiState.value = ItemUiState.Error("Failed to unarchive item: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Initialize the ViewModel
    init {
        loadItems()
    }
    
    // Load items from repository
    private fun loadItems() {
        viewModelScope.launch {
            try {
                _uiState.value = ItemUiState.Loading
                itemRepository.getAllItems()
                    .collect { items ->
                        _items.value = items
                        _uiState.value = ItemUiState.Success
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading items: ${e.message}", e)
                _uiState.value = ItemUiState.Error("Failed to load items: ${e.message}")
            }
        }
    }
    
    // Verify archive status for debugging
    fun verifyArchiveStatus(itemId: UUID, callback: (exists: Boolean, isArchived: Boolean?, error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                val item = itemRepository.getItemById(itemId).first()
                if (item != null) {
                    callback(true, !item.isActive, null)
                } else {
                    callback(false, null, "Item not found in database")
                }
            } catch (e: Exception) {
                callback(false, null, "Error: ${e.message}")
                }
        }
    }
}

sealed class ItemUiState {
    object Loading : ItemUiState()
    object Success : ItemUiState()
    data class Error(val message: String) : ItemUiState()
} 