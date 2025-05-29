package com.example.inventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Get the appropriate color for an item status
 */
@Composable
fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "available" -> Color(0xFF4CAF50) // Green
        "checked out" -> Color(0xFFFFC107) // Amber
        "maintenance" -> Color(0xFF2196F3) // Blue
        "lost" -> Color(0xFFF44336) // Red
        "retired" -> Color(0xFF9E9E9E) // Gray
        "on hold" -> Color(0xFF673AB7) // Deep Purple
        "overdue" -> Color(0xFFE91E63) // Pink
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * A small circular indicator showing the status of an item
 */
@Composable
fun StatusIndicator(
    status: String,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(getStatusColor(status))
    )
} 