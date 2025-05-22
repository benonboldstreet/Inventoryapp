package com.example.inventory.data.model

/**
 * Data Transfer Object for Item
 * 
 * Used for network operations and API responses
 */
data class ItemDto(
    val id: String? = null,
    val name: String? = null,
    val category: String? = null,
    val type: String? = null,
    val barcode: String? = null,
    val condition: String? = null,
    val status: String? = null,
    val photoPath: String? = null,
    val isActive: Boolean? = true,
    val lastModified: Long? = null
) 