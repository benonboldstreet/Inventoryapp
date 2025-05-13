package com.example.inventory.data.repository

import com.example.inventory.api.NetworkModule
import com.example.inventory.api.StaffApiService
import com.example.inventory.api.StaffDto
import com.example.inventory.data.database.Staff
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Cloud repository implementation for managing Staff data operations
 * This connects to Azure/cloud backend instead of local database
 */
class CloudStaffRepository(
    private val apiService: StaffApiService = NetworkModule.staffApiService
) : StaffRepository {
    
    /**
     * Get all staff as a Flow
     */
    override fun getAllStaff(): Flow<List<Staff>> = flow {
        try {
            // Get staff from cloud API
            val staffList = apiService.getAllStaff().map { it.toEntity() }
            emit(staffList)
        } catch (e: Exception) {
            // Handle network errors
            emit(emptyList())
            // TODO: Implement proper error handling
        }
    }
    
    /**
     * Get staff by department as a Flow
     */
    override fun getStaffByDepartment(department: String): Flow<List<Staff>> = flow {
        try {
            // Filter staff by department
            // Note: In a real implementation, this should be a dedicated API endpoint
            val staffList = apiService.getAllStaff()
                .filter { it.department == department }
                .map { it.toEntity() }
            emit(staffList)
        } catch (e: Exception) {
            emit(emptyList())
            // TODO: Implement proper error handling
        }
    }
    
    /**
     * Get staff by ID
     * 
     * [CLOUD ENDPOINT - READ] Retrieves a staff record from cloud storage by ID
     */
    override suspend fun getStaffById(id: UUID): Staff? {
        return try {
            apiService.getStaffById(id.toString()).toEntity()
        } catch (e: Exception) {
            null
            // TODO: Implement proper error handling
        }
    }
    
    /**
     * Insert a new staff record
     * 
     * [CLOUD ENDPOINT - CREATE] Creates a new staff record in cloud storage via API
     */
    override suspend fun insertStaff(staff: Staff) {
        try {
            apiService.createStaff(staff.toDto())
        } catch (e: Exception) {
            // TODO: Implement proper error handling and retries
        }
    }
    
    /**
     * Update an existing staff record
     * 
     * [CLOUD ENDPOINT - UPDATE] Updates a staff record in cloud storage via API
     */
    override suspend fun updateStaff(staff: Staff) {
        try {
            // Ensure the lastModified timestamp is updated
            val updatedStaff = staff.copy(lastModified = System.currentTimeMillis())
            apiService.updateStaff(updatedStaff.id.toString(), updatedStaff.toDto())
        } catch (e: Exception) {
            // TODO: Implement proper error handling and retries
        }
    }
    
    /**
     * Delete a staff record
     * 
     * [CLOUD ENDPOINT - DELETE] Deletes a staff record from cloud storage via API
     * Note: This could be implemented as a soft delete on the server
     */
    override suspend fun deleteStaff(staff: Staff) {
        try {
            // Note: For most cloud APIs, this might be a PATCH or PUT to set a deleted flag
            // rather than a true DELETE operation
            apiService.archiveStaff(staff.id.toString())
        } catch (e: Exception) {
            // TODO: Implement proper error handling and retries
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