package com.example.inventory.util

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Utility class to directly check Firestore for data validation
 */
object FirestoreChecker {
    
    /**
     * Directly queries Firestore for archived items (isActive=false)
     * and displays the results in a Toast message
     */
    suspend fun checkArchivedItems(context: Context) {
        try {
            withContext(Dispatchers.IO) {
                // Get Firestore instance
                val db = FirebaseFirestore.getInstance()
                
                // Get all items first to check what's actually there
                val allItems = db.collection("items").get().await()
                android.util.Log.d("FirestoreChecker", "Total items in Firestore: ${allItems.documents.size}")
                
                // Log all items with their isActive field
                android.util.Log.d("FirestoreChecker", "=== All items in Firestore ===")
                allItems.documents.forEach { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: "Unknown"
                    val isActive = doc.get("isActive")
                    val isActiveType = isActive?.javaClass?.simpleName ?: "null"
                    android.util.Log.d("FirestoreChecker", "Item: $id - $name, isActive=$isActive (type: $isActiveType)")
                }
                
                // Query for archived items
                val query = db.collection("items")
                    .whereEqualTo("isActive", false)
                    .get()
                    .await()
                
                // Process results
                val archivedItems = query.documents
                
                // Log the results
                android.util.Log.d("FirestoreChecker", "===== DIRECT FIRESTORE CHECK =====")
                android.util.Log.d("FirestoreChecker", "Found ${archivedItems.size} archived items in Firestore")
                
                // Prepare message
                val message = "Found ${archivedItems.size} archived items in Firestore"
                
                // Show results in a Toast on the main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreChecker", "Error checking archived items: ${e.message}", e)
            
            // Show error in a Toast on the main thread
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
} 