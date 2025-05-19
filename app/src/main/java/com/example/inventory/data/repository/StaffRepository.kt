package com.example.inventory.data.repository

import com.example.inventory.data.model.Staff
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for managing Staff data operations
 * 
 * This defines the contract for staff repository implementations,
 * whether they're using local storage or cloud storage.
 */
interface StaffRepository {
    /**
     * Get all staff as a Flow
     */
    fun getAllStaff(): Flow<List<Staff>>
    
    /**
     * Get staff by department as a Flow
     */
    fun getStaffByDepartment(department: String): Flow<List<Staff>>
    
    /**
     * Get staff by ID
     */
    suspend fun getStaffById(id: UUID): Staff?
    
    /**
     * Insert a new staff record
     */
    suspend fun insertStaff(staff: Staff)
    
    /**
     * Update an existing staff record
     */
    suspend fun updateStaff(staff: Staff)
    
    /**
     * Delete a staff record
     */
    suspend fun deleteStaff(staff: Staff)
} 