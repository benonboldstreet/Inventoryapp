package com.example.inventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.ui.viewmodel.ItemViewModelAdapter.toFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ItemUiState>(ItemUiState.Loading)
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    // Direct access to repository - no mapping needed since we're using model objects
    val allItems: Flow<List<Item>> = itemRepository.getAllItems()
    
    // Filter for active items in the ViewModel
    val activeItems: Flow<List<Item>> = itemRepository.getAllItems()
        .map { items -> items.filter { it.isActive } }
    
    // Filter for archived items in the ViewModel
    val archivedItems: Flow<List<Item>> = itemRepository.getAllItems()
        .map { items -> items.filter { !it.isActive } }
    
    // Items that have "Available" status
    val availableItems: Flow<List<Item>> = itemRepository.getItemsByStatus("Available")
    
    // Items that have "Checked Out" status
    val checkedOutItems: Flow<List<Item>> = itemRepository.getItemsByStatus("Checked Out")
    
    // Group items by category
    val itemsByCategory: Flow<Map<String, List<Item>>> = itemRepository.getAllItems()
        .map { items -> items.groupBy { it.category } }
    
    // Group items by type
    val itemsByType: Flow<Map<String, List<Item>>> = itemRepository.getAllItems()
        .map { items -> items.groupBy { it.type } }
    
    // Group items by status
    val itemsByStatus: Flow<Map<String, List<Item>>> = itemRepository.getAllItems()
        .map { items -> items.groupBy { it.status } }
    
    // Get item by ID - Convert suspend function to Flow
    fun getItemById(id: UUID): Flow<Item?> = flow {
        val item = itemRepository.getItemById(id)
        emit(item)
    }
    
    // Get item by barcode
    suspend fun getItemByBarcode(barcode: String): Item? = itemRepository.getItemByBarcode(barcode)
    
    // Add a new item
    fun addItem(item: Item) {
        viewModelScope.launch {
            try {
                itemRepository.insertItem(item)
                loadItems() // Reload items after adding
            } catch (e: Exception) {
                _uiState.value = ItemUiState.Error(e.message ?: "Failed to add item")
            }
        }
    }
    
    // Update an existing item
    fun updateItem(item: Item) {
        viewModelScope.launch {
            try {
                itemRepository.updateItem(item)
                loadItems() // Reload items after updating
            } catch (e: Exception) {
                _uiState.value = ItemUiState.Error(e.message ?: "Failed to update item")
            }
        }
    }
    
    // Delete an item
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            try {
                itemRepository.deleteItem(item)
                loadItems() // Reload items after deleting
            } catch (e: Exception) {
                _uiState.value = ItemUiState.Error(e.message ?: "Failed to delete item")
            }
        }
    }
    
    // Archive an item (update to mark as inactive)
    fun archiveItem(item: Item) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "===== ARCHIVE ITEM DEBUGGING =====")
                android.util.Log.d("ItemViewModel", "Original item: ${item.id} - ${item.name}, isActive=${item.isActive}")
                
                // First, check if the item exists in Firestore
                val existingItem = itemRepository.getItemById(item.id)
                android.util.Log.d("ItemViewModel", "Item exists in database before archive: ${existingItem != null}")
                
                if (existingItem == null) {
                    android.util.Log.e("ItemViewModel", "CRITICAL ERROR: Item doesn't exist in database before archive")
                    throw Exception("Item doesn't exist in database before archive")
                }
                
                // Make a copy with isActive set to false
                val updatedItem = item.copy(
                    isActive = false,
                    lastModified = System.currentTimeMillis()
                )
                
                android.util.Log.d("ItemViewModel", "Created updatedItem with isActive=false, original item ID preserved: ${updatedItem.id}")
                
                // Perform the update and wait for it to complete
                android.util.Log.d("ItemViewModel", "Calling itemRepository.updateItem...")
                itemRepository.updateItem(updatedItem)
                
                // Verify the item still exists after archive
                val checkItem = itemRepository.getItemById(item.id)
                android.util.Log.d("ItemViewModel", "Item exists in database after archive: ${checkItem != null}")
                if (checkItem != null) {
                    android.util.Log.d("ItemViewModel", "Item after archive: id=${checkItem.id}, isActive=${checkItem.isActive}")
                } else {
                    android.util.Log.e("ItemViewModel", "CRITICAL ERROR: Item was deleted instead of archived!")
                }
                
                android.util.Log.d("ItemViewModel", "===== ARCHIVE ITEM COMPLETE =====")
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error archiving item: ${e.message}", e)
                // Re-throw the exception to propagate it to the caller
                throw e
            }
        }
    }
    
    // Restore an archived item (update to mark as active)
    fun unarchiveItem(item: Item) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "Unarchiving item: ${item.id} - ${item.name}")
                val updatedItem = item.copy(
                    isActive = true,
                    lastModified = System.currentTimeMillis()
                )
                android.util.Log.d("ItemViewModel", "Created updatedItem with isActive=true")
                itemRepository.updateItem(updatedItem)
                android.util.Log.d("ItemViewModel", "Unarchive complete for item: ${item.id}")
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error unarchiving item: ${e.message}", e)
            }
        }
    }
    
    /**
     * Get items by category
     */
    fun getItemsByCategory(category: String, activeOnly: Boolean = true): Flow<List<Item>> = 
        itemRepository.getItemsByCategory(category)
            .map { items -> if (activeOnly) items.filter { it.isActive } else items }
    
    /**
     * Get items by type
     */
    fun getItemsByType(type: String, activeOnly: Boolean = true): Flow<List<Item>> = 
        itemRepository.getItemsByType(type)
            .map { items -> if (activeOnly) items.filter { it.isActive } else items }
    
    /**
     * Update item status
     */
    fun updateItemStatus(item: Item, newStatus: String) {
        val updatedItem = item.copy(status = newStatus)
        viewModelScope.launch {
            itemRepository.updateItem(updatedItem)
        }
    }
    
    /**
     * Update item condition
     */
    fun updateItemCondition(item: Item, newCondition: String) {
        val updatedItem = item.copy(condition = newCondition)
        viewModelScope.launch {
            itemRepository.updateItem(updatedItem)
        }
    }
    
    // Alternative direct archive function to use for reliability
    fun directArchiveItem(item: Item) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "Direct archive for item: ${item.id}")
                
                // Create Firestore-friendly map with explicit fields to ensure consistency
                val itemMap = mapOf(
                    "idString" to item.idString,
                    "name" to item.name,
                    "category" to item.category,
                    "type" to item.type,
                    "barcode" to item.barcode,
                    "condition" to item.condition,
                    "status" to item.status,
                    "description" to item.description,
                    "photoPath" to item.photoPath,
                    "isActive" to false, // Explicitly set to false
                    "lastModified" to System.currentTimeMillis()
                )
                
                // Directly call repository's document update
                (itemRepository as? com.example.inventory.data.firebase.FirebaseItemRepository)?.let { repo ->
                    android.util.Log.d("ItemViewModel", "Using FirebaseItemRepository for direct update")
                    repo.updateItemWithMap(item.idString, itemMap)
                } ?: run {
                    // Fall back to regular update if not FirebaseItemRepository
                    android.util.Log.d("ItemViewModel", "Falling back to regular updateItem method")
                    val updatedItem = item.copy(
                        isActive = false,
                        lastModified = System.currentTimeMillis()
                    )
                    itemRepository.updateItem(updatedItem)
                }
                
                android.util.Log.d("ItemViewModel", "Direct archive complete for item: ${item.id}")
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error in direct archive: ${e.message}", e)
                throw e
            }
        }
    }
    
    // Add a test function to verify archive status
    fun verifyArchiveStatus(itemId: UUID, callback: (exists: Boolean, isArchived: Boolean?, error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "===== VERIFY ARCHIVE TEST =====")
                android.util.Log.d("ItemViewModel", "Checking item status in database: $itemId")
                
                val item = itemRepository.getItemById(itemId)
                
                if (item != null) {
                    android.util.Log.d("ItemViewModel", "Item found: id=${item.id}, name=${item.name}, isActive=${item.isActive}")
                    callback(true, !item.isActive, null)
                } else {
                    android.util.Log.e("ItemViewModel", "Item NOT found in database: $itemId")
                    callback(false, null, "Item not found in database")
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error checking archive status: ${e.message}", e)
                callback(false, null, "Error: ${e.message}")
            }
        }
    }
    
    // Add a refresh method that forces a manual refresh
    fun refreshItems() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "Manual refresh triggered")
                
                // Check if our repository is a FirebaseItemRepository
                if (itemRepository is com.example.inventory.data.firebase.FirebaseItemRepository) {
                    // Use reflection to call a method that might not be directly accessible
                    android.util.Log.d("ItemViewModel", "Using Firebase repository, triggering refresh")
                    
                    // First check for a method called refreshItems
                    try {
                        val method = itemRepository.javaClass.getDeclaredMethod("refreshItems")
                        method.isAccessible = true
                        method.invoke(itemRepository)
                        android.util.Log.d("ItemViewModel", "Called refreshItems method")
                        return@launch
                    } catch (e: NoSuchMethodException) {
                        android.util.Log.d("ItemViewModel", "No refreshItems method found, trying to trigger listener setup")
                    }
                    
                    // If no refreshItems method, try to trigger setupItemsListener
                    try {
                        val method = itemRepository.javaClass.getDeclaredMethod("setupItemsListener")
                        method.isAccessible = true
                        method.invoke(itemRepository)
                        android.util.Log.d("ItemViewModel", "Called setupItemsListener method")
                    } catch (e: Exception) {
                        android.util.Log.e("ItemViewModel", "Error calling setupItemsListener: ${e.message}", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error refreshing items: ${e.message}", e)
            }
        }
    }
    
    // Query archived items directly from Firestore
    fun directQueryArchivedItems(callback: (List<Item>) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "Directly querying archived items")
                
                // Check if our repository is a FirebaseItemRepository
                if (itemRepository is com.example.inventory.data.firebase.FirebaseItemRepository) {
                    android.util.Log.d("ItemViewModel", "Using Firebase repository for direct query")
                    
                    // Call the direct query method
                    val archivedItems = (itemRepository as com.example.inventory.data.firebase.FirebaseItemRepository)
                        .getArchivedItemsDirectly()
                    
                    android.util.Log.d("ItemViewModel", "Direct query found ${archivedItems.size} archived items")
                    callback(archivedItems)
                } else {
                    android.util.Log.d("ItemViewModel", "Not using Firebase repository, falling back to filter")
                    
                    // Fall back to filtering the allItems flow
                    val items = itemRepository.getAllItems().map { items -> 
                        items.filter { !it.isActive } 
                    }.collect { archivedItems ->
                        android.util.Log.d("ItemViewModel", "Filter found ${archivedItems.size} archived items")
                        callback(archivedItems)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error in direct query: ${e.message}", e)
                callback(emptyList())
            }
        }
    }
    
    // Check if repository has active listeners
    fun checkRepositoryListeners() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "Checking repository listeners")
                
                // Check if our repository is a FirebaseItemRepository
                if (itemRepository is com.example.inventory.data.firebase.FirebaseItemRepository) {
                    // First check for a method called checkIsActiveFieldType
                    try {
                        val method = itemRepository.javaClass.getDeclaredMethod("checkIsActiveFieldType")
                        method.isAccessible = true
                        method.invoke(itemRepository)
                        android.util.Log.d("ItemViewModel", "Called checkIsActiveFieldType method")
                    } catch (e: Exception) {
                        android.util.Log.e("ItemViewModel", "Error calling checkIsActiveFieldType: ${e.message}", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error checking repository listeners: ${e.message}", e)
            }
        }
    }
    
    // Add a diagnostic method to check the repository setup
    fun runItemsCollectionDiagnostics() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "Running items collection diagnostics")
                
                // Check if we're using FirebaseItemRepository
                if (itemRepository is com.example.inventory.data.firebase.FirebaseItemRepository) {
                    android.util.Log.d("ItemViewModel", "Using FirebaseItemRepository, calling diagnostics")
                    
                    // Call the diagnostic method
                    (itemRepository as com.example.inventory.data.firebase.FirebaseItemRepository)
                        .checkItemsCollectionSetup()
                } else {
                    android.util.Log.d("ItemViewModel", "Not using FirebaseItemRepository, can't run diagnostics")
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error running diagnostics: ${e.message}", e)
            }
        }
    }
    
    // Add a method to log all items with their IDs for debugging
    fun logAllItemsWithIds() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "Starting full item ID dump")
                
                // Check if we're using FirebaseItemRepository
                if (itemRepository is com.example.inventory.data.firebase.FirebaseItemRepository) {
                    android.util.Log.d("ItemViewModel", "Using FirebaseItemRepository, calling ID dump function")
                    
                    // Call the diagnostic method
                    (itemRepository as com.example.inventory.data.firebase.FirebaseItemRepository)
                        .logAllItemsWithIds()
                } else {
                    android.util.Log.d("ItemViewModel", "Not using FirebaseItemRepository, can't run ID dump")
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error running ID dump: ${e.message}", e)
            }
        }
    }
    
    /**
     * Factory for creating ItemViewModel instances with dependencies
     */
    companion object {
        class Factory(private val itemRepository: ItemRepository) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
                    return ItemViewModel(itemRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    init {
        viewModelScope.launch {
            // Log whenever we get new data
            allItems.collect { items ->
                android.util.Log.d("ItemViewModel", "==== VIEWMODEL RECEIVED DATA ====")
                android.util.Log.d("ItemViewModel", "Total items: ${items.size}")
                android.util.Log.d("ItemViewModel", "Active items: ${items.count { it.isActive }}")
                android.util.Log.d("ItemViewModel", "Archived items: ${items.count { !it.isActive }}")
                
                // Log details of archived items
                items.filter { !it.isActive }.forEach { item ->
                    android.util.Log.d("ItemViewModel", "Archived item details: id=${item.id}, name=${item.name}")
                }
            }
        }
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            itemRepository.getAllItems()
                .catch { e ->
                    _uiState.value = ItemUiState.Error(e.message ?: "Unknown error")
                }
                .collect { items ->
                    _uiState.value = ItemUiState.Success(items)
                }
        }
    }
}

sealed class ItemUiState {
    object Loading : ItemUiState()
    data class Success(val items: List<Item>) : ItemUiState()
    data class Error(val message: String) : ItemUiState()
} 