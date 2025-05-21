package com.example.inventory.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryApplication

/**
 * Utility functions for creating ViewModels in Compose
 * These functions help with dependency injection for ViewModels
 */

/**
 * Create an ItemViewModel with proper dependencies
 */
@Composable
fun itemViewModel(): ItemViewModel {
    val context = LocalContext.current
    val application = context.applicationContext as InventoryApplication
    return viewModel(
        factory = ItemViewModel.Companion.Factory(application.container.itemRepository)
    )
}

/**
 * Create a StaffViewModel with proper dependencies
 */
@Composable
fun staffViewModel(): StaffViewModel {
    val context = LocalContext.current
    val application = context.applicationContext as InventoryApplication
    return viewModel(
        factory = StaffViewModel.Companion.Factory(application.container.staffRepository)
    )
}

/**
 * Create a CheckoutViewModel with proper dependencies
 */
@Composable
fun checkoutViewModel(): CheckoutViewModel {
    val context = LocalContext.current
    val application = context.applicationContext as InventoryApplication
    return viewModel(
        factory = CheckoutViewModel.Companion.Factory(
            application.container.checkoutRepository,
            application.container.itemRepository
        )
    )
} 