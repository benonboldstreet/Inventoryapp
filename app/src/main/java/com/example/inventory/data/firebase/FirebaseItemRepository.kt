package com.example.inventory.data.firebase

import android.util.Log
import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.ItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ItemRepository {
    private val TAG = "FirebaseItemRepo"
    private val collection = firestore.collection("items")

    init {
        Log.d(TAG, "FirebaseItemRepository initialized")
        Log.d(TAG, "Firestore instance: $firestore")
        Log.d(TAG, "Collection path: ${collection.path}")
        
        // Test Firestore connection
        firestore.collection("items").get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Successfully connected to Firestore. Found ${documents.size()} documents")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error connecting to Firestore: ${e.message}", e)
            }
    }

    override fun getAllItems(): Flow<List<Item>> = callbackFlow {
        Log.d(TAG, "Getting all items")
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error getting items: ${error.message}", error)
                if (error is FirebaseFirestoreException) {
                    Log.e(TAG, "Firestore error code: ${error.code}")
                    Log.e(TAG, "Firestore error details: ${error.message}")
                }
                trySend(emptyList())
                return@addSnapshotListener
            }

            Log.d(TAG, "Received snapshot with ${snapshot?.documents?.size ?: 0} documents")
            val items = snapshot?.documents?.mapNotNull { doc ->
                try {
                    Log.d(TAG, "Processing document: ${doc.id}")
                    Item(
                        id = UUID.fromString(doc.getString("idString") ?: return@mapNotNull null),
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        type = doc.getString("type") ?: "",
                        barcode = doc.getString("barcode") ?: "",
                        condition = doc.getString("condition") ?: "Good",
                        status = doc.getString("status") ?: "Available",
                        description = doc.getString("description") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis(),
                        photoPath = doc.getString("photoPath")
                    ).also { Log.d(TAG, "Successfully mapped item: ${it.name}") }
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Item: ${e.message}", e)
                    null
                }
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    override fun getItemById(id: UUID): Flow<Item?> = callbackFlow {
        Log.d(TAG, "Getting item by ID: $id")
        val listener = collection.document(id.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting item by ID: ${error.message}", error)
                    if (error is FirebaseFirestoreException) {
                        Log.e(TAG, "Firestore error code: ${error.code}")
                        Log.e(TAG, "Firestore error details: ${error.message}")
                    }
                    trySend(null)
                    return@addSnapshotListener
                }

                val item = try {
                    snapshot?.let { doc ->
                        Log.d(TAG, "Processing document: ${doc.id}")
                        Item(
                            id = UUID.fromString(doc.getString("idString") ?: return@addSnapshotListener),
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "",
                            type = doc.getString("type") ?: "",
                            barcode = doc.getString("barcode") ?: "",
                            condition = doc.getString("condition") ?: "Good",
                            status = doc.getString("status") ?: "Available",
                            description = doc.getString("description") ?: "",
                            isActive = doc.getBoolean("isActive") ?: true,
                            lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis(),
                            photoPath = doc.getString("photoPath")
                        ).also { Log.d(TAG, "Successfully mapped item: ${it.name}") }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Item: ${e.message}", e)
                    null
                }
                trySend(item)
            }
        awaitClose { listener.remove() }
    }

    override fun getItemByBarcode(barcode: String): Flow<Item?> = callbackFlow {
        Log.d(TAG, "Getting item by barcode: $barcode")
        val listener = collection.whereEqualTo("barcode", barcode)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting item by barcode: ${error.message}", error)
                    if (error is FirebaseFirestoreException) {
                        Log.e(TAG, "Firestore error code: ${error.code}")
                        Log.e(TAG, "Firestore error details: ${error.message}")
                    }
                    trySend(null)
                    return@addSnapshotListener
                }

                val item = try {
                    val documents = snapshot?.documents ?: emptyList()
                    if (documents.isNotEmpty()) {
                        val doc = documents.first()
                        Log.d(TAG, "Processing document: ${doc.id}")
                        Item(
                            id = UUID.fromString(doc.getString("idString") ?: return@addSnapshotListener),
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "",
                            type = doc.getString("type") ?: "",
                            barcode = doc.getString("barcode") ?: "",
                            condition = doc.getString("condition") ?: "Good",
                            status = doc.getString("status") ?: "Available",
                            description = doc.getString("description") ?: "",
                            isActive = doc.getBoolean("isActive") ?: true,
                            lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis(),
                            photoPath = doc.getString("photoPath")
                        ).also { Log.d(TAG, "Successfully mapped item: ${it.name}") }
                    } else {
                        Log.d(TAG, "No item found with barcode: $barcode")
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Item: ${e.message}", e)
                    null
                }
                trySend(item)
            }
        awaitClose { listener.remove() }
    }

    override fun getItemsByCategory(category: String): Flow<List<Item>> = callbackFlow {
        val listener = collection.whereEqualTo("category", category)
            .addSnapshotListener { snapshot, error ->
            if (error != null) {
                    Log.e(TAG, "Error getting items by category: ${error.message}", error)
                    trySend(emptyList())
                return@addSnapshotListener
            }
            
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Item(
                            id = UUID.fromString(doc.getString("idString") ?: return@mapNotNull null),
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "",
                            type = doc.getString("type") ?: "",
                            barcode = doc.getString("barcode") ?: "",
                            condition = doc.getString("condition") ?: "Good",
                            status = doc.getString("status") ?: "Available",
                            description = doc.getString("description") ?: "",
                            isActive = doc.getBoolean("isActive") ?: true,
                            lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis(),
                            photoPath = doc.getString("photoPath")
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to Item: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllCategories(): Flow<List<String>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error getting categories: ${error.message}", error)
                trySend(emptyList())
                return@addSnapshotListener
            }

            val categories = snapshot?.documents?.mapNotNull { doc ->
                doc.getString("category")
            }?.filter { it.isNotEmpty() }?.distinct() ?: emptyList()
            trySend(categories)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun insertItem(item: Item) {
        try {
            Log.d(TAG, "Inserting item: ${item.name}")
            val data = mapOf(
                "idString" to item.idString,
                "name" to item.name,
                "category" to item.category,
                "type" to item.type,
                "barcode" to item.barcode,
                "condition" to item.condition,
                "status" to item.status,
                "description" to item.description,
                "isActive" to item.isActive,
                "lastModified" to item.lastModified,
                "photoPath" to item.photoPath
            )
            Log.d(TAG, "Item data to insert: $data")
            
            collection.document(item.idString).set(data).await()
            Log.d(TAG, "Successfully inserted item with ID: ${item.idString}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting item: ${e.message}", e)
            if (e is FirebaseFirestoreException) {
                Log.e(TAG, "Firestore error code: ${e.code}")
                Log.e(TAG, "Firestore error details: ${e.message}")
            }
            throw e
        }
    }

    override suspend fun updateItem(item: Item) {
        try {
            Log.d(TAG, "Updating item: ${item.name} with ID: ${item.idString}")
            val data = mapOf(
                "idString" to item.idString,
                "name" to item.name,
                "category" to item.category,
                "type" to item.type,
                "barcode" to item.barcode,
                "condition" to item.condition,
                "status" to item.status,
                "description" to item.description,
                "isActive" to item.isActive,
                "lastModified" to item.lastModified,
                "photoPath" to item.photoPath
            )
            Log.d(TAG, "Item data to update: $data")
            
            collection.document(item.idString).set(data)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully updated item: ${item.name}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating item: ${e.message}", e)
                    if (e is FirebaseFirestoreException) {
                        Log.e(TAG, "Firestore error code: ${e.code}")
                        Log.e(TAG, "Firestore error details: ${e.message}")
                    }
                }
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating item: ${e.message}", e)
            if (e is FirebaseFirestoreException) {
                Log.e(TAG, "Firestore error code: ${e.code}")
                Log.e(TAG, "Firestore error details: ${e.message}")
            }
            throw e
        }
    }

    override suspend fun deleteItem(item: Item) {
        try {
            Log.d(TAG, "Deleting item: ${item.name} with ID: ${item.idString}")
            collection.document(item.idString).delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully deleted item: ${item.name}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error deleting item: ${e.message}", e)
                    if (e is FirebaseFirestoreException) {
                        Log.e(TAG, "Firestore error code: ${e.code}")
                        Log.e(TAG, "Firestore error details: ${e.message}")
                    }
                }
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting item: ${e.message}", e)
            if (e is FirebaseFirestoreException) {
                Log.e(TAG, "Firestore error code: ${e.code}")
                Log.e(TAG, "Firestore error details: ${e.message}")
            }
            throw e
        }
    }

    override suspend fun refreshFromFirebase() {
        // Already using Firestore directly, no need to do anything
        Log.d(TAG, "refreshFromFirebase called, but using Firestore directly")
    }
} 