package com.example.inventory.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    val checkoutRepository: CheckoutRepository,
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
                checkoutRepository.getActiveCheckouts()
                    .catch { e ->
                        Log.e(TAG, "Error loading active checkouts: ${e.message}", e)
                        _uiState.value = CheckoutUiState.Error("Failed to load active checkouts: ${e.message}")
                    }
                    .collect { checkouts ->
                        _activeCheckouts.value = checkouts
                        _uiState.value = CheckoutUiState.Success
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading active checkouts: ${e.message}", e)
                _uiState.value = CheckoutUiState.Error("Failed to load active checkouts: ${e.message}")
            }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            try {
                itemRepository.getAllItems()
                    .catch { e ->
                        Log.e(TAG, "Error loading items: ${e.message}", e)
                        _uiState.value = CheckoutUiState.Error("Failed to load items: ${e.message}")
                    }
                    .collect { items ->
                        _items.value = items
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading items: ${e.message}", e)
                _uiState.value = CheckoutUiState.Error("Failed to load items: ${e.message}")
            }
        }
    }

    private fun loadStaff() {
        viewModelScope.launch {
            try {
                staffRepository.getAllStaff()
                    .catch { e ->
                        Log.e(TAG, "Error loading staff: ${e.message}", e)
                        _uiState.value = CheckoutUiState.Error("Failed to load staff: ${e.message}")
                    }
                    .collect { staff ->
                        _staff.value = staff
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading staff: ${e.message}", e)
                _uiState.value = CheckoutUiState.Error("Failed to load staff: ${e.message}")
            }
        }
    }

    // Checkout Operations
    suspend fun checkoutItem(itemId: UUID, staffId: UUID, photoPath: String? = null): Result<CheckoutLog> {
        return try {
            _uiState.value = CheckoutUiState.Loading

            // Verify item exists
            val item = itemRepository.getItemById(itemId).first()
            if (item == null) {
                Log.e(TAG, "Item not found: $itemId")
                return Result.failure(IllegalArgumentException("Item not found: $itemId"))
            }

            // Verify staff exists
            val staff = staffRepository.getStaffById(staffId).first()
            if (staff == null) {
                Log.e(TAG, "Staff not found: $staffId")
                return Result.failure(IllegalArgumentException("Staff not found: $staffId"))
            }

            // Check if item is already checked out
            val currentCheckout = getCurrentCheckoutForItem(itemId)
            if (currentCheckout != null) {
                Log.e(TAG, "Item is already checked out: $itemId")
                return Result.failure(IllegalStateException("Item is already checked out: $itemId"))
            }

            // Perform checkout
            val result = checkoutRepository.checkOutItem(itemId, staffId, notes = photoPath ?: "")
            _uiState.value = CheckoutUiState.Success
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking out item: ${e.message}", e)
            _uiState.value = CheckoutUiState.Error("Failed to checkout item: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun checkinItem(checkoutLog: CheckoutLog): Result<Unit> {
        return try {
            _uiState.value = CheckoutUiState.Loading
            val updatedLog = checkoutRepository.checkInItem(checkoutLog.id, "")
            _uiState.value = CheckoutUiState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking in item: ${e.message}", e)
            _uiState.value = CheckoutUiState.Error("Failed to check in item: ${e.message}")
            Result.failure(e)
        }
    }

    // Query Operations
    fun getCheckoutLogsByItem(itemId: UUID): Flow<List<CheckoutLog>> =
        checkoutRepository.getCheckoutsByItemId(itemId)

    fun getCheckoutLogsByStaff(staffId: UUID): Flow<List<CheckoutLog>> =
        checkoutRepository.getCheckoutsByStaffId(staffId)

    fun getCheckoutLogsByDateRange(startDate: Date, endDate: Date): Flow<List<CheckoutLog>> = flow {
        // Convert dates to longs and filter logs
        val startTime = startDate.time
        val endTime = endDate.time
        
        val allLogs = checkoutRepository.getAllCheckoutLogs().first()
        val filteredLogs = allLogs.filter { log ->
            log.checkoutTimestamp >= startTime && log.checkoutTimestamp <= endTime
        }
        
        emit(filteredLogs)
    }

    suspend fun getCurrentCheckoutForItem(itemId: UUID): CheckoutLog? {
        return try {
            checkoutRepository.getActiveCheckouts()
                .first()
                .find { it.itemId == itemId }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current checkout for item: ${e.message}", e)
            null
        }
    }

    // Report Generation
    fun generateDateRangeReport(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            try {
                _uiState.value = CheckoutUiState.Loading
                getCheckoutLogsByDateRange(startDate, endDate).collect { checkouts ->
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
                checkoutRepository.getCheckoutsByItemId(itemId).collect { checkouts ->
                    _uiState.value = CheckoutUiState.ItemHistoryReport(checkouts)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate item history report: ${e.message}", e)
                _uiState.value = CheckoutUiState.Error("Failed to generate item history report: ${e.message}")
            }
        }
    }

    // Report Operations
    fun getAllCheckoutLogs(): Flow<List<CheckoutLog>> {
        Log.d(TAG, "Getting all checkout logs")
        return checkoutRepository.getAllCheckoutLogs()
            .catch { e ->
                Log.e(TAG, "Error in getAllCheckoutLogs: ${e.message}", e)
                emit(emptyList())
            }
    }

    suspend fun getItemById(itemId: UUID): Item? {
        return try {
            Log.d(TAG, "Getting item by ID: $itemId")
            val item = itemRepository.getItemById(itemId).first()
            if (item == null) {
                Log.w(TAG, "Item not found: $itemId")
            }
            item
        } catch (e: Exception) {
            Log.e(TAG, "Error getting item by ID: ${e.message}", e)
            null
        }
    }

    suspend fun getStaffById(staffId: UUID): Staff? {
        return try {
            Log.d(TAG, "Getting staff by ID: $staffId")
            val staff = staffRepository.getStaffById(staffId).first()
            if (staff == null) {
                Log.w(TAG, "Staff not found: $staffId")
            }
            staff
        } catch (e: Exception) {
            Log.e(TAG, "Error getting staff by ID: ${e.message}", e)
            null
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