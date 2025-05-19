package com.example.inventory.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.inventory.ui.camera.PhotoCaptureScreen
import com.example.inventory.ui.scanner.BarcodeScannerScreen
import com.example.inventory.ui.screens.CheckoutScreen
import com.example.inventory.ui.screens.GroupedItemsScreen
import com.example.inventory.ui.screens.ItemDetailScreen
import com.example.inventory.ui.screens.ItemListScreen
import com.example.inventory.ui.screens.ReportScreen
import com.example.inventory.ui.screens.SplashScreen
import com.example.inventory.ui.screens.StaffDetailScreen
import com.example.inventory.ui.screens.StaffListScreen
import java.util.UUID

/**
 * Routes for navigation
 */
// REMOVED object InventoryDestinations definition - using the one from InventoryDestinations.kt instead

/**
 * Bottom navigation items
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Items : BottomNavItem(
        route = InventoryDestinations.ITEMS_ROUTE,
        title = "Items",
        selectedIcon = Icons.Filled.Inventory,
        unselectedIcon = Icons.Outlined.Inventory
    )
    
    object GroupedItems : BottomNavItem(
        route = InventoryDestinations.GROUPED_ITEMS_ROUTE,
        title = "Categories",
        selectedIcon = Icons.Filled.Category,
        unselectedIcon = Icons.Outlined.Category
    )
    
    object Staff : BottomNavItem(
        route = InventoryDestinations.STAFF_ROUTE,
        title = "Staff",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    )
    
    object Reports : BottomNavItem(
        route = InventoryDestinations.REPORTS_ROUTE,
        title = "Reports",
        selectedIcon = Icons.Filled.Report,
        unselectedIcon = Icons.Outlined.Report
    )
}

/**
 * Bottom navigation bar
 */
@Composable
fun InventoryBottomNavBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Items,
        BottomNavItem.GroupedItems,
        BottomNavItem.Staff,
        BottomNavItem.Reports
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to avoid building up a large stack
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Navigation host for the app
 */
@Composable
fun InventoryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = InventoryDestinations.SPLASH_ROUTE,
        modifier = modifier
    ) {
        // Splash screen
        composable(InventoryDestinations.SPLASH_ROUTE) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(InventoryDestinations.GROUPED_ITEMS_ROUTE) {
                        popUpTo(InventoryDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        
        // Main tab screens
        composable(InventoryDestinations.ITEMS_ROUTE) {
            ItemListScreen(
                onItemClick = { itemId ->
                    navController.navigate("${InventoryDestinations.ITEM_DETAIL_ROUTE}/$itemId")
                },
                onBarcodeScanner = {
                    navController.navigate(InventoryDestinations.BARCODE_SCANNER_ROUTE)
                },
                bottomBar = { InventoryBottomNavBar(navController) }
            )
        }
        
        composable(InventoryDestinations.GROUPED_ITEMS_ROUTE) {
            GroupedItemsScreen(
                onBarcodeScanner = {
                    navController.navigate(InventoryDestinations.BARCODE_SCANNER_ROUTE)
                },
                onItemClick = { item ->
                    navController.navigate("${InventoryDestinations.ITEM_DETAIL_ROUTE}/${item.id}")
                },
                bottomBar = { InventoryBottomNavBar(navController) }
            )
        }
        
        composable(InventoryDestinations.STAFF_ROUTE) {
            StaffListScreen(
                bottomBar = { InventoryBottomNavBar(navController) },
                onStaffClick = { staff ->
                    navController.navigate("${InventoryDestinations.STAFF_DETAIL_ROUTE}/${staff.id}")
                }
            )
        }
        
        composable(InventoryDestinations.REPORTS_ROUTE) {
            ReportScreen(
                bottomBar = { InventoryBottomNavBar(navController) }
            )
        }
        
        // Detail screens
        composable(
            route = InventoryDestinations.ITEM_DETAIL_WITH_ID_ROUTE,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            val savedStateHandle = backStackEntry.savedStateHandle
            val photoPath = savedStateHandle.get<String>("photoPath")
            val photoItemId = savedStateHandle.get<String>("itemId")
            val photoStaffId = savedStateHandle.get<String>("staffId")
            
            ItemDetailScreen(
                itemId = itemId?.let { UUID.fromString(it) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPhotoCapture = { route -> 
                    android.util.Log.d("Navigation", "About to navigate to photo capture with route: $route")
                    navController.navigate(route)
                },
                photoPath = photoPath,
                photoItemId = photoItemId,
                photoStaffId = photoStaffId,
                onPhotoProcessed = {
                    // Clear the saved state
                    savedStateHandle.remove<String>("photoPath")
                    savedStateHandle.remove<String>("itemId")
                    savedStateHandle.remove<String>("staffId")
                }
            )
        }
        
        composable(
            route = InventoryDestinations.STAFF_DETAIL_WITH_ID_ROUTE,
            arguments = listOf(
                navArgument("staffId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val staffId = backStackEntry.arguments?.getString("staffId")
            StaffDetailScreen(
                staffId = staffId?.let { UUID.fromString(it) },
                onNavigateBack = { navController.popBackStack() },
                onItemClick = { itemId ->
                    navController.navigate("${InventoryDestinations.ITEM_DETAIL_ROUTE}/$itemId")
                }
            )
        }
        
        composable(InventoryDestinations.CHECKOUT_ROUTE) {
            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Barcode scanner screen
        composable(InventoryDestinations.BARCODE_SCANNER_ROUTE) {
            val returnBarcode = navController.previousBackStackEntry?.destination?.route == InventoryDestinations.ITEMS_ROUTE
            
            BarcodeScannerScreen(
                onNavigateBack = { 
                    navController.popBackStack()
                },
                onNavigateToPhotoCapture = { route ->
                    navController.navigate(route)
                },
                returnBarcode = returnBarcode
            )
        }
        
        // Photo capture screen
        composable(
            route = InventoryDestinations.PHOTO_CAPTURE_WITH_ITEM_STAFF_ROUTE,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                },
                navArgument("staffId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            val staffId = backStackEntry.arguments?.getString("staffId")
            
            android.util.Log.e("Navigation", "*** PHOTO CAPTURE SCREEN COMPOSABLE CALLED *** with itemId: $itemId, staffId: $staffId")
            
            if (itemId != null && staffId != null) {
                // Create UUID objects from the strings
                val itemUuid = try { UUID.fromString(itemId) } catch (e: Exception) { 
                    android.util.Log.e("Navigation", "Invalid itemId format", e)
                    null 
                }
                val staffUuid = try { UUID.fromString(staffId) } catch (e: Exception) { 
                    android.util.Log.e("Navigation", "Invalid staffId format", e)
                    null 
                }
                
                if (itemUuid != null && staffUuid != null) {
                    PhotoCaptureScreen(
                        itemId = itemUuid,
                        onPhotoTaken = { photoPath ->
                            // After photo is taken, go back to previous screen and pass data via ViewModel
                            android.util.Log.d("Navigation", "Photo taken, path: $photoPath, returning to previous screen")
                            val viewModel = navController.previousBackStackEntry?.savedStateHandle
                            viewModel?.set("photoPath", photoPath)
                            viewModel?.set("itemId", itemId)
                            viewModel?.set("staffId", staffId)
                            navController.popBackStack()
                        },
                        onNavigateBack = {
                            android.util.Log.d("Navigation", "User navigated back from photo capture")
                            navController.popBackStack()
                        }
                    )
                } else {
                    // Show error message
                    android.util.Log.e("Navigation", "Could not parse UUID from string")
                    androidx.compose.material3.Text("Invalid item or staff ID format")
                    
                    // Go back after a short delay
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        navController.popBackStack()
                    }
                }
            } else {
                android.util.Log.e("Navigation", "Missing itemId or staffId for photo capture")
                androidx.compose.material3.Text("Missing item or staff ID")
                
                // Go back after a short delay
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    navController.popBackStack()
                }
            }
        }
    }
}

/**
 * Main screen definitions for the bottom navigation
 */
sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object GroupedItems : Screen(InventoryDestinations.GROUPED_ITEMS_ROUTE, "Categories", Icons.Default.Category)
    object Items : Screen(InventoryDestinations.ITEMS_ROUTE, "Items", Icons.Default.Inventory)
    object Staff : Screen(InventoryDestinations.STAFF_ROUTE, "Staff", Icons.Default.People)
    object Reports : Screen(InventoryDestinations.REPORTS_ROUTE, "Reports", Icons.Default.Assessment)
} 