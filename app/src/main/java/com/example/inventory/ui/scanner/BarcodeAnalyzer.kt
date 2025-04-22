package com.example.inventory.ui.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.TimeUnit

/**
 * ML Kit based image analyzer that processes camera frames to detect barcodes
 */
class BarcodeAnalyzer(private val onBarcodeDetected: (List<String>) -> Unit) : ImageAnalysis.Analyzer {
    
    private val barcodeScanner = BarcodeScanning.getClient()
    
    // Control flow to avoid processing every frame
    private var lastAnalyzedTimestamp = 0L
    private val minimumProcessInterval = 500L // Process at most every 500ms
    
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        
        // Only process every minimumProcessInterval
        if (currentTimestamp - lastAnalyzedTimestamp >= minimumProcessInterval) {
            imageProxy.image?.let { image ->
                val inputImage = InputImage.fromMediaImage(
                    image,
                    imageProxy.imageInfo.rotationDegrees
                )
                
                // Process image with ML Kit
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            // Extract barcode values and pass them through the callback
                            val barcodeValues = barcodes.mapNotNull { barcode -> 
                                when (barcode.valueType) {
                                    Barcode.TYPE_PRODUCT, 
                                    Barcode.TYPE_TEXT,
                                    Barcode.TYPE_ISBN,
                                    Barcode.TYPE_URL -> barcode.rawValue
                                    else -> null
                                }
                            }
                            
                            if (barcodeValues.isNotEmpty()) {
                                onBarcodeDetected(barcodeValues)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Log error but continue processing
                        exception.printStackTrace()
                    }
                    .addOnCompleteListener {
                        // Always close the image proxy to release resources
                        imageProxy.close()
                    }
                
                lastAnalyzedTimestamp = currentTimestamp
            } ?: imageProxy.close()
        } else {
            imageProxy.close()
        }
    }
} 