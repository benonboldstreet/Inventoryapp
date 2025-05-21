package com.example.inventory.data.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for Firebase Storage operations
 */
@Singleton
class FirebaseStorageUtils @Inject constructor(
    private val firebaseConfig: FirebaseConfig
) {
    private val storage: FirebaseStorage = firebaseConfig.storage
    private val TAG = "FirebaseStorageUtils"

    /**
     * Upload a photo to Firebase Storage
     * @param file Local file containing the photo
     * @param path Storage path (e.g., "checkouts", "items", etc.)
     * @return The download URL of the uploaded file
     */
    suspend fun uploadPhoto(file: File, path: String): String {
        try {
            // Create a unique filename using UUID
            val filename = "${UUID.randomUUID()}_${file.name}"
            val storageRef = storage.reference.child("$path/$filename")
            
            // Upload file
            Log.d(TAG, "Uploading file: ${file.absolutePath} to $path/$filename")
            val uploadTask = storageRef.putFile(Uri.fromFile(file)).await()
            
            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "File uploaded successfully. Download URL: $downloadUrl")
            
            return downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo", e)
            throw e
        }
    }

    /**
     * Upload a checkout photo
     * @param file Local file containing the photo
     * @return The download URL of the uploaded file
     */
    suspend fun uploadCheckoutPhoto(file: File): String {
        return uploadPhoto(file, "checkouts")
    }

    /**
     * Upload an item photo
     * @param file Local file containing the photo
     * @return The download URL of the uploaded file
     */
    suspend fun uploadItemPhoto(file: File): String {
        return uploadPhoto(file, "items")
    }

    /**
     * Check if a file exists in Firebase Storage
     * @param photoUrl The URL of the photo to check
     * @return true if the file exists, false otherwise
     */
    suspend fun doesPhotoExist(photoUrl: String): Boolean {
        return try {
            // Extract the path from the URL
            val path = Uri.parse(photoUrl).lastPathSegment ?: return false
            val storageRef = storage.reference.child(path)
            
            // Try to get metadata (will throw exception if file doesn't exist)
            storageRef.metadata.await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if photo exists: $photoUrl", e)
            false
        }
    }
} 