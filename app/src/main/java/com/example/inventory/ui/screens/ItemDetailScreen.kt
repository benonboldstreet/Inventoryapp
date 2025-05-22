package com.example.inventory.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.ui.components.SimpleStaffSelector
import com.example.inventory.ui.navigation.InventoryDestinations
import com.example.inventory.ui.viewmodel.CheckoutViewModel
import com.example.inventory.ui.viewmodel.ItemViewModel
import com.example.inventory.ui.viewmodel.checkoutViewModel
import com.example.inventory.ui.viewmodel.itemViewModel
import com.example.inventory.ui.viewmodel.staffViewModel
import com.example.inventory.ui.viewmodel.StaffViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.UUID
import coil.request.ImageRequest
import androidx.compose.foundation.layout.Arrangement
import com.example.inventory.ui.utils.SmartImage
import com.example.inventory.ui.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: UUID?,
    onNavigateBack: () -> Unit,
    onNavigateToPhotoCapture: (String) -> Unit = {},
    photoPath: String? = null,
    photoItemId: String? = null,
    photoStaffId: String? = null,
    onPhotoProcessed: () -> Unit = {}
) {
    val itemViewModel = itemViewModel()
    val checkoutViewModel = checkoutViewModel()
    val staffViewModel = staffViewModel()
    val context = LocalContext.current
    
    var item by remember { mutableStateOf<Item?>(null) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showStaffSelectorDialog by remember { mutableStateOf(false) }
    var staffList by remember { mutableStateOf<List<Staff>>(emptyList()) }
    var selectedStaffForPhoto by remember { mutableStateOf<Staff?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // Notification states
    var showNotificationDialog by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf("") }
    var notificationTitle by remember { mutableStateOf("") }
    
    // Checkout logs state
    var checkoutLogs by remember { mutableStateOf<List<CheckoutLog>>(emptyList()) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Process returned photo if available - simplified to avoid crashes
    LaunchedEffect(photoPath, photoItemId, photoStaffId) {
        if (photoPath != null && photoItemId != null && photoStaffId != null) {
            // Just log this for now and call onPhotoProcessed
            android.util.Log.d("Photo", "Photo path: $photoPath, itemId: $photoItemId, staffId: $photoStaffId")
            
            // For now, we'll disable this functionality to prevent crashes
            notificationTitle = "Photo Feature Disabled"
            notificationMessage = "The photo capture feature is currently disabled."
            showNotificationDialog = true
            
            // Always notify that the photo has been processed
            onPhotoProcessed()
        }
    }
    
    // Fetch item details
    LaunchedEffect(itemId, refreshTrigger) {
        itemId?.let {
            itemViewModel.getItemById(it).collect { fetchedItem ->
                item = fetchedItem
            }
            
            // Get checkout logs for this item
            checkoutLogs = checkoutViewModel.getCheckoutLogsByItem(it).first()
        }
    }
    
    // Fetch staff list when needed
    LaunchedEffect(showStaffSelectorDialog) {
        if (showStaffSelectorDialog) {
            staffList = staffViewModel.allStaff.first().filter { it.isActive } // Only show active staff
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "Item Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item?.let { currentItem ->
                // Item details card
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentItem.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                // Show archived status if item is inactive
                                if (!currentItem.isActive) {
                                    Text(
                                        text = "Archived",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            
                            // Archive/Unarchive button
                            if (!currentItem.isActive) {
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                android.util.Log.d("ItemDetailScreen", "Attempting to unarchive item: ${currentItem.id}")
                                                itemViewModel.unarchiveItem(currentItem)
                                                
                                                // Show notification
                                                notificationTitle = "Success"
                                                notificationMessage = "Item unarchived successfully"
                                                showNotificationDialog = true
                                                
                                                refreshTrigger++
                                            } catch (e: Exception) {
                                                android.util.Log.e("ItemDetailScreen", "Error unarchiving: ${e.message}", e)
                                                
                                                // Show error notification
                                                notificationTitle = "Error"
                                                notificationMessage = "Failed to unarchive item: ${e.message}"
                                                showNotificationDialog = true
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Unarchive")
                                }
                            } else {
                                TextButton(
                                    onClick = { showArchiveDialog = true },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Archive")
                                }
                            }
                        }
                        
                        // After the Show archived status section, add this button
                        if (!currentItem.isActive) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        itemViewModel.verifyArchiveStatus(currentItem.id) { exists, isArchived, error ->
                                            if (exists) {
                                                notificationTitle = "Database Check"
                                                notificationMessage = "Item exists in database. Archived status: ${isArchived ?: "unknown"}"
                                            } else {
                                                notificationTitle = "Database Error" 
                                                notificationMessage = error ?: "Unknown error checking database"
                                            }
                                            showNotificationDialog = true
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Text("Verify Archive Status")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ItemDetailRow("Type", currentItem.type)
                        ItemDetailRow("Barcode", currentItem.barcode)
                        ItemDetailRow("Condition", currentItem.condition)
                        
                        if (currentItem.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Description",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = currentItem.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        
                        ItemDetailRow(
                            label = "Status",
                            value = currentItem.status,
                            valueColor = when(currentItem.status) {
                                "Available" -> Color.Green
                                "Checked Out" -> Color.Red
                                else -> Color.Black
                            }
                        )
                        
                        if (currentItem.photoPath != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Photo",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Action buttons
                        if (currentItem.isActive) { // Only show checkout/edit buttons for active items
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                if (currentItem.status == "Available") {
                                    Button(
                                        onClick = { showStaffSelectorDialog = true }
                                    ) {
                                        Text("Check Out")
                                    }
                                } else if (currentItem.status == "Checked Out") {
                                    Button(
                                        onClick = {
                                            // Show processing notification first
                                            notificationTitle = "Processing Check-in"
                                            notificationMessage = "Checking in ${currentItem.name}..."
                                            showNotificationDialog = true
                                            
                                            // Then handle the check-in process in the background
                                            coroutineScope.launch {
                                                try {
                                                    // Find the active checkout log
                                                    val activeCheckout = checkoutLogs.find { it.getCheckInTimeAsLong() == null }
                                                    activeCheckout?.let { checkout ->
                                                        // Check in the item
                                                        val result = checkoutViewModel.checkInItem(checkout)
                                                        if (result.isSuccess) {
                                                            // Just refresh the trigger to update the UI
                                                            refreshTrigger++
                                                        } else {
                                                            throw result.exceptionOrNull() ?: Exception("Failed to check in item")
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("Checkin", "Error during check-in: ${e.message}", e)
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Check In")
                                    }
                                }
                            }
                            
                            // Debug Test Button - REMOVE IN PRODUCTION
                            if (currentItem.id.toString() == "00000000-0000-0000-0000-000000000000") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            testArchiveItem(currentItem, itemViewModel) { success, message ->
                                                notificationTitle = if (success) "Test Success" else "Test Failed"
                                                notificationMessage = message
                                                showNotificationDialog = true
                                                refreshTrigger++
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Text("Test Archive")
                                }
                            }
                        }
                    }
                }
                
                // Display checkout logs if available
                if (checkoutLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Checkout History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    checkoutLogs.sortedByDescending { it.getCheckOutTimeAsLong() }.forEach { log ->
                        CheckoutLogCard(log, staffViewModel)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } ?: run {
                // Show loading or error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Item not found or still loading...")
                }
            }
        }
    }
    
    // Staff selector dialog for checkout - using the simplified version
    if (showStaffSelectorDialog) {
        item?.let { currentItem ->
            SimpleStaffSelector(
                staffList = staffList,
                onStaffSelected = { selectedStaff ->
                    // Close dialog first
                    showStaffSelectorDialog = false
                    
                    // Show checkout in progress notification
                    notificationTitle = "Processing"
                    notificationMessage = "Processing checkout..."
                    showNotificationDialog = true
                    
                    // Simple background checkout without complex coroutines
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        coroutineScope.launch {
                            try {
                                // Perform checkout
                                val checkoutResult = checkoutViewModel.checkOutItem(currentItem.id, selectedStaff.id)
                                
                                if (checkoutResult.isSuccess) {
                                    // Refresh UI
                                    refreshTrigger++
                                    
                                    // Show completion notification
                                    notificationTitle = "Success"
                                    notificationMessage = "Item checked out successfully"
                                    showNotificationDialog = true
                                } else {
                                    throw checkoutResult.exceptionOrNull() ?: Exception("Unknown error during checkout")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("Checkout", "Error: ${e.message}", e)
                                
                                // Show error notification
                                notificationTitle = "Error"
                                notificationMessage = "Could not complete checkout"
                                showNotificationDialog = true
                            }
                        }
                    }, 100) // Small delay to ensure UI is updated first
                },
                onDismiss = {
                    showStaffSelectorDialog = false
                }
            )
        }
    }
    
    // Notification dialog
    if (showNotificationDialog) {
        if (notificationTitle == "Success" && notificationMessage.contains("archived successfully")) {
            // Add a View Archived Items button
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                title = { Text(notificationTitle) },
                text = { 
                    Column {
                        Text(notificationMessage)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Would you like to view archived items?", fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            showNotificationDialog = false
                            // Set the flag to show archived items
                            SharedViewModel.setShowArchivedItems(true)
                            // Navigate back to list
                            onNavigateBack()
                        }
                    ) {
                        Text("View Archived Items")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationDialog = false }) {
                        Text("Close")
                    }
                }
            )
        } else {
            // Regular notification dialog (existing code)
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                title = { Text(notificationTitle) },
                text = { Text(notificationMessage) },
                confirmButton = {
                    TextButton(onClick = { showNotificationDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
    
    // Archive confirmation dialog
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Archive Item") },
            text = { Text("Are you sure you want to archive this item? It will be marked as inactive but its data and checkout history will be preserved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        item?.let {
                            coroutineScope.launch {
                                try {
                                    // Close dialog immediately
                                    showArchiveDialog = false
                                    
                                    // Show processing indicator
                                    notificationTitle = "Processing"
                                    notificationMessage = "Archiving item..."
                                    showNotificationDialog = true
                                    
                                    // Debug logging to help diagnose issues
                                    android.util.Log.d("ItemDetailScreen", "===== ARCHIVE PROCESS START =====")
                                    android.util.Log.d("ItemDetailScreen", "Item before archive: id=${it.id}, name=${it.name}, isActive=${it.isActive}")
                                    
                                    try {
                                        // Use standard archive method
                                        itemViewModel.archiveItem(it)
                                        
                                        // Wait a moment to allow the operation to complete
                                        kotlinx.coroutines.delay(500)
                                        
                                        // Verify the item was properly archived by refetching it
                                        val archivedItem = itemViewModel.getItemById(it.id).first()
                                        
                                        if (archivedItem != null) {
                                            android.util.Log.d("ItemDetailScreen", "Item after archive: id=${archivedItem.id}, isActive=${archivedItem.isActive}")
                                            
                                            if (!archivedItem.isActive) {
                                                // Successfully archived
                                                android.util.Log.d("ItemDetailScreen", "Archive successful - item marked as inactive")
                                                
                                                // Update UI
                                                refreshTrigger++
                                                
                                                // Show success message
                                                notificationTitle = "Success"
                                                notificationMessage = "Item archived successfully. It will now appear in the Archived section."
                                                showNotificationDialog = true
                                            } else {
                                                // Item still active for some reason
                                                android.util.Log.e("ItemDetailScreen", "Item is still active after archive operation")
                                                throw Exception("Failed to mark item as archived")
                                            }
                                        } else {
                                            // Item not found after archive - this is wrong
                                            android.util.Log.e("ItemDetailScreen", "CRITICAL ERROR: Item not found after archive")
                                            throw Exception("Item was deleted instead of archived")
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("ItemDetailScreen", "Error during archive operation: ${e.message}", e)
                                        
                                        // Show error notification
                                        notificationTitle = "Error"
                                        notificationMessage = "Failed to archive item: ${e.message}\nPlease try again or contact support."
                                        showNotificationDialog = true
                                    }
                                
                                    android.util.Log.d("ItemDetailScreen", "===== ARCHIVE PROCESS COMPLETE =====")
                                } catch (e: Exception) {
                                    android.util.Log.e("ItemDetailScreen", "Outer error in archive process: ${e.message}", e)
                                    
                                    // Show error notification
                                    notificationTitle = "Error"
                                    notificationMessage = "Failed to archive item: ${e.message}"
                                    showNotificationDialog = true
                                }
                            }
                        }
                    }
                ) {
                    Text("Archive", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ItemDetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}

@Composable
fun CheckoutLogCard(
    log: CheckoutLog,
    staffViewModel: StaffViewModel
) {
    var staffName by remember { mutableStateOf("Unknown Staff") }
    val context = LocalContext.current
    
    // Load staff name
    LaunchedEffect(log) {
        staffViewModel.getStaffById(UUID.fromString(log.staffIdString)).collect { staff ->
            staffName = staff?.name ?: "Unknown Staff"
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Staff name
        Text(
            text = "Staff: $staffName",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        
        // Check-out time
        val checkoutDateFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
        val checkoutDate = java.util.Date(log.getCheckOutTimeAsLong())
        Text(
            text = "Checked out: ${checkoutDateFormat.format(checkoutDate)}",
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Check-in time if available
        log.getCheckInTimeAsLong()?.let { checkInTime ->
            val checkinDate = java.util.Date(checkInTime)
            Text(
                text = "Checked in: ${checkoutDateFormat.format(checkinDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Debug helper function to test archive functionality
private suspend fun testArchiveItem(
    item: Item,
    itemViewModel: ItemViewModel,
    onResult: (success: Boolean, message: String) -> Unit
) {
    try {
        android.util.Log.d("ArchiveTest", "Starting archive test for item: ${item.id}")
        val updatedItem = item.copy(isActive = false, lastModified = System.currentTimeMillis())
        android.util.Log.d("ArchiveTest", "Created updated item with isActive=false")
        itemViewModel.updateItem(updatedItem)
        android.util.Log.d("ArchiveTest", "Archive test complete - updateItem called")
        onResult(true, "Test archive successful")
    } catch (e: Exception) {
        android.util.Log.e("ArchiveTest", "Error in archive test: ${e.message}", e)
        onResult(false, "Test archive failed: ${e.message}")
    }
} 