package com.example.inventory.data.mapper

import com.example.inventory.data.model.CheckoutLog
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.UUID

object CheckoutLogMapper {
    /**
     * Convert Firestore document to CheckoutLog model
     */
    fun DocumentSnapshot.toCheckoutLog(): CheckoutLog? {
        if (!exists()) return null
        
        return try {
            val idString = id
            val itemIdString = getString("itemIdString") ?: ""
            val staffIdString = getString("staffIdString") ?: ""
            val checkOutTime = getLong("checkOutTime") ?: 0L
            val checkInTime = getLong("checkInTime")
            val photoPath = getString("photoPath")
            val status = getString("status") ?: "CHECKED_OUT"
            val lastModified = getLong("lastModified") ?: System.currentTimeMillis()
            
            CheckoutLog(
                idString = idString,
                itemIdString = itemIdString,
                staffIdString = staffIdString,
                checkOutTime = checkOutTime,
                checkInTime = checkInTime,
                photoPath = photoPath,
                status = status,
                lastModified = lastModified
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Convert CheckoutLog model to Map for Firestore
     */
    fun CheckoutLog.toMap(): Map<String, Any?> {
        return mapOf(
            "itemIdString" to itemIdString,
            "staffIdString" to staffIdString,
            "checkOutTime" to checkOutTime,
            "checkInTime" to checkInTime,
            "photoPath" to photoPath,
            "status" to status,
            "lastModified" to lastModified
        )
    }
} 