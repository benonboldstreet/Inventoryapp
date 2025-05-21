package com.example.inventory.data.firebase

import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository
import com.example.inventory.data.repository.CheckoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to generate test data for Firestore database
 * 
 * This helps with testing the migration and ensuring the app works correctly
 * with Firebase Firestore.
 */
@Singleton
class FirestoreTestDataGenerator @Inject constructor(
    private val itemRepository: ItemRepository,
    private val staffRepository: StaffRepository,
    private val checkoutRepository: CheckoutRepository
) {
    /**
     * Generate a complete set of test data
     * 
     * @return A message indicating how many records were created
     */
    suspend fun generateTestData(): String = withContext(Dispatchers.IO) {
        val itemCount = generateTestItems()
        val staffCount = generateTestStaff()
        val checkoutCount = generateTestCheckouts()
        
        return@withContext "Created $itemCount items, $staffCount staff, and $checkoutCount checkouts"
    }
    
    /**
     * Generate test items
     * 
     * @return Number of items created
     */
    private suspend fun generateTestItems(): Int {
        val items = listOf(
            Item(
                idString = UUID.randomUUID().toString(),
                name = "MacBook Pro",
                category = "Electronics",
                type = "Laptop",
                barcode = "MB-PRO-001",
                condition = "Excellent",
                status = "Available"
            ),
            Item(
                idString = UUID.randomUUID().toString(),
                name = "iPad Pro",
                category = "Electronics",
                type = "Tablet",
                barcode = "IPAD-PRO-001",
                condition = "Good",
                status = "Available"
            ),
            Item(
                idString = UUID.randomUUID().toString(),
                name = "Projector",
                category = "Equipment",
                type = "Presentation",
                barcode = "PROJ-001",
                condition = "Fair",
                status = "Available"
            ),
            Item(
                idString = UUID.randomUUID().toString(),
                name = "Desk Chair",
                category = "Furniture",
                type = "Chair",
                barcode = "CHAIR-001",
                condition = "Good",
                status = "Available"
            ),
            Item(
                idString = UUID.randomUUID().toString(),
                name = "USB-C Cable",
                category = "Accessories",
                type = "Cable",
                barcode = "CABLE-001",
                condition = "Excellent",
                status = "Available"
            )
        )
        
        items.forEach { item ->
            itemRepository.insertItem(item)
        }
        
        return items.size
    }
    
    /**
     * Generate test staff records
     * 
     * @return Number of staff records created
     */
    private suspend fun generateTestStaff(): Int {
        val staffList = listOf(
            Staff(
                idString = UUID.randomUUID().toString(),
                name = "John Smith",
                department = "IT",
                email = "john.smith@example.com",
                phone = "555-1234",
                position = "IT Manager"
            ),
            Staff(
                idString = UUID.randomUUID().toString(),
                name = "Jane Doe",
                department = "Marketing",
                email = "jane.doe@example.com",
                phone = "555-5678",
                position = "Marketing Director"
            ),
            Staff(
                idString = UUID.randomUUID().toString(),
                name = "Bob Johnson",
                department = "Finance",
                email = "bob.johnson@example.com",
                phone = "555-9012",
                position = "Accountant"
            )
        )
        
        staffList.forEach { staff ->
            staffRepository.insertStaff(staff)
        }
        
        return staffList.size
    }
    
    /**
     * Generate test checkout logs
     * 
     * @return Number of checkout logs created
     */
    private suspend fun generateTestCheckouts(): Int {
        // Get the first few items and staff members to create checkouts for
        val items = try {
            val itemsList = mutableListOf<Item>()
            itemRepository.getAllItems().collect { itemList ->
                itemsList.addAll(itemList.take(3))
            }
            itemsList
        } catch (e: Exception) {
            emptyList()
        }
        
        val staff = try {
            val staffList = mutableListOf<Staff>()
            staffRepository.getAllStaff().collect { staffMembers ->
                staffList.addAll(staffMembers.take(2))
            }
            staffList
        } catch (e: Exception) {
            emptyList()
        }
        
        // If we don't have both items and staff, we can't create checkouts
        if (items.isEmpty() || staff.isEmpty()) {
            return 0
        }
        
        // Create checkouts - one active and one completed
        val checkouts = mutableListOf<CheckoutLog>()
        
        // Active checkout - first item checked out by first staff member
        if (items.isNotEmpty() && staff.isNotEmpty()) {
            val activeCheckout = CheckoutLog(
                idString = UUID.randomUUID().toString(),
                itemIdString = items[0].idString,
                staffIdString = staff[0].idString,
                checkOutTime = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000), // 3 days ago
                checkInTime = null,
                status = "CHECKED_OUT"
            )
            checkouts.add(activeCheckout)
            checkoutRepository.insertCheckoutLog(activeCheckout)
        }
        
        // Completed checkout - second item was checked out and returned
        if (items.size > 1 && staff.isNotEmpty()) {
            val completedCheckout = CheckoutLog(
                idString = UUID.randomUUID().toString(),
                itemIdString = items[1].idString,
                staffIdString = staff[0].idString,
                checkOutTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000), // 7 days ago
                checkInTime = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000), // 5 days ago
                status = "CHECKED_IN"
            )
            checkouts.add(completedCheckout)
            checkoutRepository.insertCheckoutLog(completedCheckout)
        }
        
        // Overdue checkout - third item was checked out but not returned for a long time
        if (items.size > 2 && staff.size > 1) {
            val overdueCheckout = CheckoutLog(
                idString = UUID.randomUUID().toString(),
                itemIdString = items[2].idString,
                staffIdString = staff[1].idString,
                checkOutTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000), // 30 days ago
                checkInTime = null,
                status = "OVERDUE"
            )
            checkouts.add(overdueCheckout)
            checkoutRepository.insertCheckoutLog(overdueCheckout)
        }
        
        return checkouts.size
    }
} 