package com.example.inventory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventory.data.database.CheckoutLog
import com.example.inventory.ui.viewmodel.CheckoutViewModel
import com.example.inventory.ui.viewmodel.ItemViewModel
import com.example.inventory.ui.viewmodel.StaffViewModel
import com.example.inventory.ui.viewmodel.checkoutViewModel
import com.example.inventory.ui.viewmodel.itemViewModel
import com.example.inventory.ui.viewmodel.staffViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit
) {
    val checkoutViewModel = checkoutViewModel()
    val itemViewModel = itemViewModel()
    val staffViewModel = staffViewModel()
    
    val currentCheckouts by checkoutViewModel.currentCheckouts.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Current Checkouts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (currentCheckouts.isEmpty()) {
                item {
                    Text(
                        "No items are currently checked out",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                items(currentCheckouts) { checkout ->
                    CheckoutCard(
                        checkout = checkout,
                        onCheckIn = {
                            coroutineScope.launch {
                                checkoutViewModel.checkInItem(checkout.id)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CheckoutCard(
    checkout: CheckoutLog,
    onCheckIn: () -> Unit
) {
    val itemViewModel = itemViewModel()
    val staffViewModel = staffViewModel()
    val coroutineScope = rememberCoroutineScope()
    
    var itemName by remember { mutableStateOf("") }
    var staffName by remember { mutableStateOf("") }
    
    // Use LaunchedEffect instead of directly launching a coroutine in composition
    LaunchedEffect(checkout) {
        launch {
            val item = itemViewModel.getItemById(checkout.itemId)
            val staff = staffViewModel.getStaffById(checkout.staffId)
            
            item?.let { itemName = it.name }
            staff?.let { staffName = it.name }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                Text("Item: ", fontWeight = FontWeight.Bold)
                Text(itemName.ifEmpty { "Loading..." })
            }
            
            Row {
                Text("Staff: ", fontWeight = FontWeight.Bold)
                Text(staffName.ifEmpty { "Loading..." })
            }
            
            Row {
                Text("Checked Out: ", fontWeight = FontWeight.Bold)
                Text(formatTimestamp(checkout.checkOutTime))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onCheckIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check In")
            }
        }
    }
}

@Composable
fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
} 