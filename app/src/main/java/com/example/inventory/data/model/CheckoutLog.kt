package com.example.inventory.data.model

/**
 * Represents a checkout log entry for an item checked out by a staff member
 */
data class CheckoutLog(
    val id: String,
    val itemId: String,
    val staffId: String,
    val checkoutTimestamp: Long,
    val checkinTimestamp: Long? = null,
    val checkoutPhotoPath: String? = null,
    val checkinPhotoPath: String? = null,
    val notes: String = ""
) {
    // Helper properties for Firebase - keeping for backward compatibility
    val idString: String get() = id
    val itemIdString: String get() = itemId
    val staffIdString: String get() = staffId
    
    // Calculated properties
    val isCheckedIn: Boolean get() = checkinTimestamp != null
    val durationMillis: Long? get() = if (checkinTimestamp != null) checkinTimestamp - checkoutTimestamp else null
    
    // Helper methods for timestamp conversion
    fun getCheckoutTimeAsString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(checkoutTimestamp))
    }
    
    fun getCheckinTimeAsString(): String? {
        return checkinTimestamp?.let {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(it))
        }
    }
    
    // Helper methods to get timestamps as longs
    fun getCheckoutTimeAsLong(): Long = checkoutTimestamp
    fun getCheckinTimeAsLong(): Long? = checkinTimestamp
} 