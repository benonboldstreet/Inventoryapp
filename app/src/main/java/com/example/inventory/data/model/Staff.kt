package com.example.inventory.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Data model for staff members
 * 
 * Model designed for Firebase Firestore operations
 */
data class Staff(
    @DocumentId val idString: String = "",
    val name: String = "",
    val department: String = "",
    val email: String = "", // Optional email address
    val phone: String = "", // Optional phone number
    val position: String = "", // Job title or position
    val isActive: Boolean = true, // Flag for active vs archived staff
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
        department: String,
        email: String = "",
        phone: String = "",
        position: String = "",
        isActive: Boolean = true,
        lastModified: Long = System.currentTimeMillis()
    ) : this(
        idString = id.toString(),
        name = name,
        department = department,
        email = email,
        phone = phone,
        position = position,
        isActive = isActive,
        lastModified = lastModified
    )
} 