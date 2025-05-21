package com.example.inventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val checkoutRepository: CheckoutRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {
    
    // Direct access to repository - no mapping needed since we're using model objects
    val allCheckoutLogs: Flow<List<CheckoutLog>> = checkoutRepository.getAllCheckoutLogs()
    
    // Active checkouts (not checked in yet)
    val activeCheckouts: Flow<List<CheckoutLog>> = checkoutRepository.getCurrentCheckouts()
    
    // Completed checkouts (already checked in)
    val completedCheckouts: Flow<List<CheckoutLog>> = checkoutRepository.getAllCheckoutLogs()
        .map { logs -> logs.filter { it.getCheckInTimeAsLong() != null } }
    
    // Get checkout by ID
    fun getCheckoutById(id: UUID): Flow<CheckoutLog?> = flow {
        val log = checkoutRepository.getCheckoutLogById(id)
        emit(log)
    }
    
    // Get checkouts for a specific item
    fun getCheckoutsByItemId(itemId: UUID): Flow<List<CheckoutLog>> = 
        checkoutRepository.getCheckoutLogsByItemId(itemId)
    
    // Get checkouts for a specific staff member
    fun getCheckoutsByStaffId(staffId: UUID): Flow<List<CheckoutLog>> = 
        checkoutRepository.getCheckoutLogsByStaffId(staffId)
    
    // Get current checkout for specific item
    fun getCurrentCheckoutForItem(itemId: UUID): Flow<CheckoutLog?> = flow {
        val log = checkoutRepository.getCurrentCheckoutForItem(itemId)
        emit(log)
    }
    
    // Get checkout logs by item - convenience method for UI
    fun getCheckoutLogsByItem(itemId: UUID): Flow<List<CheckoutLog>> = 
        checkoutRepository.getCheckoutLogsByItemId(itemId)
    
    // Get checkout logs by staff - convenience method for UI
    fun getCheckoutLogsByStaff(staffId: UUID): Flow<List<CheckoutLog>> =
        checkoutRepository.getCheckoutLogsByStaffId(staffId)
    
    // Add a new checkout
    fun addCheckout(checkout: CheckoutLog) {
        viewModelScope.launch {
            checkoutRepository.insertCheckoutLog(checkout)
        }
    }
    
    // Update an existing checkout
    fun updateCheckout(checkout: CheckoutLog) {
        viewModelScope.launch {
            checkoutRepository.updateCheckoutLog(checkout)
        }
    }
    
    // Delete a checkout
    fun deleteCheckout(checkout: CheckoutLog) {
        viewModelScope.launch {
            checkoutRepository.deleteCheckoutLog(checkout)
        }
    }
    
    // Mark a checkout as complete
    fun completeCheckout(checkout: CheckoutLog) {
        viewModelScope.launch {
            val updatedCheckout = checkout.copy(
                checkInTime = com.google.firebase.Timestamp.now(),
                status = "CHECKED_IN"
            )
            checkoutRepository.updateCheckoutLog(updatedCheckout)
            
            // Also update the item status
            checkout.itemId.let { itemId ->
                val item = itemRepository.getItemById(itemId)
                item?.let { existingItem ->
                    val updatedItem = existingItem.copy(status = "Available")
                    itemRepository.updateItem(updatedItem)
                }
            }
        }
    }
    
    // Check out an item to a staff member
    fun checkOutItem(itemId: UUID, staffId: UUID) {
        viewModelScope.launch {
            // Get the item to update its status
            val item = itemRepository.getItemById(itemId)
            
            // Only proceed if the item exists and is available
            item?.let {
                if (it.status == "Available") {
                    // Create checkout log and update the item in the repository
                    val checkout = checkoutRepository.checkOutItem(itemId, staffId)
                    
                    // Update item status
                    val updatedItem = it.copy(
                        status = "Checked Out",
                        lastModified = System.currentTimeMillis()
                    )
                    itemRepository.updateItem(updatedItem)
                }
            }
        }
    }
    
    // Check in an item
    fun checkInItem(checkoutLogId: UUID) {
        viewModelScope.launch {
            // Get the checkout log
            val checkoutLog = checkoutRepository.getCheckoutLogById(checkoutLogId)
            
            // Only proceed if we have a valid checkout log without a check-in time
            checkoutLog?.let {
                if (it.getCheckInTimeAsLong() == null) {
                    // Update the checkout log with check-in time and update the repository
                    val updatedCheckout = checkoutRepository.checkInItem(it)
                    
                    // Get the item to update its status back to available
                    val item = itemRepository.getItemById(it.itemId)
                    item?.let { foundItem ->
                        val updatedItem = foundItem.copy(
                            status = "Available",
                            lastModified = System.currentTimeMillis()
                        )
                        itemRepository.updateItem(updatedItem)
                    }
                }
            }
        }
    }
    
    // Check out an item with a photo
    fun checkOutItemWithPhoto(itemId: UUID, staffId: UUID, photoPath: String?) {
        viewModelScope.launch {
            // Get the item to update its status
            val item = itemRepository.getItemById(itemId)
            
            // Only proceed if the item exists and is available
            item?.let {
                if (it.status == "Available") {
                    // Create checkout log with photo
                    if (photoPath != null) {
                        checkoutRepository.checkOutItemWithPhoto(itemId, staffId, photoPath)
                    } else {
                        checkoutRepository.checkOutItem(itemId, staffId)
                    }
                    
                    // Update item status
                    val updatedItem = it.copy(
                        status = "Checked Out",
                        lastModified = System.currentTimeMillis()
                    )
                    itemRepository.updateItem(updatedItem)
                }
            }
        }
    }
    
    /**
     * Factory for creating CheckoutViewModel instances with dependencies
     */
    companion object {
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
} 