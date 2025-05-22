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
    // Create a deterministic UUID from the idString if it's not already a UUID format
    val id: UUID
        get() = try {
            if (idString.isEmpty()) {
                android.util.Log.d("Item", "Empty idString, returning random UUID")
                UUID.randomUUID()
            } else {
                try {
                    // First try to parse directly as UUID
                    android.util.Log.d("Item", "Attempting to parse as UUID: $idString")
                    UUID.fromString(idString)
                } catch (e: IllegalArgumentException) {
                    // Not a valid UUID format, create a deterministic UUID from the string
                    android.util.Log.d("Item", "Not valid UUID, using nameUUIDFromBytes: $idString")
                    UUID.nameUUIDFromBytes(idString.toByteArray())
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Item", "Error creating UUID from idString: $idString", e)
            // Fallback to deterministic UUID in any error case
            UUID.nameUUIDFromBytes(idString.toByteArray())
        }
    
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