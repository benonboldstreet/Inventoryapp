package com.example.inventory.data.firebase

import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.ItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseItemRepository @Inject constructor(
    firebaseConfig: FirebaseConfig
) : ItemRepository {
    private val itemsCollection = firebaseConfig.firestore.collection("items")

    override fun getAllItems(): Flow<List<Item>> = flow {
        try {
            android.util.Log.d("FirebaseItemRepo", "Getting all items from Firestore")
            val snapshot = itemsCollection.get().await()
            android.util.Log.d("FirebaseItemRepo", "Retrieved ${snapshot.documents.size} documents from Firestore")
            
            // Add explicit check for isActive field in the raw documents
            snapshot.documents.forEach { doc ->
                val id = doc.id
                android.util.Log.d("FirebaseItemRepo", "=== DOCUMENT FIELDS ===")
                android.util.Log.d("FirebaseItemRepo", "Document $id contains fields: ${doc.data?.keys?.joinToString()}")
                
                // Log every field in the document
                doc.data?.forEach { (key, value) ->
                    android.util.Log.d("FirebaseItemRepo", "Field '$key' = $value (${value?.javaClass?.simpleName})")
                }
                
                val isActiveField = doc.get("isActive")
                android.util.Log.d("FirebaseItemRepo", "Document $id raw isActive field: $isActiveField (${isActiveField?.javaClass?.simpleName})")
            }
            
            // Enhanced conversion with error handling for each document
            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    // First try standard conversion
                    val item = doc.toObject(Item::class.java)
                    
                    if (item == null) {
                        android.util.Log.w("FirebaseItemRepo", "Failed to convert document ${doc.id} to Item")
                        
                        // Try manual conversion as fallback
                        try {
                            // Manual conversion with explicit Boolean handling
                            val idString = doc.id
                            val name = doc.getString("name") ?: ""
                            val category = doc.getString("category") ?: ""
                            val type = doc.getString("type") ?: ""
                            val barcode = doc.getString("barcode") ?: ""
                            val condition = doc.getString("condition") ?: ""
                            val status = doc.getString("status") ?: ""
                            val description = doc.getString("description") ?: ""
                            val photoPath = doc.getString("photoPath")
                            
                            // Special handling for isActive field - force Boolean type
                            val isActiveRaw = doc.get("isActive")
                            val isActive = when {
                                isActiveRaw is Boolean -> isActiveRaw
                                isActiveRaw.toString().equals("false", ignoreCase = true) -> false
                                isActiveRaw.toString().equals("0", ignoreCase = true) -> false
                                isActiveRaw.toString().equals("no", ignoreCase = true) -> false
                                else -> true // Default to active if unclear
                            }
                            
                            val lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis()
                            
                            android.util.Log.d("FirebaseItemRepo", "Manual conversion for document ${doc.id}")
                            
                            Item(
                                idString = idString,
                                name = name,
                                category = category,
                                type = type,
                                barcode = barcode,
                                condition = condition,
                                status = status,
                                description = description,
                                photoPath = photoPath,
                                isActive = isActive,
                                lastModified = lastModified
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("FirebaseItemRepo", "Manual conversion also failed for ${doc.id}: ${e.message}", e)
                            null
                        }
                    } else {
                        android.util.Log.d("FirebaseItemRepo", "Converted item ${doc.id} - isActive=${item.isActive}")
                        item
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseItemRepo", "Error converting document ${doc.id} to Item: ${e.message}", e)
                    null
                }
            }
            
            // Count and log archived items explicitly
            val archivedItems = items.filter { !it.isActive }
            if (archivedItems.isNotEmpty()) {
                android.util.Log.d("FirebaseItemRepo", "===== ARCHIVED ITEMS DETECTED =====")
                android.util.Log.d("FirebaseItemRepo", "Found ${archivedItems.size} archived items that should display in the ARCHIVED tab")
                archivedItems.forEach { item ->
                    android.util.Log.d("FirebaseItemRepo", "Archived item: ${item.id} - ${item.name}, isActive=${item.isActive}")
                }
            } else {
                android.util.Log.d("FirebaseItemRepo", "No archived items found in the dataset")
            }
            
            android.util.Log.d("FirebaseItemRepo", "Successfully converted ${items.size} documents to Item objects")
            emit(items)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseItemRepo", "Error getting all items: ${e.message}", e)
            emit(emptyList<Item>())
        }
    }

    override fun getItemsByStatus(status: String): Flow<List<Item>> = flow {
        val snapshot = itemsCollection.whereEqualTo("status", status).get().await()
        val items = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Item::class.java)
        }
        emit(items)
    }

    override fun getItemsByType(type: String): Flow<List<Item>> = flow {
        val snapshot = itemsCollection.whereEqualTo("type", type).get().await()
        val items = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Item::class.java)
        }
        emit(items)
    }

    override fun getItemsByCategory(category: String): Flow<List<Item>> = flow {
        val snapshot = itemsCollection.whereEqualTo("category", category).get().await()
        val items = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Item::class.java)
        }
        emit(items)
    }

    override fun getAllCategories(): Flow<List<String>> = flow {
        val snapshot = itemsCollection.get().await()
        val categories = snapshot.documents.mapNotNull { it.getString("category") }.distinct()
        emit(categories)
    }

    override suspend fun getItemByBarcode(barcode: String): Item? {
        val snapshot = itemsCollection.whereEqualTo("barcode", barcode).get().await()
        return snapshot.documents.firstOrNull()?.toObject(Item::class.java)
    }

    override suspend fun getItemById(id: UUID): Item? {
        try {
            android.util.Log.d("FirebaseItemRepo", "Looking up item by ID: $id")
            
            // Try looking up by the original ID format first
            val doc = itemsCollection.document(id.toString()).get().await()
            
            if (doc.exists()) {
                android.util.Log.d("FirebaseItemRepo", "Item found with ID: $id")
                return doc.toObject(Item::class.java)
            } else {
                android.util.Log.d("FirebaseItemRepo", "Item not found with ID: $id, trying to query by idString field")
                
                // If not found, try querying by idString field
                val query = itemsCollection.whereEqualTo("idString", id.toString()).get().await()
                if (!query.isEmpty) {
                    android.util.Log.d("FirebaseItemRepo", "Item found using idString query")
                    return query.documents.firstOrNull()?.toObject(Item::class.java)
                }
                
                android.util.Log.d("FirebaseItemRepo", "Item not found with ID: $id")
                return null
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseItemRepo", "Error getting item by ID: $id, error: ${e.message}", e)
            return null
        }
    }

    override suspend fun insertItem(item: Item) {
        try {
            android.util.Log.d("FirebaseItemRepo", "Inserting new item with ID: ${item.id}")
            
            // Check if this item ID already existed but was deleted
            val existingDoc = itemsCollection.document(item.id.toString()).get().await()
            if (existingDoc.exists()) {
                android.util.Log.w("FirebaseItemRepo", "⚠️ WARNING: Document already exists with ID: ${item.id}")
            }
            
            val itemMap = mapOf(
                "idString" to item.idString,
                "name" to item.name,
                "category" to item.category,
                "type" to item.type,
                "barcode" to item.barcode,
                "condition" to item.condition,
                "status" to item.status,
                "description" to item.description,
                "photoPath" to item.photoPath,
                "isActive" to item.isActive,
                "lastModified" to item.lastModified
            )
            
            // Always use the item's UUID toString as the document ID
            val docId = item.id.toString()
            android.util.Log.d("FirebaseItemRepo", "Using document ID for insert: $docId")
            
            itemsCollection.document(docId).set(itemMap).await()
            android.util.Log.d("FirebaseItemRepo", "Item inserted successfully: ${item.id}")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseItemRepo", "Error inserting item: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateItem(item: Item) {
        try {
            android.util.Log.d("FirebaseItemRepo", "===== UPDATE ITEM DEBUGGING =====")
            android.util.Log.d("FirebaseItemRepo", "Updating item ID: ${item.id}, idString: ${item.idString}")
            
            // Before update, verify the item exists
            val checkDoc = itemsCollection.document(item.id.toString()).get().await()
            if (!checkDoc.exists()) {
                android.util.Log.w("FirebaseItemRepo", "WARNING: Document doesn't exist at path: ${item.id}")
                
                // Try to look up by idString field
                val query = itemsCollection.whereEqualTo("idString", item.idString).get().await()
                if (query.isEmpty) {
                    android.util.Log.e("FirebaseItemRepo", "CRITICAL ERROR: Item doesn't exist in database, can't update!")
                    throw Exception("Item ${item.id} doesn't exist in database")
                } else {
                    android.util.Log.d("FirebaseItemRepo", "Found item using idString query")
                }
            }
            
            // Create a complete map of all item properties
            val itemMap = mapOf(
                "idString" to item.idString,
                "name" to item.name,
                "category" to item.category,
                "type" to item.type, 
                "barcode" to item.barcode,
                "condition" to item.condition,
                "status" to item.status,
                "description" to item.description,
                "photoPath" to item.photoPath,
                "isActive" to item.isActive,
                "lastModified" to item.lastModified
            )
            
            android.util.Log.d("FirebaseItemRepo", "Prepared item map with fields: ${itemMap.keys.joinToString()}")
            android.util.Log.d("FirebaseItemRepo", "isActive value being set to: ${item.isActive}")
            
            // Determine the correct document ID to use
            val docId = item.id.toString()
            android.util.Log.d("FirebaseItemRepo", "Using document ID for update: $docId")
            
            // Get document reference and set data
            val docRef = itemsCollection.document(docId)
            android.util.Log.d("FirebaseItemRepo", "Document reference path: ${docRef.path}")
            
            // Use set() instead of update() to ensure all fields are written
            docRef.set(itemMap).await()
            
            // Verify the update was successful
            val verifyDoc = itemsCollection.document(docId).get().await()
            if (verifyDoc.exists()) {
                val verifyItem = verifyDoc.toObject(Item::class.java)
                android.util.Log.d("FirebaseItemRepo", "Verification - Item after update: id=${verifyItem?.id}, isActive=${verifyItem?.isActive}")
            } else {
                android.util.Log.e("FirebaseItemRepo", "CRITICAL ERROR: Document not found after update!")
            }
            
            android.util.Log.d("FirebaseItemRepo", "===== UPDATE ITEM COMPLETE =====")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseItemRepo", "ERROR updating item: ${e.message}", e)
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun deleteItem(item: Item) {
        android.util.Log.w("FirebaseItemRepo", "⚠️ deleteItem called for ID: ${item.id}, CONVERTING TO ARCHIVE OPERATION")
        android.util.Log.d("FirebaseItemRepo", "Instead of deleting, we will mark the item as inactive")
        
        try {
            // First check if the item exists
            val docRef = itemsCollection.document(item.id.toString())
            val doc = docRef.get().await()
            
            if (doc.exists()) {
                // Log the current state of the isActive field
                val currentIsActive = doc.getBoolean("isActive")
                val rawIsActive = doc.get("isActive")
                android.util.Log.d("FirebaseItemRepo", "Current isActive value: $currentIsActive (raw: $rawIsActive, type: ${rawIsActive?.javaClass?.simpleName})")
                
                // Instead of deleting, update to mark as inactive
                val updateMap = mapOf(
                    "isActive" to false,
                    "lastModified" to System.currentTimeMillis()
                )
                
                // Log what we're updating to
                android.util.Log.d("FirebaseItemRepo", "Setting isActive to: ${updateMap["isActive"]} (type: ${updateMap["isActive"]?.javaClass?.simpleName})")
                
                // Use set with merge option to ensure proper updating
                docRef.set(updateMap, com.google.firebase.firestore.SetOptions.merge()).await()
                
                // Verify the update was successful
                val verifyDoc = docRef.get().await()
                val verifyIsActive = verifyDoc.getBoolean("isActive")
                val verifyRawIsActive = verifyDoc.get("isActive")
                
                android.util.Log.d("FirebaseItemRepo", "After update - isActive: $verifyIsActive (raw: $verifyRawIsActive, type: ${verifyRawIsActive?.javaClass?.simpleName})")
                android.util.Log.d("FirebaseItemRepo", "Item ${item.id} marked as inactive instead of being deleted")
                
                if (verifyIsActive != false) {
                    android.util.Log.e("FirebaseItemRepo", "CRITICAL ERROR: Item was not properly archived! isActive is still $verifyIsActive")
                    
                    // Try an alternative update method
                    android.util.Log.d("FirebaseItemRepo", "Trying alternative update method with .update() instead of .set()")
                    docRef.update("isActive", false).await()
                    
                    // Verify again
                    val secondVerifyDoc = docRef.get().await()
                    val secondVerifyIsActive = secondVerifyDoc.getBoolean("isActive")
                    android.util.Log.d("FirebaseItemRepo", "After second update attempt - isActive: $secondVerifyIsActive")
                }
            } else {
                android.util.Log.e("FirebaseItemRepo", "Item ${item.id} not found, couldn't mark as inactive")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseItemRepo", "Error marking item as inactive: ${e.message}", e)
            throw e
        }
    }

    // Direct document update method for more reliable updates
    suspend fun updateItemWithMap(documentId: String, itemMap: Map<String, Any?>) {
        try {
            android.util.Log.d("FirebaseItemRepo", "Direct updating document: $documentId")
            android.util.Log.d("FirebaseItemRepo", "Map fields: ${itemMap.keys.joinToString()}")
            
            // Get document reference
            val docRef = itemsCollection.document(documentId)
            
            // Update the document
            docRef.set(itemMap).await()
            
            android.util.Log.d("FirebaseItemRepo", "Direct update completed for document: $documentId")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseItemRepo", "Error in direct update: ${e.message}", e)
            throw e
        }
    }

    // Add a method to directly query Firestore for archived items
    suspend fun getArchivedItemsDirectly(): List<Item> {
        try {
            android.util.Log.d("FirebaseItemRepo", "===== DIRECT QUERY FOR ARCHIVED ITEMS =====")
            
            // Print all documents first for diagnosis
            val allDocsSnapshot = itemsCollection.get().await()
            android.util.Log.d("FirebaseItemRepo", "Total items in Firestore: ${allDocsSnapshot.documents.size}")
            allDocsSnapshot.documents.forEach { doc ->
                val id = doc.id
                val name = doc.getString("name") ?: "Unknown"
                val rawIsActive = doc.get("isActive")
                val isActiveType = rawIsActive?.javaClass?.simpleName ?: "null"
                android.util.Log.d("FirebaseItemRepo", "Item: $id - $name, isActive raw value: $rawIsActive (type: $isActiveType)")
            }
            
            // Directly query Firestore for items where isActive = false
            val query = itemsCollection.whereEqualTo("isActive", false).get().await()
            
            android.util.Log.d("FirebaseItemRepo", "Direct query with whereEqualTo('isActive', false) found ${query.documents.size} archived documents")
            
            // Try alternative query approaches if the first one returns no results
            if (query.documents.isEmpty()) {
                android.util.Log.d("FirebaseItemRepo", "Trying alternative queries since standard query returned no results")
                
                // Try different approaches for querying inactive items
                val manuallyFilteredDocs = allDocsSnapshot.documents.filter { doc ->
                    val isActiveValue = doc.get("isActive")
                    when {
                        isActiveValue is Boolean -> !isActiveValue
                        isActiveValue.toString().equals("false", ignoreCase = true) -> true
                        isActiveValue.toString().equals("0", ignoreCase = true) -> true
                        else -> false
                    }
                }
                
                android.util.Log.d("FirebaseItemRepo", "Manual filtering found ${manuallyFilteredDocs.size} potentially archived items")
                
                if (manuallyFilteredDocs.isNotEmpty()) {
                    // Convert manually filtered documents to items
                    val manualItems = manuallyFilteredDocs.mapNotNull { doc ->
                        try {
                            // Use manual conversion to ensure proper handling
                            val idString = doc.id
                            val name = doc.getString("name") ?: ""
                            val category = doc.getString("category") ?: ""
                            val type = doc.getString("type") ?: ""
                            val barcode = doc.getString("barcode") ?: ""
                            val condition = doc.getString("condition") ?: ""
                            val status = doc.getString("status") ?: ""
                            val description = doc.getString("description") ?: ""
                            val photoPath = doc.getString("photoPath")
                            val lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis()
                            
                            android.util.Log.d("FirebaseItemRepo", "Creating manually converted item for $idString - $name")
                            
                            Item(
                                idString = idString,
                                name = name,
                                category = category,
                                type = type,
                                barcode = barcode,
                                condition = condition,
                                status = status,
                                description = description,
                                photoPath = photoPath,
                                isActive = false, // Explicitly set to false
                                lastModified = lastModified
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("FirebaseItemRepo", "Manual conversion failed for document ${doc.id}: ${e.message}", e)
                            null
                        }
                    }
                    
                    android.util.Log.d("FirebaseItemRepo", "Manual conversion produced ${manualItems.size} archived items")
                    return manualItems
                }
            }
            
            // Convert documents to items using standard approach if manual wasn't needed
            val archivedItems = query.documents.mapNotNull { doc ->
                try {
                    val item = doc.toObject(Item::class.java)
                    if (item != null) {
                        android.util.Log.d("FirebaseItemRepo", "Found archived item: id=${item.id}, name=${item.name}, isActive=${item.isActive}")
                    } else {
                        android.util.Log.w("FirebaseItemRepo", "Failed to convert document ${doc.id} to Item")
                    }
                    item
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseItemRepo", "Error converting document ${doc.id} to Item: ${e.message}", e)
                    null
                }
            }
            
            android.util.Log.d("FirebaseItemRepo", "Direct query returning ${archivedItems.size} archived items")
            return archivedItems
        } catch (e: Exception) {
            android.util.Log.e("FirebaseItemRepo", "Error in direct query for archived items: ${e.message}", e)
            return emptyList()
        }
    }

    // Add a diagnostic method for the isActive field type issue
    suspend fun checkIsActiveFieldType() {
        try {
            android.util.Log.d("FirebaseItemRepo", "===== CHECKING isActive FIELD TYPE =====")
            val snapshot = itemsCollection.get().await()
            
            android.util.Log.d("FirebaseItemRepo", "Retrieved ${snapshot.documents.size} documents from Firestore")
            
            // Detailed logging of each document's isActive field
            snapshot.documents.forEach { doc ->
                val id = doc.id
                val isActiveRaw = doc.get("isActive")
                val isActiveType = isActiveRaw?.javaClass?.simpleName ?: "null"
                
                // Get item using the normal conversion
                val item = doc.toObject(Item::class.java)
                val itemIsActive = item?.isActive
                
                // Manual Boolean extraction for comparison
                val isActiveBoolean = doc.getBoolean("isActive")
                
                android.util.Log.d("FirebaseItemRepo", 
                    "Document $id: isActive field=" +
                    "\n  - Raw value: $isActiveRaw (Type: $isActiveType)" +
                    "\n  - Converted Item.isActive: $itemIsActive" +
                    "\n  - Manual getBoolean(): $isActiveBoolean"
                )
                
                // Check if there's a mismatch
                if (itemIsActive != isActiveBoolean && isActiveBoolean != null) {
                    android.util.Log.e("FirebaseItemRepo", "⚠️ TYPE MISMATCH DETECTED! Item conversion not matching manual Boolean extraction")
                }
            }
            
            // Count of inactive items using different methods
            val inactiveItems = snapshot.documents.mapNotNull { it.toObject(Item::class.java) }.filter { !it.isActive }
            val manualInactiveCount = snapshot.documents.count { it.getBoolean("isActive") == false }
            
            android.util.Log.d("FirebaseItemRepo", "Inactive items count:" +
                "\n  - Using Item model conversion: ${inactiveItems.size}" +
                "\n  - Using manual Boolean extraction: $manualInactiveCount"
            )
            
        } catch (e: Exception) {
            android.util.Log.e("FirebaseItemRepo", "Error checking isActive field type: ${e.message}", e)
        }
    }
} 