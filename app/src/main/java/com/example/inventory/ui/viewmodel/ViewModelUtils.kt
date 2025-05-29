package com.example.inventory.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Utility functions for creating ViewModels in Compose
 * These functions help with dependency injection for ViewModels
 */

/**
 * Create an ItemViewModel with proper dependencies
 */
@Composable
fun itemViewModel(): ItemViewModel {
    return hiltViewModel<ItemViewModel>()
}

/**
 * Create a StaffViewModel with proper dependencies
 */
@Composable
fun staffViewModel(): StaffViewModel {
    return hiltViewModel<StaffViewModel>()
}

/**
 * Create a CheckoutViewModel with proper dependencies
 */
@Composable
fun checkoutViewModel(): CheckoutViewModel {
    return hiltViewModel<CheckoutViewModel>()
}

/**
 * Create a HiltViewModel with proper dependencies
 * This function was causing a recursive call - removing it
 */
// @Composable
// inline fun <reified T : androidx.lifecycle.ViewModel> hiltViewModel(): T {
//     return hiltViewModel()
// } 