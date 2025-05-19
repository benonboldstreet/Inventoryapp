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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.inventory.data.model.Item
import com.example.inventory.ui.components.StatusIndicator
import com.example.inventory.ui.components.getStatusColor
import com.example.inventory.ui.viewmodel.itemViewModel
import com.example.inventory.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically

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
    
    var showAddItemDialog by remember { mutableStateOf(false) }
    val scannedBarcode = SharedViewModel.scannedBarcode.value
    val recentlyViewedItems = SharedViewModel.recentlyViewedItems.value
    val isCloudConnected = SharedViewModel.isCloudConnected.value
    
    var searchQuery by remember { mutableStateOf("") }
    var itemFilter by remember { mutableStateOf(ItemFilter.ACTIVE) }
    
    // Add these new state variables
    var selectedCategoryTab by remember { mutableStateOf("All") }
    val coroutineScope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Load categories
    LaunchedEffect(Unit) {
        try {
            val repo = ItemRepository.getRepository(context)
            val loadedCategories = repo.getAllCategories().first()
            categories = listOf("All") + loadedCategories
        } catch (e: Exception) {
            // Handle error loading categories
            categories = listOf("All", "Laptop", "Mobile Phone", "Tablet", "Accessory", "Other")
        }
    }
    
    // Filter items based on active status, search query, and category
    val filteredItems = allItems.filter { item ->
        // First filter by active/archived status
        when (itemFilter) {
            ItemFilter.ALL -> true
            ItemFilter.ACTIVE -> item.isActive
            ItemFilter.ARCHIVED -> !item.isActive
        }
    }.filter { item ->
        // Then filter by category if not "All"
        if (selectedCategoryTab == "All") true
        else item.category == selectedCategoryTab
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Items") },
                actions = {
                    // Cloud connectivity indicator
                    if (isCloudConnected) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Connected to Cloud",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    } else {
                        IconButton(onClick = { 
                            // TODO: Add refresh cloud connection logic
                            SharedViewModel.isCloudConnected.value = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Cloud Disconnected - Tap to retry",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
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
                onConfirm = { name, type, barcode, condition, status, category ->
                    viewModel.addItem(
                        name = name,
                        type = type,
                        barcode = barcode,
                        condition = condition,
                        status = status,
                        category = category
                    )
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
            // IMPROVED: Large prominent search bar with rounded corners and icon
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
                    placeholder = { Text("Find items by name, barcode, etc.") },
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
            
            // NEW: Category tabs
            if (categories.isNotEmpty()) {
                TabRow(
                    selectedTabIndex = categories.indexOf(selectedCategoryTab).coerceAtLeast(0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    categories.forEach { category ->
                        Tab(
                            selected = selectedCategoryTab == category,
                            onClick = { selectedCategoryTab = category },
                            text = { Text(category) }
                        )
                    }
                }
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
            
            // NEW: Recently viewed items section
            AnimatedVisibility(
                visible = recentlyViewedItems.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Recently Viewed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentlyViewedItems) { item ->
                            RecentItemCard(
                                item = item,
                                onClick = { onItemClick(item.id) }
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            
            // No items found message
            if (filteredItems.isEmpty()) {
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
                                selectedCategoryTab != "All" -> "No ${selectedCategoryTab.lowercase()} items found"
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
                // Items list
                LazyColumn {
                    items(filteredItems) { item ->
                        ItemCard(
                            item = item,
                            onClick = { 
                                // Add to recently viewed when clicked
                                SharedViewModel.addToRecentlyViewed(item)
                                onItemClick(item.id) 
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// NEW: Card for recently viewed items
@Composable
fun RecentItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status indicator circle at the top
            StatusIndicator(
                status = item.status,
                modifier = Modifier.size(12.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ItemsList(
    items: List<Item>,
    onItemClick: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp)
    ) {
        items(items) { item ->
            ItemCard(
                item = item,
                onClick = { onItemClick(item.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                        text = "Category: ${item.category}",
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
    onConfirm: (name: String, type: String, barcode: String, condition: String, status: String, category: String) -> Unit,
    onScanBarcode: () -> Unit,
    initialBarcode: String = ""
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf(initialBarcode) }
    var condition by remember { mutableStateOf("Good") }
    var status by remember { mutableStateOf("Available") }
    
    // Set barcode if initialBarcode is provided
    LaunchedEffect(initialBarcode) {
        if (initialBarcode.isNotEmpty()) {
            barcode = initialBarcode
        }
    }
    
    // Category selection
    var selectedCategory by remember { mutableStateOf("") }
    var isCustomCategory by remember { mutableStateOf(false) }
    var customCategory by remember { mutableStateOf("") }
    val predefinedCategories = listOf("Laptop", "Mobile Phone", "Tablet", "Accessory", "Custom Category")
    var expandedCategory by remember { mutableStateOf(false) }
    
    // For other categories dropdown
    var expandedOtherCategories by remember { mutableStateOf(false) }
    var otherCategoriesList by remember { mutableStateOf(emptyList<String>()) }
    
    // Load categories from the viewModel
    val context = LocalContext.current
    val itemRepository = ItemRepository.getRepository(context)

    LaunchedEffect(Unit) {
        val categories = itemRepository.getAllCategories().first()
        otherCategoriesList = categories
            .filter { category -> category !in listOf("Laptop", "Mobile Phone", "Tablet", "Accessory") }
            .distinct()
            .sorted()
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
                
                // Category dropdown
                Column {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Category *") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.clickable { expandedCategory = !expandedCategory }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        predefinedCategories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    when (option) {
                                        "Custom Category" -> {
                                            isCustomCategory = true
                                            selectedCategory = ""
                                        }
                                        else -> {
                                            selectedCategory = option
                                            isCustomCategory = false
                                        }
                                    }
                                    expandedCategory = false
                                }
                            )
                        }
                        
                        // Add recently used categories if available
                        if (otherCategoriesList.isNotEmpty()) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "Recently Used Categories",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            
                            otherCategoriesList.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        isCustomCategory = false
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Custom category input field
                AnimatedVisibility(visible = isCustomCategory) {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { 
                            customCategory = it
                            selectedCategory = it  // Update the selectedCategory as the user types
                        },
                        label = { Text("Enter Custom Category *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Item name (model number)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name/Model *") },
                    placeholder = { Text("e.g., MSI Temup Leopard Pro") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Brand/manufacturer
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Brand/Manufacturer *") },
                    placeholder = { Text("e.g., Dell, Apple, Lenovo") },
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    androidx.compose.material3.Button(
                        onClick = {
                            val effectiveCategory = if (isCustomCategory) customCategory else selectedCategory
                            onConfirm(name, type, barcode, condition, status, effectiveCategory)
                        },
                        enabled = name.isNotBlank() && 
                                 (selectedCategory.isNotBlank() || (isCustomCategory && customCategory.isNotBlank())) && 
                                 type.isNotBlank() && 
                                 condition.isNotBlank() && 
                                 status.isNotBlank(),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Add Item")
                    }
                }
            }
        }
    }
} 