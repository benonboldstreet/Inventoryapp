package com.example.inventory.barcode

import android.util.Log
import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Barcode Manager
 * 
 * Handles barcode scanning operations and lookups.
 * Provides utilities for finding items by barcode and managing scan history.
 */
@Singleton
class BarcodeManager @Inject constructor(
    private val itemRepository: ItemRepository
) {
    companion object {
        private const val TAG = "BarcodeManager"
        const val NO_MATCH = "NO_MATCH"
        const val INVALID_BARCODE = "INVALID_BARCODE"
        private const val MAX_RECENT_SCANS = 10
    }
    
    // Recent scan history (limit to last 10)
    private val recentScans = mutableListOf<String>()
    
    /**
     * Find an item by its barcode
     * Uses Firebase repository for lookups
     * 
     * @param barcode The barcode to look up
     * @return Flow of the found item or null if not found
     */
    fun findItemByBarcode(barcode: String): Flow<Item?> = flow {
        try {
            Log.d(TAG, "Looking up item with barcode: $barcode")
            
            // Add to recent scans list
            addToRecentScans(barcode)
            
            // Use the repository to find the item
            val item = itemRepository.getItemByBarcode(barcode).first()
            emit(item)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding item by barcode: $barcode", e)
            emit(null)
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
    
    /**
     * Look up an item by barcode
     * 
     * @param barcode The barcode to look up
     * @return The item if found, null otherwise
     */
    suspend fun lookupItemByBarcode(barcode: String): Item? {
        if (barcode.isBlank()) {
            Log.w(TAG, "Cannot lookup empty barcode")
            return null
        }
        
        return try {
            val item = itemRepository.getItemByBarcode(barcode).first()
            Log.d(TAG, "Barcode lookup result: ${item?.name ?: "No match"}")
            item
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up barcode: ${e.message}", e)
            null
        }
    }
    
    /**
     * Process a scanned barcode
     * 
     * @param barcode The scanned barcode
     * @return The found item or null if not found
     */
    suspend fun processScan(barcode: String): Result<Item?> {
        if (barcode.isBlank()) {
            return Result.failure(IllegalArgumentException("Barcode cannot be empty"))
        }
        
        return try {
            val item = itemRepository.getItemByBarcode(barcode).first()
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing barcode scan: ${e.message}", e)
            Result.failure(e)
        }
    }
} 