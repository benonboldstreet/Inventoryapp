package com.example.inventory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventory.data.database.CheckoutLog
import com.example.inventory.data.database.Item
import com.example.inventory.data.database.Staff
import com.example.inventory.ui.viewmodel.CheckoutViewModel
import com.example.inventory.ui.viewmodel.ItemViewModel
import com.example.inventory.ui.viewmodel.StaffViewModel
import com.example.inventory.ui.viewmodel.checkoutViewModel
import com.example.inventory.ui.viewmodel.itemViewModel
import com.example.inventory.ui.viewmodel.staffViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDetailScreen(
    staffId: UUID?,
    onNavigateBack: () -> Unit,
    onItemClick: (UUID) -> Unit
) {
    val staffViewModel = staffViewModel()
    val checkoutViewModel = checkoutViewModel()
    val itemViewModel = itemViewModel()
    
    var staff by remember { mutableStateOf<Staff?>(null) }
    var currentAssignments by remember { mutableStateOf<List<Pair<Item, CheckoutLog>>>(emptyList()) }
    var checkoutHistory by remember { mutableStateOf<List<Triple<Item, CheckoutLog, Long?>>>(emptyList()) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Load staff and their assignments
    LaunchedEffect(staffId, refreshTrigger) {
        staffId?.let { id ->
            // Load staff details
            staff = staffViewModel.getStaffById(id)
            
            // Load all checkout logs for this staff
            val allCheckoutLogs = checkoutViewModel.getCheckoutLogsByStaff(id).first()
            
            // Separate current checkouts from history
            val currentCheckouts = allCheckoutLogs.filter { it.checkInTime == null }
            val pastCheckouts = allCheckoutLogs.filter { it.checkInTime != null }
                .sortedByDescending { it.checkOutTime } // Most recent first
            
            // Get item details for current checkouts
            val currentItems = mutableListOf<Pair<Item, CheckoutLog>>()
            for (checkout in currentCheckouts) {
                val item = itemViewModel.getItemById(checkout.itemId)
                if (item != null) {
                    currentItems.add(Pair(item, checkout))
                }
            }
            currentAssignments = currentItems
            
            // Get item details for checkout history
            val historyItems = mutableListOf<Triple<Item, CheckoutLog, Long?>>()
            for (checkout in pastCheckouts) {
                val item = itemViewModel.getItemById(checkout.itemId)
                if (item != null) {
                    // Calculate checkout duration in hours
                    val duration = checkout.checkInTime?.let { checkInTime ->
                        val diff = checkInTime - checkout.checkOutTime
                        TimeUnit.MILLISECONDS.toHours(diff)
                    }
                    historyItems.add(Triple(item, checkout, duration))
                }
            }
            checkoutHistory = historyItems
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(staff?.name ?: "Staff Details") },
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
        ) {
            staff?.let { currentStaff ->
                // Staff profile card
                StaffProfileCard(
                    staff = currentStaff,
                    modifier = Modifier.padding(16.dp),
                    onDeleteClick = { showDeleteDialog = true },
                    onUnarchive = {
                        // Refresh the screen after unarchiving
                        refreshTrigger++
                    }
                )
                
                // Tabs for assignments and history
                TabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { 
                            Text(
                                "Current Assignments (${currentAssignments.size})",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        }
                    )
                    
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { 
                            Text(
                                "Checkout History (${checkoutHistory.size})",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        }
                    )
                }
                
                // Content based on selected tab
                when (selectedTabIndex) {
                    0 -> {
                        // Current assignments tab
                        LazyColumn(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            if (currentAssignments.isEmpty()) {
                                item {
                                    Text(
                                        text = "No items currently assigned",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            } else {
                                items(currentAssignments) { (item, checkout) ->
                                    AssignedItemCard(
                                        item = item, 
                                        checkout = checkout,
                                        onItemClick = { onItemClick(item.id) },
                                        onCheckIn = {
                                            coroutineScope.launch {
                                                checkoutViewModel.checkInItem(checkout.id)
                                                
                                                // Refresh both lists after check-in
                                                staffId?.let { id ->
                                                    // Reload all checkout logs
                                                    val allLogs = checkoutViewModel.getCheckoutLogsByStaff(id).first()
                                                    
                                                    // Update current checkouts
                                                    val currentCheckouts = allLogs.filter { it.checkInTime == null }
                                                    val currentItems = mutableListOf<Pair<Item, CheckoutLog>>()
                                                    for (co in currentCheckouts) {
                                                        val updatedItem = itemViewModel.getItemById(co.itemId)
                                                        if (updatedItem != null) {
                                                            currentItems.add(Pair(updatedItem, co))
                                                        }
                                                    }
                                                    currentAssignments = currentItems
                                                    
                                                    // Update history
                                                    val pastCheckouts = allLogs.filter { it.checkInTime != null }
                                                        .sortedByDescending { it.checkOutTime }
                                                    
                                                    val historyItems = mutableListOf<Triple<Item, CheckoutLog, Long?>>()
                                                    for (co in pastCheckouts) {
                                                        val historyItem = itemViewModel.getItemById(co.itemId)
                                                        if (historyItem != null) {
                                                            val duration = co.checkInTime?.let { checkInTime ->
                                                                val diff = checkInTime - co.checkOutTime
                                                                TimeUnit.MILLISECONDS.toHours(diff)
                                                            }
                                                            historyItems.add(Triple(historyItem, co, duration))
                                                        }
                                                    }
                                                    checkoutHistory = historyItems
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                    
                    1 -> {
                        // Checkout history tab
                        LazyColumn(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            if (checkoutHistory.isEmpty()) {
                                item {
                                    Text(
                                        text = "No checkout history available",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            } else {
                                items(checkoutHistory) { (item, checkout, duration) ->
                                    CheckoutHistoryCard(
                                        item = item,
                                        checkout = checkout,
                                        duration = duration,
                                        onItemClick = { onItemClick(item.id) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
                
            } ?: run {
                // Show loading or error state if staff not found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Staff not found or still loading...")
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Archive Staff") },
            text = { Text("Are you sure you want to archive this staff member? They will be marked as inactive but their data and checkout history will be preserved.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        staff?.let {
                            coroutineScope.launch {
                                staffViewModel.archiveStaff(it)
                                showDeleteDialog = false
                                onNavigateBack()
                            }
                        }
                    }
                ) {
                    Text("Archive", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StaffProfileCard(
    staff: Staff, 
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit = {},
    onUnarchive: () -> Unit = {}
) {
    val staffViewModel = staffViewModel()
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Profile header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = staff.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = staff.department,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    if (staff.position.isNotBlank()) {
                        Text(
                            text = staff.position,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Show archived status if staff is inactive
                    if (!staff.isActive) {
                        Text(
                            text = "Archived",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // If staff is archived, show Unarchive button, otherwise show Archive button
                if (!staff.isActive) {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            coroutineScope.launch {
                                // Update staff to be active again
                                val updatedStaff = staff.copy(isActive = true, lastModified = System.currentTimeMillis())
                                staffViewModel.updateStaff(updatedStaff)
                                onUnarchive() // Call the callback to refresh
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Unarchive")
                    }
                } else {
                    androidx.compose.material3.TextButton(
                        onClick = onDeleteClick,
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Archive")
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Contact details
            if (staff.email.isNotBlank()) {
                Row {
                    Text("Email: ", fontWeight = FontWeight.Bold)
                    Text(staff.email)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            if (staff.phone.isNotBlank()) {
                Row {
                    Text("Phone: ", fontWeight = FontWeight.Bold)
                    Text(staff.phone)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Staff ID
            Text(
                text = "ID: ${staff.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignedItemCard(
    item: Item,
    checkout: CheckoutLog,
    onItemClick: () -> Unit,
    onCheckIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Type: ${item.type}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Condition: ${item.condition}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Checked out: ${formatDate(checkout.checkOutTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            androidx.compose.material3.TextButton(
                onClick = onCheckIn,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Check In")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutHistoryCard(
    item: Item,
    checkout: CheckoutLog,
    duration: Long?,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Type: ${item.type}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Checkout details
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Checked out:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDateTime(checkout.checkOutTime),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Checked in:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    checkout.checkInTime?.let {
                        Text(
                            text = formatDateTime(it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } ?: Text("N/A")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Duration (if available)
            duration?.let {
                Text(
                    text = "Duration: ${formatDuration(it)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

@Composable
fun formatDateTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

@Composable
fun formatDuration(hours: Long): String {
    return when {
        hours < 1 -> "Less than 1 hour"
        hours == 1L -> "1 hour"
        hours < 24 -> "$hours hours"
        hours == 24L -> "1 day"
        else -> "${hours / 24} days, ${hours % 24} hours"
    }
} 