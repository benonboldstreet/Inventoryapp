package com.example.inventory.ui.viewmodel

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryApplication

/**
 * Utility functions for creating ViewModels in Compose
 */

/**
 * Create an ItemViewModel with proper dependencies
 */
@Composable
fun itemViewModel(): ItemViewModel {
    val context = LocalContext.current
    val application = context.applicationContext as InventoryApplication
    return viewModel(
        factory = ItemViewModel.Factory(application.container.itemRepository)
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
        factory = StaffViewModel.Factory(application.container.staffRepository)
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
        factory = CheckoutViewModel.Factory(
            application.container.checkoutRepository,
            application.container.itemRepository
        )
    )
} 