package com.example.inventory.data.repository

import com.example.inventory.data.database.CheckoutLog
import com.example.inventory.data.database.CheckoutLogDao
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for managing CheckoutLog data operations
 */
class CheckoutRepository(private val checkoutLogDao: CheckoutLogDao) {
    
    /**
     * Get all checkout logs as a Flow
     */
    fun getAllCheckoutLogs(): Flow<List<CheckoutLog>> = checkoutLogDao.getAllCheckoutLogs()
    
    /**
     * Get checkout logs for a specific item as a Flow
     */
    fun getCheckoutLogsByItemId(itemId: UUID): Flow<List<CheckoutLog>> = 
        checkoutLogDao.getCheckoutLogsByItemId(itemId)
    
    /**
     * Get checkout logs for a specific staff member as a Flow
     */
    fun getCheckoutLogsByStaffId(staffId: UUID): Flow<List<CheckoutLog>> = 
        checkoutLogDao.getCheckoutLogsByStaffId(staffId)
    
    /**
     * Get all current checkouts (not checked in yet) as a Flow
     */
    fun getCurrentCheckouts(): Flow<List<CheckoutLog>> = checkoutLogDao.getCurrentCheckouts()
    
    /**
     * Get current checkout for a specific item
     */
    suspend fun getCurrentCheckoutForItem(itemId: UUID): CheckoutLog? = 
        checkoutLogDao.getCurrentCheckoutForItem(itemId)
    
    /**
     * Get checkout log by ID
     */
    suspend fun getCheckoutLogById(id: UUID): CheckoutLog? = checkoutLogDao.getCheckoutLogById(id)
    
    /**
     * Insert a new checkout log
     */
    suspend fun insertCheckoutLog(checkoutLog: CheckoutLog) = checkoutLogDao.insert(checkoutLog)
    
    /**
     * Update an existing checkout log
     */
    suspend fun updateCheckoutLog(checkoutLog: CheckoutLog) {
        // Ensure the lastModified timestamp is updated
        val updatedLog = checkoutLog.copy(lastModified = System.currentTimeMillis())
        checkoutLogDao.update(updatedLog)
    }
    
    /**
     * Delete a checkout log
     */
    suspend fun deleteCheckoutLog(checkoutLog: CheckoutLog) = checkoutLogDao.delete(checkoutLog)
    
    /**
     * Check out an item to a staff member
     */
    suspend fun checkOutItem(itemId: UUID, staffId: UUID): CheckoutLog {
        val checkoutLog = CheckoutLog(
            itemId = itemId,
            staffId = staffId,
            checkOutTime = System.currentTimeMillis(),
            checkInTime = null,
            lastModified = System.currentTimeMillis()
        )
        checkoutLogDao.insert(checkoutLog)
        return checkoutLog
    }
    
    /**
     * Check out an item with a photo
     */
    suspend fun checkOutItemWithPhoto(itemId: UUID, staffId: UUID, photoPath: String): CheckoutLog {
        val checkoutLog = CheckoutLog(
            itemId = itemId,
            staffId = staffId,
            checkOutTime = System.currentTimeMillis(),
            checkInTime = null,
            photoPath = photoPath,
            lastModified = System.currentTimeMillis()
        )
        checkoutLogDao.insert(checkoutLog)
        return checkoutLog
    }
    
    /**
     * Check in an item
     */
    suspend fun checkInItem(checkoutLog: CheckoutLog): CheckoutLog {
        val updatedLog = checkoutLog.copy(
            checkInTime = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        checkoutLogDao.update(updatedLog)
        return updatedLog
    }
} 