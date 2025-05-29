package com.example.inventory.data.model

/**
 * Represents an inventory item in the system
 */
data class Item(
    val id: String,
    val name: String,
    val category: String,
    val type: String,
    val barcode: String,
    val condition: String,
    val status: String,
    val description: String,
    val isActive: Boolean = true,
    val lastModified: Long = System.currentTimeMillis(),
    val photoPath: String? = null
) {
    // Helper property for Firebase - keeping for backward compatibility
    val idString: String get() = id
} 