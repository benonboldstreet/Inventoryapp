package com.example.inventory.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Utility class for photo capture functionality
 */
object PhotoCaptureUtils {
    
    /**
     * Helper class to hold the ImageCapture use case for later photo capture
     */
    class CameraState(val imageCapture: ImageCapture)
    
    /**
     * Create a file for storing a photo
     */
    fun createPhotoFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val storageDir = context.getExternalFilesDir("photos")
        
        // Create the directory if it doesn't exist
        storageDir?.mkdirs()
        
        return File(storageDir, "IMG_${timestamp}.jpg")
    }
    
    /**
     * Take a photo and save it to a file
     */
    suspend fun takePhoto(
        cameraState: CameraState,
        file: File,
        executor: Executor
    ): Uri = suspendCoroutine { continuation ->
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        
        cameraState.imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    outputFileResults.savedUri?.let { uri ->
                        continuation.resume(uri)
                    } ?: continuation.resumeWithException(
                        Exception("Failed to get saved photo URI")
                    )
                }
                
                override fun onError(exception: ImageCaptureException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
    
    /**
     * Add timestamp overlay to a photo
     */
    fun addTimestampOverlay(context: Context, photoFile: File): File {
        // Load the image into a bitmap
        val bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath)
        
        // Create a mutable copy to draw on
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        
        // Create a paint for the timestamp text
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = bitmap.width * 0.05f // Scale text based on image size
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(5f, 2f, 2f, Color.BLACK) // Add shadow for better readability
        }
        
        // Format current date and time
        val timestamp = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))
        
        // Measure text width to position it
        val rect = Rect()
        paint.getTextBounds(timestamp, 0, timestamp.length, rect)
        
        // Draw timestamp at the bottom of the image with some padding
        val x = bitmap.width * 0.05f // 5% from left edge
        val y = bitmap.height * 0.95f // 95% from top (near bottom)
        
        // Draw the text
        canvas.drawText(timestamp, x, y, paint)
        
        // Save the modified bitmap back to the file
        val outputStream = FileOutputStream(photoFile)
        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        outputStream.close()
        
        // Return the file with the timestamp overlay
        return photoFile
    }
}

/**
 * Composable for a camera preview that can capture photos
 */
@Composable
fun CameraPreview(
    onCameraStateReady: (PhotoCaptureUtils.CameraState) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Create a PreviewView for camera preview
    val previewView = remember { PreviewView(context) }
    previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    
    // Get camera provider future
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    // Set up the view
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
    
    // Set up camera when the composable is first created
    DisposableEffect(lifecycleOwner) {
        val executor = ContextCompat.getMainExecutor(context)
        
        val cameraListener = Runnable {
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                // Image capture use case
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()
                
                // Unbind any bound use cases before rebinding
                cameraProvider.unbindAll()
                
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                
                // Provide the camera state
                onCameraStateReady(PhotoCaptureUtils.CameraState(imageCapture))
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Add the listener to the future
        cameraProviderFuture.addListener(cameraListener, executor)
        
        // When the effect leaves composition, remove all use cases
        onDispose {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 