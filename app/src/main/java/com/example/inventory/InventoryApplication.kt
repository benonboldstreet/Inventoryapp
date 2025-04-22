package com.example.inventory

import android.app.Application
import com.example.inventory.api.ApiServer
import com.example.inventory.data.AppContainer
import com.example.inventory.data.AppContainerImpl

class InventoryApplication : Application() {
    
    // AppContainer instance used by the rest of the app
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
        
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