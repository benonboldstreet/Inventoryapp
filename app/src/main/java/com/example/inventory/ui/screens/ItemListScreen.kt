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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
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
import androidx.compose.ui.window.Dialog
import com.example.inventory.data.database.Item
import com.example.inventory.ui.components.StatusIndicator
import com.example.inventory.ui.components.getStatusColor
import com.example.inventory.ui.viewmodel.itemViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility

// Enum for item filtering
enum class ItemFilter {
    ALL, ACTIVE, ARCHIVED
}

// ViewModel to share data between screens
object SharedViewModel {
    var scannedBarcode = mutableStateOf("")
    
    fun setBarcode(barcode: String) {
        scannedBarcode.value = barcode
    }
    
    fun clearBarcode() {
        scannedBarcode.value = ""
    }
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
    
    var showAddItemDialog by remember { mutableStateOf(false) }
    val scannedBarcode = SharedViewModel.scannedBarcode.value
    var searchQuery by remember { mutableStateOf("") }
    var itemFilter by remember { mutableStateOf(ItemFilter.ACTIVE) }
    
    // Filter items based on active status and search query
    val filteredItems = allItems.filter { item ->
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Items") },
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
            
            if (filteredItems.isEmpty()) {
                Text(
                    text = when (itemFilter) {
                        ItemFilter.ALL -> if (searchQuery.isBlank()) "No items found" else "No results for '$searchQuery'"
                        ItemFilter.ACTIVE -> if (searchQuery.isBlank()) "No active items found" else "No active items match '$searchQuery'"
                        ItemFilter.ARCHIVED -> if (searchQuery.isBlank()) "No archived items found" else "No archived items match '$searchQuery'"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Items list
                LazyColumn {
                    items(filteredItems) { item ->
                        ItemCard(
                            item = item,
                            onClick = { onItemClick(item.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
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