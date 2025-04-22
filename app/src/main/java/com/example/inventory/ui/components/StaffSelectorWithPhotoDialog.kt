package com.example.inventory.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import com.example.inventory.data.database.Staff
import com.example.inventory.ui.navigation.InventoryDestinations
import java.util.UUID

/**
 * Dialog for selecting a staff member with option to take a photo
 */
@Composable
fun StaffSelectorWithPhotoDialog(
    itemId: UUID,
    staffList: List<Staff>,
    onStaffSelectedWithPhoto: (Staff, Boolean) -> Unit,
    onNavigateToPhotoCapture: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStaff by remember { mutableStateOf<Staff?>(null) }
    var takePhoto by remember { mutableStateOf(true) } // Default to taking a photo
    
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Select Staff Member",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (staffList.isEmpty()) {
                    Text("No staff members available. Please add staff first.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(bottom = 16.dp)
                    ) {
                        items(staffList) { staff ->
                            StaffItemWithRadio(
                                staff = staff,
                                isSelected = selectedStaff?.id == staff.id,
                                onClick = { selectedStaff = staff }
                            )
                            Divider()
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Take photo option with camera icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { takePhoto = !takePhoto }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = takePhoto,
                        onClick = { takePhoto = !takePhoto }
                    )
                    
                    Text(
                        text = "Take condition photo",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Camera",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            selectedStaff?.let { staff ->
                                if (takePhoto) {
                                    // Navigate to photo capture screen with route
                                    val route = "${InventoryDestinations.PHOTO_CAPTURE_ROUTE}/${itemId}/${staff.id}"
                                    android.util.Log.d("StaffSelectorDialog", "Navigating directly to photo capture with route: $route")
                                    onNavigateToPhotoCapture(route)
                                } else {
                                    // Proceed without photo
                                    onStaffSelectedWithPhoto(staff, false)
                                }
                            }
                        },
                        enabled = selectedStaff != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Check Out")
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffItemWithRadio(
    staff: Staff,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = staff.name,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = "Department: ${staff.department}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 