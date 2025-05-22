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
     * Get all staff members as a Flow
     */
    fun getAllStaff(): Flow<List<Staff>>
    
    /**
     * Get active staff as a Flow
     */
    fun getActiveStaff(): Flow<List<Staff>>
    
    /**
     * Get staff by department as a Flow
     */
    fun getStaffByDepartment(department: String): Flow<List<Staff>>
    
    /**
     * Get staff by ID
     */
    suspend fun getStaffById(id: UUID): Staff?
    
    /**
     * Get staff by email
     */
    suspend fun getStaffByEmail(email: String): Staff?
    
    /**
     * Insert a new staff member
     */
    suspend fun insertStaff(staff: Staff)
    
    /**
     * Update an existing staff member
     */
    suspend fun updateStaff(staff: Staff)
    
    /**
     * Delete a staff member
     */
    suspend fun deleteStaff(staff: Staff)
    
    /**
     * Refresh staff data from Firebase
     */
    suspend fun refreshFromFirebase()
} 