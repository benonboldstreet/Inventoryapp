package com.example.inventory.ui.camera

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.inventory.ui.utils.RequestCameraPermission
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Screen for taking photos during checkout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureScreen(
    itemId: UUID,
    onPhotoTaken: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Add debug log for when this screen is composed
    LaunchedEffect(Unit) {
        android.util.Log.d("PhotoCaptureScreen", "*** PhotoCaptureScreen INITIALIZED with itemId: $itemId ***")
    }
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    var cameraState by remember { mutableStateOf<PhotoCaptureUtils.CameraState?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Take Photo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasCameraPermission && cameraState != null) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            isCapturing = true
                            android.util.Log.d("PhotoCaptureScreen", "Starting photo capture process")
                            
                            try {
                                // Create file to store the photo
                                val photoFile = PhotoCaptureUtils.createPhotoFile(context)
                                android.util.Log.d("PhotoCaptureScreen", "Created photo file: ${photoFile.absolutePath}")
                                
                                // Take photo and get URI
                                val uri = PhotoCaptureUtils.takePhoto(
                                    cameraState = cameraState!!,
                                    file = photoFile,
                                    executor = ContextCompat.getMainExecutor(context)
                                )
                                android.util.Log.d("PhotoCaptureScreen", "Photo captured, URI: $uri")
                                
                                // Add timestamp overlay to the photo
                                val timestampedPhoto = PhotoCaptureUtils.addTimestampOverlay(context, photoFile)
                                android.util.Log.d("PhotoCaptureScreen", "Added timestamp overlay to photo")
                                
                                // Pass the file path back
                                onPhotoTaken(timestampedPhoto.absolutePath)
                                android.util.Log.d("PhotoCaptureScreen", "Photo taken callback executed with path: ${timestampedPhoto.absolutePath}")
                            } catch (e: Exception) {
                                android.util.Log.e("PhotoCaptureScreen", "Error taking photo", e)
                                e.printStackTrace()
                            } finally {
                                isCapturing = false
                            }
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = "Take Photo",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        // Request camera permission
        RequestCameraPermission(
            context = context,
            onPermissionGranted = { 
                android.util.Log.d("PhotoCaptureScreen", "Camera permission granted!")
                hasCameraPermission = true 
            },
            onPermissionDenied = {
                android.util.Log.e("PhotoCaptureScreen", "Camera permission denied!")
                hasCameraPermission = false
                onNavigateBack()
            }
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                // Camera preview
                CameraPreview(
                    onCameraStateReady = { state ->
                        android.util.Log.d("PhotoCaptureScreen", "Camera preview is ready")
                        cameraState = state
                    }
                )
                
                // Capture indicator
                if (isCapturing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Capturing photo...",
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
    }
} 