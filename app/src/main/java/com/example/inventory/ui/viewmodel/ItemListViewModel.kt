package com.example.inventory.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the ItemList screen
 */
@HiltViewModel
class ItemListViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {
    private val TAG = "ItemListViewModel"
    
    // Get all categories from repository
    val categories: Flow<List<String>> = itemRepository.getAllCategories()
    
    // Get all items as a Flow
    val allItems: Flow<List<Item>> = itemRepository.getAllItems()
    
    /**
     * Function to directly query archived items
     */
    fun directQueryArchivedItems(callback: (List<Item>) -> Unit) {
        viewModelScope.launch {
            try {
                val allItems = itemRepository.getAllItems().first()
                val archivedItems = allItems.filter { !it.isActive }
                callback(archivedItems)
            } catch (e: Exception) {
                Log.e(TAG, "Error querying archived items: ${e.message}", e)
                callback(emptyList())
            }
        }
    }
    
    /**
     * Function to refresh items
     */
    fun refreshItems() {
        viewModelScope.launch {
            try {
                itemRepository.refreshFromFirebase()
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing items: ${e.message}", e)
            }
        }
    }
    
    /**
     * Function to get an item by ID
     */
    fun getItemById(id: UUID): Flow<Item?> {
        return flow {
            try {
                val item = itemRepository.getItemById(id).first()
                emit(item)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting item by ID: ${e.message}", e)
                emit(null)
            }
        }
    }
    
    /**
     * Function to run diagnostics
     */
    fun runItemsCollectionDiagnostics() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Running repository diagnostics")
                // You would implement actual diagnostics here
            } catch (e: Exception) {
                Log.e(TAG, "Error running diagnostics: ${e.message}", e)
            }
        }
    }
    
    /**
     * Function to log all items with IDs
     */
    fun logAllItemsWithIds() {
        viewModelScope.launch {
            try {
                val items = itemRepository.getAllItems().first()
                Log.d(TAG, "All items in repository: ${items.size}")
                items.forEach { item ->
                    Log.d(TAG, "Item: id=${item.id}, name=${item.name}, isActive=${item.isActive}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error logging items: ${e.message}", e)
            }
        }
    }
    
    /**
     * Function to add an item
     */
    fun addItem(item: Item) {
        viewModelScope.launch {
            try {
                itemRepository.insertItem(item)
                Log.d(TAG, "Item added successfully: ${item.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding item: ${e.message}", e)
            }
        }
    }
    
    /**
     * Function to check repository listeners
     */
    fun checkRepositoryListeners() {
        Log.d(TAG, "Checking repository listeners")
        // You would implement actual listener checks here
    }
    
    /**
     * Get archived items as a List
     */
    suspend fun getArchivedItems(): List<Item> {
        return try {
            val allItems = itemRepository.getAllItems().first()
            allItems.filter { !it.isActive }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting archived items: ${e.message}", e)
            emptyList()
        }
    }
} 