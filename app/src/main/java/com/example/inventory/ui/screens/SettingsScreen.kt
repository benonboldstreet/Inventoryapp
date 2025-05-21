package com.example.inventory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.inventory.R
import com.example.inventory.ui.viewmodel.SettingsViewModel
import com.example.inventory.ui.viewmodel.SharedViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showBackupDialog by remember { mutableStateOf(false) }
    var showTestDataDialog by remember { mutableStateOf(false) }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var isCloudConnected by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
        isCloudConnected = viewModel.isCloudConnected()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ... existing settings ...
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Backup Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Database Backup",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showBackupDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create Backup")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { showTestDataDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Generate Test Data")
                    }
                }
                
                backupStatus?.let { status ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cloud Synchronization Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Cloud Synchronization",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Display the cloud sync status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isCloudConnected) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Connected to Cloud",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "Connected",
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Cloud Disconnected",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "Disconnected",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Add manual sync button
                    if (!isCloudConnected) {
                        Button(
                            onClick = { 
                                SharedViewModel.setCloudConnected(true)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Attempt to Reconnect"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reconnect")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Display the cloud sync explanation
                Text(
                    text = stringResource(R.string.cloud_sync_explanation),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Create Backup") },
            text = { Text("This will create a backup of all your data. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackupDialog = false
                        viewModel.createBackup { status ->
                            backupStatus = status
                        }
                    }
                ) {
                    Text("Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showTestDataDialog) {
        AlertDialog(
            onDismissRequest = { showTestDataDialog = false },
            title = { Text("Generate Test Data") },
            text = { Text("This will create test items, staff, and checkout records. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTestDataDialog = false
                        viewModel.generateTestData { status ->
                            backupStatus = status
                        }
                    }
                ) {
                    Text("Generate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTestDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 