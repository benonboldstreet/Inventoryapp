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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.ui.viewmodel.CheckoutViewModel
import com.example.inventory.ui.viewmodel.ItemViewModel
import com.example.inventory.ui.viewmodel.StaffViewModel
import com.example.inventory.ui.viewmodel.checkoutViewModel
import com.example.inventory.ui.viewmodel.itemViewModel
import com.example.inventory.ui.viewmodel.staffViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDetailScreen(
    staffId: UUID?,
    onNavigateBack: () -> Unit
) {
    val staffViewModel = staffViewModel()
    val checkoutViewModel = checkoutViewModel()
    val itemViewModel = itemViewModel()
    
    var staff by remember { mutableStateOf<Staff?>(null) }
    var checkoutHistory by remember { mutableStateOf<List<CheckoutLog>>(emptyList()) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    
    // For loading item details for each checkout
    var checkoutItemNames by remember { mutableStateOf<Map<UUID, String>>(emptyMap()) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Fetch staff details
    LaunchedEffect(staffId) {
        staffId?.let { id ->
            staffViewModel.getStaffById(id).collect { fetchedStaff ->
                staff = fetchedStaff
                
                // Fetch checkout history for this staff member
                checkoutHistory = checkoutViewModel.getCheckoutsByStaffId(id).first()
                
                // Fetch item names for each checkout
                val itemNameMap = mutableMapOf<UUID, String>()
                checkoutHistory.forEach { checkout ->
                    val item = itemViewModel.getItemById(checkout.itemId).first()
                    item?.let { 
                        itemNameMap[checkout.id] = it.name
                    }
                }
                checkoutItemNames = itemNameMap
            }
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
        },
        floatingActionButton = {
            if (staff != null) {
                FloatingActionButton(
                    onClick = { /* TODO: Implement edit functionality */ }
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            staff?.let { currentStaff ->
                // Staff details card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
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
                                    text = currentStaff.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                // Show archived status if staff is inactive
                                if (!currentStaff.isActive) {
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
                            if (!currentStaff.isActive) {
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            staffViewModel.unarchiveStaff(currentStaff)
                                        }
                                    }
                                ) {
                                    Text("Unarchive")
                                }
                            } else {
                                TextButton(
                                    onClick = { showArchiveDialog = true }
                                ) {
                                    Text("Archive")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Staff details
                        StaffDetailRow("Department", currentStaff.department)
                        
                        if (currentStaff.position.isNotBlank()) {
                            StaffDetailRow("Position", currentStaff.position)
                        }
                        
                        if (currentStaff.email.isNotBlank()) {
                            StaffDetailRow("Email", currentStaff.email)
                        }
                        
                        if (currentStaff.phone.isNotBlank()) {
                            StaffDetailRow("Phone", currentStaff.phone)
                        }
                    }
                }
                
                // Checkout history section
                Text(
                    text = "Checkout History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                if (checkoutHistory.isEmpty()) {
                    Text(
                        text = "No checkout history for this staff member",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    LazyColumn {
                        items(checkoutHistory.sortedByDescending { it.getCheckOutTimeAsLong() }) { checkout ->
                            CheckoutHistoryCard(
                                checkout = checkout,
                                itemName = checkoutItemNames[checkout.id] ?: "Unknown Item"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            } ?: run {
                // Loading or error state
                Text(
                    text = "Staff member not found or still loading...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
    
    // Archive confirmation dialog
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Archive Staff Member") },
            text = { Text("Are you sure you want to archive this staff member? They will be marked as inactive.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        staff?.let {
                            coroutineScope.launch {
                                staffViewModel.archiveStaff(it)
                                showArchiveDialog = false
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
fun StaffDetailRow(
    label: String,
    value: String
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
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CheckoutHistoryCard(
    checkout: CheckoutLog,
    itemName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Item name
            Text(
                text = itemName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Checkout date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            
            Row {
                Text(
                    text = "Checked out: ",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateFormat.format(Date(checkout.getCheckOutTimeAsLong())),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Check-in date if available
            checkout.getCheckInTimeAsLong()?.let { checkInTime ->
                Row {
                    Text(
                        text = "Checked in: ",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = dateFormat.format(Date(checkInTime)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Calculate duration
                val durationMillis = checkInTime - checkout.getCheckOutTimeAsLong()
                val hours = durationMillis / (1000 * 60 * 60)
                val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
                
                Text(
                    text = "Duration: ${hours}h ${minutes}m",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            } ?: run {
                // If still checked out
                Text(
                    text = "Status: Currently checked out",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 