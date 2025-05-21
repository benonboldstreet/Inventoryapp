package com.example.inventory.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.inventory.data.model.Item
import com.example.inventory.ui.components.StatusIndicator
import com.example.inventory.ui.components.getStatusColor
import com.example.inventory.ui.viewmodel.itemViewModel
import com.example.inventory.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import com.example.inventory.util.FirestoreChecker
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Enum for item filtering
enum class ItemFilter {
    ALL, ACTIVE, ARCHIVED
}

// Simple ItemRepository singleton for accessing categories
class ItemRepository private constructor(private val context: android.content.Context) {
    companion object {
        @Volatile
        private var INSTANCE: ItemRepository? = null
        
        fun getRepository(context: android.content.Context): ItemRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ItemRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
    
    // Get the real repository from the app container
    private val appContainer = (context.applicationContext as com.example.inventory.InventoryApplication).container
    private val realRepository = appContainer.itemRepository
    
    // Method to get all categories from database
    suspend fun getAllCategories(): Flow<List<String>> {
        return realRepository.getAllCategories()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    onItemClick: (UUID) -> Unit,
    onBarcodeScanner: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val viewModel = itemViewModel()
    val allItems by viewModel.allItems.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State for direct query results
    var directQueryResults by remember { mutableStateOf<List<Item>>(emptyList()) }
    
    var showAddItemDialog by remember { mutableStateOf(false) }
    val scannedBarcode by SharedViewModel.scannedBarcode.collectAsState()
    val isCloudConnected by SharedViewModel.isCloudConnected.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var itemFilter by remember { mutableStateOf(ItemFilter.ACTIVE) }
    
    // Get the showArchivedItems flag from SharedViewModel
    val showArchivedItems by SharedViewModel.showArchivedItems.collectAsState()
    
    // React to showArchivedItems changes
    LaunchedEffect(showArchivedItems) {
        if (showArchivedItems) {
            // Switch to archived items filter
            itemFilter = ItemFilter.ARCHIVED
            // Reset the flag after we've used it
            SharedViewModel.setShowArchivedItems(false)
        }
    }
    
    // When switching to ARCHIVED filter, perform a direct query
    LaunchedEffect(itemFilter) {
        android.util.Log.d("ItemListScreen", "Filter changed to: $itemFilter")
        
        if (itemFilter == ItemFilter.ARCHIVED) {
            android.util.Log.d("ItemListScreen", "Querying archived items directly from Firestore")
            // Use the direct query method instead of filtering
            viewModel.directQueryArchivedItems { items ->
                directQueryResults = items
                android.util.Log.d("ItemListScreen", "Received ${items.size} archived items from direct query")
            }
        }
    }
    
    // Choose items source based on filter type
    val itemsToFilter = if (itemFilter == ItemFilter.ARCHIVED && directQueryResults.isNotEmpty()) {
        // Use direct query results for archived items
        android.util.Log.d("ItemListScreen", "Using direct query results for archived items: ${directQueryResults.size} items")
        directQueryResults
    } else {
        // Use normal flow for active and all items
        allItems
    }
    
    // Filter items based on active status and search query only
    val filteredItems = itemsToFilter.filter { item ->
        // First filter by active/archived status
        when (itemFilter) {
            ItemFilter.ALL -> true
            ItemFilter.ACTIVE -> item.isActive
            ItemFilter.ARCHIVED -> !item.isActive
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
    
    // Check for scanned barcode from the shared view model
    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode.isNotEmpty()) {
            showAddItemDialog = true
        }
    }
    
    // Inside the composable, add this logging after the allItems collection
    LaunchedEffect(allItems) {
        android.util.Log.d("ItemListScreen", "==== ITEMS DEBUG ====")
        android.util.Log.d("ItemListScreen", "Total items from repository: ${allItems.size}")
        android.util.Log.d("ItemListScreen", "Active items: ${allItems.count { it.isActive }}")
        android.util.Log.d("ItemListScreen", "Inactive (archived) items: ${allItems.count { !it.isActive }}")
        
        // Log each archived item for debugging
        allItems.filter { !it.isActive }.forEach { item ->
            android.util.Log.d("ItemListScreen", "Archived item: id=${item.id}, name=${item.name}")
        }
    }

    // Add this logging when the filter changes
    LaunchedEffect(itemFilter) {
        android.util.Log.d("ItemListScreen", "Filter changed to: $itemFilter")
        android.util.Log.d("ItemListScreen", "Filtered items count: ${filteredItems.size}")
    }
    
    // Add this right after setting the filteredItems but before the LaunchedEffect
    // Log detailed information when in ARCHIVED mode
    if (itemFilter == ItemFilter.ARCHIVED) {
        android.util.Log.d("ItemListScreen", "===== ARCHIVED VIEW DETAILS =====")
        android.util.Log.d("ItemListScreen", "Archived filter is active")
        android.util.Log.d("ItemListScreen", "Filtered items count: ${filteredItems.size}")
        android.util.Log.d("ItemListScreen", "Direct query results count: ${directQueryResults.size}")
        
        if (filteredItems.isEmpty()) {
            android.util.Log.d("ItemListScreen", "No archived items to display")
            
            // Double check if there are actually inactive items in the dataset
            val inactiveItems = allItems.filter { !it.isActive }
            if (inactiveItems.isNotEmpty()) {
                android.util.Log.e("ItemListScreen", "ERROR: Found ${inactiveItems.size} inactive items but they're not being displayed!")
                inactiveItems.forEach { item ->
                    android.util.Log.d("ItemListScreen", "  Inactive item not displayed: id=${item.id}, name=${item.name}, isActive=${item.isActive}")
                }
            }
        } else {
            android.util.Log.d("ItemListScreen", "Displaying ${filteredItems.size} archived items")
            filteredItems.forEach { item ->
                android.util.Log.d("ItemListScreen", "  Displaying archived item: id=${item.id}, name=${item.name}")
            }
        }
    }
    
    // After the existing button for checking Firestore directly
    if (itemFilter == ItemFilter.ARCHIVED) {
        // Existing debug info column is still here
        
        // Add a button to directly check Firestore
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        // Call FirestoreChecker utility
                        FirestoreChecker.checkArchivedItems(context)
                        
                        // Also run direct query again
                        viewModel.directQueryArchivedItems { items ->
                            directQueryResults = items
                            Toast.makeText(
                                context,
                                "Direct query found ${items.size} archived items",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ItemListScreen", "Error checking archived items: ${e.message}", e)
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text("Check Firestore Directly")
        }
        
        // Simplify the Fix Archived Items button
        Button(
            onClick = {
                // Show message first
                Toast.makeText(
                    context,
                    "Checking for archived items...",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Just run the direct query to show what's available
                viewModel.directQueryArchivedItems { items ->
                    directQueryResults = items
                    if (items.isNotEmpty()) {
                        Toast.makeText(
                            context,
                            "Found ${items.size} archived items",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "No archived items found. Try archiving an item first.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Find Archived Items", color = Color.White)
        }
        
        // Add a button to create a test archived item directly
        Button(
            onClick = {
                // Create a test item directly in Firestore
                val firestore = FirebaseFirestore.getInstance()
                val testItemId = UUID.randomUUID().toString()
                
                // Create a map with all required item fields
                val testItem = mapOf(
                    "idString" to testItemId,
                    "name" to "Test Archived Item",
                    "category" to "Test",
                    "type" to "Debug",
                    "barcode" to "TEST123",
                    "condition" to "Good",
                    "status" to "Available",
                    "description" to "This is a test archived item",
                    "photoPath" to "",
                    "isActive" to false,  // Explicitly set as Boolean false
                    "lastModified" to System.currentTimeMillis()
                )
                
                // Add the item to Firestore
                firestore.collection("items").document(testItemId)
                    .set(testItem)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context, 
                            "Test archived item created in Firestore", 
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Refresh to show the new item
                        viewModel.directQueryArchivedItems { items ->
                            directQueryResults = items
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context, 
                            "Error creating test item: ${e.message}", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Create Test Archived Item", color = Color.White)
        }
    }
    
    // After the filter chips but before the rest of the content
    if (itemFilter == ItemFilter.ARCHIVED) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Archived Items Debug Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Total items from repository: ${allItems.size}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "Items with isActive=false: ${allItems.count { !it.isActive }}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "Direct query results: ${directQueryResults.size}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "Currently displaying: ${filteredItems.size} items",
                style = MaterialTheme.typography.bodySmall,
                color = if (filteredItems.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            
            // Display the first archived item if any
            val inactiveItems = allItems.filter { !it.isActive }
            if (inactiveItems.isNotEmpty()) {
                val firstInactive = inactiveItems.first()
                Text(
                    text = "First inactive item: ${firstInactive.name} (ID: ${firstInactive.id})",
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (directQueryResults.isNotEmpty()) {
                val firstDirect = directQueryResults.first()
                Text(
                    text = "First direct query item: ${firstDirect.name} (ID: ${firstDirect.id}, isActive=${firstDirect.isActive})",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Inventory Items") },
                    actions = {
                        // Cloud connectivity indicator
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = if (isCloudConnected) "Cloud Connected" else "Cloud Disconnected",
                                tint = if (isCloudConnected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                        // QR code scanner
                        IconButton(onClick = { onBarcodeScanner() }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
                        }
                    }
                )
                
                // Debug button to check Firestore data directly
                Button(
                    onClick = {
                        val firestore = FirebaseFirestore.getInstance()
                        coroutineScope.launch {
                            try {
                                // Check all three collections
                                val collectionsToCheck = listOf("items", "staff", "checkouts")
                                val results = StringBuilder("Firestore Collections:\n")
                                
                                for (collection in collectionsToCheck) {
                                    val snapshot = firestore.collection(collection).get().await()
                                    results.append("- $collection: ${snapshot.size()} documents\n")
                                    
                                    // Log details about first document in each collection
                                    if (snapshot.documents.isNotEmpty()) {
                                        val firstDoc = snapshot.documents.first()
                                        results.append("  Fields: ${firstDoc.data?.keys?.joinToString()}\n")
                                        
                                        // Add special check for isActive field
                                        val isActiveField = firstDoc.get("isActive")
                                        if (isActiveField != null) {
                                            results.append("  isActive: $isActiveField (${isActiveField.javaClass.simpleName})\n")
                                        }
                                    }
                                }
                                
                                // Also log Firestore instance details
                                results.append("\nApp Instance Info:\n")
                                results.append("- DB URL: ${firestore.app.options.databaseUrl ?: "null"}\n")
                                results.append("- Project ID: ${firestore.app.options.projectId ?: "null"}\n")
                                
                                // Show in toast and log
                                val message = results.toString()
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                android.util.Log.d("FirestoreDebug", message)
                                
                                // Directly check listeners
                                viewModel.checkRepositoryListeners()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                android.util.Log.e("ItemListDebug", "Error querying Firestore", e)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Debug: Check All Collections")
                }
                
                // Button to add a test item to Firestore
                Button(
                    onClick = {
                        val firestore = FirebaseFirestore.getInstance()
                        val itemId = UUID.randomUUID().toString()
                        val testItem = mapOf(
                            "idString" to itemId,
                            "name" to "Test Item ${System.currentTimeMillis()}",
                            "category" to "Test Category",
                            "type" to "Test Type",
                            "barcode" to "TEST-${System.currentTimeMillis()}",
                            "condition" to "Good",
                            "status" to "Available",
                            "description" to "Test item created from debug button",
                            "photoPath" to null,
                            "isActive" to true,
                            "lastModified" to System.currentTimeMillis()
                        )
                        
                        coroutineScope.launch {
                            try {
                                // Add the item
                                firestore.collection("items").document(itemId).set(testItem).await()
                                // Verify it was added
                                val doc = firestore.collection("items").document(itemId).get().await()
                                if (doc.exists()) {
                                    val message = "Test item created successfully! ID: $itemId"
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    android.util.Log.d("ItemListDebug", "Created test item: $testItem")
                                } else {
                                    Toast.makeText(context, "Failed to create test item", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                android.util.Log.e("ItemListDebug", "Error creating test item", e)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Debug: Create Test Item")
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddItemDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                text = { Text("Add Item") }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = bottomBar
    ) { paddingValues ->
        
        if (showAddItemDialog) {
            AddItemDialog(
                onDismiss = { 
                    showAddItemDialog = false 
                    SharedViewModel.clearBarcode()
                },
                onConfirm = { item ->
                    viewModel.addItem(item)
                    showAddItemDialog = false
                    SharedViewModel.clearBarcode()
                },
                onScanBarcode = onBarcodeScanner,
                initialBarcode = scannedBarcode
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Improved search bar with rounded corners and icon
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text("Search items by name, barcode, type, etc.") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Text("Clear", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
            }
            
            // Filter chips in a row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = itemFilter == ItemFilter.ACTIVE,
                    onClick = { itemFilter = ItemFilter.ACTIVE },
                    label = { Text("Active") }
                )
                
                FilterChip(
                    selected = itemFilter == ItemFilter.ARCHIVED,
                    onClick = { itemFilter = ItemFilter.ARCHIVED },
                    label = { Text("Archived") }
                )
                
                FilterChip(
                    selected = itemFilter == ItemFilter.ALL,
                    onClick = { itemFilter = ItemFilter.ALL },
                    label = { Text("All") }
                )
            }
            
            // No items found message
            if (filteredItems.isEmpty()) {
                // Add detailed debug logging
                android.util.Log.d("ItemListScreen", "=== DEBUG: EMPTY FILTERED ITEMS ===")
                android.util.Log.d("ItemListScreen", "Current filter: $itemFilter")
                android.util.Log.d("ItemListScreen", "Total items in dataset: ${allItems.size}")
                android.util.Log.d("ItemListScreen", "Active items: ${allItems.count { it.isActive }}")
                android.util.Log.d("ItemListScreen", "Archived items: ${allItems.count { !it.isActive }}")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when {
                                searchQuery.isNotEmpty() -> "No items match '$searchQuery'"
                                else -> when(itemFilter) {
                                    ItemFilter.ALL -> "No items found"
                                    ItemFilter.ACTIVE -> "No active items found"
                                    ItemFilter.ARCHIVED -> "No archived items found"
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Tap the + button to add a new item",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Items grid instead of list for better visual display
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { item ->
                        ItemGridCard(
                            item = item,
                            onClick = { onItemClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

// New grid-style card for items
@Composable
fun ItemGridCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status indicator at the top
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatusIndicator(
                    status = item.status,
                    modifier = Modifier.size(12.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.labelMedium,
                    color = getStatusColor(item.status)
                )
            }
            
            // Item name with potential strikethrough if archived
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (!item.isActive) TextDecoration.LineThrough else TextDecoration.None
            )
            
            // Bottom info section
            Column {
                Text(
                    text = item.type,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!item.isActive) {
                    Text(
                        text = "Archived",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Keep original ItemCard for possible reuse
@Composable
fun ItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status indicator circle
                StatusIndicator(
                    status = item.status,
                    modifier = Modifier
                        .size(12.dp)
                        .padding(end = 4.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    // Item name with strikethrough if archived
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (!item.isActive) TextDecoration.LineThrough else TextDecoration.None
                    )
                    
                    Text(
                        text = item.type,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = getStatusColor(item.status)
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (item: Item) -> Unit,
    onScanBarcode: () -> Unit,
    initialBarcode: String = ""
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf(initialBarcode) }
    var condition by remember { mutableStateOf("Good") }
    var status by remember { mutableStateOf("Available") }
    var description by remember { mutableStateOf("") }
    
    // Set barcode if initialBarcode is provided
    LaunchedEffect(initialBarcode) {
        if (initialBarcode.isNotEmpty()) {
            barcode = initialBarcode
        }
    }
    
    // Condition options
    val conditions = listOf("Excellent", "Good", "Fair", "Poor")
    var expandedCondition by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add New Item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Barcode with scanner button
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode *") },
                    placeholder = { Text("Scan or enter barcode") },
                    trailingIcon = {
                        IconButton(onClick = onScanBarcode) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Barcode"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    placeholder = { Text("Enter item name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Type field (simple, no categories)
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type *") },
                    placeholder = { Text("Enter item type") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Enter item description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Condition dropdown
                Column {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = { condition = it },
                        label = { Text("Condition") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.clickable { expandedCondition = !expandedCondition }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = expandedCondition,
                        onDismissRequest = { expandedCondition = false }
                    ) {
                        conditions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    condition = option
                                    expandedCondition = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (name.isNotBlank() && barcode.isNotBlank() && type.isNotBlank()) {
                                val newItem = Item(
                                    id = UUID.randomUUID(),
                                    name = name,
                                    type = type,
                                    barcode = barcode,
                                    category = type, // Use type as category for backward compatibility
                                    condition = condition,
                                    status = status,
                                    description = description,
                                    isActive = true,
                                    lastModified = System.currentTimeMillis(),
                                    photoPath = null
                                )
                                onConfirm(newItem)
                            }
                        },
                        enabled = name.isNotBlank() && barcode.isNotBlank() && type.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
} 