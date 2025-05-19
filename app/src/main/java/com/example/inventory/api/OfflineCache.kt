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

/**
 * Offline Cache for data persistence when the app is offline
 * 
 * This class provides a mechanism to store and retrieve data when the device
 * is offline and sync it when the connection is restored.
 */
object OfflineCache {
    private val TAG = "OfflineCache"
    
    // In-memory cache for quick access
    private val itemCache = ConcurrentHashMap<UUID, Item>()
    private val staffCache = ConcurrentHashMap<UUID, Staff>()
    private val checkoutCache = ConcurrentHashMap<UUID, CheckoutLog>()
    
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
                    loadedItems.forEach { (key, item) ->
                        itemCache[UUID.fromString(key)] = item
                    }
                }
                
                // Load staff
                preferences[STAFF_KEY]?.let { staffJson ->
                    val staffType = object : TypeToken<Map<String, Staff>>() {}.type
                    val loadedStaff: Map<String, Staff> = gson.fromJson(staffJson, staffType)
                    loadedStaff.forEach { (key, staff) ->
                        staffCache[UUID.fromString(key)] = staff
                    }
                }
                
                // Load checkouts
                preferences[CHECKOUTS_KEY]?.let { checkoutsJson ->
                    val checkoutsType = object : TypeToken<Map<String, CheckoutLog>>() {}.type
                    val loadedCheckouts: Map<String, CheckoutLog> = gson.fromJson(checkoutsJson, checkoutsType)
                    loadedCheckouts.forEach { (key, checkout) ->
                        checkoutCache[UUID.fromString(key)] = checkout
                    }
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
                val itemsJson = gson.toJson(itemCache.mapKeys { it.key.toString() })
                preferences[ITEMS_KEY] = itemsJson
                
                // Save staff
                val staffJson = gson.toJson(staffCache.mapKeys { it.key.toString() })
                preferences[STAFF_KEY] = staffJson
                
                // Save checkouts
                val checkoutsJson = gson.toJson(checkoutCache.mapKeys { it.key.toString() })
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
    fun getCachedItem(id: UUID): Item? = itemCache[id]
    
    /**
     * Get cached staff by ID
     */
    fun getCachedStaff(id: UUID): Staff? = staffCache[id]
    
    /**
     * Get cached checkout by ID
     */
    fun getCachedCheckout(id: UUID): CheckoutLog? = checkoutCache[id]
    
    /**
     * Cache an item
     */
    suspend fun cacheItem(item: Item, context: Context) {
        itemCache[item.id] = item
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache a staff member
     */
    suspend fun cacheStaff(staff: Staff, context: Context) {
        staffCache[staff.id] = staff
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache a checkout log
     */
    suspend fun cacheCheckout(checkout: CheckoutLog, context: Context) {
        checkoutCache[checkout.id] = checkout
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache a batch of items
     */
    suspend fun cacheItems(items: List<Item>, context: Context) {
        items.forEach { item -> itemCache[item.id] = item }
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache a batch of staff members
     */
    suspend fun cacheStaff(staffList: List<Staff>, context: Context) {
        staffList.forEach { staff -> staffCache[staff.id] = staff }
        saveToPersistentStorage(context)
    }
    
    /**
     * Cache a batch of checkout logs
     */
    suspend fun cacheCheckouts(checkouts: List<CheckoutLog>, context: Context) {
        checkouts.forEach { checkout -> checkoutCache[checkout.id] = checkout }
        saveToPersistentStorage(context)
    }
    
    /**
     * Add a pending operation to be synced when online
     */
    suspend fun addPendingOperation(operation: PendingOperation, context: Context) {
        pendingOperations[operation.id] = operation
        saveToPersistentStorage(context)
    }
    
    /**
     * Get all pending operations
     */
    fun getPendingOperations(): List<PendingOperation> = pendingOperations.values.toList()
    
    /**
     * Remove a pending operation once it's been synced
     */
    suspend fun removePendingOperation(operationId: String, context: Context) {
        pendingOperations.remove(operationId)
        saveToPersistentStorage(context)
    }
    
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
            // Sort operations by priority (highest first) and then by timestamp
            val operations = pendingOperations.values
                .sortedWith(compareByDescending<PendingOperation> { it.priority }
                    .thenBy { it.timestamp })
                .toList()
            
            var successCount = 0
            val maxRetries = 3
            
            for (operation in operations) {
                var retryCount = 0
                var success = false
                
                while (!success && retryCount < maxRetries) {
                    success = when (operation.type) {
                        OperationType.INSERT_ITEM -> syncInsertItem(operation)
                        OperationType.UPDATE_ITEM -> syncUpdateItem(operation)
                        OperationType.DELETE_ITEM -> syncDeleteItem(operation)
                        OperationType.INSERT_STAFF -> syncInsertStaff(operation)
                        OperationType.UPDATE_STAFF -> syncUpdateStaff(operation)
                        OperationType.DELETE_STAFF -> syncDeleteStaff(operation)
                        OperationType.INSERT_CHECKOUT -> syncInsertCheckout(operation)
                        OperationType.UPDATE_CHECKOUT -> syncUpdateCheckout(operation)
                        OperationType.CHECK_IN_ITEM -> syncCheckInItem(operation)
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
                    pendingOperations.remove(operation.id)
                    successCount++
                } else {
                    Log.e(TAG, "Failed to sync operation after $maxRetries retries: ${operation.type}")
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
     * Sync a pending insert item operation
     */
    private suspend fun syncInsertItem(operation: PendingOperation): Boolean {
        try {
            val item = gson.fromJson(operation.data, Item::class.java)
            NetworkModule.itemApiService.createItem(item.toDto())
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing insert item operation", e)
            return false
        }
    }
    
    /**
     * Sync a pending update item operation
     */
    private suspend fun syncUpdateItem(operation: PendingOperation): Boolean {
        try {
            val item = gson.fromJson(operation.data, Item::class.java)
            NetworkModule.itemApiService.updateItem(item.id.toString(), item.toDto())
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing update item operation", e)
            return false
        }
    }
    
    /**
     * Sync a pending delete item operation
     */
    private suspend fun syncDeleteItem(operation: PendingOperation): Boolean {
        try {
            val item = gson.fromJson(operation.data, Item::class.java)
            NetworkModule.itemApiService.updateItemStatus(item.id.toString(), mapOf("isActive" to "false"))
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing delete item operation", e)
            return false
        }
    }
    
    /**
     * Sync a pending insert staff operation
     */
    private suspend fun syncInsertStaff(operation: PendingOperation): Boolean {
        try {
            val staff = gson.fromJson(operation.data, Staff::class.java)
            NetworkModule.staffApiService.createStaff(staff.toDto())
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing insert staff operation", e)
            return false
        }
    }
    
    /**
     * Sync a pending update staff operation
     */
    private suspend fun syncUpdateStaff(operation: PendingOperation): Boolean {
        try {
            val staff = gson.fromJson(operation.data, Staff::class.java)
            NetworkModule.staffApiService.updateStaff(staff.id.toString(), staff.toDto())
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing update staff operation", e)
            return false
        }
    }
    
    /**
     * Sync a pending delete staff operation
     */
    private suspend fun syncDeleteStaff(operation: PendingOperation): Boolean {
        try {
            val staff = gson.fromJson(operation.data, Staff::class.java)
            NetworkModule.staffApiService.archiveStaff(staff.id.toString())
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing delete staff operation", e)
            return false
        }
    }
    
    /**
     * Sync a pending insert checkout operation
     */
    private suspend fun syncInsertCheckout(operation: PendingOperation): Boolean {
        try {
            val checkout = gson.fromJson(operation.data, CheckoutLog::class.java)
            NetworkModule.checkoutApiService.createCheckoutLog(checkout.toDto())
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing insert checkout operation", e)
            return false
        }
    }
    
    /**
     * Sync a pending update checkout operation
     */
    private suspend fun syncUpdateCheckout(operation: PendingOperation): Boolean {
        try {
            val checkout = gson.fromJson(operation.data, CheckoutLog::class.java)
            NetworkModule.checkoutApiService.createCheckoutLog(checkout.toDto())
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing update checkout operation", e)
            return false
        }
    }
    
    /**
     * Sync a pending check in item operation
     */
    private suspend fun syncCheckInItem(operation: PendingOperation): Boolean {
        try {
            val checkout = gson.fromJson(operation.data, CheckoutLog::class.java)
            NetworkModule.checkoutApiService.checkInItem(
                checkout.id.toString(),
                mapOf("checkInTime" to checkout.checkInTime.toString())
            )
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing check in item operation", e)
            return false
        }
    }
    
    /**
     * Register a sync status listener
     */
    fun addSyncListener(listener: (SyncStatus) -> Unit) {
        syncListeners.add(listener)
        listener(currentSyncStatus)
    }
    
    /**
     * Remove a sync status listener
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
     * Clear all cached data (typically used for logout)
     */
    suspend fun clearCache(context: Context) {
        itemCache.clear()
        staffCache.clear()
        checkoutCache.clear()
        pendingOperations.clear()
        saveToPersistentStorage(context)
    }
    
    /**
     * Shutdown the sync scheduler (typically called in onDestroy)
     */
    fun shutdown() {
        if (::syncScheduler.isInitialized && !syncScheduler.isShutdown) {
            syncScheduler.shutdown()
        }
    }
}

/**
 * Pending operation for offline actions
 */
data class PendingOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val data: String,  // JSON string of the entity
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = 0  // Higher number means higher priority
)

/**
 * Types of operations that can be performed offline
 */
enum class OperationType {
    INSERT_ITEM,
    UPDATE_ITEM,
    DELETE_ITEM,
    INSERT_STAFF,
    UPDATE_STAFF,
    DELETE_STAFF,
    INSERT_CHECKOUT,
    UPDATE_CHECKOUT,
    CHECK_IN_ITEM
}

/**
 * Sync status enum
 */
enum class SyncStatus {
    IDLE,
    IN_PROGRESS,
    COMPLETE,
    PARTIAL,
    FAILED,
    OFFLINE
} 