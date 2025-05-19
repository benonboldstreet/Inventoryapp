package com.example.inventory.barcode

import android.content.Context
import android.util.Log
import com.example.inventory.api.NetworkRetry
import com.example.inventory.api.NetworkModule
import com.example.inventory.data.database.Item
import com.example.inventory.data.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Barcode Manager
 * 
 * Handles barcode scanning operations and lookups.
 * Provides utilities for finding items by barcode and managing scan history.
 */
object BarcodeManager {
    private const val TAG = "BarcodeManager"
    
    // Recent scan history (limit to last 10)
    private val recentScans = mutableListOf<String>()
    private const val MAX_RECENT_SCANS = 10
    
    /**
     * Find an item by its barcode
     * Includes retry mechanism for network resilience
     * 
     * @param barcode The barcode to look up
     * @param itemRepository The repository to use for lookups
     * @return Flow of the found item or null if not found
     */
    fun findItemByBarcode(
        barcode: String,
        itemRepository: ItemRepository
    ): Flow<Item?> = flow {
        try {
            Log.d(TAG, "Looking up item with barcode: $barcode")
            
            // Add to recent scans list
            addToRecentScans(barcode)
            
            // Use the repository to find the item with retry mechanism
            val item = NetworkRetry.executeWithRetry {
                itemRepository.getItemByBarcode(barcode)
            }
            
            emit(item)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding item by barcode: $barcode", e)
            emit(null)
        }
    }
    
    /**
     * Direct lookup using the API service
     * Useful when repository is not available
     */
    suspend fun findItemByBarcodeDirectApi(barcode: String): Item? {
        return try {
            // Use network retry for resilience
            NetworkRetry.executeWithRetry {
                val itemDto = NetworkModule.itemApiService.getItemByBarcode(barcode)
                itemDto.toEntity()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding item by barcode direct API: $barcode", e)
            null
        }
    }
    
    /**
     * Add a barcode to the recent scans list
     */
    private fun addToRecentScans(barcode: String) {
        // Remove if already in list (to move to front)
        recentScans.remove(barcode)
        
        // Add to front of list
        recentScans.add(0, barcode)
        
        // Trim list if needed
        if (recentScans.size > MAX_RECENT_SCANS) {
            recentScans.removeAt(recentScans.lastIndex)
        }
        
        Log.d(TAG, "Recent scans updated, count: ${recentScans.size}")
    }
    
    /**
     * Get list of recent scans
     */
    fun getRecentScans(): List<String> {
        return recentScans.toList()
    }
    
    /**
     * Clear scan history
     */
    fun clearScanHistory() {
        recentScans.clear()
        Log.d(TAG, "Scan history cleared")
    }
    
    /**
     * Generate a sample barcode for testing
     */
    fun generateSampleBarcode(): String {
        // Create EAN-13 format sample
        val prefix = "200" // Standard prefix
        val middle = (100000..999999).random() // Random middle section
        
        // Simple checksum calculation (not actual EAN-13 algorithm, just for demo)
        var sum = 0
        val digits = "$prefix$middle"
        for (i in digits.indices) {
            val digit = digits[i].toString().toInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        val checkDigit = (10 - (sum % 10)) % 10
        
        return "$prefix$middle$checkDigit"
    }
} 