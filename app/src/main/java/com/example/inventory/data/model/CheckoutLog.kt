package com.example.inventory.data.model

import java.util.UUID

/**
 * Data model for checkout logs
 * 
 * Simplified model for cloud operations without Room database annotations
 */
data class CheckoutLog(
    val id: UUID = UUID.randomUUID(),
    val itemId: UUID,
    val staffId: UUID,
    val checkOutTime: Long,
    val checkInTime: Long? = null,
    val photoPath: String? = null,
    val lastModified: Long? = System.currentTimeMillis()
) 