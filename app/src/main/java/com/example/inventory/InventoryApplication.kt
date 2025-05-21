package com.example.inventory

import android.app.Application
import com.example.inventory.data.AppContainer
import com.example.inventory.data.AppContainerImpl
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.inventory.api.AuthManager
import com.example.inventory.api.AuthNetworkModule
import com.example.inventory.api.DataPrefetcher
import com.example.inventory.api.OfflineCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.inventory.api.NetworkModule
import com.google.firebase.FirebaseApp

/**
 * Inventory Application
 * 
 * This application connects to Firebase Firestore for all data operations.
 * All inventory items, staff data, and checkout logs are stored in Firestore.
 * Supports offline operation with data caching.
 */
class InventoryApplication : Application(), LifecycleObserver {
    
    // AppContainer instance used by the rest of the app
    lateinit var container: AppContainer
    
    // Application scope for coroutines that should live as long as the application
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize the AppContainer with Firebase repositories
        container = AppContainerImpl(this)
        
        // Initialize the offline cache
        applicationScope.launch {
            OfflineCache.initialize(this@InventoryApplication)
        }
        
        // Initialize data prefetcher for performance optimization
        DataPrefetcher.initialize(this)
        
        // Register as lifecycle observer for application lifecycle events
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Log that we're running with Firebase Firestore
        Log.i("InventoryApp", "Starting with FIREBASE FIRESTORE - Includes offline support")
    }
    
    /**
     * Called when the application is terminating
     */
    override fun onTerminate() {
        super.onTerminate()
        // Shutdown any background processes
        // No shutdown method in OfflineCache anymore, so nothing to do here
    }
    
    /**
     * Called when the application moves to the background
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d("InventoryApp", "App in background")
        // Perform background sync
        applicationScope.launch {
            OfflineCache.attemptSync(this@InventoryApplication)
        }
    }
    
    /**
     * Called when the application comes to the foreground
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.d("InventoryApp", "App in foreground")
        // Perform sync on app resume
        applicationScope.launch {
            OfflineCache.attemptSync(this@InventoryApplication)
        }
    }
} 