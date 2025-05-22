package com.example.inventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.Staff
import com.example.inventory.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository
) : ViewModel() {

    // Direct access to repository - no mapping needed since we're using model objects
    val allStaff: Flow<List<Staff>> = flow {
        try {
            android.util.Log.d("StaffViewModel", "Collecting staff data from repository")
            staffRepository.getAllStaff().collect { staffList ->
                android.util.Log.d("StaffViewModel", "Received ${staffList.size} staff members from repository")
                emit(staffList)
            }
        } catch (e: Exception) {
            android.util.Log.e("StaffViewModel", "Error collecting staff data: ${e.message}", e)
            emit(emptyList<Staff>())
        }
    }
    
    /**
     * Get staff by department
     */
    fun getStaffByDepartment(department: String): Flow<List<Staff>> = 
        staffRepository.getStaffByDepartment(department)
    
    /**
     * Add a new staff record
     * 
     * [CLOUD ENDPOINT - CREATE] Creates a new staff member with name, department, and optional contact details
     */
    fun addStaff(staff: Staff) {
        viewModelScope.launch {
            staffRepository.insertStaff(staff)
        }
    }
    
    /**
     * Update an existing staff record
     * 
     * [CLOUD ENDPOINT - UPDATE] Modifies all properties of an existing staff record
     */
    fun updateStaff(staff: Staff) {
        viewModelScope.launch {
            staffRepository.updateStaff(staff)
        }
    }
    
    /**
     * Delete a staff record
     * 
     * [CLOUD ENDPOINT - DELETE] Permanently removes a staff record from the database
     */
    fun deleteStaff(staff: Staff) {
        viewModelScope.launch {
            staffRepository.deleteStaff(staff)
        }
    }
    
    /**
     * Archive a staff member (mark as inactive) instead of deleting
     * 
     * [CLOUD ENDPOINT - UPDATE] Soft-delete by setting isActive=false on a staff record
     */
    fun archiveStaff(staff: Staff) {
        viewModelScope.launch {
            val updatedStaff = staff.copy(
                isActive = false,
                lastModified = System.currentTimeMillis()
            )
            staffRepository.updateStaff(updatedStaff)
        }
    }
    
    /**
     * Restore an archived staff member
     */
    fun restoreStaff(staff: Staff) {
        viewModelScope.launch {
            val updatedStaff = staff.copy(
                isActive = true,
                lastModified = System.currentTimeMillis()
            )
            staffRepository.updateStaff(updatedStaff)
        }
    }
    
    /**
     * Unarchive a staff member (same as restore)
     */
    fun unarchiveStaff(staff: Staff) {
        viewModelScope.launch {
            val updatedStaff = staff.copy(
                isActive = true,
                lastModified = System.currentTimeMillis()
            )
            staffRepository.updateStaff(updatedStaff)
        }
    }
    
    /**
     * Get staff by ID
     */
    fun getStaffById(id: UUID): Flow<Staff?> = flow {
        val staff = staffRepository.getStaffById(id)
        emit(staff)
    }
    
    /**
     * Get staff by ID (suspend function for direct access)
     */
    suspend fun getStaffByIdSuspend(id: UUID): Staff? = staffRepository.getStaffById(id)
    
    /**
     * Update staff department
     * 
     * [CLOUD ENDPOINT - UPDATE] Changes only the department field of a staff record
     */
    fun updateStaffDepartment(staff: Staff, newDepartment: String) {
        val updatedStaff = staff.copy(department = newDepartment)
        viewModelScope.launch {
            staffRepository.updateStaff(updatedStaff)
        }
    }
    
    /**
     * Update staff contact information
     */
    fun updateStaffContact(staff: Staff, phone: String, email: String) {
        val updatedStaff = staff.copy(phone = phone, email = email)
        viewModelScope.launch {
            staffRepository.updateStaff(updatedStaff)
        }
    }
    
    /**
     * Factory for creating StaffViewModel instances with dependencies
     */
    companion object {
        class Factory(private val staffRepository: StaffRepository) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StaffViewModel::class.java)) {
                    return StaffViewModel(staffRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
} 