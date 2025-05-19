package com.example.inventory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
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
import com.example.inventory.api.OfflineCache
import com.example.inventory.api.SyncStatus
import com.example.inventory.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.delay

/**
 * Shows the current sync status of the application
 */
@Composable
fun SyncStatusIndicator(modifier: Modifier = Modifier) {
    var syncStatus by remember { mutableStateOf(SyncStatus.IDLE) }
    var showIndicator by remember { mutableStateOf(false) }
    
    // Auto-hide the indicator after some time when completed
    LaunchedEffect(syncStatus) {
        if (syncStatus == SyncStatus.COMPLETE) {
            delay(3000)
            showIndicator = false
        } else {
            showIndicator = syncStatus != SyncStatus.IDLE
        }
    }
    
    // Register as a sync listener to get updates
    LaunchedEffect(Unit) {
        OfflineCache.addSyncListener { status ->
            syncStatus = status
        }
    }
    
    AnimatedVisibility(
        visible = showIndicator,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        when (syncStatus) {
            SyncStatus.IN_PROGRESS -> SyncProgressIndicator()
            SyncStatus.COMPLETE -> SyncCompleteIndicator()
            SyncStatus.PARTIAL -> SyncPartialIndicator()
            SyncStatus.FAILED -> SyncFailedIndicator()
            SyncStatus.OFFLINE -> SyncOfflineIndicator()
            SyncStatus.IDLE -> { /* Don't show anything */ }
        }
    }
}

@Composable
private fun SyncProgressIndicator() {
    SyncIndicatorBase(
        icon = Icons.Default.Sync,
        message = "Syncing with cloud...",
        color = Color(0xFF2196F3), // Blue
        contentColor = Color.White
    )
}

@Composable
private fun SyncCompleteIndicator() {
    SyncIndicatorBase(
        icon = Icons.Default.Done,
        message = "Sync complete",
        color = Color(0xFF4CAF50), // Green
        contentColor = Color.White
    )
}

@Composable
private fun SyncPartialIndicator() {
    SyncIndicatorBase(
        icon = Icons.Default.Warning,
        message = "Partial sync - some changes pending",
        color = Color(0xFFFF9800), // Orange
        contentColor = Color.White
    )
}

@Composable
private fun SyncFailedIndicator() {
    SyncIndicatorBase(
        icon = Icons.Default.Error,
        message = "Sync failed - will retry later",
        color = Color(0xFFF44336), // Red
        contentColor = Color.White
    )
}

@Composable
private fun SyncOfflineIndicator() {
    SyncIndicatorBase(
        icon = Icons.Default.CloudOff,
        message = "Offline - changes will sync when connected",
        color = Color(0xFF9E9E9E), // Gray
        contentColor = Color.White
    )
}

@Composable
private fun SyncIndicatorBase(
    icon: ImageVector,
    message: String,
    color: Color,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(color)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = message,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
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
    
    // Register as a sync listener to get updates
    LaunchedEffect(Unit) {
        OfflineCache.addSyncListener { status ->
            syncStatus = status
        }
    }
    
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
        status == SyncStatus.IN_PROGRESS -> Color(0xFF2196F3) // Blue when syncing
        status == SyncStatus.FAILED -> Color(0xFFF44336) // Red when failed
        status == SyncStatus.PARTIAL -> Color(0xFFFF9800) // Orange when partial
        status == SyncStatus.COMPLETE -> Color(0xFF4CAF50) // Green when complete
        else -> Color(0xFF4CAF50) // Green when idle and connected
    }
}

private fun getStatusIcon(status: SyncStatus, isConnected: Boolean): ImageVector {
    return when {
        !isConnected -> Icons.Default.CloudOff
        status == SyncStatus.IN_PROGRESS -> Icons.Default.Sync
        status == SyncStatus.FAILED -> Icons.Default.Error
        status == SyncStatus.PARTIAL -> Icons.Default.Warning
        else -> Icons.Default.CloudSync
    }
}

private fun getStatusDescription(status: SyncStatus, isConnected: Boolean): String {
    return when {
        !isConnected -> "Offline"
        status == SyncStatus.IN_PROGRESS -> "Syncing with cloud"
        status == SyncStatus.FAILED -> "Sync failed"
        status == SyncStatus.PARTIAL -> "Partial sync"
        status == SyncStatus.COMPLETE -> "Sync complete"
        else -> "Connected to cloud"
    }
} 