package com.example.inventory.data.repository

import android.content.Context
import com.example.inventory.api.NetworkErrorHandler
import com.example.inventory.api.NetworkModule
import com.example.inventory.api.OfflineCache
import com.example.inventory.api.OperationType
import com.example.inventory.api.PendingOperation
import com.example.inventory.api.StaffApiService
import com.example.inventory.api.StaffDto
import com.example.inventory.data.model.Staff
import com.example.inventory.ui.viewmodel.SharedViewModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Cloud repository implementation for managing Staff data operations
 * This connects to Azure/cloud backend instead of local database
 * Supports offline operation with caching and synchronization
 */
class CloudStaffRepository(
    private val apiService: StaffApiService = NetworkModule.staffApiService,
    private val appContext: Context
) : StaffRepository {
    
    // Gson for serializing staff for offline storage
    private val gson = Gson()
    
    /**
     * Get all staff as a Flow
     */
    override fun getAllStaff(): Flow<List<Staff>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get All Staff",
                apiCall = { 
                    val staffList = apiService.getAllStaff().map { it.toEntity() }
                    // Cache the results for offline use
                    OfflineCache.cacheStaff(staffList, appContext)
                    staffList
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache
                emit(OfflineCache.getCachedStaff())
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedStaff())
        }
    }
    
    /**
     * Get staff by department as a Flow
     */
    override fun getStaffByDepartment(department: String): Flow<List<Staff>> = flow {
        // First check if we're online
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Staff by Department ($department)",
                apiCall = { 
                    apiService.getAllStaff()
                        .filter { it.department == department }
                        .map { it.toEntity() }
                }
            )
            
            if (result != null) {
                emit(result)
            } else {
                // If cloud request failed, fall back to cache
                emit(OfflineCache.getCachedStaff().filter { it.department == department })
            }
        } else {
            // If offline, use cached data
            emit(OfflineCache.getCachedStaff().filter { it.department == department })
        }
    }
    
    /**
     * Get staff by ID
     * 
     * [CLOUD ENDPOINT - READ] Retrieves a staff record from cloud storage by ID
     */
    override suspend fun getStaffById(id: UUID): Staff? {
        // First check cache for immediate response
        val cachedStaff = OfflineCache.getCachedStaff(id)
        
        // If online, try to get fresh data
        if (SharedViewModel.isCloudConnected.value) {
            // Try to get from cloud
            val result = NetworkErrorHandler.handleApiCall(
                operationName = "Get Staff by ID ($id)",
                apiCall = { 
                    apiService.getStaffById(id.toString()).toEntity()
                }
            )
            
            if (result != null) {
                // Cache the staff member
                OfflineCache.cacheStaff(result, appContext)
                return result
            }
        }
        
        // Return cached staff if we have it
        return cachedStaff
    }
    
    /**
     * Insert a new staff record
     * 
     * [CLOUD ENDPOINT - CREATE] Creates a new staff record in cloud storage via API
     */
    override suspend fun insertStaff(staff: Staff) {
        // Add to the local cache immediately for responsiveness
        OfflineCache.cacheStaff(staff, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            NetworkErrorHandler.handleApiCall(
                operationName = "Insert Staff",
                apiCall = { 
                    apiService.createStaff(staff.toDto())
                }
            )
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.INSERT_STAFF,
                data = gson.toJson(staff)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
    }
    
    /**
     * Update an existing staff record
     * 
     * [CLOUD ENDPOINT - UPDATE] Updates a staff record in cloud storage via API
     */
    override suspend fun updateStaff(staff: Staff) {
        // Update the local cache immediately for responsiveness
        val updatedStaff = staff.copy(lastModified = System.currentTimeMillis())
        OfflineCache.cacheStaff(updatedStaff, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            NetworkErrorHandler.handleApiCall(
                operationName = "Update Staff",
                apiCall = { 
                    apiService.updateStaff(updatedStaff.id.toString(), updatedStaff.toDto())
                }
            )
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.UPDATE_STAFF,
                data = gson.toJson(updatedStaff)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
    }
    
    /**
     * Delete a staff record
     * 
     * [CLOUD ENDPOINT - DELETE] Deletes a staff record from cloud storage via API
     * Note: This could be implemented as a soft delete on the server
     */
    override suspend fun deleteStaff(staff: Staff) {
        // Update the local cache immediately for responsiveness
        val deletedStaff = staff.copy(isActive = false, lastModified = System.currentTimeMillis())
        OfflineCache.cacheStaff(deletedStaff, appContext)
        
        // If online, send to cloud
        if (SharedViewModel.isCloudConnected.value) {
            NetworkErrorHandler.handleApiCall(
                operationName = "Delete Staff",
                apiCall = { 
                    apiService.archiveStaff(staff.id.toString())
                }
            )
        } else {
            // If offline, add to pending operations
            val operation = PendingOperation(
                type = OperationType.DELETE_STAFF,
                data = gson.toJson(deletedStaff)
            )
            OfflineCache.addPendingOperation(operation, appContext)
        }
    }
}

// Extension functions for converting between Staff and StaffDto
private fun StaffDto.toEntity(): Staff = Staff(
    id = id?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
    name = name,
    department = department,
    email = email,
    phone = phone,
    position = position,
    isActive = isActive,
    lastModified = lastModified
)

private fun Staff.toDto(): StaffDto = StaffDto(
    id = id.toString(),
    name = name,
    department = department,
    email = email,
    phone = phone,
    position = position,
    isActive = isActive,
    lastModified = lastModified
) 