package com.example.inventory.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Utility class to check Firestore data directly
 * Used for debugging issues with archived items
 */
object FirestoreChecker {
    private const val TAG = "FirestoreChecker"
    
    /**
     * Check for archived items in Firestore
     * 
     * @param context The context to show Toast messages
     */
    suspend fun checkArchivedItems(context: Context) {
        try {
            Log.d(TAG, "Checking for archived items in Firestore")
            val firestore = FirebaseFirestore.getInstance()
            
            // Query for archived items (isActive = false)
            val query = firestore.collection("items")
                .whereEqualTo("isActive", false)
                .get()
                .await()
            
            // Log results
            Log.d(TAG, "Found ${query.size()} archived items in Firestore")
            query.documents.forEachIndexed { index, doc ->
                Log.d(TAG, "Archived item $index: id=${doc.id}, name=${doc.getString("name")}")
            }
            
            // Show toast with results
            Toast.makeText(
                context,
                "Found ${query.size()} archived items in Firestore",
                Toast.LENGTH_LONG
            ).show()
            
            // Also check for items without the isActive field
            val allQuery = firestore.collection("items").get().await()
            val itemsWithoutIsActive = allQuery.documents.filter { !it.contains("isActive") }
            
            if (itemsWithoutIsActive.isNotEmpty()) {
                Log.d(TAG, "Found ${itemsWithoutIsActive.size} items without isActive field")
                itemsWithoutIsActive.forEach { doc ->
                    Log.d(TAG, "Item without isActive: id=${doc.id}, name=${doc.getString("name")}")
                }
                
                Toast.makeText(
                    context,
                    "Warning: Found ${itemsWithoutIsActive.size} items without isActive field",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking archived items: ${e.message}", e)
            Toast.makeText(
                context,
                "Error checking archived items: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
} 