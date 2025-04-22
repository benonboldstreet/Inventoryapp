package com.example.inventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A visual indicator for item status displayed as a colored circle
 */
@Composable
fun StatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val color = getStatusColor(status)
    Box(
        modifier = modifier
            .size(16.dp)
            .background(color, CircleShape)
    )
}

/**
 * Returns the appropriate color for each status
 */
fun getStatusColor(status: String): Color {
    return when(status) {
        "Available" -> Color(0xFF4CAF50) // Green
        "Checked Out" -> Color(0xFFF44336) // Red
        "Needs Repair" -> Color(0xFFFF9800) // Orange
        else -> Color(0xFF9E9E9E) // Gray
    }
} 