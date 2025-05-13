package com.example.inventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.database.Staff
import com.example.inventory.data.repository.StaffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class StaffViewModel(private val repository: StaffRepository) : ViewModel() {

    // Get all staff as a Flow
    val allStaff: Flow<List<Staff>> = repository.getAllStaff()
    
    /**
     * Get staff by department
     */
    fun getStaffByDepartment(department: String): Flow<List<Staff>> = 
        repository.getStaffByDepartment(department)
    
    /**
     * Add a new staff record
     * 
     * [CLOUD ENDPOINT - CREATE] Creates a new staff member with name, department, and optional contact details
     * Should be migrated to create staff records in cloud storage
     */
    fun addStaff(
        name: String, 
        department: String, 
        email: String = "", 
        phone: String = "", 
        position: String = ""
    ) {
        val newStaff = Staff(
            name = name,
            department = department,
            email = email,
            phone = phone,
            position = position
        )
        viewModelScope.launch {
            repository.insertStaff(newStaff)
        }
    }
    
    /**
     * Update an existing staff record
     * 
     * [CLOUD ENDPOINT - UPDATE] Modifies all properties of an existing staff record
     * Should be migrated to update staff records in cloud storage
     */
    fun updateStaff(staff: Staff) {
        viewModelScope.launch {
            repository.updateStaff(staff)
        }
    }
    
    /**
     * Delete a staff record
     * 
     * [CLOUD ENDPOINT - DELETE] Permanently removes a staff record from the database
     * Should be migrated to delete staff records in cloud storage
     */
    fun deleteStaff(staff: Staff) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
        }
    }
    
    /**
     * Archive a staff member (mark as inactive) instead of deleting
     * 
     * [CLOUD ENDPOINT - UPDATE] Soft-delete by setting isActive=false on a staff record
     * Should be migrated to update staff status in cloud storage
     */
    fun archiveStaff(staff: Staff) {
        val archivedStaff = staff.copy(isActive = false, lastModified = System.currentTimeMillis())
        viewModelScope.launch {
            repository.updateStaff(archivedStaff)
        }
    }
    
    /**
     * Get staff by ID
     */
    suspend fun getStaffById(id: UUID): Staff? = repository.getStaffById(id)
    
    /**
     * Update staff department
     * 
     * [CLOUD ENDPOINT - UPDATE] Changes only the department field of a staff record
     * Should be migrated to update staff department in cloud storage
     */
    fun updateStaffDepartment(staff: Staff, newDepartment: String) {
        val updatedStaff = staff.copy(department = newDepartment)
        viewModelScope.launch {
            repository.updateStaff(updatedStaff)
        }
    }
    
    /**
     * Factory for creating StaffViewModel with dependency injection
     */
    class Factory(private val repository: StaffRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StaffViewModel::class.java)) {
                return StaffViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 