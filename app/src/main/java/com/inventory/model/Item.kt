package com.inventory.model

/**
 * Data model representing an inventory item.
 */
data class Item(
    val id: String,
    val name: String,
    val quantity: Int,
    val category: String,
    val lastUpdated: String
) 