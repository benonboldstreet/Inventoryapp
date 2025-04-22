package com.example.inventory.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.inventory.data.database.Staff
import com.example.inventory.ui.viewmodel.staffViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color

enum class StaffFilter {
    ALL, ACTIVE, ARCHIVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffListScreen(
    bottomBar: @Composable () -> Unit,
    onStaffClick: (Staff) -> Unit = {}
) {
    val viewModel = staffViewModel()
    val staffList by viewModel.allStaff.collectAsState(initial = emptyList())
    
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var staffFilter by remember { mutableStateOf(StaffFilter.ACTIVE) }
    
    // Filter staff list based on search query and active/archived status
    val filteredStaffList = staffList.filter { staff ->
        // First filter by active/archived status
        when (staffFilter) {
            StaffFilter.ALL -> true
            StaffFilter.ACTIVE -> staff.isActive
            StaffFilter.ARCHIVED -> !staff.isActive
        }
    }.filter { staff ->
        // Then filter by search query if one exists
        if (searchQuery.isBlank()) true
        else {
            staff.name.contains(searchQuery, ignoreCase = true) || 
            staff.department.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Directory") }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddStaffDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                text = { Text("Add Staff") }
            )
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        
        if (showAddStaffDialog) {
            AddStaffDialog(
                onDismiss = { showAddStaffDialog = false },
                onConfirm = { name, department, email, phone, position ->
                    viewModel.addStaff(
                        name = name,
                        department = department,
                        email = email,
                        phone = phone,
                        position = position
                    )
                    showAddStaffDialog = false
                }
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
                placeholder = { Text("Search by name or department") },
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
                    selected = staffFilter == StaffFilter.ACTIVE,
                    onClick = { staffFilter = StaffFilter.ACTIVE },
                    label = { Text("Active") }
                )
                
                FilterChip(
                    selected = staffFilter == StaffFilter.ARCHIVED,
                    onClick = { staffFilter = StaffFilter.ARCHIVED },
                    label = { Text("Archived") }
                )
                
                FilterChip(
                    selected = staffFilter == StaffFilter.ALL,
                    onClick = { staffFilter = StaffFilter.ALL },
                    label = { Text("All") }
                )
            }
            
            if (filteredStaffList.isEmpty()) {
                Text(
                    text = when (staffFilter) {
                        StaffFilter.ALL -> if (searchQuery.isBlank()) "No staff members found" else "No results for '$searchQuery'"
                        StaffFilter.ACTIVE -> if (searchQuery.isBlank()) "No active staff members found" else "No active staff members match '$searchQuery'"
                        StaffFilter.ARCHIVED -> if (searchQuery.isBlank()) "No archived staff members found" else "No archived staff members match '$searchQuery'"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Staff list
                LazyColumn {
                    items(filteredStaffList) { staff ->
                        StaffCard(
                            staff = staff,
                            onClick = { onStaffClick(staff) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCard(
    staff: Staff,
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
            Text(
                text = staff.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textDecoration = if (!staff.isActive) TextDecoration.LineThrough else TextDecoration.None
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Department: ${staff.department}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (staff.position.isNotBlank()) {
                Text(
                    text = "Position: ${staff.position}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (!staff.isActive) {
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

@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, department: String, email: String, phone: String, position: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf(false) }
    var departmentError by remember { mutableStateOf(false) }
    
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
                    text = "Add New Staff Member",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Name *") },
                    placeholder = { Text("Enter staff name") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Name is required") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = department,
                    onValueChange = { 
                        department = it
                        departmentError = it.isBlank()
                    },
                    label = { Text("Department *") },
                    placeholder = { Text("Enter department") },
                    isError = departmentError,
                    supportingText = { if (departmentError) Text("Department is required") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Position") },
                    placeholder = { Text("Enter job title/position") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("Enter email address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    placeholder = { Text("Enter phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    androidx.compose.material3.Button(
                        onClick = {
                            nameError = name.isBlank()
                            departmentError = department.isBlank()
                            
                            if (!nameError && !departmentError) {
                                onConfirm(name, department, email, phone, position)
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
} 