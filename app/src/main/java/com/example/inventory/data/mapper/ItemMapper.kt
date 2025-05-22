package com.example.inventory.data.mapper

import com.example.inventory.data.model.Item
import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID

object ItemMapper {
    /**
     * Convert Firestore document to Item model
     */
    fun DocumentSnapshot.toItem(): Item? {
        if (!exists()) return null
        
        return try {
            val idString = id
            val name = getString("name") ?: ""
            val category = getString("category") ?: ""
            val type = getString("type") ?: ""
            val barcode = getString("barcode") ?: ""
            val condition = getString("condition") ?: ""
            val status = getString("status") ?: ""
            val description = getString("description") ?: ""
            val photoPath = getString("photoPath")
            val isActive = getBoolean("isActive") ?: true
            val lastModified = getLong("lastModified") ?: System.currentTimeMillis()
            
            Item(
                idString = idString,
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
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Convert Item model to Map for Firestore
     */
    fun Item.toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "category" to category,
            "type" to type,
            "barcode" to barcode,
            "condition" to condition,
            "status" to status,
            "description" to description,
            "photoPath" to photoPath,
            "isActive" to isActive,
            "lastModified" to lastModified
        )
    }
} 