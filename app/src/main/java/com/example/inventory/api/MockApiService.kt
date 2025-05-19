package com.example.inventory.api

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.UUID
import kotlin.random.Random

/**
 * Mock implementation of the API services for testing
 * 
 * This provides mock data for testing the app before connecting to the actual cloud backend.
 * It simulates network delays and can be configured to simulate failures for testing error handling.
 */
class MockApiService(private val context: Context) {
    
    private val TAG = "MockApiService"
    
    // Configuration for simulating network conditions
    var shouldFail = false
    var networkDelayMs = 500L
    var failureRate = 0 // percentage (0-100)
    
    // Gson for JSON parsing
    private val gson = Gson()
    
    // Mock data containers
    private var items: MutableList<ItemDto> = mutableListOf()
    private var staff: MutableList<StaffDto> = mutableListOf()
    private var checkoutLogs: MutableList<CheckoutLogDto> = mutableListOf()
    
    // Path to mock data file
    private val mockDataFile = "mock_data.json"
    
    /**
     * Load mock data from assets
     */
    init {
        try {
            val jsonString = context.assets.open(mockDataFile).bufferedReader().use { it.readText() }
            val mockData = gson.fromJson(jsonString, MockData::class.java)
            
            items = mockData.items.toMutableList()
            staff = mockData.staff.toMutableList()
            checkoutLogs = mockData.checkoutLogs.toMutableList()
            
            Log.d(TAG, "Mock data loaded: ${items.size} items, ${staff.size} staff, ${checkoutLogs.size} checkout logs")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading mock data", e)
        }
    }
    
    /**
     * Data classes for parsing the mock data JSON
     */
    data class MockData(
        val items: List<ItemDto>,
        val staff: List<StaffDto>,
        val checkoutLogs: List<CheckoutLogDto>
    )
    
    /**
     * Create a mock implementation of ItemApiService
     */
    fun createMockItemApiService(): ItemApiService {
        return object : ItemApiService {
            override suspend fun getAllItems(): List<ItemDto> {
                simulateNetworkConditions()
                return items
            }
            
            override suspend fun getItemById(id: String): ItemDto {
                simulateNetworkConditions()
                return items.find { it.id == id } 
                    ?: throw IOException("Item not found with ID: $id")
            }
            
            override suspend fun getItemByBarcode(barcode: String): ItemDto {
                simulateNetworkConditions()
                return items.find { it.barcode == barcode } 
                    ?: throw IOException("Item not found with barcode: $barcode")
            }
            
            override suspend fun createItem(item: ItemDto): ItemDto {
                simulateNetworkConditions()
                val newItem = item.copy(
                    id = item.id ?: UUID.randomUUID().toString(),
                    lastModified = System.currentTimeMillis()
                )
                items.add(newItem)
                return newItem
            }
            
            override suspend fun updateItem(id: String, item: ItemDto): ItemDto {
                simulateNetworkConditions()
                val index = items.indexOfFirst { it.id == id }
                if (index == -1) {
                    throw IOException("Item not found with ID: $id")
                }
                
                val updatedItem = item.copy(lastModified = System.currentTimeMillis())
                items[index] = updatedItem
                return updatedItem
            }
            
            override suspend fun updateItemStatus(id: String, statusUpdate: Map<String, String>): ItemDto {
                simulateNetworkConditions()
                val index = items.indexOfFirst { it.id == id }
                if (index == -1) {
                    throw IOException("Item not found with ID: $id")
                }
                
                val item = items[index]
                val updatedItem = item.copy(
                    status = statusUpdate["status"] ?: item.status,
                    lastModified = System.currentTimeMillis()
                )
                items[index] = updatedItem
                return updatedItem
            }
            
            override suspend fun archiveItem(id: String): ItemDto {
                simulateNetworkConditions()
                val index = items.indexOfFirst { it.id == id }
                if (index == -1) {
                    throw IOException("Item not found with ID: $id")
                }
                
                val item = items[index]
                val updatedItem = item.copy(
                    isActive = false,
                    lastModified = System.currentTimeMillis()
                )
                items[index] = updatedItem
                return updatedItem
            }
            
            override suspend fun unarchiveItem(id: String): ItemDto {
                simulateNetworkConditions()
                val index = items.indexOfFirst { it.id == id }
                if (index == -1) {
                    throw IOException("Item not found with ID: $id")
                }
                
                val item = items[index]
                val updatedItem = item.copy(
                    isActive = true,
                    lastModified = System.currentTimeMillis()
                )
                items[index] = updatedItem
                return updatedItem
            }
        }
    }
    
    /**
     * Create a mock implementation of StaffApiService
     */
    fun createMockStaffApiService(): StaffApiService {
        return object : StaffApiService {
            override suspend fun getAllStaff(): List<StaffDto> {
                simulateNetworkConditions()
                return staff
            }
            
            override suspend fun getStaffById(id: String): StaffDto {
                simulateNetworkConditions()
                return staff.find { it.id == id } 
                    ?: throw IOException("Staff not found with ID: $id")
            }
            
            override suspend fun createStaff(staffDto: StaffDto): StaffDto {
                simulateNetworkConditions()
                val newStaff = staffDto.copy(
                    id = staffDto.id ?: UUID.randomUUID().toString(),
                    lastModified = System.currentTimeMillis()
                )
                staff.add(newStaff)
                return newStaff
            }
            
            override suspend fun updateStaff(id: String, staffDto: StaffDto): StaffDto {
                simulateNetworkConditions()
                val index = staff.indexOfFirst { it.id == id }
                if (index == -1) {
                    throw IOException("Staff not found with ID: $id")
                }
                
                val updatedStaff = staffDto.copy(lastModified = System.currentTimeMillis())
                staff[index] = updatedStaff
                return updatedStaff
            }
            
            override suspend fun archiveStaff(id: String): StaffDto {
                simulateNetworkConditions()
                val index = staff.indexOfFirst { it.id == id }
                if (index == -1) {
                    throw IOException("Staff not found with ID: $id")
                }
                
                val staffMember = staff[index]
                val updatedStaff = staffMember.copy(
                    isActive = false,
                    lastModified = System.currentTimeMillis()
                )
                staff[index] = updatedStaff
                return updatedStaff
            }
            
            override suspend fun unarchiveStaff(id: String): StaffDto {
                simulateNetworkConditions()
                val index = staff.indexOfFirst { it.id == id }
                if (index == -1) {
                    throw IOException("Staff not found with ID: $id")
                }
                
                val staffMember = staff[index]
                val updatedStaff = staffMember.copy(
                    isActive = true,
                    lastModified = System.currentTimeMillis()
                )
                staff[index] = updatedStaff
                return updatedStaff
            }
        }
    }
    
    /**
     * Create a mock implementation of CheckoutApiService
     */
    fun createMockCheckoutApiService(): CheckoutApiService {
        return object : CheckoutApiService {
            override suspend fun getAllCheckoutLogs(): List<CheckoutLogDto> {
                simulateNetworkConditions()
                return checkoutLogs
            }
            
            override suspend fun getCheckoutLogsByItemId(itemId: String): List<CheckoutLogDto> {
                simulateNetworkConditions()
                return checkoutLogs.filter { it.itemId == itemId }
            }
            
            override suspend fun getCheckoutLogsByStaffId(staffId: String): List<CheckoutLogDto> {
                simulateNetworkConditions()
                return checkoutLogs.filter { it.staffId == staffId }
            }
            
            override suspend fun getCurrentCheckouts(): List<CheckoutLogDto> {
                simulateNetworkConditions()
                return checkoutLogs.filter { it.checkInTime == null }
            }
            
            override suspend fun createCheckoutLog(checkoutLog: CheckoutLogDto): CheckoutLogDto {
                simulateNetworkConditions()
                val newCheckout = checkoutLog.copy(
                    id = checkoutLog.id ?: UUID.randomUUID().toString(),
                    checkOutTime = checkoutLog.checkOutTime ?: System.currentTimeMillis(),
                    lastModified = System.currentTimeMillis()
                )
                checkoutLogs.add(newCheckout)
                
                // Update item status to "Checked Out"
                val itemIndex = items.indexOfFirst { it.id == checkoutLog.itemId }
                if (itemIndex != -1) {
                    items[itemIndex] = items[itemIndex].copy(
                        status = "Checked Out",
                        lastModified = System.currentTimeMillis()
                    )
                }
                
                return newCheckout
            }
            
            override suspend fun checkInItem(id: String, checkInData: Map<String, String>?): CheckoutLogDto {
                simulateNetworkConditions()
                val index = checkoutLogs.indexOfFirst { it.id == id }
                if (index == -1) {
                    throw IOException("Checkout log not found with ID: $id")
                }
                
                val checkout = checkoutLogs[index]
                val updatedCheckout = checkout.copy(
                    checkInTime = System.currentTimeMillis(),
                    lastModified = System.currentTimeMillis()
                )
                checkoutLogs[index] = updatedCheckout
                
                // Update item status to "Available"
                val itemIndex = items.indexOfFirst { it.id == checkout.itemId }
                if (itemIndex != -1) {
                    items[itemIndex] = items[itemIndex].copy(
                        status = "Available",
                        lastModified = System.currentTimeMillis()
                    )
                }
                
                return updatedCheckout
            }
        }
    }
    
    /**
     * Simulates network conditions like delays and errors
     */
    private suspend fun simulateNetworkConditions() {
        // Simulate network delay
        delay(networkDelayMs)
        
        // Check if we should simulate a failure
        if (shouldFail || (failureRate > 0 && Random.nextInt(100) < failureRate)) {
            throw IOException("Simulated network failure")
        }
    }
} 