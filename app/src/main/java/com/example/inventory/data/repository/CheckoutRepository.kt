package com.example.inventory.data.repository

import com.example.inventory.data.model.CheckoutLog
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for managing CheckoutLog data operations
 * 
 * This defines the contract for checkout repository implementations,
 * whether they're using local storage or cloud storage.
 */
interface CheckoutRepository {
    /**
     * Get all checkout logs as a Flow
     */
    fun getAllCheckoutLogs(): Flow<List<CheckoutLog>>
    
    /**
     * Get checkout logs for a specific item as a Flow
     */
    fun getCheckoutLogsByItemId(itemId: UUID): Flow<List<CheckoutLog>>
    
    /**
     * Get checkout logs for a specific staff member as a Flow
     */
    fun getCheckoutLogsByStaffId(staffId: UUID): Flow<List<CheckoutLog>>
    
    /**
     * Get all current checkouts (not checked in yet) as a Flow
     */
    fun getCurrentCheckouts(): Flow<List<CheckoutLog>>
    
    /**
     * Get current checkout for a specific item
     */
    suspend fun getCurrentCheckoutForItem(itemId: UUID): CheckoutLog?
    
    /**
     * Get checkout log by ID
     */
    suspend fun getCheckoutLogById(id: UUID): CheckoutLog?
    
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
    suspend fun checkOutItem(itemId: UUID, staffId: UUID): CheckoutLog
    
    /**
     * Check out an item with a photo
     */
    suspend fun checkOutItemWithPhoto(itemId: UUID, staffId: UUID, photoPath: String): CheckoutLog
    
    /**
     * Check in an item
     */
    suspend fun checkInItem(checkoutLog: CheckoutLog): CheckoutLog
} 