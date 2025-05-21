package com.example.inventory.ui.scanner

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.ui.navigation.InventoryDestinations
import com.example.inventory.ui.utils.RequestCameraPermission
import com.example.inventory.ui.viewmodel.CheckoutViewModel
import com.example.inventory.ui.viewmodel.ItemViewModel
import com.example.inventory.ui.viewmodel.StaffViewModel
import com.example.inventory.ui.viewmodel.checkoutViewModel
import com.example.inventory.ui.viewmodel.itemViewModel
import com.example.inventory.ui.viewmodel.staffViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import com.example.inventory.ui.screens.SharedViewModel

/**
 * Composable that shows a camera preview and scans for barcodes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPhotoCapture: (String) -> Unit = {},
    returnBarcode: Boolean = false,
    onBarcodeDetected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // ViewModels
    val itemViewModel = itemViewModel()
    val staffViewModel = staffViewModel()
    val checkoutViewModel = checkoutViewModel()
    
    // Get list of all staff for checkout dialog
    val allStaff by staffViewModel.allStaff.collectAsState(initial = emptyList())
    
    // States for camera and scanning
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }
    
    // States for item handling
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var scannedItem by remember { mutableStateOf<Item?>(null) }
    var scannedCheckout by remember { mutableStateOf<CheckoutLog?>(null) }
    var scannedStaff by remember { mutableStateOf<Staff?>(null) }
    
    // Dialog states
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showCheckinDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Function to reset state after scanning
    fun resetScanState() {
        scannedBarcode = null
        scannedItem = null
        scannedCheckout = null
        scannedStaff = null
        isScanning = true
    }
    
    // Process scanned barcode
    LaunchedEffect(scannedBarcode) {
        scannedBarcode?.let { barcode ->
            // Stop scanning while processing
            isScanning = false
            
            // If returnBarcode is true, just return the barcode value and go back
            if (returnBarcode) {
                // Use SharedViewModel to pass the barcode
                SharedViewModel.setBarcode(barcode)
                // Return to the previous screen
                onNavigateBack()
                return@LaunchedEffect
            }
            
            coroutineScope.launch {
                // Find the item with this barcode
                val item = itemViewModel.getItemByBarcode(barcode)
                
                if (item != null) {
                    scannedItem = item
                    
                    // Check if the item is currently checked out
                    val checkout = checkoutViewModel.getCurrentCheckoutForItem(item.id).first()
                    
                    if (checkout != null) {
                        // Item is checked out - prepare for check-in
                        scannedCheckout = checkout
                        scannedStaff = staffViewModel.getStaffById(checkout.staffId).first()
                        
                        // Show check-in dialog
                        showCheckinDialog = true
                    } else if (item.status == "Available") {
                        // Item is available - prepare for checkout
                        showCheckoutDialog = true
                    } else {
                        // Item has inconsistent state
                        errorMessage = "Item status is inconsistent. Please check the database."
                        showErrorDialog = true
                        
                        // Reset scanning
                        resetScanState()
                    }
                } else {
                    // Barcode not found
                    errorMessage = "No item found with barcode: $barcode"
                    showErrorDialog = true
                    
                    // Reset scanning
                    resetScanState()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Barcode") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Request camera permission
        RequestCameraPermission(
            context = context,
            onPermissionGranted = { 
                hasCameraPermission = true 
            },
            onPermissionDenied = {
                // Show error and go back
                hasCameraPermission = false
                onNavigateBack()
            }
        )
        
        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                if (isScanning) {
                    // Camera preview with barcode scanning
                    CameraPreviewWithBarcodeScanner(
                        onBarcodeDetected = { barcodes ->
                            if (barcodes.isNotEmpty() && scannedBarcode == null) {
                                scannedBarcode = barcodes.first()
                            }
                        }
                    )
                    
                    // Scanning overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scanning...",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(3.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                } else {
                    // Processing state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Processing barcode...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            } else {
                // No camera permission
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Camera permission required",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
        
        // Show checkout dialog if needed
        if (showCheckoutDialog && scannedItem != null) {
            CheckoutDialog(
                item = scannedItem!!,
                staffList = allStaff,
                onCheckout = { item, staff ->
                    // Create checkout in the database
                    coroutineScope.launch {
                        checkoutViewModel.checkOutItem(item.id, staff.id)
                        
                        // Reset scanning
                        showCheckoutDialog = false
                        resetScanState()
                    }
                },
                onCheckoutWithPhoto = { item, staff ->
                    // Navigate to photo capture screen
                    showCheckoutDialog = false
                    
                    // Pass the item ID and staff ID to the photo capture screen
                    val route = "${InventoryDestinations.PHOTO_CAPTURE_ROUTE}/${item.id}/${staff.id}"
                    onNavigateToPhotoCapture(route)
                },
                onDismiss = {
                    showCheckoutDialog = false
                    resetScanState()
                }
            )
        }
        
        // Show check-in dialog if needed
        if (showCheckinDialog && scannedItem != null && scannedStaff != null && scannedCheckout != null) {
            CheckinDialog(
                item = scannedItem!!,
                staffName = scannedStaff!!.name,
                onCheckin = {
                    // Check in the item
                    coroutineScope.launch {
                        checkoutViewModel.checkInItem(scannedCheckout!!.id)
                        
                        // Reset scanning
                        showCheckinDialog = false
                        resetScanState()
                    }
                },
                onDismiss = {
                    showCheckinDialog = false
                    resetScanState()
                }
            )
        }
        
        // Show error dialog if needed
        if (showErrorDialog) {
            ErrorDialog(
                message = errorMessage,
                onDismiss = {
                    showErrorDialog = false
                    resetScanState()
                }
            )
        }
    }
}

/**
 * Camera preview with barcode scanning
 */
@Composable
fun CameraPreviewWithBarcodeScanner(
    onBarcodeDetected: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { ContextCompat.getMainExecutor(context) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                // Create the PreviewView
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                
                // Set up camera and barcode analyzer
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Preview use case
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    // Image analysis for barcode scanning
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                executor,
                                BarcodeAnalyzer(onBarcodeDetected)
                            )
                        }
                    
                    // Select back camera
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    try {
                        // Unbind any bound use cases before rebinding
                        cameraProvider.unbindAll()
                        
                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("CameraX", "Use case binding failed", e)
                    }
                }, executor)
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Dialog for displaying error messages
 */
@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Barcode Error") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
} 