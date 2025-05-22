package com.example.inventory.data.repository

import com.example.inventory.data.model.CheckoutLog
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for managing CheckoutLog data operations
 */
interface CheckoutLogRepository {
    /**
     * Get all checkout logs as a Flow
     */
    fun getAllCheckoutLogs(): Flow<List<CheckoutLog>>
    
    /**
     * Get checkout logs by item ID as a Flow
     */
    fun getCheckoutLogsByItem(itemId: UUID): Flow<List<CheckoutLog>>
    
    /**
     * Get checkout logs by staff ID as a Flow
     */
    fun getCheckoutLogsByStaff(staffId: UUID): Flow<List<CheckoutLog>>
    
    /**
     * Get checkout logs by status as a Flow
     */
    fun getCheckoutLogsByStatus(status: String): Flow<List<CheckoutLog>>
    
    /**
     * Get checkout logs by date range as a Flow
     * @param startTime Start time in milliseconds since epoch
     * @param endTime End time in milliseconds since epoch
     */
    fun getCheckoutLogsByDateRange(startTime: Long, endTime: Long): Flow<List<CheckoutLog>>
    
    /**
     * Get active checkouts as a Flow
     */
    fun getActiveCheckouts(): Flow<List<CheckoutLog>>
    
    /**
     * Check out an item
     */
    suspend fun checkoutItem(itemId: UUID, staffId: UUID, photoPath: String? = null): CheckoutLog
    
    /**
     * Check in an item
     */
    suspend fun checkinItem(checkoutLog: CheckoutLog)
    
    /**
     * Refresh checkout logs from Firebase
     */
    suspend fun refreshFromFirebase()
} 