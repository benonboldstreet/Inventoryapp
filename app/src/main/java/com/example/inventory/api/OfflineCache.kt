package com.example.inventory.api

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.ui.viewmodel.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import com.example.inventory.data.model.PendingOperation
import com.example.inventory.data.model.OperationType
import com.example.inventory.data.model.SyncStatus

/**
 * Offline Cache for data persistence when the app is offline
 * 
 * This class provides a mechanism to store and retrieve data when the device
 * is offline and sync it when the connection is restored.
 */
object OfflineCache {
    private val TAG = "OfflineCache"
    
    // In-memory cache for quick access
    private val itemCache = ConcurrentHashMap<String, Item>()
    private val staffCache = ConcurrentHashMap<String, Staff>()
    private val checkoutCache = ConcurrentHashMap<String, CheckoutLog>()
    
    // Pending operations to sync when online
    private val pendingOperations = ConcurrentHashMap<String, PendingOperation>()
    
    // Gson for serialization/deserialization
    private val gson = Gson()
    
    // DataStore for persistent storage
    private val Context.dataStore by preferencesDataStore(name = "offline_cache")
    
    // Keys for DataStore
    private val ITEMS_KEY = stringPreferencesKey("cached_items")
    private val STAFF_KEY = stringPreferencesKey("cached_staff")
    private val CHECKOUTS_KEY = stringPreferencesKey("cached_checkouts")
    private val PENDING_OPS_KEY = stringPreferencesKey("pending_operations")
    
    // Scheduler for periodic sync attempts
    private lateinit var syncScheduler: ScheduledExecutorService
    
    // Sync listeners
    private val syncListeners = mutableListOf<(SyncStatus) -> Unit>()
    
    // Current sync status
    private var currentSyncStatus = SyncStatus.IDLE
    
    /**
     * Initialize the cache with application context
     * This should be called from the Application class
     */
    fun initialize(context: Context) {
        // Load any previously cached data from persistent storage
        kotlinx.coroutines.runBlocking {
            loadFromPersistentStorage(context)
        }
        
        // Set up periodic sync attempts when online
        setupSyncScheduler(context)
    }
    
    /**
     * Set up the sync scheduler to attempt sync periodically
     */
    private fun setupSyncScheduler(context: Context) {
        syncScheduler = Executors.newSingleThreadScheduledExecutor()
        syncScheduler.scheduleAtFixedRate({
            if (SharedViewModel.isCloudConnected.value && pendingOperations.isNotEmpty()) {
                kotlinx.coroutines.runBlocking {
                    attemptSync(context)
                }
            }
        }, 1, 15, TimeUnit.MINUTES)
    }
    
    /**
     * Load cached data from persistent storage
     */
    private suspend fun loadFromPersistentStorage(context: Context) {
        try {
            context.dataStore.data.first().let { preferences ->
                // Load items
                preferences[ITEMS_KEY]?.let { itemsJson ->
                    val itemsType = object : TypeToken<Map<String, Item>>() {}.type
                    val loadedItems: Map<String, Item> = gson.fromJson(itemsJson, itemsType)
                    itemCache.putAll(loadedItems)
                }
                
                // Load staff
                preferences[STAFF_KEY]?.let { staffJson ->
                    val staffType = object : TypeToken<Map<String, Staff>>() {}.type
                    val loadedStaff: Map<String, Staff> = gson.fromJson(staffJson, staffType)
                    staffCache.putAll(loadedStaff)
                }
                
                // Load checkouts
                preferences[CHECKOUTS_KEY]?.let { checkoutsJson ->
                    val checkoutsType = object : TypeToken<Map<String, CheckoutLog>>() {}.type
                    val loadedCheckouts: Map<String, CheckoutLog> = gson.fromJson(checkoutsJson, checkoutsType)
                    checkoutCache.putAll(loadedCheckouts)
                }
                
                // Load pending operations
                preferences[PENDING_OPS_KEY]?.let { pendingOpsJson ->
                    val pendingOpsType = object : TypeToken<Map<String, PendingOperation>>() {}.type
                    val loadedOps: Map<String, PendingOperation> = gson.fromJson(pendingOpsJson, pendingOpsType)
                    pendingOperations.putAll(loadedOps)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached data", e)
        }
    }
    
    /**
     * Save current cache to persistent storage
     */
    private suspend fun saveToPersistentStorage(context: Context) {
        try {
            context.dataStore.edit { preferences ->
                // Save items
                val itemsJson = gson.toJson(itemCache)
                preferences[ITEMS_KEY] = itemsJson
                
                // Save staff
                val staffJson = gson.toJson(staffCache)
                preferences[STAFF_KEY] = staffJson
                
                // Save checkouts
                val checkoutsJson = gson.toJson(checkoutCache)
                preferences[CHECKOUTS_KEY] = checkoutsJson
                
                // Save pending operations
                val pendingOpsJson = gson.toJson(pendingOperations)
                preferences[PENDING_OPS_KEY] = pendingOpsJson
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cached data", e)
        }
    }
    
    /**
     * Get all cached items
     */
    fun getCachedItems(): List<Item> = itemCache.values.toList()
    
    /**
     * Get all cached staff
     */
    fun getCachedStaff(): List<Staff> = staffCache.values.toList()
    
    /**
     * Get all cached checkouts
     */
    fun getCachedCheckouts(): List<CheckoutLog> = checkoutCache.values.toList()
    
    /**
     * Get cached item by ID
     */
    fun getCachedItem(id: UUID): Item? = itemCache[id.toString()]
    
    /**
     * Get cached staff by ID
     */
    fun getCachedStaff(id: UUID): Staff? = staffCache[id.toString()]
    
    /**
     * Get cached checkout by ID
     */
    fun getCachedCheckout(id: UUID): CheckoutLog? = checkoutCache[id.toString()]
    
    /**
     * Cache an item
     */
    suspend fun cacheItem(item: Item, context: Context) {
        itemCache[item.idString] = item
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache multiple items
     */
    suspend fun cacheItems(items: List<Item>, context: Context) {
        items.forEach { item ->
            itemCache[item.idString] = item
        }
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache a staff member
     */
    suspend fun cacheStaff(staff: Staff, context: Context) {
        staffCache[staff.idString] = staff
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache multiple staff members
     */
    suspend fun cacheStaffList(staffList: List<Staff>, context: Context) {
        staffList.forEach { staff ->
            staffCache[staff.idString] = staff
        }
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache a checkout log
     */
    suspend fun cacheCheckoutLog(checkout: CheckoutLog, context: Context) {
        checkoutCache[checkout.idString] = checkout
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache multiple checkout logs
     */
    suspend fun cacheCheckoutLogs(checkouts: List<CheckoutLog>, context: Context) {
        checkouts.forEach { checkout ->
            checkoutCache[checkout.idString] = checkout
        }
        saveToPersistentStorage(context)
    }
    
    /**
     * Add a pending operation to be synced when online
     */
    suspend fun addPendingOperation(operation: PendingOperation, context: Context) {
        pendingOperations[operation.id.toString()] = operation
        saveToPersistentStorage(context)
        
        // Attempt to sync immediately if online
        if (SharedViewModel.isCloudConnected.value) {
            attemptSync(context)
        }
    }
    
    /**
     * Register a listener for sync status updates
     */
    fun addSyncListener(listener: (SyncStatus) -> Unit) {
        syncListeners.add(listener)
    }
    
    /**
     * Unregister a sync listener
     */
    fun removeSyncListener(listener: (SyncStatus) -> Unit) {
        syncListeners.remove(listener)
    }
    
    /**
     * Update the sync status and notify listeners
     */
    private fun updateSyncStatus(status: SyncStatus) {
        currentSyncStatus = status
        syncListeners.forEach { it(status) }
    }
    
    /**
     * Get the current sync status
     */
    fun getCurrentSyncStatus(): SyncStatus = currentSyncStatus
    
    /**
     * Attempt to sync pending operations with the cloud
     */
    suspend fun attemptSync(context: Context) {
        if (!SharedViewModel.isCloudConnected.value) {
            Log.d(TAG, "Cannot sync - offline")
            updateSyncStatus(SyncStatus.OFFLINE)
            return
        }
        
        if (pendingOperations.isEmpty()) {
            Log.d(TAG, "No pending operations to sync")
            updateSyncStatus(SyncStatus.COMPLETE)
            return
        }
        
        updateSyncStatus(SyncStatus.IN_PROGRESS)
        
        try {
            // Sort operations by timestamp (oldest first)
            val operations = pendingOperations.values.toList()
            
            var successCount = 0
            val maxRetries = 3
            
            for (operation in operations) {
                var retryCount = 0
                var success = false
                
                while (!success && retryCount < maxRetries) {
                    success = when (operation.operationType) {
                        OperationType.CREATE -> syncCreateOperation(operation)
                        OperationType.UPDATE -> syncUpdateOperation(operation)
                        OperationType.DELETE -> syncDeleteOperation(operation)
                        else -> false
                    }
                    
                    if (!success) {
                        retryCount++
                        if (retryCount < maxRetries) {
                            // Exponential backoff
                            kotlinx.coroutines.delay(1000L * (1 shl retryCount))
                        }
                    }
                }
                
                if (success) {
                    pendingOperations.remove(operation.id.toString())
                    successCount++
                } else {
                    Log.e(TAG, "Failed to sync operation after $maxRetries retries: ${operation.operationType}")
                }
            }
            
            // Save updated pending operations list
            saveToPersistentStorage(context)
            
            if (successCount == operations.size) {
                updateSyncStatus(SyncStatus.COMPLETE)
                Log.d(TAG, "Sync complete - all operations successful")
            } else {
                updateSyncStatus(SyncStatus.PARTIAL)
                Log.d(TAG, "Sync partial - $successCount/${operations.size} operations successful")
            }
        } catch (e: Exception) {
            updateSyncStatus(SyncStatus.FAILED)
            Log.e(TAG, "Sync failed", e)
        }
    }
    
    /**
     * Sync a CREATE operation (for any entity type)
     */
    private suspend fun syncCreateOperation(operation: PendingOperation): Boolean {
        try {
            when (operation.collectionName) {
                "items" -> {
                    val item = gson.fromJson(operation.data, Item::class.java)
                    NetworkModule.itemApiService.createItem(item.toNetworkDto())
                }
                "staff" -> {
                    val staff = gson.fromJson(operation.data, Staff::class.java)
                    NetworkModule.staffApiService.createStaff(staff.toNetworkDto())
                }
                "checkouts" -> {
                    val checkout = gson.fromJson(operation.data, CheckoutLog::class.java)
                    NetworkModule.checkoutApiService.createCheckoutLog(checkout.toNetworkDto())
                }
                else -> return false
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing CREATE operation for ${operation.collectionName}", e)
            return false
        }
    }
    
    /**
     * Sync an UPDATE operation (for any entity type)
     */
    private suspend fun syncUpdateOperation(operation: PendingOperation): Boolean {
        try {
            when (operation.collectionName) {
                "items" -> {
                    val item = gson.fromJson(operation.data, Item::class.java)
                    NetworkModule.itemApiService.updateItem(item.idString, item.toNetworkDto())
                }
                "staff" -> {
                    val staff = gson.fromJson(operation.data, Staff::class.java)
                    NetworkModule.staffApiService.updateStaff(staff.idString, staff.toNetworkDto())
                }
                "checkouts" -> {
                    val checkout = gson.fromJson(operation.data, CheckoutLog::class.java)
                    NetworkModule.checkoutApiService.createCheckoutLog(checkout.toNetworkDto())
                }
                else -> return false
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing UPDATE operation for ${operation.collectionName}", e)
            return false
        }
    }
    
    /**
     * Sync a DELETE operation (for any entity type)
     */
    private suspend fun syncDeleteOperation(operation: PendingOperation): Boolean {
        try {
            when (operation.collectionName) {
                "items" -> {
                    val item = gson.fromJson(operation.data, Item::class.java)
                    NetworkModule.itemApiService.archiveItem(item.idString)
                }
                "staff" -> {
                    val staff = gson.fromJson(operation.data, Staff::class.java)
                    NetworkModule.staffApiService.archiveStaff(staff.idString)
                }
                "checkouts" -> {
                    // For checkouts, we don't delete but mark as completed
                    val checkout = gson.fromJson(operation.data, CheckoutLog::class.java)
                    NetworkModule.checkoutApiService.checkInItem(checkout.idString, mapOf("status" to "COMPLETE"))
                }
                else -> return false
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing DELETE operation for ${operation.collectionName}", e)
            return false
        }
    }
}

/**
 * Class representing a pending operation for offline support
 */
// This class has been moved to com.example.inventory.data.model.PendingOperation

/**
 * Types of operations supported for offline caching
 */
// This enum has been moved to com.example.inventory.data.model.OperationType

/**
 * Status of a sync operation
 */
// This enum has been moved to com.example.inventory.data.model.SyncStatus 