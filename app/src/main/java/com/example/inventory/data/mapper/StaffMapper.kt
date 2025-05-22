package com.example.inventory.data.mapper

import com.example.inventory.data.model.Staff
import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

object StaffMapper {
    /**
     * Convert Firestore document to Staff model
     */
    fun DocumentSnapshot.toStaff(): Staff? {
        if (!exists()) return null
        
        return try {
            val idString = id
            val name = getString("name") ?: ""
            val department = getString("department") ?: ""
            val email = getString("email") ?: ""
            val phone = getString("phone") ?: ""
            val position = getString("position") ?: ""
            val isActive = getBoolean("isActive") ?: true
            val lastModified = getLong("lastModified") ?: System.currentTimeMillis()
            
            Staff(
                idString = idString,
                name = name,
                department = department,
                email = email,
                phone = phone,
                position = position,
                isActive = isActive,
                lastModified = lastModified
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Convert Staff model to Map for Firestore
     */
    fun Staff.toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "department" to department,
            "email" to email,
            "phone" to phone,
            "position" to position,
            "isActive" to isActive,
            "lastModified" to lastModified
        )
    }
} 