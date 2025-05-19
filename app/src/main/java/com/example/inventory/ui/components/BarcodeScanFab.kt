package com.example.inventory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Barcode Scan FAB Component
 * 
 * A floating action button that can be used to launch the barcode scanner.
 * Supports both compact and extended modes.
 * 
 * @param onClick Callback when the FAB is clicked
 * @param extended Whether to show the extended version with text
 * @param modifier Modifier for customizing the FAB
 */
@Composable
fun BarcodeScanFab(
    onClick: () -> Unit,
    extended: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (extended) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Barcode"
                )
            },
            text = { Text("Scan") },
            modifier = modifier.padding(16.dp)
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan Barcode"
            )
        }
    }
}

/**
 * Animated Barcode Scan FAB
 * 
 * A version of the BarcodeScanFab that animates between visible and hidden states.
 * 
 * @param visible Whether the FAB should be visible
 * @param onClick Callback when the FAB is clicked
 * @param extended Whether to show the extended version with text
 * @param modifier Modifier for customizing the FAB
 */
@Composable
fun AnimatedBarcodeScanFab(
    visible: Boolean,
    onClick: () -> Unit,
    extended: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut() + shrinkVertically()
    ) {
        BarcodeScanFab(
            onClick = onClick,
            extended = extended,
            modifier = modifier
        )
    }
} 