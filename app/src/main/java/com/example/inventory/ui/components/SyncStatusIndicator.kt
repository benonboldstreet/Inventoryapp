package com.example.inventory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.inventory.ui.viewmodel.SharedViewModel
import com.google.firebase.firestore.FirebaseFirestore

// Define the SyncStatus enum for use in this file
enum class SyncStatus {
    SYNCED, 
    SYNCING, 
    OFFLINE, 
    PENDING, 
    IDLE,
    FAILED,
    PARTIAL,
    COMPLETE
}

/**
 * Sync status indicator that shows the current sync state with Firebase
 */
@Composable
fun SyncStatusIndicator(
    modifier: Modifier = Modifier
) {
    var syncStatus by remember { mutableStateOf(SyncStatus.SYNCED) }
    var isOnline by remember { mutableStateOf(true) }
    
    // Listen for network connectivity changes
    LaunchedEffect(Unit) {
        SharedViewModel.addConnectivityListener { isConnected ->
            isOnline = isConnected
            syncStatus = if (isConnected) {
                SyncStatus.SYNCED
        } else {
                SyncStatus.OFFLINE
            }
        }
    }
    
    // Listen for Firestore sync state
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.enableNetwork().addOnSuccessListener {
            syncStatus = SyncStatus.SYNCED
        }.addOnFailureListener {
            syncStatus = SyncStatus.OFFLINE
        }
    }
    
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier = modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        when (syncStatus) {
                SyncStatus.SYNCED -> {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced",
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Synced",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green
    )
}
                SyncStatus.SYNCING -> {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Syncing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Syncing...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                SyncStatus.OFFLINE -> {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
    )
}
                SyncStatus.PENDING -> {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Pending",
                        tint = Color.Yellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Yellow
                    )
                }
                else -> {
                    // Handle other statuses if needed
        Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced",
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp)
        )
                    Spacer(modifier = Modifier.width(4.dp))
        Text(
                        text = "Synced",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green
        )
                }
            }
        }
    }
}

/**
 * Small indicator that shows the current cloud connection and sync status
 * This can be used in a toolbar or other small space
 */
@Composable
fun SmallSyncIndicator(modifier: Modifier = Modifier) {
    var syncStatus by remember { mutableStateOf(SyncStatus.IDLE) }
    val isConnected by SharedViewModel.isCloudConnected.collectAsState()
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(getStatusColor(syncStatus, isConnected))
    ) {
        Icon(
            imageVector = getStatusIcon(syncStatus, isConnected),
            contentDescription = getStatusDescription(syncStatus, isConnected),
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
    }
}

private fun getStatusColor(status: SyncStatus, isConnected: Boolean): Color {
    return when {
        !isConnected -> Color(0xFF9E9E9E) // Gray when offline
        status == SyncStatus.SYNCING -> Color(0xFF2196F3) // Blue when syncing
        status == SyncStatus.FAILED -> Color(0xFFF44336) // Red when failed
        status == SyncStatus.PARTIAL -> Color(0xFFFF9800) // Orange when partial
        status == SyncStatus.COMPLETE -> Color(0xFF4CAF50) // Green when complete
        else -> Color(0xFF4CAF50) // Green when idle and connected
    }
}

private fun getStatusIcon(status: SyncStatus, isConnected: Boolean): ImageVector {
    return when {
        !isConnected -> Icons.Default.CloudOff
        status == SyncStatus.SYNCING -> Icons.Default.Sync
        status == SyncStatus.FAILED -> Icons.Default.Error
        status == SyncStatus.PARTIAL -> Icons.Default.Warning
        else -> Icons.Default.CloudSync
    }
}

private fun getStatusDescription(status: SyncStatus, isConnected: Boolean): String {
    return when {
        !isConnected -> "Offline"
        status == SyncStatus.SYNCING -> "Syncing with cloud"
        status == SyncStatus.FAILED -> "Sync failed"
        status == SyncStatus.PARTIAL -> "Partial sync"
        status == SyncStatus.COMPLETE -> "Sync complete"
        else -> "Connected to cloud"
    }
} 