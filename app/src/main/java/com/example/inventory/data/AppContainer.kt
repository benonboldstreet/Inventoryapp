package com.example.inventory.data

import android.content.Context
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository
import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.CloudItemRepository
import com.example.inventory.data.repository.CloudStaffRepository
import com.example.inventory.data.repository.CloudCheckoutRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Container for dependencies needed by the app
 * 
 * This provides access to the cloud repositories for inventory items,
 * staff members, and checkout logs.
 */
interface AppContainer {
    val itemRepository: ItemRepository
    val staffRepository: StaffRepository
    val checkoutRepository: CheckoutRepository
}

/**
 * Container for application-wide dependencies
 * 
 * CLOUD IMPLEMENTATION: This implementation provides cloud-based
 * repositories that connect to Azure/cloud services.
 * 
 * Provides offline caching support by passing the application context
 * to repository implementations.
 */
class AppContainerImpl(private val context: Context) : AppContainer {
    // CLOUD IMPLEMENTATION: Initialize repositories with application context for offline caching
    override val itemRepository: ItemRepository by lazy {
        CloudItemRepository(appContext = context)
    }
    
    override val staffRepository: StaffRepository by lazy {
        CloudStaffRepository(appContext = context)
    }
    
    override val checkoutRepository: CheckoutRepository by lazy {
        CloudCheckoutRepository(appContext = context)
    }
} 