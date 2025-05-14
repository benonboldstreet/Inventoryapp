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
     * 
     * [CLOUD ENDPOINT - CREATE] Direct database insertion of checkout log
     * Should be migrated to create checkout records in cloud storage via API
     */
    suspend fun insertCheckoutLog(checkoutLog: CheckoutLog) = checkoutLogDao.insert(checkoutLog)
    
    /**
     * Update an existing checkout log
     * 
     * [CLOUD ENDPOINT - UPDATE] Direct database update of checkout log with timestamp refresh
     * Should be migrated to update checkout records in cloud storage via API
     */
    suspend fun updateCheckoutLog(checkoutLog: CheckoutLog) {
        // Ensure the lastModified timestamp is updated
        val updatedLog = checkoutLog.copy(lastModified = System.currentTimeMillis())
        checkoutLogDao.update(updatedLog)
    }
    
    /**
     * Delete a checkout log
     * 
     * [CLOUD ENDPOINT - DELETE] Direct database deletion of checkout log
     * Should be migrated to delete checkout records in cloud storage via API
     */
    suspend fun deleteCheckoutLog(checkoutLog: CheckoutLog) = checkoutLogDao.delete(checkoutLog)
    
    /**
     * Check out an item to a staff member
     * 
     * [CLOUD ENDPOINT - CREATE] Creates checkout record with current timestamp
     * Should be migrated to create checkout records in cloud storage via API
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
     * 
     * [CLOUD ENDPOINT - CREATE] Creates checkout record with photo path
     * Should be migrated to create checkout records in cloud storage via API
     * The photo should be stored in cloud blob storage with secure access
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
     * 
     * [CLOUD ENDPOINT - UPDATE] Updates existing checkout record with check-in time
     * Should be migrated to update checkout records in cloud storage via API
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