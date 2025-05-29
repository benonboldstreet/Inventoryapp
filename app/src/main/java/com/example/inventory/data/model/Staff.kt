package com.example.inventory.data.model

/**
 * Represents a staff member in the inventory system
 */
data class Staff(
    val id: String,
    val name: String,
    val department: String,
    val position: String,
    val email: String,
    val phone: String,
    val isActive: Boolean = true,
    val lastModified: Long = System.currentTimeMillis(),
    val firebaseUid: String? = null,
    val role: String = "User",
    val photoPath: String? = null
) {
    // Helper properties for Firebase
    val idString: String get() = id.toString()
} 