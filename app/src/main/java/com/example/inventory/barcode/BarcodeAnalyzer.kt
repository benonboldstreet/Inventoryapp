package com.example.inventory.barcode

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Barcode scanner analyzer that processes camera frames to detect barcodes
 */
class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit,
    private val onError: (Exception) -> Unit = {}
) : ImageAnalysis.Analyzer {
    
    private val TAG = "BarcodeAnalyzer"
    
    // Create barcode scanner
    private val scanner = BarcodeScanning.getClient()
    
    // Flag to track if we're currently processing a frame
    private var isProcessing = false
    
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        // If already processing, release this frame and return
        if (isProcessing) {
            imageProxy.close()
            return
        }
        
        // Mark as processing
        isProcessing = true
        
        // Get the mediaImage from the proxy
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            isProcessing = false
            return
        }
        
        // Convert to ML Kit input image
        val inputImage = InputImage.fromMediaImage(
            mediaImage, 
            imageProxy.imageInfo.rotationDegrees
        )
        
        // Process the image for barcodes
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    // We found at least one barcode
                    Log.d(TAG, "Barcodes found: ${barcodes.size}")
                    
                    // Get the first valid barcode
                    val barcode = barcodes.firstOrNull { it.rawValue != null }
                    
                    if (barcode != null) {
                        // Callback with the barcode value
                        barcode.rawValue?.let { value ->
                            Log.d(TAG, "Barcode detected: $value")
                            onBarcodeDetected(value)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors in barcode processing
                Log.e(TAG, "Barcode scanning failed", e)
                onError(e)
            }
            .addOnCompleteListener {
                // Always release the image and mark as not processing
                imageProxy.close()
                isProcessing = false
            }
    }
    
    /**
     * Helper method to get information about detected barcode
     */
    private fun getBarcodeInfo(barcode: Barcode): String {
        // Get barcode type
        val typeString = when (barcode.format) {
            Barcode.FORMAT_QR_CODE -> "QR Code"
            Barcode.FORMAT_AZTEC -> "Aztec"
            Barcode.FORMAT_CODABAR -> "Codabar"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_CODE_93 -> "Code 93"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_ITF -> "ITF"
            Barcode.FORMAT_PDF417 -> "PDF417"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            else -> "Unknown format"
        }
        
        return "Type: $typeString, Value: ${barcode.rawValue}"
    }
} 