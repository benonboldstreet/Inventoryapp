package com.example.inventory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventory.data.database.CheckoutLog
import com.example.inventory.data.database.Item
import com.example.inventory.data.database.Staff
import com.example.inventory.ui.viewmodel.checkoutViewModel
import com.example.inventory.ui.viewmodel.itemViewModel
import com.example.inventory.ui.viewmodel.staffViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CheckoutReport(
    val item: Item,
    val staff: Staff,
    val checkout: CheckoutLog
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    bottomBar: @Composable () -> Unit
) {
    val checkoutViewModel = checkoutViewModel()
    val itemViewModel = itemViewModel()
    val staffViewModel = staffViewModel()
    
    var searchQuery by remember { mutableStateOf("") }
    var currentCheckouts by remember { mutableStateOf<List<CheckoutReport>>(emptyList()) }
    var recentCheckoutHistory by remember { mutableStateOf<List<CheckoutReport>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) } // 0 for current, 1 for history
    val coroutineScope = rememberCoroutineScope()
    
    // Load current checkouts and history
    LaunchedEffect(Unit) {
        // Load current checkouts
        loadCheckedOutItems(
            checkoutViewModel = checkoutViewModel,
            itemViewModel = itemViewModel,
            staffViewModel = staffViewModel,
            onReportsLoaded = { reports -> 
                currentCheckouts = reports
            }
        )
        
        // Load recent checkout history (completed checkouts)
        loadCheckoutHistory(
            checkoutViewModel = checkoutViewModel,
            itemViewModel = itemViewModel,
            staffViewModel = staffViewModel,
            onHistoryLoaded = { reports ->
                recentCheckoutHistory = reports
            }
        )
    }
    
    // Filter reports based on search query and selected tab
    val reportsList = if (selectedTab == 0) currentCheckouts else recentCheckoutHistory
    val filteredReports = if (searchQuery.isBlank()) {
        reportsList
    } else {
        reportsList.filter { report ->
            report.item.name.contains(searchQuery, ignoreCase = true) ||
            report.staff.name.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Reports") }
            )
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search by item or staff name") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
            
            // Tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                androidx.compose.material3.FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Current Checkouts") },
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                androidx.compose.material3.FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Checkout History") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Header with summary stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (selectedTab == 0) "Current Checkouts" else "Checkout History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Total Items: ${reportsList.size}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "Search Results: ${filteredReports.size}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // List of items
            if (filteredReports.isEmpty()) {
                Text(
                    text = if (reportsList.isEmpty()) 
                        if (selectedTab == 0) "No items are currently checked out." else "No checkout history available."
                    else 
                        "No items match your search.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    items(filteredReports) { report ->
                        if (selectedTab == 0) {
                            // Current checkouts without check-in button
                            CheckoutReportCard(
                                report = report,
                                onCheckIn = {} // Empty callback as we're making it view-only
                            )
                        } else {
                            // Checkout history with timestamps
                            CheckoutHistoryCard(report = report)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

private suspend fun loadCheckedOutItems(
    checkoutViewModel: com.example.inventory.ui.viewmodel.CheckoutViewModel,
    itemViewModel: com.example.inventory.ui.viewmodel.ItemViewModel,
    staffViewModel: com.example.inventory.ui.viewmodel.StaffViewModel,
    onReportsLoaded: (List<CheckoutReport>) -> Unit
) {
    // Get current checkouts
    val checkouts = checkoutViewModel.currentCheckouts.first()
    
    // Build reports with item and staff details
    val reports = mutableListOf<CheckoutReport>()
    for (checkout in checkouts) {
        val item = itemViewModel.getItemById(checkout.itemId)
        val staff = staffViewModel.getStaffById(checkout.staffId)
        
        if (item != null && staff != null) {
            reports.add(CheckoutReport(item = item, staff = staff, checkout = checkout))
        }
    }
    
    onReportsLoaded(reports)
}

private suspend fun loadCheckoutHistory(
    checkoutViewModel: com.example.inventory.ui.viewmodel.CheckoutViewModel,
    itemViewModel: com.example.inventory.ui.viewmodel.ItemViewModel,
    staffViewModel: com.example.inventory.ui.viewmodel.StaffViewModel,
    onHistoryLoaded: (List<CheckoutReport>) -> Unit
) {
    // Get all checkout logs
    val allLogs = checkoutViewModel.allCheckoutLogs.first()
    
    // Filter for completed checkouts (those with checkInTime not null)
    val completedCheckouts = allLogs.filter { it.checkInTime != null }
        .sortedByDescending { it.checkInTime } // Most recent check-ins first
    
    // Build reports with item and staff details
    val reports = mutableListOf<CheckoutReport>()
    for (checkout in completedCheckouts) {
        val item = itemViewModel.getItemById(checkout.itemId)
        val staff = staffViewModel.getStaffById(checkout.staffId)
        
        if (item != null && staff != null) {
            reports.add(CheckoutReport(item = item, staff = staff, checkout = checkout))
        }
    }
    
    onHistoryLoaded(reports)
}

@Composable
fun CheckoutReportCard(
    report: CheckoutReport,
    onCheckIn: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item type and name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Type: ${report.item.type}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Checkout date
                Text(
                    text = formatDateShort(report.checkout.checkOutTime),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Top)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Staff info without check-in button
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Assigned to: ${report.staff.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Department: ${report.staff.department}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CheckoutHistoryCard(
    report: CheckoutReport
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item type and name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Type: ${report.item.type}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Staff: ${report.staff.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Department: ${report.staff.department}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Checkout and checkin timestamps
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            
            Text(
                text = "Checked out: ${dateFormat.format(Date(report.checkout.checkOutTime))}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            report.checkout.checkInTime?.let { checkInTime ->
                Text(
                    text = "Checked in: ${dateFormat.format(Date(checkInTime))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Calculate duration if checked in
            report.checkout.checkInTime?.let { checkInTime ->
                val durationMillis = checkInTime - report.checkout.checkOutTime
                val hours = durationMillis / (1000 * 60 * 60)
                val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
                
                Text(
                    text = "Duration: ${hours}h ${minutes}m",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun formatDateShort(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
} 