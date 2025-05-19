package com.example.inventory.api

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Data Prefetcher
 * 
 * Optimizes performance by pre-loading frequently accessed data in the background.
 * This reduces wait times for users when they navigate to common screens.
 */
object DataPrefetcher : DefaultLifecycleObserver {
    private const val TAG = "DataPrefetcher"
    
    private val prefetchScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var initialized = false
    
    // Track whether we've prefetched data this session
    private var hasPrefetchedItems = false
    private var hasPrefetchedStaff = false
    private var hasPrefetchedCurrentCheckouts = false
    
    /**
     * Initialize the prefetcher
     */
    fun initialize(context: Context) {
        if (initialized) return
        
        // Register for lifecycle events
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        initialized = true
        
        Log.d(TAG, "Data prefetcher initialized")
    }
    
    /**
     * Trigger prefetching when app comes to foreground
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        
        // Only prefetch if user is logged in
        if (AuthManager.isLoggedIn.value) {
            prefetchData()
        }
    }
    
    /**
     * Prefetch commonly accessed data
     */
    fun prefetchData() {
        prefetchScope.launch {
            try {
                Log.d(TAG, "Starting data prefetch")
                
                // Prefetch items if not already prefetched
                if (!hasPrefetchedItems) {
                    prefetchItems()
                }
                
                // Prefetch staff if not already prefetched
                if (!hasPrefetchedStaff) {
                    prefetchStaff()
                }
                
                // Always prefetch current checkouts as they change frequently
                prefetchCurrentCheckouts()
                
                Log.d(TAG, "Data prefetch completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during data prefetch", e)
            }
        }
    }
    
    /**
     * Clear prefetch flags when user logs out
     */
    fun clearPrefetchFlags() {
        hasPrefetchedItems = false
        hasPrefetchedStaff = false
        hasPrefetchedCurrentCheckouts = false
    }
    
    /**
     * Prefetch all items
     */
    private suspend fun prefetchItems() {
        try {
            withContext(Dispatchers.IO) {
                // Retrieve all items
                NetworkModule.itemApiService.getAllItems()
                
                // Mark as prefetched
                hasPrefetchedItems = true
                Log.d(TAG, "Items prefetched successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error prefetching items", e)
        }
    }
    
    /**
     * Prefetch all staff
     */
    private suspend fun prefetchStaff() {
        try {
            withContext(Dispatchers.IO) {
                // Retrieve all staff
                NetworkModule.staffApiService.getAllStaff()
                
                // Mark as prefetched
                hasPrefetchedStaff = true
                Log.d(TAG, "Staff prefetched successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error prefetching staff", e)
        }
    }
    
    /**
     * Prefetch current checkouts
     */
    private suspend fun prefetchCurrentCheckouts() {
        try {
            withContext(Dispatchers.IO) {
                // Retrieve current checkouts
                NetworkModule.checkoutApiService.getCurrentCheckouts()
                
                // Mark as prefetched
                hasPrefetchedCurrentCheckouts = true
                Log.d(TAG, "Current checkouts prefetched successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error prefetching current checkouts", e)
        }
    }
} 