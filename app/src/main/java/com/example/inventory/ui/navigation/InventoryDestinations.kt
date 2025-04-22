package com.example.inventory.ui.navigation

/**
 * Contains the screen destinations and routes for the app
 */
object InventoryDestinations {
    // Splash screen
    const val SPLASH_ROUTE = "splash"
    
    // Main screen routes
    const val ITEMS_ROUTE = "items"
    const val GROUPED_ITEMS_ROUTE = "grouped_items"
    const val STAFF_ROUTE = "staff"
    const val REPORTS_ROUTE = "reports"
    
    // Detail screen routes
    const val ITEM_DETAIL_ROUTE = "item_detail"
    const val ITEM_DETAIL_WITH_ID_ROUTE = "item_detail/{itemId}"
    
    const val STAFF_DETAIL_ROUTE = "staff_detail"
    const val STAFF_DETAIL_WITH_ID_ROUTE = "staff_detail/{staffId}"
    
    const val CHECKOUT_ROUTE = "checkout"
    
    // Utility screen routes
    const val BARCODE_SCANNER_ROUTE = "barcode_scanner"
    
    // Photo capture screen routes
    const val PHOTO_CAPTURE_ROUTE = "photo_capture"
    const val PHOTO_CAPTURE_WITH_ITEM_STAFF_ROUTE = "photo_capture/{itemId}/{staffId}"
} 