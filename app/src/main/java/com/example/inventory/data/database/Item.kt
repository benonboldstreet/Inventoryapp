package com.example.inventory.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "items")
data class Item(
    @PrimaryKey
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