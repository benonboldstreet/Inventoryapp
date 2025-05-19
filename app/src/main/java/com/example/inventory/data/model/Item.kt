package com.example.inventory.data.model

import java.util.UUID

/**
 * Data model for inventory items
 * 
 * Simplified model for cloud operations without Room database annotations
 */
data class Item(
    val id: UUID = UUID.randomUUID(),
    val name: String, // Model name (e.g., "MSI Temup Leopard Pro")
    val category: String, // General category (e.g., "Laptop", "Mobile Phone", "Tablet")
    val type: String, // Specific details or brand (e.g., "Dell", "Apple", "Lenovo")
    val barcode: String,
    val condition: String,
    val status: String,
    val photoPath: String?,
    val isActive: Boolean = true, // Flag for active vs archived items
    val lastModified: Long? = System.currentTimeMillis()
) 