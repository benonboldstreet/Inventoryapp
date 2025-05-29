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
                id = UUID.randomUUID(),
                name = "MacBook Pro",
                category = "Electronics",
                type = "Laptop",
                barcode = "MB-PRO-001",
                condition = "Excellent",
                status = "Available",
                description = "13-inch, M1 chip, 16GB RAM",
                isActive = true,
                lastModified = System.currentTimeMillis(),
                photoPath = null
            ),
            Item(
                id = UUID.randomUUID(),
                name = "iPad Pro",
                category = "Electronics",
                type = "Tablet",
                barcode = "IPAD-PRO-001",
                condition = "Good",
                status = "Available",
                description = "11-inch, M1 chip, 128GB",
                isActive = true,
                lastModified = System.currentTimeMillis(),
                photoPath = null
            ),
            Item(
                id = UUID.randomUUID(),
                name = "Projector",
                category = "Equipment",
                type = "Presentation",
                barcode = "PROJ-001",
                condition = "Fair",
                status = "Available",
                description = "1080p, HDMI, 3000 lumens",
                isActive = true,
                lastModified = System.currentTimeMillis(),
                photoPath = null
            ),
            Item(
                id = UUID.randomUUID(),
                name = "Desk Chair",
                category = "Furniture",
                type = "Chair",
                barcode = "CHAIR-001",
                condition = "Good",
                status = "Available",
                description = "Ergonomic office chair",
                isActive = true,
                lastModified = System.currentTimeMillis(),
                photoPath = null
            ),
            Item(
                id = UUID.randomUUID(),
                name = "USB-C Cable",
                category = "Accessories",
                type = "Cable",
                barcode = "CABLE-001",
                condition = "Excellent",
                status = "Available",
                description = "2m length, charging and data",
                isActive = true,
                lastModified = System.currentTimeMillis(),
                photoPath = null
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
                id = UUID.randomUUID(),
                name = "John Smith",
                department = "IT",
                email = "john.smith@example.com",
                phone = "555-1234",
                position = "IT Manager",
                isActive = true,
                lastModified = System.currentTimeMillis(),
                firebaseUid = null,
                role = "User",
                photoPath = null
            ),
            Staff(
                id = UUID.randomUUID(),
                name = "Jane Doe",
                department = "Marketing",
                email = "jane.doe@example.com",
                phone = "555-5678",
                position = "Marketing Director",
                isActive = true,
                lastModified = System.currentTimeMillis(),
                firebaseUid = null,
                role = "User",
                photoPath = null
            ),
            Staff(
                id = UUID.randomUUID(),
                name = "Bob Johnson",
                department = "Finance",
                email = "bob.johnson@example.com",
                phone = "555-9012",
                position = "Accountant",
                isActive = true,
                lastModified = System.currentTimeMillis(),
                firebaseUid = null,
                role = "User",
                photoPath = null
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
                id = UUID.randomUUID(),
                itemId = items[0].id,
                staffId = staff[0].id,
                checkoutTimestamp = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000), // 3 days ago
                checkinTimestamp = null,
                checkoutPhotoPath = null,
                checkinPhotoPath = null,
                notes = "Active checkout for testing"
            )
            checkouts.add(activeCheckout)
            checkoutRepository.insertCheckoutLog(activeCheckout)
        }
        
        // Completed checkout - second item was checked out and returned
        if (items.size > 1 && staff.isNotEmpty()) {
            val completedCheckout = CheckoutLog(
                id = UUID.randomUUID(),
                itemId = items[1].id,
                staffId = staff[0].id,
                checkoutTimestamp = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000), // 7 days ago
                checkinTimestamp = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000), // 5 days ago
                checkoutPhotoPath = null,
                checkinPhotoPath = null,
                notes = "Completed checkout for testing"
            )
            checkouts.add(completedCheckout)
            checkoutRepository.insertCheckoutLog(completedCheckout)
        }
        
        // Overdue checkout - third item was checked out but not returned for a long time
        if (items.size > 2 && staff.size > 1) {
            val overdueCheckout = CheckoutLog(
                id = UUID.randomUUID(),
                itemId = items[2].id,
                staffId = staff[1].id,
                checkoutTimestamp = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000), // 30 days ago
                checkinTimestamp = null,
                checkoutPhotoPath = null,
                checkinPhotoPath = null,
                notes = "Overdue checkout for testing"
            )
            checkouts.add(overdueCheckout)
            checkoutRepository.insertCheckoutLog(overdueCheckout)
        }
        
        return checkouts.size
    }

    /**
     * Generate test checkout logs
     */
    private fun generateCheckoutLogs(): List<CheckoutLog> {
        val checkoutLogs = mutableListOf<CheckoutLog>()
        
        // Checkout log 1 - checked out
        checkoutLogs.add(
            CheckoutLog(
                id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                itemId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                staffId = UUID.fromString("10000000-0000-0000-0000-000000000001"),
                checkoutTimestamp = System.currentTimeMillis() - (86400000 * 2), // 2 days ago
                checkinTimestamp = null,
                checkoutPhotoPath = "checkouts/11111111-1111-1111-1111-111111111111_checkout.jpg",
                checkinPhotoPath = null,
                notes = "Borrowed for classroom use"
            )
        )
        
        // Checkout log 2 - checked in
        checkoutLogs.add(
            CheckoutLog(
                id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                itemId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                staffId = UUID.fromString("10000000-0000-0000-0000-000000000002"),
                checkoutTimestamp = System.currentTimeMillis() - (86400000 * 5), // 5 days ago
                checkinTimestamp = System.currentTimeMillis() - (86400000 * 3), // 3 days ago
                checkoutPhotoPath = "checkouts/22222222-2222-2222-2222-222222222222_checkout.jpg",
                checkinPhotoPath = "checkouts/22222222-2222-2222-2222-222222222222_checkin.jpg",
                notes = "Used for demonstration"
            )
        )
        
        // Checkout log 3 - checked out long time ago
        checkoutLogs.add(
            CheckoutLog(
                id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
                itemId = UUID.fromString("00000000-0000-0000-0000-000000000003"),
                staffId = UUID.fromString("10000000-0000-0000-0000-000000000003"),
                checkoutTimestamp = System.currentTimeMillis() - (86400000 * 20), // 20 days ago
                checkinTimestamp = null,
                checkoutPhotoPath = null,
                checkinPhotoPath = null,
                notes = "Borrowed for long-term project"
            )
        )
        
        return checkoutLogs
    }
} 