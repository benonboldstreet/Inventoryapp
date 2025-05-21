package com.example.inventory.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.example.inventory.data.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared ViewModel for managing app-wide state
 */
object SharedViewModel {
    // Barcode scanning state
    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode = _scannedBarcode.asStateFlow()
    
    // Recently viewed items
    private val _recentlyViewedItems = MutableStateFlow<List<Item>>(emptyList())
    val recentlyViewedItems = _recentlyViewedItems.asStateFlow()
    
    // Cloud connectivity state
    private val _isCloudConnected = MutableStateFlow(true)
    val isCloudConnected = _isCloudConnected.asStateFlow()
    
    // Network connectivity listeners
    private val connectivityListeners = mutableListOf<(Boolean) -> Unit>()
    
    // For showing archived items (after archiving an item)
    private val _showArchivedItems = MutableStateFlow(false)
    val showArchivedItems = _showArchivedItems.asStateFlow()
    
    /**
     * Set the scanned barcode value
     */
    fun setBarcode(barcode: String) {
        _scannedBarcode.value = barcode
    }
    
    /**
     * Clear the scanned barcode value
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
     * Set the cloud connectivity state
     */
    fun setCloudConnected(isConnected: Boolean) {
        updateConnectivity(isConnected)
    }
    
    /**
     * Update cloud connectivity state
     */
    fun updateConnectivity(isConnected: Boolean) {
        _isCloudConnected.value = isConnected
        // Notify all listeners
        connectivityListeners.forEach { it(isConnected) }
    }
    
    /**
     * Add a connectivity state change listener
     */
    fun addConnectivityListener(listener: (Boolean) -> Unit) {
        connectivityListeners.add(listener)
    }
    
    /**
     * Remove a connectivity state change listener
     */
    fun removeConnectivityListener(listener: (Boolean) -> Unit) {
        connectivityListeners.remove(listener)
    }
    
    fun setShowArchivedItems(show: Boolean) {
        _showArchivedItems.value = show
    }
} 