package com.example.inventory.data.repository

import com.example.inventory.data.model.Staff
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for staff operations
 */
interface StaffRepository {
    
    /**
     * Get all staff members
     */
    fun getAllStaff(): Flow<List<Staff>>
    
    /**
     * Get only active staff members
     */
    fun getActiveStaff(): Flow<List<Staff>>
    
    /**
     * Get a staff member by ID
     */
    fun getStaffById(id: UUID): Flow<Staff?>
    
    /**
     * Get staff members by role
     */
    fun getStaffByRole(role: String): Flow<List<Staff>>
    
    /**
     * Insert a new staff member
     */
    suspend fun insertStaff(staff: Staff)
    
    /**
     * Create a new staff member and return it
     */
    suspend fun createStaff(staff: Staff): Staff
    
    /**
     * Update an existing staff member
     */
    suspend fun updateStaff(staff: Staff): Staff
    
    /**
     * Delete a staff member
     */
    suspend fun deleteStaff(staff: Staff)
    
    /**
     * Archive a staff member (mark as inactive)
     */
    suspend fun archiveStaff(staffId: UUID): Staff
    
    /**
     * Refresh staff data from Firebase
     */
    suspend fun refreshFromFirebase()
    
    /**
     * Get a staff member by Firebase UID
     */
    suspend fun getStaffByFirebaseUid(uid: String): Staff?
} 