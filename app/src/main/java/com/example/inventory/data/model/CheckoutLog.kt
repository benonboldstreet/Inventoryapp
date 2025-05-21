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
    val checkOutTime: Any? = null, // Changed from Long to Any to handle Timestamp
    val checkInTime: Any? = null, // Changed from Long? to Any? to handle Timestamp
    val photoPath: String? = null,
    val status: String = "CHECKED_OUT", // Status of the checkout (CHECKED_OUT, CHECKED_IN, OVERDUE, etc.)
    val lastModified: Any? = null // Changed from Long to Any to handle Timestamp
) {
    val id: UUID
        get() = if (idString.isEmpty()) UUID.randomUUID() else UUID.fromString(idString)
    
    val itemId: UUID
        get() = UUID.fromString(itemIdString)
    
    val staffId: UUID
        get() = UUID.fromString(staffIdString)
    
    // Helper function to safely get checkOutTime as Long
    fun getCheckOutTimeAsLong(): Long {
        return when (checkOutTime) {
            is Long -> checkOutTime
            is Timestamp -> (checkOutTime as Timestamp).seconds * 1000
            else -> 0
        }
    }
    
    // Helper function to safely get checkInTime as Long
    fun getCheckInTimeAsLong(): Long? {
        return when (checkInTime) {
            is Long -> checkInTime
            is Timestamp -> (checkInTime as Timestamp).seconds * 1000
            else -> null
        }
    }
    
    // Helper function to safely get lastModified as Long
    fun getLastModifiedTime(): Long {
        return when (lastModified) {
            is Long -> lastModified
            is Timestamp -> (lastModified as Timestamp).seconds * 1000
            else -> System.currentTimeMillis()
        }
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