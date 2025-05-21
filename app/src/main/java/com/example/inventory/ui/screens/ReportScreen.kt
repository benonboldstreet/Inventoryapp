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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

/**
 * A data class to hold the information for checkout reports
 */
data class CheckoutReport(
    val checkoutLog: CheckoutLog,
    val item: Item,
    val staff: Staff,
    val checkoutDate: String,
    val checkInDate: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    bottomBar: @Composable () -> Unit
) {
    val checkoutViewModel = checkoutViewModel()
    val itemViewModel = itemViewModel()
    val staffViewModel = staffViewModel()
    val coroutineScope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableStateOf(0) }
    var checkoutReports by remember { mutableStateOf<List<CheckoutReport>>(emptyList()) }
    
    // Fetch active checkouts and generate reports
    LaunchedEffect(selectedTab) {
        coroutineScope.launch {
            val checkoutLogs = when (selectedTab) {
                0 -> checkoutViewModel.activeCheckouts.first() // Active checkouts
                1 -> checkoutViewModel.completedCheckouts.first() // Completed checkouts
                else -> emptyList()
            }
            
            val reports = mutableListOf<CheckoutReport>()
            
            for (log in checkoutLogs) {
                val itemData = itemViewModel.getItemById(log.itemId).first()
                val staffData = staffViewModel.getStaffById(log.staffId).first()
                
                if (itemData != null && staffData != null) {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val checkoutDate = dateFormat.format(Date(log.getCheckOutTimeAsLong()))
                    val checkInDate = log.getCheckInTimeAsLong()?.let { dateFormat.format(Date(it)) }
                    
                    reports.add(
                        CheckoutReport(
                            checkoutLog = log,
                            item = itemData,
                            staff = staffData,
                            checkoutDate = checkoutDate,
                            checkInDate = checkInDate
                        )
                    )
                }
            }
            
            checkoutReports = reports
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Reports") }
            )
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Inventory Reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Current Checkouts") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Checkout History") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Export button
            Button(
                onClick = { /* TODO: Implement export functionality */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export Report")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (checkoutReports.isEmpty()) {
                // Show empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.height(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = when (selectedTab) {
                            0 -> "No active checkouts"
                            1 -> "No checkout history"
                            else -> "No data available"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when (selectedTab) {
                            0 -> "All items are currently available"
                            1 -> "No items have been checked out yet"
                            else -> "Please try again later"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Show report list
                LazyColumn {
                    items(checkoutReports) { report ->
                        ReportCard(report = report)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCard(report: CheckoutReport) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Item information
            Text(
                text = report.item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Category: ${report.item.category}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Staff information
            Text(
                text = "Checked out by: ${report.staff.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Department: ${report.staff.department}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Checkout dates
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Checkout date: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = report.checkoutDate,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (report.checkInDate != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Return date: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = report.checkInDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Green
                    )
                }
            } else {
                Text(
                    text = "Status: Currently checked out",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 