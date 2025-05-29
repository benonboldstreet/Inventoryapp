package com.example.inventory.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.Staff
import com.example.inventory.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StaffViewModel"
    }

    private val _uiState = MutableStateFlow<StaffUiState>(StaffUiState.Loading)
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    // All staff
    fun getAllStaff(): Flow<List<Staff>> {
        return staffRepository.getAllStaff()
    }

    // Active staff
    val activeStaff: Flow<List<Staff>> = staffRepository.getAllStaff()
        .map { staffList -> staffList.filter { it.isActive } }

    // Archived staff
    val archivedStaff: Flow<List<Staff>> = staffRepository.getAllStaff()
        .map { staffList -> staffList.filter { !it.isActive } }

    // Staff by department
    val staffByDepartment: Flow<Map<String, List<Staff>>> = staffRepository.getAllStaff()
        .map { staffList -> staffList.groupBy { it.department } }

    // Get staff by ID
    fun getStaffById(id: UUID): Flow<Staff?> {
        return staffRepository.getStaffById(id)
    }

    // Get staff by department
    fun getStaffByDepartment(department: String): Flow<List<Staff>> {
        return staffRepository.getAllStaff()
            .map { staffList -> staffList.filter { it.department == department } }
    }

    // Add a new staff member
    suspend fun addStaff(staff: Staff): Result<Staff> {
        return try {
            _uiState.value = StaffUiState.Loading
            staffRepository.insertStaff(staff)
            _uiState.value = StaffUiState.Success
            Result.success(staff)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding staff: ${e.message}", e)
            _uiState.value = StaffUiState.Error("Failed to add staff: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Update an existing staff member
    suspend fun updateStaff(staff: Staff): Result<Staff> {
        return try {
            _uiState.value = StaffUiState.Loading
            staffRepository.updateStaff(staff)
            _uiState.value = StaffUiState.Success
            Result.success(staff)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating staff: ${e.message}", e)
            _uiState.value = StaffUiState.Error("Failed to update staff: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Delete a staff member
    suspend fun deleteStaff(staff: Staff): Result<Unit> {
        return try {
            _uiState.value = StaffUiState.Loading
            staffRepository.deleteStaff(staff)
            _uiState.value = StaffUiState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting staff: ${e.message}", e)
            _uiState.value = StaffUiState.Error("Failed to delete staff: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Archive a staff member
    suspend fun archiveStaff(staff: Staff): Result<Staff> {
        return try {
            _uiState.value = StaffUiState.Loading
            val archivedStaff = staff.copy(
                isActive = false,
                lastModified = System.currentTimeMillis()
            )
            staffRepository.updateStaff(archivedStaff)
            _uiState.value = StaffUiState.Success
            Result.success(archivedStaff)
        } catch (e: Exception) {
            Log.e(TAG, "Error archiving staff: ${e.message}", e)
            _uiState.value = StaffUiState.Error("Failed to archive staff: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Unarchive a staff member
    suspend fun unarchiveStaff(staff: Staff): Result<Staff> {
        return try {
            _uiState.value = StaffUiState.Loading
            val unarchivedStaff = staff.copy(
                isActive = true,
                lastModified = System.currentTimeMillis()
            )
            staffRepository.updateStaff(unarchivedStaff)
            _uiState.value = StaffUiState.Success
            Result.success(unarchivedStaff)
        } catch (e: Exception) {
            Log.e(TAG, "Error unarchiving staff: ${e.message}", e)
            _uiState.value = StaffUiState.Error("Failed to unarchive staff: ${e.message}")
            Result.failure(e)
        }
    }
}

sealed class StaffUiState {
    object Loading : StaffUiState()
    object Success : StaffUiState()
    data class Error(val message: String) : StaffUiState()
} 