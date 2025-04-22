package com.example.inventory.ui.scanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.inventory.data.database.Item
import com.example.inventory.data.database.Staff

/**
 * Dialog for checking out an item to a staff member
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    item: Item,
    staffList: List<Staff>,
    onCheckout: (Item, Staff) -> Unit,
    onCheckoutWithPhoto: (Item, Staff) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedStaff by remember { mutableStateOf<Staff?>(null) }
    
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
                    text = "Check Out Item",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Item info
                Text(
                    text = "Item: ${item.name}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = "Type: ${item.type}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Barcode: ${item.barcode}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Staff dropdown selection
                Column {
                    OutlinedTextField(
                        value = selectedStaff?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Staff Member") },
                        trailingIcon = { 
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.clickable { expanded = !expanded }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        staffList.forEach { staff ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(staff.name)
                                        Text(
                                            staff.department,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    selectedStaff = staff
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Row {
                        // Take photo button
                        OutlinedButton(
                            onClick = {
                                selectedStaff?.let { staff ->
                                    onCheckoutWithPhoto(item, staff)
                                }
                            },
                            enabled = selectedStaff != null
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("With Photo")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Regular checkout button
                        Button(
                            onClick = {
                                selectedStaff?.let { staff ->
                                    onCheckout(item, staff)
                                }
                            },
                            enabled = selectedStaff != null
                        ) {
                            Text("Checkout")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for checking in an item
 */
@Composable
fun CheckinDialog(
    item: Item,
    staffName: String,
    onCheckin: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "Check In Item",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Item info
                Text(
                    text = "Item: ${item.name}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = "Type: ${item.type}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Barcode: ${item.barcode}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Currently assigned to: $staffName",
                    style = MaterialTheme.typography.bodyLarge
                )
                
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
                        onClick = onCheckin
                    ) {
                        Text("Check In")
                    }
                }
            }
        }
    }
} 