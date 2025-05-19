package com.example.inventory.ui.screens

import androidx.compose.runtime.mutableStateOf
import com.example.inventory.data.model.Item

/**
 * Shared ViewModel for cross-screen communication
 * This is a singleton object that can be accessed from anywhere in the app
 */
object SharedViewModel {
    // Store the scanned barcode from the barcode scanner screen
    var scannedBarcode = mutableStateOf("")
    
    // Store recently viewed items for the home screen
    var recentlyViewedItems = mutableStateOf<List<Item>>(emptyList())
    
    // Track cloud connectivity status
    var isCloudConnected = mutableStateOf(true) // Default to true, will be updated based on network state
    
    // List of connectivity listeners
    private val connectivityListeners = mutableListOf<(Boolean) -> Unit>()
    
    /**
     * Set the scanned barcode value
     */
    fun setBarcode(barcode: String) {
        scannedBarcode.value = barcode
    }
    
    /**
     * Clear the scanned barcode
     */
    fun clearBarcode() {
        scannedBarcode.value = ""
    }
    
    /**
     * Add an item to recently viewed
     * Keeps only the 5 most recent items
     */
    fun addToRecentlyViewed(item: Item) {
        val currentList = recentlyViewedItems.value.toMutableList()
        // Remove if already in list to avoid duplicates
        currentList.removeIf { it.id == item.id }
        // Add to beginning of list
        currentList.add(0, item)
        // Keep only the 5 most recent
        recentlyViewedItems.value = currentList.take(5)
    }
    
    /**
     * Register a connectivity listener
     * 
     * @param listener A callback that will be invoked when connectivity status changes
     */
    fun addConnectivityListener(listener: (Boolean) -> Unit) {
        connectivityListeners.add(listener)
        // Immediately notify with the current status
        listener(isCloudConnected.value)
    }
    
    /**
     * Remove a previously registered connectivity listener
     * 
     * @param listener The callback to remove
     */
    fun removeConnectivityListener(listener: (Boolean) -> Unit) {
        connectivityListeners.remove(listener)
    }
    
    /**
     * Update the connectivity status and notify all listeners
     * This should only be called by the MainActivity network monitoring
     * 
     * @param isConnected Whether the device is connected to the cloud
     */
    fun updateConnectivity(isConnected: Boolean) {
        val previousValue = isCloudConnected.value
        isCloudConnected.value = isConnected
        
        // Only notify if the value changed
        if (previousValue != isConnected) {
            connectivityListeners.forEach { it(isConnected) }
        }
    }
} 