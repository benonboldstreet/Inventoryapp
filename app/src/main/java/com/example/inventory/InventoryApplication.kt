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
        try {
            Log.i("InventoryApp", "Initializing Firebase...")
            FirebaseApp.initializeApp(this)
            Log.i("InventoryApp", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("InventoryApp", "ERROR initializing Firebase: ${e.message}", e)
        }
        
        // Initialize the AppContainer with Firebase repositories
        try {
            Log.i("InventoryApp", "Initializing AppContainer...")
            container = AppContainerImpl(this)
            Log.i("InventoryApp", "AppContainer initialized successfully")
        } catch (e: Exception) {
            Log.e("InventoryApp", "ERROR initializing AppContainer: ${e.message}", e)
        }
        
        // Initialize the offline cache
        applicationScope.launch {
            try {
                Log.i("InventoryApp", "Initializing offline cache...")
                OfflineCache.initialize(this@InventoryApplication)
                Log.i("InventoryApp", "Offline cache initialized successfully")
            } catch (e: Exception) {
                Log.e("InventoryApp", "ERROR initializing offline cache: ${e.message}", e)
            }
        }
        
        // Initialize data prefetcher for performance optimization
        try {
            Log.i("InventoryApp", "Initializing data prefetcher...")
            DataPrefetcher.initialize(this)
            Log.i("InventoryApp", "Data prefetcher initialized successfully")
        } catch (e: Exception) {
            Log.e("InventoryApp", "ERROR initializing data prefetcher: ${e.message}", e)
        }
        
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