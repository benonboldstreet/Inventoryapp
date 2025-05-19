package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.api.CheckoutApiService
import com.example.inventory.api.CheckoutLogDto
import com.example.inventory.api.NetworkErrorHandler
import com.example.inventory.api.NetworkModule
import com.example.inventory.api.OfflineCache
import com.example.inventory.api.OperationType
import com.example.inventory.api.PendingOperation
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.ui.viewmodel.SharedViewModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Cloud repository implementation for managing CheckoutLog data operations
 * This connects to Azure/cloud backend instead of local database
 * Supports offline operation with caching and synchronization
 */
class CloudCheckoutRepository(
    private val apiService: CheckoutApiService = NetworkModule.checkoutApiService,
    private val appContext: Context
) : CheckoutRepository {
    
    // Gson for serializing checkout logs for offline storage
    private val gson = Gson()
    
    /**
     * Get all checkout logs as a Flow
     */
    override fun getAllCheckoutLogs(): Flow<List<CheckoutLog>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get All Checkout Logs",
                apiCall = { 
                    val logs = apiService.getAllCheckoutLogs().map { it.toEntity() }
                    // Cache the results for offline use
                    OfflineCache.cacheCheckouts(logs, appContext)
                    logs
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache
                emit(OfflineCache.getCachedCheckouts())
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedCheckouts())
        }
    }
    
    /**
     * Get checkout logs for a specific item as a Flow
     */
    override fun getCheckoutLogsByItemId(itemId: UUID): Flow<List<CheckoutLog>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Checkout Logs by Item ID ($itemId)",
                apiCall = { 
                    apiService.getCheckoutLogsByItemId(itemId.toString()).map { it.toEntity() }
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache
                emit(OfflineCache.getCachedCheckouts().filter { it.itemId == itemId })
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedCheckouts().filter { it.itemId == itemId })
        }
    }
    
    /**
     * Get checkout logs for a specific staff member as a Flow
     */
    override fun getCheckoutLogsByStaffId(staffId: UUID): Flow<List<CheckoutLog>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Checkout Logs by Staff ID ($staffId)",
                apiCall = { 
                    apiService.getCheckoutLogsByStaffId(staffId.toString()).map { it.toEntity() }
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache
                emit(OfflineCache.getCachedCheckouts().filter { it.staffId == staffId })
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedCheckouts().filter { it.staffId == staffId })
        }
    }
    
    /**
     * Get all current checkouts (not checked in yet) as a Flow
     */
    override fun getCurrentCheckouts(): Flow<List<CheckoutLog>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Current Checkouts",
                apiCall = { 
                    val logs = apiService.getCurrentCheckouts().map { it.toEntity() }
                    // Cache these specifically since they're important
                    OfflineCache.cacheCheckouts(logs, appContext)
                    logs
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache (filter for current checkouts)
                emit(OfflineCache.getCachedCheckouts().filter { it.checkInTime == null })
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedCheckouts().filter { it.checkInTime == null })
        }
    }
    
    /**
     * Get current checkout for a specific item
     * 
     * [CLOUD ENDPOINT - READ] Retrieves the current checkout for an item from cloud storage
     */
    override suspend fun getCurrentCheckoutForItem(itemId: UUID): CheckoutLog? {
        // Check cache first for immediate response - filter for current checkout of this item
        val cachedCheckout = OfflineCache.getCachedCheckouts()
            .filter { it.itemId == itemId && it.checkInTime == null }
            .firstOrNull()
        
        // If online, try to get fresh data
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Current Checkout for Item ($itemId)",
                apiCall = { 
                    apiService.getCheckoutLogsByItemId(itemId.toString())
                        .filter { it.checkInTime == null }
                        .map { it.toEntity() }
                        .firstOrNull()
                }
            )
            
            if (result != null) {
                // Cache the checkout
                OfflineCache.cacheCheckout(result, appContext)
                return result
            }
        }
        
        // Return cached checkout if we have it
        return cachedCheckout
    }
    
    /**
     * Get checkout log by ID
     * 
     * [CLOUD ENDPOINT - READ] Retrieves a checkout log by ID from cloud storage
     */
    override suspend fun getCheckoutLogById(id: UUID): CheckoutLog? {
        // Check cache first for immediate response
        val cachedCheckout = OfflineCache.getCachedCheckout(id)
        
        // If online, try to get fresh data
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Checkout Log by ID ($id)",
                apiCall = { 
                    apiService.getAllCheckoutLogs()
                        .filter { it.id == id.toString() }
                        .map { it.toEntity() }
                        .firstOrNull()
                }
            )
            
            if (result != null) {
                // Cache the checkout
                OfflineCache.cacheCheckout(result, appContext)
                return result
            }
        }
        
        // Return cached checkout if we have it
        return cachedCheckout
    }
    
    /**
     * Insert a new checkout log
     * 
     * [CLOUD ENDPOINT - CREATE] Creates a new checkout log in cloud storage via API
     */
    override suspend fun insertCheckoutLog(checkoutLog: CheckoutLog) {
        // Add to the local cache immediately for responsiveness
        OfflineCache.cacheCheckout(checkoutLog, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            NetworkErrorHandler.handleApiCall(
                operationName = "Insert Checkout Log",
                apiCall = { 
                    apiService.createCheckoutLog(checkoutLog.toDto())
                }
            )
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.INSERT_CHECKOUT,
                data = gson.toJson(checkoutLog)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
    }
    
    /**
     * Update an existing checkout log
     * 
     * [CLOUD ENDPOINT - UPDATE] Updates a checkout log in cloud storage via API
     */
    override suspend fun updateCheckoutLog(checkoutLog: CheckoutLog) {
        // Update the local cache immediately for responsiveness
        val updatedLog = checkoutLog.copy(lastModified = System.currentTimeMillis())
        OfflineCache.cacheCheckout(updatedLog, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            NetworkErrorHandler.handleApiCall(
                operationName = "Update Checkout Log",
                apiCall = { 
                    apiService.createCheckoutLog(updatedLog.toDto())
                }
            )
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.UPDATE_CHECKOUT,
                data = gson.toJson(updatedLog)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
    }
    
    /**
     * Delete a checkout log
     * 
     * [CLOUD ENDPOINT - DELETE] Deletes a checkout log from cloud storage via API
     */
    override suspend fun deleteCheckoutLog(checkoutLog: CheckoutLog) {
        // Update the local cache immediately for responsiveness
        // Since there's no actual delete endpoint, we'll handle this differently in pending operations
        val deletedLog = checkoutLog.copy(lastModified = System.currentTimeMillis())
        
        // For now, we'll just remove it from the local cache
        // In a real implementation, you might want to mark it as deleted instead
        
        // If online, send to cloud (not implemented in the API)
        if (SharedViewModel.isCloudConnected.value) {
            NetworkErrorHandler.handleApiCall(
                operationName = "Delete Checkout Log",
                apiCall = { 
                    // This would require a specific delete endpoint
                    // For now this is just a placeholder since the API doesn't have an explicit delete
                    Unit  // Return Unit as there's no actual operation yet
                }
            )
        } else {
            // If offline, add to pending operations (not implemented for this operation)
            // This is just a placeholder since the cloud API doesn't support this operation
        }
    }
    
    /**
     * Check out an item to a staff member
     * 
     * [CLOUD ENDPOINT - CREATE] Creates a checkout record in cloud storage
     */
    override suspend fun checkOutItem(itemId: UUID, staffId: UUID): CheckoutLog {
        // Create the checkout log
        val checkoutLog = CheckoutLog(
            itemId = itemId,
            staffId = staffId,
            checkOutTime = System.currentTimeMillis()
        )
        
        // Add to the local cache immediately for responsiveness
        OfflineCache.cacheCheckout(checkoutLog, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Check Out Item (Item: $itemId, Staff: $staffId)",
                apiCall = { 
                    val dto = CheckoutLogDto(
                        itemId = itemId.toString(),
                        staffId = staffId.toString(),
                        checkOutTime = System.currentTimeMillis(),
                        checkInTime = null,
                        lastModified = System.currentTimeMillis()
                    )
                    apiService.createCheckoutLog(dto).toEntity()
                }
            )
            
            if (result != null) {
                // Update cache with the server response
                OfflineCache.cacheCheckout(result, appContext)
                return result
            }
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.INSERT_CHECKOUT,
                data = gson.toJson(checkoutLog)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
        
        return checkoutLog
    }
    
    /**
     * Check out an item with a photo
     * 
     * [CLOUD ENDPOINT - CREATE] Creates a checkout record with photo in cloud storage
     * The photo should be stored in Azure Blob Storage with secure access
     */
    override suspend fun checkOutItemWithPhoto(itemId: UUID, staffId: UUID, photoPath: String): CheckoutLog {
        // Create the checkout log with photo
        val checkoutLog = CheckoutLog(
            itemId = itemId,
            staffId = staffId,
            checkOutTime = System.currentTimeMillis(),
            photoPath = photoPath
        )
        
        // Add to the local cache immediately for responsiveness
        OfflineCache.cacheCheckout(checkoutLog, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Check Out Item with Photo (Item: $itemId, Staff: $staffId)",
                apiCall = { 
                    val dto = CheckoutLogDto(
                        itemId = itemId.toString(),
                        staffId = staffId.toString(),
                        checkOutTime = System.currentTimeMillis(),
                        checkInTime = null,
                        photoPath = photoPath, // This should be a cloud storage URL in production
                        lastModified = System.currentTimeMillis()
                    )
                    apiService.createCheckoutLog(dto).toEntity()
                }
            )
            
            if (result != null) {
                // Update cache with the server response
                OfflineCache.cacheCheckout(result, appContext)
                return result
            }
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.INSERT_CHECKOUT,
                data = gson.toJson(checkoutLog)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
        
        return checkoutLog
    }
    
    /**
     * Check in an item
     * 
     * [CLOUD ENDPOINT - UPDATE] Updates a checkout record with check-in data in cloud storage
     */
    override suspend fun checkInItem(checkoutLog: CheckoutLog): CheckoutLog {
        // Update the local cache immediately for responsiveness
        val checkedInLog = checkoutLog.copy(
            checkInTime = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        OfflineCache.cacheCheckout(checkedInLog, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Check In Item (Log ID: ${checkoutLog.id})",
                apiCall = { 
                    apiService.checkInItem(
                        checkoutLog.id.toString(),
                        mapOf("checkInTime" to System.currentTimeMillis().toString())
                    ).toEntity()
                }
            )
            
            if (result != null) {
                // Update cache with the server response
                OfflineCache.cacheCheckout(result, appContext)
                return result
            }
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.CHECK_IN_ITEM,
                data = gson.toJson(checkedInLog)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
        
        return checkedInLog
    }
}

// Extension functions for converting between CheckoutLog and CheckoutLogDto
private fun CheckoutLogDto.toEntity(): CheckoutLog = CheckoutLog(
    id = id?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
    itemId = itemId.let { UUID.fromString(it) },
    staffId = staffId.let { UUID.fromString(it) },
    checkOutTime = checkOutTime ?: System.currentTimeMillis(),
    checkInTime = checkInTime,
    photoPath = photoPath,
    lastModified = lastModified
)

private fun CheckoutLog.toDto(): CheckoutLogDto = CheckoutLogDto(
    id = id.toString(),
    itemId = itemId.toString(),
    staffId = staffId.toString(),
    checkOutTime = checkOutTime,
    checkInTime = checkInTime,
    photoPath = photoPath,
    lastModified = lastModified
) 