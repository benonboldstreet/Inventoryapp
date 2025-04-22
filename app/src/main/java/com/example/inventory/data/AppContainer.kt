package com.example.inventory.data

import android.content.Context
import com.example.inventory.data.database.InventoryDatabase
import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository

/**
 * Container for dependencies needed by the app
 */
interface AppContainer {
    val itemRepository: ItemRepository
    val staffRepository: StaffRepository
    val checkoutRepository: CheckoutRepository
}

/**
 * Implementation of the AppContainer providing the repositories
 */
class AppContainerImpl(private val context: Context) : AppContainer {
    
    private val database: InventoryDatabase by lazy {
        InventoryDatabase.getDatabase(context)
    }
    
    override val itemRepository: ItemRepository by lazy {
        ItemRepository(database.itemDao())
    }
    
    override val staffRepository: StaffRepository by lazy {
        StaffRepository(database.staffDao())
    }
    
    override val checkoutRepository: CheckoutRepository by lazy {
        CheckoutRepository(database.checkoutLogDao())
    }
} 