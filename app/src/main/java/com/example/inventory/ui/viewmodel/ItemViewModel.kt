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
            itemRepository.insertItem(item)
        }
    }
    
    // Update an existing item
    fun updateItem(item: Item) {
        viewModelScope.launch {
            itemRepository.updateItem(item)
        }
    }
    
    // Delete an item
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemRepository.deleteItem(item)
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
    
    // Add a method to force a refresh of items from Firestore
    fun refreshItems() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "===== FORCED REFRESH STARTED =====")
                
                // Create a temporary flow to receive fresh data
                itemRepository.getAllItems().collect { freshItems ->
                    android.util.Log.d("ItemViewModel", "Forced refresh retrieved ${freshItems.size} total items")
                    android.util.Log.d("ItemViewModel", "Active items: ${freshItems.count { it.isActive }}")
                    android.util.Log.d("ItemViewModel", "Archived items: ${freshItems.count { !it.isActive }}")
                    
                    // Log details of each archived item
                    freshItems.filter { !it.isActive }.forEach { item ->
                        android.util.Log.d("ItemViewModel", "Refreshed archived item: id=${item.id}, name=${item.name}")
                    }
                    
                    // Only need to collect once to trigger UI update
                    android.util.Log.d("ItemViewModel", "===== FORCED REFRESH COMPLETED =====")
                    return@collect
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error during forced refresh: ${e.message}", e)
            }
        }
    }
    
    // Add a method to directly query Firestore for archived items
    fun directQueryArchivedItems(callback: (List<Item>) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "===== DIRECT QUERY FOR ARCHIVED ITEMS =====")
                
                // Check if we have FirebaseItemRepository
                if (itemRepository is com.example.inventory.data.firebase.FirebaseItemRepository) {
                    android.util.Log.d("ItemViewModel", "Using FirebaseItemRepository for direct query")
                    
                    // Cast and use the repository
                    val firebaseRepo = itemRepository as com.example.inventory.data.firebase.FirebaseItemRepository
                    val archivedItems = firebaseRepo.getArchivedItemsDirectly()
                    
                    android.util.Log.d("ItemViewModel", "Direct query found ${archivedItems.size} archived items")
                    archivedItems.forEach { item ->
                        android.util.Log.d("ItemViewModel", "Direct query found: id=${item.id}, name=${item.name}, isActive=${item.isActive}")
                    }
                    
                    // Return the results via callback
                    callback(archivedItems)
                } else {
                    android.util.Log.e("ItemViewModel", "Not using FirebaseItemRepository, can't direct query")
                    callback(emptyList())
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error in direct query for archived items: ${e.message}", e)
                callback(emptyList())
            }
        }
    }
    
    /**
     * Fix archived items in Firestore to ensure they have proper isActive=false Boolean values
     */
    fun fixArchivedItems(callback: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ItemViewModel", "===== FIXING ARCHIVED ITEMS =====")
                
                // Check if we have FirebaseItemRepository
                if (itemRepository is com.example.inventory.data.firebase.FirebaseItemRepository) {
                    android.util.Log.d("ItemViewModel", "Using FirebaseItemRepository for fixing")
                    
                    // Cast to FirebaseItemRepository
                    val firebaseRepo = itemRepository as com.example.inventory.data.firebase.FirebaseItemRepository
                    
                    // Get a reference to Firestore
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val itemsCollection = firestore.collection("items")
                    
                    // Get all documents
                    val snapshot = itemsCollection.get().await()
                    android.util.Log.d("ItemViewModel", "Found ${snapshot.documents.size} total items in Firestore")
                    
                    // Filter for items that might be archived but have incorrect isActive field
                    val potentialArchivedItems = snapshot.documents.filter { doc ->
                        val isActiveValue = doc.get("isActive")
                        
                        // Check for various non-Boolean false values
                        isActiveValue != null && (
                            (isActiveValue !is Boolean && isActiveValue.toString().equals("false", ignoreCase = true)) ||
                            (isActiveValue !is Boolean && isActiveValue.toString().equals("0", ignoreCase = true)) ||
                            (isActiveValue is Number && isActiveValue.toInt() == 0)
                        )
                    }
                    
                    android.util.Log.d("ItemViewModel", "Found ${potentialArchivedItems.size} items with potentially incorrect isActive values")
                    
                    // Fix each item by explicitly setting isActive to Boolean false
                    var fixedCount = 0
                    potentialArchivedItems.forEach { doc ->
                        try {
                            val id = doc.id
                            val name = doc.getString("name") ?: "Unknown"
                            val rawIsActive = doc.get("isActive")
                            
                            android.util.Log.d("ItemViewModel", "Fixing item $id - $name with current isActive value: $rawIsActive (${rawIsActive?.javaClass?.simpleName})")
                            
                            // Update the document with explicit Boolean false
                            itemsCollection.document(id)
                                .update("isActive", false)
                                .await()
                            
                            // Verify the fix
                            val verifyDoc = itemsCollection.document(id).get().await()
                            val verifyIsActive = verifyDoc.getBoolean("isActive")
                            
                            if (verifyIsActive == false) {
                                android.util.Log.d("ItemViewModel", "Successfully fixed item $id")
                                fixedCount++
                            } else {
                                android.util.Log.e("ItemViewModel", "Failed to fix item $id - isActive is still $verifyIsActive")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ItemViewModel", "Error fixing item ${doc.id}: ${e.message}", e)
                        }
                    }
                    
                    // Also check for any items with proper Boolean isActive=false
                    val properArchivedItems = snapshot.documents.filter { doc ->
                        val isActiveValue = doc.get("isActive")
                        isActiveValue is Boolean && isActiveValue == false
                    }
                    
                    android.util.Log.d("ItemViewModel", "Found ${properArchivedItems.size} items with proper Boolean isActive=false")
                    
                    // Return the count of fixed items
                    android.util.Log.d("ItemViewModel", "Fixed $fixedCount items with incorrect isActive values")
                    callback(fixedCount)
                } else {
                    android.util.Log.e("ItemViewModel", "Not using FirebaseItemRepository, can't fix archived items")
                    callback(0)
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error fixing archived items: ${e.message}", e)
                callback(0)
            }
        }
    }
    
    /**
     * Debug method to check if the repository listeners are properly set up
     */
    fun checkRepositoryListeners() {
        viewModelScope.launch {
            try {
                // Force a repository refresh
                val repo = itemRepository
                
                // Try to fetch items directly
                if (repo is com.example.inventory.data.firebase.FirebaseItemRepository) {
                    android.util.Log.d("ItemViewModel", "Checking FirebaseItemRepository listeners")
                    // Try to force a refresh
                    val items = repo.getArchivedItemsDirectly()
                    android.util.Log.d("ItemViewModel", "Direct query found ${items.size} items")
                    
                    // Log diagnostic info about the repository
                    repo.checkIsActiveFieldType()
                    android.util.Log.d("ItemViewModel", "Repository diagnostic check complete")
                }
                else {
                    android.util.Log.d("ItemViewModel", "Repository is not FirebaseItemRepository, it's: ${repo.javaClass.simpleName}")
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemViewModel", "Error checking repository listeners: ${e.message}", e)
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
    }
} 