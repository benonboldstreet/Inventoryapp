package com.example.inventory.data.repository

import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.model.Item
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for checkout operations
 */
interface CheckoutRepository {
    
    /**
     * Get all checkout logs
     */
    fun getAllCheckoutLogs(): Flow<List<CheckoutLog>>
    
    /**
     * Get a checkout log by ID
     */
    fun getCheckoutLogById(id: UUID): Flow<CheckoutLog?>
    
    /**
     * Get checkout logs for an item
     */
    fun getCheckoutsByItemId(itemId: UUID): Flow<List<CheckoutLog>>
    
    /**
     * Get checkout logs for a staff member
     */
    fun getCheckoutsByStaffId(staffId: UUID): Flow<List<CheckoutLog>>
    
    /**
     * Get active checkouts (not checked in)
     */
    fun getActiveCheckouts(): Flow<List<CheckoutLog>>
    
    /**
     * Insert a new checkout log
     */
    suspend fun insertCheckoutLog(checkoutLog: CheckoutLog)
    
    /**
     * Update an existing checkout log
     */
    suspend fun updateCheckoutLog(checkoutLog: CheckoutLog)
    
    /**
     * Delete a checkout log
     */
    suspend fun deleteCheckoutLog(checkoutLog: CheckoutLog)
    
    /**
     * Check out an item to a staff member
     */
    suspend fun checkOutItem(itemId: UUID, staffId: UUID, notes: String = ""): CheckoutLog
    
    /**
     * Check in an item
     */
    suspend fun checkInItem(checkoutLogId: UUID, notes: String = ""): CheckoutLog
    
    /**
     * Refresh data from Firebase
     */
    suspend fun refreshFromFirebase()
} 