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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import com.example.inventory.data.database.Item
import com.example.inventory.data.database.Staff
import com.example.inventory.data.database.CheckoutLog
import com.example.inventory.ui.components.StaffSelectorDialog
import com.example.inventory.ui.components.StaffSelectorWithPhotoDialog
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
    
    // Process returned photo if available
    LaunchedEffect(photoPath, photoItemId, photoStaffId) {
        if (photoPath != null && photoItemId != null && photoStaffId != null) {
            val itemUuid = UUID.fromString(photoItemId)
            val staffUuid = UUID.fromString(photoStaffId)
            
            // Process checkout with photo
            checkoutViewModel.checkOutItemWithPhoto(itemUuid, staffUuid, photoPath)
            
            // Get staff name for notification
            val staff = staffViewModel.getStaffById(staffUuid)
            staff?.let { selectedStaff ->
                // Format the timestamp for notification
                val timestamp = System.currentTimeMillis()
                val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
                val formattedDate = dateFormat.format(java.util.Date(timestamp))
                
                // Create notification message
                notificationTitle = "Item Checked Out"
                notificationMessage = "${item?.name} was successfully checked out to ${selectedStaff.name} on $formattedDate with photo"
                showNotificationDialog = true
            }
            
            // Refresh item data and checkout logs
            item = itemViewModel.getItemById(itemUuid)
            item?.id?.let { id ->
                checkoutLogs = checkoutViewModel.getCheckoutLogsByItem(id).first()
            }
            
            // Notify that the photo has been processed
            onPhotoProcessed()
        }
    }
    
    // Fetch item details
    LaunchedEffect(itemId, refreshTrigger) {
        itemId?.let {
            item = itemViewModel.getItemById(it)
            
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
                                            itemViewModel.unarchiveItem(currentItem)
                                            refreshTrigger++
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
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ItemDetailRow("Type", currentItem.type)
                        ItemDetailRow("Barcode", currentItem.barcode)
                        ItemDetailRow("Condition", currentItem.condition)
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
                            
                            // Load and display the item photo
                            val file = File(currentItem.photoPath)
                            val uri = Uri.fromFile(file)
                            val imageRequest = ImageRequest.Builder(context)
                                .data(uri)
                                .build()
                                
                            Image(
                                painter = rememberAsyncImagePainter(imageRequest),
                                contentDescription = "Item photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, Color.Gray),
                                contentScale = ContentScale.Fit
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
                                            coroutineScope.launch {
                                                // Find the active checkout log
                                                val activeCheckout = checkoutLogs.find { it.checkInTime == null }
                                                activeCheckout?.let { checkout ->
                                                    // Check in the item
                                                    checkoutViewModel.checkInItem(checkout.id)
                                                    
                                                    // Format the timestamp for notification
                                                    val timestamp = System.currentTimeMillis()
                                                    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
                                                    val formattedDate = dateFormat.format(java.util.Date(timestamp))
                                                    
                                                    // Create notification message
                                                    notificationTitle = "Item Checked In"
                                                    notificationMessage = "${currentItem.name} was successfully checked in on $formattedDate"
                                                    showNotificationDialog = true
                                                    
                                                    // Refresh item and checkout logs
                                                    item = itemViewModel.getItemById(currentItem.id)
                                                    checkoutLogs = checkoutViewModel.getCheckoutLogsByItem(currentItem.id).first()
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Check In")
                                    }
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
                    
                    checkoutLogs.sortedByDescending { it.checkOutTime }.forEach { log ->
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
    
    // Staff selector dialog for checkout
    if (showStaffSelectorDialog) {
        item?.let { currentItem ->
            // Always show the staff selector with photo dialog
            StaffSelectorWithPhotoDialog(
                itemId = currentItem.id,
                staffList = staffList,
                onStaffSelectedWithPhoto = { selectedStaff, takePhoto ->
                    if (takePhoto) {
                        // Set the selected staff for when we return from photo capture
                        selectedStaffForPhoto = selectedStaff
                        // Navigate to photo capture screen
                        onNavigateToPhotoCapture("${currentItem.id},${selectedStaff.id}")
                    } else {
                        // Regular checkout process without photo
                        coroutineScope.launch {
                            checkoutViewModel.checkOutItem(currentItem.id, selectedStaff.id)
                            
                            // Format the timestamp for notification
                            val timestamp = System.currentTimeMillis()
                            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
                            val formattedDate = dateFormat.format(java.util.Date(timestamp))
                            
                            // Create notification message
                            notificationTitle = "Item Checked Out"
                            notificationMessage = "${currentItem.name} was successfully checked out to ${selectedStaff.name} on $formattedDate"
                            showNotificationDialog = true
                            
                            // Refresh item and checkout logs
                            item = itemViewModel.getItemById(currentItem.id)
                            checkoutLogs = checkoutViewModel.getCheckoutLogsByItem(currentItem.id).first()
                        }
                    }
                    showStaffSelectorDialog = false
                },
                onNavigateToPhotoCapture = { route ->
                    onNavigateToPhotoCapture(route)
                    showStaffSelectorDialog = false
                },
                onDismiss = { showStaffSelectorDialog = false }
            )
        }
    }
    
    // Notification dialog
    if (showNotificationDialog) {
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
                                itemViewModel.archiveItem(it)
                                showArchiveDialog = false
                                refreshTrigger++
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
        val staff = staffViewModel.getStaffById(log.staffId)
        staffName = staff?.name ?: "Unknown Staff"
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
        val checkoutDate = java.util.Date(log.checkOutTime)
        Text(
            text = "Checked out: ${checkoutDateFormat.format(checkoutDate)}",
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Check-in time if available
        log.checkInTime?.let { checkInTime ->
            val checkinDate = java.util.Date(checkInTime)
            Text(
                text = "Checked in: ${checkoutDateFormat.format(checkinDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Display checkout photo if available
        log.photoPath?.let { photoPath ->
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Condition Photo:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Display the image using Coil
            val file = File(photoPath)
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(data = file)
                    .build()
            )
            
            Image(
                painter = painter,
                contentDescription = "Checkout photo",
                modifier = Modifier
                    .size(200.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
        }
    }
} 