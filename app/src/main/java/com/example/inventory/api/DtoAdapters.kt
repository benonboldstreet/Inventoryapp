package com.example.inventory.api

import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.model.CheckoutLog
import java.util.UUID

/**
 * Adapter methods for converting between model objects and DTOs
 * This is a compatibility layer for the migration from Room to Firestore
 */

/**
 * Convert a model Item to a DTO
 */
fun Item.toDto(): Map<String, Any?> {
    return mapOf(
        "id" to id.toString(),
        "name" to name,
        "category" to category,
        "type" to type,
        "barcode" to barcode,
        "condition" to condition,
        "status" to status,
        "photoPath" to photoPath,
        "isActive" to isActive,
        "lastModified" to lastModified
    )
}

/**
 * Convert a model Staff to a DTO
 */
fun Staff.toDto(): Map<String, Any?> {
    return mapOf(
        "id" to id.toString(),
        "name" to name,
        "department" to department,
        "email" to email,
        "phone" to phone,
        "position" to position,
        "isActive" to isActive,
        "lastModified" to lastModified
    )
}

/**
 * Convert a model CheckoutLog to a DTO
 */
fun CheckoutLog.toDto(): Map<String, Any?> {
    return mapOf(
        "id" to id.toString(),
        "itemId" to itemId.toString(),
        "staffId" to staffId.toString(),
        "checkOutTime" to checkOutTime,
        "checkInTime" to checkInTime,
        "photoPath" to photoPath,
        "status" to status,
        "lastModified" to lastModified
    )
}

/**
 * Convert a DTO to a model Item (legacy method for compatibility)
 */
fun Map<String, Any?>.toEntity(): Item {
    return Item(
        idString = (this["id"] as? String) ?: UUID.randomUUID().toString(),
        name = (this["name"] as? String) ?: "",
        category = (this["category"] as? String) ?: "",
        type = (this["type"] as? String) ?: "",
        barcode = (this["barcode"] as? String) ?: "",
        condition = (this["condition"] as? String) ?: "",
        status = (this["status"] as? String) ?: "",
        photoPath = this["photoPath"] as? String,
        isActive = (this["isActive"] as? Boolean) ?: true,
        lastModified = (this["lastModified"] as? Long) ?: System.currentTimeMillis()
    )
}

/**
 * Convert a DTO to a model Staff (legacy method for compatibility)
 */
fun Map<String, Any?>.toStaffEntity(): Staff {
    return Staff(
        idString = (this["id"] as? String) ?: UUID.randomUUID().toString(),
        name = (this["name"] as? String) ?: "",
        department = (this["department"] as? String) ?: "",
        email = (this["email"] as? String) ?: "",
        phone = (this["phone"] as? String) ?: "",
        position = (this["position"] as? String) ?: "",
        isActive = (this["isActive"] as? Boolean) ?: true,
        lastModified = (this["lastModified"] as? Long) ?: System.currentTimeMillis()
    )
}

/**
 * Convert a DTO to a model CheckoutLog (legacy method for compatibility)
 */
fun Map<String, Any?>.toCheckoutEntity(): CheckoutLog {
    return CheckoutLog(
        idString = (this["id"] as? String) ?: UUID.randomUUID().toString(),
        itemIdString = (this["itemId"] as? String) ?: "",
        staffIdString = (this["staffId"] as? String) ?: "",
        checkOutTime = (this["checkOutTime"] as? Long) ?: System.currentTimeMillis(),
        checkInTime = this["checkInTime"] as? Long,
        photoPath = this["photoPath"] as? String,
        status = (this["status"] as? String) ?: "CHECKED_OUT",
        lastModified = (this["lastModified"] as? Long) ?: System.currentTimeMillis()
    )
} 