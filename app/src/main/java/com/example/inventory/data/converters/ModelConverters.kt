package com.example.inventory.data.converters

import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.model.CheckoutLog
import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Utility functions for model conversions
 * 
 * Since we can't fully implement database converters without access to the database entity details,
 * this class now focuses only on utility functions for model classes (no database conversions).
 */
object ModelConverters {
    /**
     * Utility function to convert a String ID to a UUID safely
     */
    fun stringToUuid(idString: String): UUID {
        return try {
            UUID.fromString(idString)
        } catch (e: Exception) {
            UUID.randomUUID()
        }
    }
    
    /**
     * Utility function to convert a timestamp value to a Long timestamp
     */
    fun anyToTimestamp(value: Any?): Long {
        return when (value) {
            is Long -> value
            is Timestamp -> (value).seconds * 1000
            else -> System.currentTimeMillis()
        }
    }
    
    /**
     * Utility function to create a Firestore Timestamp from the current time
     */
    fun getCurrentTimestamp(): Timestamp {
        return Timestamp.now()
    }
} 