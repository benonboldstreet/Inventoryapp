package com.example.inventory

import android.app.Application
import com.example.inventory.api.ApiServer
import com.example.inventory.data.AppContainer
import com.example.inventory.data.AppContainerImpl
import android.util.Log

/**
 * Inventory Application
 * CLOUD MODE: This application now connects to an Azure backend instead of a local database
 */
class InventoryApplication : Application() {
    
    // AppContainer instance used by the rest of the app
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the AppContainer with cloud repositories
        container = AppContainerImpl(this)
        
        // Log that we're running in cloud mode
        Log.i("InventoryApp", "Starting in CLOUD MODE - using Azure backend")
        
        // Temporarily disabled for local testing
        // Start the API server with repositories from the container
        // ApiServer.start(
        //     itemRepository = container.itemRepository,
        //     staffRepository = container.staffRepository,
        //     checkoutRepository = container.checkoutRepository,
        //     port = 8080
        // )
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Temporarily disabled for local testing
        // Stop the API server when the application is terminated
        // ApiServer.stop()
    }
} 