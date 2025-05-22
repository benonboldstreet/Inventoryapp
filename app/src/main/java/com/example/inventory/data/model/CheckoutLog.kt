package com.example.inventory.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Data model for checkout logs
 * 
 * Model designed for Firebase Firestore operations
 */
data class CheckoutLog(
    @DocumentId val idString: String = "",
    val itemIdString: String = "",
    val staffIdString: String = "",
    val checkOutTime: Long = System.currentTimeMillis(),
    val checkInTime: Long? = null,
    val photoPath: String? = null,
    val status: String = "CHECKED_OUT", // Status of the checkout (CHECKED_OUT, CHECKED_IN, OVERDUE, etc.)
    val lastModified: Long = System.currentTimeMillis()
) {
    val id: UUID
        get() = if (idString.isEmpty()) UUID.randomUUID() else UUID.fromString(idString)
    
    val itemId: UUID
        get() = UUID.fromString(itemIdString)
    
    val staffId: UUID
        get() = UUID.fromString(staffIdString)
    
    // Helper function to safely get checkOutTime as Long
    fun getCheckOutTimeAsLong(): Long {
        return checkOutTime
    }
    
    // Helper function to safely get checkInTime as Long
    fun getCheckInTimeAsLong(): Long? {
        return checkInTime
    }
    
    // Helper function to safely get lastModified as Long
    fun getLastModifiedTime(): Long {
        return lastModified
    }
    
    constructor(
        id: UUID = UUID.randomUUID(),
        itemId: UUID,
        staffId: UUID,
        checkOutTime: Long,
        checkInTime: Long? = null,
        photoPath: String? = null,
        status: String = "CHECKED_OUT",
        lastModified: Long = System.currentTimeMillis()
    ) : this(
        idString = id.toString(),
        itemIdString = itemId.toString(),
        staffIdString = staffId.toString(),
        checkOutTime = checkOutTime,
        checkInTime = checkInTime,
        photoPath = photoPath,
        status = status,
        lastModified = lastModified
    )
} 