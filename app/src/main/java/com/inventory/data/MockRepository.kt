package com.inventory.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inventory.model.Item
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import kotlin.random.Random

/**
 * Mock implementation of the Repository interface that reads data from a local JSON file.
 * Used for testing and development without requiring a real backend connection.
 */
class MockRepository(private val context: Context) : Repository {
    private val gson = Gson()
    private val jsonFileName = "mock_data.json"
    
    // Configuration for simulating network conditions
    private var shouldFail = false
    private var networkDelayMs = 1500L // 1.5 seconds delay by default
    
    /**
     * Data class representing the structure of our mock JSON database
     */
    private data class MockDatabase(val items: List<Item>)
    
    /**
     * Retrieves all items from the mock JSON database
     */
    override suspend fun getAllItems(): Flow<List<Item>> = flow {
        simulateNetworkConditions()
        val items = readItemsFromJson()
        emit(items)
    }
    
    /**
     * Gets an item by its ID
     */
    override suspend fun getItemById(id: String): Flow<Item?> = flow {
        simulateNetworkConditions()
        val items = readItemsFromJson()
        emit(items.find { it.id == id })
    }
    
    /**
     * Adds a new item to the mock database
     * Note: This doesn't actually persist the change to the JSON file
     * In a real implementation, you would want to save changes back to the file
     */
    override suspend fun addItem(item: Item): Flow<Boolean> = flow {
        simulateNetworkConditions()
        emit(true) // Simulates successful addition
    }
    
    /**
     * Updates an existing item
     * Note: This doesn't actually persist the change to the JSON file
     */
    override suspend fun updateItem(item: Item): Flow<Boolean> = flow {
        simulateNetworkConditions()
        emit(true) // Simulates successful update
    }
    
    /**
     * Deletes an item by ID
     * Note: This doesn't actually persist the change to the JSON file
     */
    override suspend fun deleteItem(id: String): Flow<Boolean> = flow {
        simulateNetworkConditions()
        emit(true) // Simulates successful deletion
    }
    
    /**
     * Reads the list of items from the JSON file
     */
    private fun readItemsFromJson(): List<Item> {
        val jsonString = context.assets.open(jsonFileName).bufferedReader().use { it.readText() }
        val type = object : TypeToken<MockDatabase>() {}.type
        return gson.fromJson<MockDatabase>(jsonString, type).items
    }
    
    /**
     * Simulates network conditions like delays and errors
     */
    private suspend fun simulateNetworkConditions() {
        delay(networkDelayMs)
        if (shouldFail) {
            throw IOException("Simulated network failure")
        }
    }
    
    /**
     * Testing function to configure if operations should fail
     */
    fun setFailureMode(shouldFail: Boolean) {
        this.shouldFail = shouldFail
    }
    
    /**
     * Testing function to configure network delay
     */
    fun setNetworkDelay(delayMs: Long) {
        this.networkDelayMs = delayMs
    }
    
    /**
     * Testing function to simulate random connectivity issues
     */
    fun simulateUnstableConnection(unstablePercentage: Int = 30) {
        shouldFail = Random.nextInt(100) < unstablePercentage
    }
} 