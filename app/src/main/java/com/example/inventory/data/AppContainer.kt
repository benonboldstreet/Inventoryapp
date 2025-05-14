package com.example.inventory.data

import android.content.Context
import androidx.room.Room
import com.example.inventory.data.database.InventoryDatabase
import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository
import com.example.inventory.data.repository.CloudItemRepository
import com.example.inventory.data.repository.CloudStaffRepository
import com.example.inventory.data.repository.CloudCheckoutRepository

/**
 * Container for dependencies needed by the app
 */
interface AppContainer {
    val itemRepository: ItemRepository
    val staffRepository: StaffRepository
    val checkoutRepository: CheckoutRepository
}

/**
 * Container for application-wide dependencies
 */
class AppContainerImpl(private val context: Context) : AppContainer {
    // Database instance - retained for potential fallback but not used in cloud implementation
    private val database: InventoryDatabase by lazy {
        Room.databaseBuilder(
            context,
            InventoryDatabase::class.java,
            "inventory_database"
        ).build()
    }
    
    // CLOUD IMPLEMENTATION: Using cloud repositories instead of local database
    override val itemRepository: ItemRepository by lazy {
        // For cloud-only implementation, use CloudItemRepository
        CloudItemRepository()
        
        // The original local implementation was:
        // ItemRepository(database.itemDao())
    }
    
    // CLOUD IMPLEMENTATION: Using cloud repositories instead of local database
    override val staffRepository: StaffRepository by lazy {
        // For cloud-only implementation, use CloudStaffRepository 
        CloudStaffRepository()
        
        // The original local implementation was:
        // StaffRepository(database.staffDao())
    }
    
    // CLOUD IMPLEMENTATION: Using cloud repositories instead of local database
    override val checkoutRepository: CheckoutRepository by lazy {
        // For cloud-only implementation, use CloudCheckoutRepository
        CloudCheckoutRepository()
        
        // The original local implementation was:
        // CheckoutRepository(database.checkoutLogDao())
    }
} 