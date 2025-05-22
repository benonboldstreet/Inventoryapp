package com.example.inventory.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.inventory.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * Utility functions for handling images
 */
object ImageUtils {
    private const val TAG = "ImageUtils"
    
    /**
     * Check if the path is a URL
     */
    fun isUrl(path: String?): Boolean {
        if (path == null) return false
        return path.startsWith("http://") || path.startsWith("https://")
    }
    
    /**
     * Load a bitmap from a file or URL
     */
    suspend fun loadBitmap(context: Context, path: String?): Bitmap? {
        if (path == null) return null
        
        return withContext(Dispatchers.IO) {
            try {
                if (isUrl(path)) {
                    // Load from URL
                    val url = URL(path)
                    val connection = url.openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()
                    val inputStream = connection.getInputStream()
                    BitmapFactory.decodeStream(inputStream)
                } else {
                    // Load from local file
                    val file = File(path)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else {
                        Log.e(TAG, "File doesn't exist: $path")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image from path: $path", e)
                null
            }
        }
    }
}

/**
 * Composable function for displaying an image from either a local file path or a URL
 */
@Composable
fun SmartImage(
    path: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null
) {
    if (path == null) {
        // Display placeholder if path is null
        Image(
            painter = painterResource(id = R.drawable.ic_image_placeholder),
            contentDescription = contentDescription ?: "No image",
            modifier = modifier,
            contentScale = contentScale
        )
        return
    }
    
    if (ImageUtils.isUrl(path)) {
        // Use Coil for loading remote images
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(path)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            error = painterResource(id = R.drawable.ic_image_error),
            placeholder = painterResource(id = R.drawable.ic_image_placeholder)
        )
    } else {
        // Handle local file paths
        var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        val context = LocalContext.current
        
        LaunchedEffect(path) {
            val loadedBitmap = ImageUtils.loadBitmap(context, path)
            bitmap = loadedBitmap?.asImageBitmap()
        }
        
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            // Display placeholder while loading or if loading fails
            Image(
                painter = painterResource(id = R.drawable.ic_image_placeholder),
                contentDescription = contentDescription ?: "Loading image",
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
} 