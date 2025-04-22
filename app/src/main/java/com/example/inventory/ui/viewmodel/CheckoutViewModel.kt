package com.example.inventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.database.CheckoutLog
import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    // Get all checkout logs as a Flow
    val allCheckoutLogs: Flow<List<CheckoutLog>> = checkoutRepository.getAllCheckoutLogs()
    
    // Get current checkouts (items not checked in yet)
    val currentCheckouts: Flow<List<CheckoutLog>> = checkoutRepository.getCurrentCheckouts()
    
    /**
     * Get checkout logs for a specific item
     */
    fun getCheckoutLogsByItem(itemId: UUID): Flow<List<CheckoutLog>> = 
        checkoutRepository.getCheckoutLogsByItemId(itemId)
    
    /**
     * Get checkout logs for a specific staff member
     */
    fun getCheckoutLogsByStaff(staffId: UUID): Flow<List<CheckoutLog>> = 
        checkoutRepository.getCheckoutLogsByStaffId(staffId)
    
    /**
     * Check out an item to a staff member
     */
    fun checkOutItem(itemId: UUID, staffId: UUID) {
        viewModelScope.launch {
            // Get the item to update its status
            val item = itemRepository.getItemById(itemId)
            
            // Only proceed if the item exists and is available
            item?.let {
                if (it.status == "Available") {
                    // Create checkout log
                    checkoutRepository.checkOutItem(itemId, staffId)
                    
                    // Update item status
                    val updatedItem = it.copy(status = "Checked Out")
                    itemRepository.updateItem(updatedItem)
                }
            }
        }
    }
    
    /**
     * Check out an item with a photo
     */
    fun checkOutItemWithPhoto(itemId: UUID, staffId: UUID, photoPath: String) {
        viewModelScope.launch {
            // Get the item to update its status
            val item = itemRepository.getItemById(itemId)
            
            // Only proceed if the item exists and is available
            item?.let {
                if (it.status == "Available") {
                    // Create checkout log with photo
                    val checkoutLog = CheckoutLog(
                        itemId = itemId,
                        staffId = staffId,
                        checkOutTime = System.currentTimeMillis(),
                        checkInTime = null,
                        photoPath = photoPath,
                        lastModified = System.currentTimeMillis()
                    )
                    checkoutRepository.insertCheckoutLog(checkoutLog)
                    
                    // Update item status
                    val updatedItem = it.copy(status = "Checked Out")
                    itemRepository.updateItem(updatedItem)
                }
            }
        }
    }
    
    /**
     * Check in an item
     */
    fun checkInItem(checkoutLogId: UUID) {
        viewModelScope.launch {
            // Get the checkout log
            val checkoutLog = checkoutRepository.getCheckoutLogById(checkoutLogId)
            
            // Only proceed if we have a valid checkout log without a check-in time
            checkoutLog?.let {
                if (it.checkInTime == null) {
                    // Update the checkout log with check-in time
                    checkoutRepository.checkInItem(it)
                    
                    // Get the item to update its status back to available
                    val item = itemRepository.getItemById(it.itemId)
                    item?.let { foundItem ->
                        val updatedItem = foundItem.copy(status = "Available")
                        itemRepository.updateItem(updatedItem)
                    }
                }
            }
        }
    }
    
    /**
     * Get checkout log by ID
     */
    suspend fun getCheckoutLogById(id: UUID): CheckoutLog? = 
        checkoutRepository.getCheckoutLogById(id)
    
    /**
     * Get current checkout for an item
     */
    suspend fun getCurrentCheckoutForItem(itemId: UUID): CheckoutLog? = 
        checkoutRepository.getCurrentCheckoutForItem(itemId)
    
    /**
     * Delete a checkout log
     */
    fun deleteCheckoutLog(checkoutLog: CheckoutLog) {
        viewModelScope.launch {
            checkoutRepository.deleteCheckoutLog(checkoutLog)
        }
    }
    
    /**
     * Factory for creating CheckoutViewModel with dependency injection
     */
    class Factory(
        private val checkoutRepository: CheckoutRepository,
        private val itemRepository: ItemRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
                return CheckoutViewModel(checkoutRepository, itemRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 