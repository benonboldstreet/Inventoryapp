package com.example.inventory.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.database.Item
import com.example.inventory.ui.components.StatusIndicator
import com.example.inventory.ui.components.getStatusColor
import com.example.inventory.ui.viewmodel.itemViewModel
import com.example.inventory.ui.viewmodel.checkoutViewModel
import com.example.inventory.ui.viewmodel.staffViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import java.util.UUID

// Enum for item filtering
enum class CategoryItemFilter {
    ALL, ACTIVE, ARCHIVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupedItemsScreen(
    onBarcodeScanner: () -> Unit,
    onItemClick: (Item) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val viewModel = itemViewModel()
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }
    var itemFilter by remember { mutableStateOf(CategoryItemFilter.ACTIVE) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Collect items with error handling
    LaunchedEffect(key1 = viewModel) {
        try {
            viewModel.allItems.collect { itemsList ->
                items = itemsList
            }
        } catch (e: Exception) {
            // Check for database integrity error specifically
            val errorMessage = e.message ?: ""
            error = if (errorMessage.contains("unable to verify the data integrity") || 
                         errorMessage.contains("schema") || 
                         errorMessage.contains("version")) {
                "Database schema has changed. Please restart the app to update."
            } else {
                "Error loading items: ${e.message}"
            }
        }
    }
    
    // Filter items based on active status and search query
    val filteredItems = items.filter { item ->
        // First filter by active/archived status
        when (itemFilter) {
            CategoryItemFilter.ALL -> true
            CategoryItemFilter.ACTIVE -> item.isActive
            CategoryItemFilter.ARCHIVED -> !item.isActive
        }
    }.filter { item ->
        // Then filter by search query if one exists
        if (searchQuery.isBlank()) true
        else {
            item.name.contains(searchQuery, ignoreCase = true) || 
            item.type.contains(searchQuery, ignoreCase = true) ||
            item.barcode.contains(searchQuery, ignoreCase = true) ||
            item.category.contains(searchQuery, ignoreCase = true)
        }
    }
    
    // Group filtered items by category
    val groupedItems = filteredItems.groupBy { it.category }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory by Category") },
                actions = {
                    // Add barcode scanner button in the top app bar
                    IconButton(onClick = onBarcodeScanner) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode"
                        )
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        if (error != null) {
            // Show error state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "An error occurred",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Red
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(error ?: "Unknown error")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        error = null
                        // Force a refresh
                        coroutineScope.launch {
                            try {
                                // Attempt to get items again
                                val refreshedItems = viewModel.allItems.first()
                                items = refreshedItems
                            } catch (e: Exception) {
                                error = "Error refreshing: ${e.message}"
                            }
                        }
                    }
                ) {
                    Text("Retry")
                }
            }
        } else {
            // Regular content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search by name, type, barcode or category") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true
                )
                
                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = itemFilter == CategoryItemFilter.ACTIVE,
                        onClick = { itemFilter = CategoryItemFilter.ACTIVE },
                        label = { Text("Active") }
                    )
                    
                    FilterChip(
                        selected = itemFilter == CategoryItemFilter.ARCHIVED,
                        onClick = { itemFilter = CategoryItemFilter.ARCHIVED },
                        label = { Text("Archived") }
                    )
                    
                    FilterChip(
                        selected = itemFilter == CategoryItemFilter.ALL,
                        onClick = { itemFilter = CategoryItemFilter.ALL },
                        label = { Text("All") }
                    )
                }
                
                if (groupedItems.isEmpty()) {
                    Text(
                        text = when (itemFilter) {
                            CategoryItemFilter.ALL -> if (searchQuery.isBlank()) "No items found" else "No results for '$searchQuery'"
                            CategoryItemFilter.ACTIVE -> if (searchQuery.isBlank()) "No active items found" else "No active items match '$searchQuery'"
                            CategoryItemFilter.ARCHIVED -> if (searchQuery.isBlank()) "No archived items found" else "No archived items match '$searchQuery'"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        groupedItems.forEach { (category, itemsInCategory) ->
                            item {
                                CategoryHeader(
                                    text = category,
                                    count = itemsInCategory.size,
                                    expanded = expandedCategories.contains(category),
                                    onClick = {
                                        expandedCategories = if (expandedCategories.contains(category)) {
                                            expandedCategories - category
                                        } else {
                                            expandedCategories + category
                                        }
                                    }
                                )
                            }
                            
                            if (expandedCategories.contains(category)) {
                                items(itemsInCategory) { item ->
                                    ItemCardCompact(
                                        item = item,
                                        onClick = { onItemClick(item) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    text: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "($count)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
    }
}

@Composable
fun ItemCardCompact(
    item: Item,
    onClick: () -> Unit
) {
    val checkoutViewModel = checkoutViewModel()
    val staffViewModel = staffViewModel()
    val coroutineScope = rememberCoroutineScope()
    
    // Track if the item data has been updated
    var itemState by remember { mutableStateOf(item) }
    
    // State for storing checked-out staff name
    var checkedOutToName by remember { mutableStateOf<String?>(null) }
    
    // Load staff name if item is checked out
    LaunchedEffect(itemState) {
        if (itemState.status == "Checked Out") {
            try {
                val checkout = checkoutViewModel.getCurrentCheckoutForItem(itemState.id)
                if (checkout != null) {
                    val staff = staffViewModel.getStaffById(checkout.staffId)
                    checkedOutToName = staff?.name
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Status indicator circle at the top right
            StatusIndicator(
                status = itemState.status,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = itemState.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Brand: ${itemState.type}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Barcode: ${itemState.barcode}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Condition: ${itemState.condition}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Status: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = itemState.status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = getStatusColor(itemState.status)
                    )
                }
                
                // Display who the item is checked out to
                if (itemState.status == "Checked Out" && !checkedOutToName.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Checked out to: $checkedOutToName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIndicator(
                status = item.status,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (!item.isActive) TextDecoration.LineThrough else TextDecoration.None
                )
                
                Text(
                    text = "Type: ${item.type}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (!item.isActive) {
                    Text(
                        text = "Archived",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Text(
                text = item.status,
                style = MaterialTheme.typography.bodyMedium,
                color = when (item.status) {
                    "Available" -> MaterialTheme.colorScheme.primary
                    "Checked Out" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
} 