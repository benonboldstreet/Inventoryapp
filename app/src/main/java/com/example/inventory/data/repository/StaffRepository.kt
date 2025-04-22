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
     */
    suspend fun insertStaff(staff: Staff) = staffDao.insert(staff)
    
    /**
     * Update an existing staff record
     */
    suspend fun updateStaff(staff: Staff) {
        // Ensure the lastModified timestamp is updated
        val updatedStaff = staff.copy(lastModified = System.currentTimeMillis())
        staffDao.update(updatedStaff)
    }
    
    /**
     * Delete a staff record
     */
    suspend fun deleteStaff(staff: Staff) = staffDao.delete(staff)
} 