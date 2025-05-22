package com.example.inventory.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.repository.CheckoutLogRepository
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val checkoutLogRepository: CheckoutLogRepository,
    private val itemRepository: ItemRepository,
    private val staffRepository: StaffRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CheckoutViewModel"
    }

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    // Report states
    private val _activeCheckouts = MutableStateFlow<List<CheckoutLog>>(emptyList())
    val activeCheckouts: StateFlow<List<CheckoutLog>> = _activeCheckouts.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _staff = MutableStateFlow<List<Staff>>(emptyList())
    val staff: StateFlow<List<Staff>> = _staff.asStateFlow()

    init {
        loadActiveCheckouts()
        loadItems()
        loadStaff()
    }

    private fun loadActiveCheckouts() {
        viewModelScope.launch {
            try {
                checkoutLogRepository.getActiveCheckouts()
                    .collect { checkouts ->
                        _activeCheckouts.value = checkouts
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading active checkouts: ${e.message}", e)
            }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            try {
                itemRepository.getAllItems()
                    .collect { items ->
                        _items.value = items
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading items: ${e.message}", e)
            }
        }
    }

    private fun loadStaff() {
        viewModelScope.launch {
            try {
                staffRepository.getAllStaff()
                    .collect { staff ->
                        _staff.value = staff
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading staff: ${e.message}", e)
            }
        }
    }

    fun getCheckoutLogsByItem(itemId: UUID): Flow<List<CheckoutLog>> {
        return checkoutLogRepository.getCheckoutLogsByItem(itemId)
    }

    fun getCheckoutLogsByStaff(staffId: UUID): Flow<List<CheckoutLog>> {
        return checkoutLogRepository.getCheckoutLogsByStaff(staffId)
    }

    fun getCheckoutLogsByDateRange(startDate: Date, endDate: Date): Flow<List<CheckoutLog>> {
        return checkoutLogRepository.getCheckoutLogsByDateRange(startDate.time, endDate.time)
    }

    suspend fun getCurrentCheckoutForItem(itemId: UUID): CheckoutLog? {
        return try {
            checkoutLogRepository.getActiveCheckouts()
                .first()
                .find { it.itemId == itemId }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current checkout for item: ${e.message}", e)
            null
        }
    }

    suspend fun checkoutItem(itemId: UUID, staffId: UUID, photoPath: String?) {
        try {
            // Verify item exists
            val item = itemRepository.getItemById(itemId)
            if (item == null) {
                Log.e(TAG, "Item not found: $itemId")
                return
            }

            // Verify staff exists
            val staff = staffRepository.getStaffById(staffId)
            if (staff == null) {
                Log.e(TAG, "Staff not found: $staffId")
                return
            }

            // Check if item is already checked out
            val currentCheckout = getCurrentCheckoutForItem(itemId)
            if (currentCheckout != null) {
                Log.e(TAG, "Item is already checked out: $itemId")
                return
            }

            // Perform checkout
            checkoutLogRepository.checkoutItem(itemId, staffId, photoPath)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking out item: ${e.message}", e)
            throw e
        }
    }

    suspend fun checkinItem(checkoutLog: CheckoutLog) {
        try {
            checkoutLogRepository.checkinItem(checkoutLog)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking in item: ${e.message}", e)
            throw e
        }
    }

    fun getItemById(id: UUID): Flow<Item?> {
        return itemRepository.getItemById(id)
    }

    fun getStaffById(id: UUID): Flow<Staff?> {
        return staffRepository.getStaffById(id)
    }

    fun loadStaffCheckouts(staffId: UUID) {
        viewModelScope.launch {
            try {
                checkoutLogRepository.getCheckoutLogsByStaff(staffId).collect { checkouts ->
                    _activeCheckouts.value = checkouts
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load staff checkouts: ${e.message}", e)
                _uiState.value = CheckoutUiState.Error("Failed to load staff checkouts: ${e.message}")
            }
        }
    }

    fun generateDateRangeReport(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            try {
                _uiState.value = CheckoutUiState.Loading
                checkoutLogRepository.getCheckoutLogsByDateRange(
                    startDate.time,
                    endDate.time
                ).collect { checkouts ->
                    _uiState.value = CheckoutUiState.DateRangeReport(checkouts)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate date range report: ${e.message}", e)
                _uiState.value = CheckoutUiState.Error("Failed to generate date range report: ${e.message}")
            }
        }
    }

    fun generateItemHistoryReport(itemId: UUID) {
        viewModelScope.launch {
            try {
                _uiState.value = CheckoutUiState.Loading
                checkoutLogRepository.getCheckoutLogsByItem(itemId).collect { checkouts ->
                    _uiState.value = CheckoutUiState.ItemHistoryReport(checkouts)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate item history report: ${e.message}", e)
                _uiState.value = CheckoutUiState.Error("Failed to generate item history report: ${e.message}")
            }
        }
    }

    // Function to get all checkout logs
    fun getAllCheckoutLogs(): Flow<List<CheckoutLog>> {
        return checkoutLogRepository.getAllCheckoutLogs()
    }
    
    // Get all items
    fun getAllItems(): Flow<List<Item>> {
        return itemRepository.getAllItems()
    }
    
    // Get all staff
    fun getAllStaff(): Flow<List<Staff>> {
        return staffRepository.getAllStaff()
    }

    // Get checkout logs by item ID
    fun getCheckoutLogsByItemId(itemId: UUID): Flow<List<CheckoutLog>> {
        return checkoutLogRepository.getCheckoutLogsByItem(itemId)
    }

    // Get checkout logs by staff ID
    fun getCheckoutLogsByStaffId(staffId: UUID): Flow<List<CheckoutLog>> {
        return checkoutLogRepository.getCheckoutLogsByStaff(staffId)
    }

    // Get current checkouts
    fun getCurrentCheckouts(): Flow<List<CheckoutLog>> {
        return checkoutLogRepository.getActiveCheckouts()
    }

    // Get checkout logs by item - convenience method for UI
    fun getCheckoutLogsByItem(itemId: UUID): Flow<List<CheckoutLog>> = 
        checkoutLogRepository.getCheckoutLogsByItem(itemId)
    
    // Get checkout logs by staff - convenience method for UI
    fun getCheckoutsByStaffId(staffId: UUID): Flow<List<CheckoutLog>> =
        checkoutLogRepository.getCheckoutLogsByStaff(staffId)

    // Check out an item to a staff member
    suspend fun checkOutItem(itemId: UUID, staffId: UUID): Result<CheckoutLog> {
        return try {
            Log.d(TAG, "Checking out item $itemId to staff $staffId")
            val result = checkoutLogRepository.checkoutItem(itemId, staffId, null)
            Log.d(TAG, "Successfully checked out item $itemId to staff $staffId")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking out item $itemId to staff $staffId: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Check out an item with a photo
    suspend fun checkOutItemWithPhoto(itemId: UUID, staffId: UUID, photoPath: String): Result<CheckoutLog> {
        return try {
            Log.d(TAG, "Checking out item $itemId to staff $staffId with photo")
            val result = checkoutLogRepository.checkoutItem(itemId, staffId, photoPath)
            Log.d(TAG, "Successfully checked out item $itemId to staff $staffId with photo")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking out item with photo: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Check in an item
    suspend fun checkInItem(checkoutLog: CheckoutLog): Result<CheckoutLog> {
        return try {
            Log.d(TAG, "Checking in item for checkout ${checkoutLog.id}")
            checkoutLogRepository.checkinItem(checkoutLog)
            Log.d(TAG, "Successfully checked in item for checkout ${checkoutLog.id}")
            Result.success(checkoutLog)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking in item for checkout ${checkoutLog.id}: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Factory for creating a [CheckoutViewModel] with a constructor that takes a
     * [CheckoutRepository] and [ItemRepository]
     */
    companion object {
        fun provideFactory(
            checkoutRepository: CheckoutLogRepository,
            itemRepository: ItemRepository,
            staffRepository: StaffRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CheckoutViewModel(checkoutRepository, itemRepository, staffRepository) as T
            }
        }
    }
}

sealed class CheckoutUiState {
    object Loading : CheckoutUiState()
    object Success : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
    data class DateRangeReport(val checkouts: List<CheckoutLog>) : CheckoutUiState()
    data class ItemHistoryReport(val checkouts: List<CheckoutLog>) : CheckoutUiState()
} 