package com.example.inventory.data.repository

import com.example.inventory.data.database.Staff
import com.example.inventory.data.database.StaffDao
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for managing Staff data operations
 */
class StaffRepository(private val staffDao: StaffDao) {
    
    /**
     * Get all staff as a Flow
     */
    fun getAllStaff(): Flow<List<Staff>> = staffDao.getAllStaff()
    
    /**
     * Get staff by department as a Flow
     */
    fun getStaffByDepartment(department: String): Flow<List<Staff>> = 
        staffDao.getStaffByDepartment(department)
    
    /**
     * Get staff by ID
     */
    suspend fun getStaffById(id: UUID): Staff? = staffDao.getStaffById(id)
    
    /**
     * Insert a new staff record
     * 
     * [CLOUD ENDPOINT - CREATE] Direct database insertion of staff record
     * Should be migrated to create staff records in cloud storage via API
     */
    suspend fun insertStaff(staff: Staff) = staffDao.insert(staff)
    
    /**
     * Update an existing staff record
     * 
     * [CLOUD ENDPOINT - UPDATE] Direct database update of staff record with timestamp refresh
     * Should be migrated to update staff records in cloud storage via API
     */
    suspend fun updateStaff(staff: Staff) {
        // Ensure the lastModified timestamp is updated
        val updatedStaff = staff.copy(lastModified = System.currentTimeMillis())
        staffDao.update(updatedStaff)
    }
    
    /**
     * Delete a staff record
     * 
     * [CLOUD ENDPOINT - DELETE] Direct database deletion of staff record
     * Should be migrated to delete staff records in cloud storage via API
     */
    suspend fun deleteStaff(staff: Staff) = staffDao.delete(staff)
} 