package com.example.inventory.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.example.inventory.data.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared ViewModel for app-wide state management
 * 
 * This object is used to share state between different screens and components
 * without passing parameters through navigation.
 */
object SharedViewModel {
    // Barcode scanner state
    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()
    
    // Recently viewed items
    private val _recentlyViewedItems = MutableStateFlow<List<Item>>(emptyList())
    val recentlyViewedItems = _recentlyViewedItems.asStateFlow()
    
    // Cloud connectivity state
    private val _isCloudConnected = MutableStateFlow(true)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()
    
    // Network connectivity listeners
    private val connectivityListeners = mutableListOf<(Boolean) -> Unit>()
    
    // Show archived items flag
    private val _showArchivedItems = MutableStateFlow(false)
    val showArchivedItems: StateFlow<Boolean> = _showArchivedItems.asStateFlow()
    
    /**
     * Set the scanned barcode value
     */
    fun setScannedBarcode(barcode: String) {
        _scannedBarcode.value = barcode
    }
    
    /**
     * Clear the scanned barcode
     */
    fun clearBarcode() {
        _scannedBarcode.value = ""
    }
    
    /**
     * Add an item to recently viewed items
     */
    fun addToRecentlyViewed(item: Item) {
        val currentList = _recentlyViewedItems.value.toMutableList()
        // Remove if already in list to avoid duplicates
        currentList.removeIf { it.id == item.id }
        // Add to beginning of list
        currentList.add(0, item)
        // Keep only the 5 most recent
        _recentlyViewedItems.value = currentList.take(5)
    }
    
    /**
     * Set cloud connectivity state
     */
    fun setCloudConnected(isConnected: Boolean) {
        _isCloudConnected.value = isConnected
        
        // Notify all listeners
        connectivityListeners.forEach { listener ->
            listener(isConnected)
        }
    }
    
    /**
     * Update cloud connectivity state (alias for setCloudConnected)
     */
    fun updateConnectivity(isConnected: Boolean) {
        setCloudConnected(isConnected)
    }
    
    /**
     * Add a connectivity listener
     */
    fun addConnectivityListener(listener: (Boolean) -> Unit) {
        connectivityListeners.add(listener)
        // Immediately notify with current state
        listener(_isCloudConnected.value)
    }
    
    /**
     * Remove a connectivity listener
     */
    fun removeConnectivityListener(listener: (Boolean) -> Unit) {
        connectivityListeners.remove(listener)
    }
    
    /**
     * Set the flag to show archived items
     */
    fun setShowArchivedItems(show: Boolean) {
        _showArchivedItems.value = show
    }
} 