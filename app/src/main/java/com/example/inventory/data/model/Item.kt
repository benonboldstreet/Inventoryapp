package com.example.inventory.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Data model for inventory items
 * 
 * Model designed for Firebase Firestore operations
 */
data class Item(
    @DocumentId val idString: String = "",
    val name: String = "", // Model name (e.g., "MSI Temup Leopard Pro")
    val category: String = "", // General category (e.g., "Laptop", "Mobile Phone", "Tablet")
    val type: String = "", // Specific details or brand (e.g., "Dell", "Apple", "Lenovo")
    val barcode: String = "",
    val condition: String = "",
    val status: String = "",
    val description: String = "", // Item description
    val photoPath: String? = null,
    val isActive: Boolean = true, // Flag for active vs archived items
    val lastModified: Any? = null // Changed from Long to Any to handle both Timestamp and Long
) {
    val id: UUID
        get() = if (idString.isEmpty()) UUID.randomUUID() else UUID.fromString(idString)
    
    // Add a method to safely get lastModified as a timestamp
    fun getLastModifiedTime(): Long {
        return when (lastModified) {
            is Long -> lastModified
            is Timestamp -> (lastModified as Timestamp).seconds * 1000
            else -> System.currentTimeMillis()
        }
    }
    
    constructor(
        id: UUID,
        name: String,
        category: String,
        type: String,
        barcode: String,
        condition: String,
        status: String,
        description: String = "",
        photoPath: String?,
        isActive: Boolean = true,
        lastModified: Long = System.currentTimeMillis()
    ) : this(
        idString = id.toString(),
        name = name,
        category = category,
        type = type,
        barcode = barcode,
        condition = condition,
        status = status,
        description = description,
        photoPath = photoPath,
        isActive = isActive,
        lastModified = lastModified
    )
} 