package com.example.inventory.data.firebase

import com.example.inventory.data.model.Item
import com.example.inventory.data.repository.ItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import android.util.Log

@Singleton
class FirebaseItemRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ItemRepository {
    private val itemsCollection = firestore.collection("items")
    private val TAG = "FirebaseItemRepository"
    
    // MutableStateFlow to hold the latest items
    private val _itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    
    init {
        android.util.Log.d(TAG, "Initializing FirebaseItemRepository")
        
        // Start monitoring the flow state
        monitorFlowState()
        
        // Set up a real-time listener for the items collection
        setupItemsListener()
        
        // Also do an immediate fetch to populate data right away
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d(TAG, "Performing initial data fetch")
                val snapshot = itemsCollection.get().await()
                android.util.Log.d(TAG, "Initial fetch retrieved ${snapshot.size()} documents")
                
                if (snapshot.isEmpty) {
                    android.util.Log.w(TAG, "Initial fetch returned empty collection!")
                    return@launch
                }
                
                // Process items
                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Get the data as a map to ensure we can manually handle fields
                        val data = doc.data
                        if (data != null) {
                            // Always use document ID as idString if it's not set explicitly
                            val idString = data["idString"] as? String ?: doc.id
                            
                            // Get other fields
                            val name = data["name"] as? String ?: ""
                            val category = data["category"] as? String ?: ""
                            val type = data["type"] as? String ?: ""
                            val barcode = data["barcode"] as? String ?: ""
                            val condition = data["condition"] as? String ?: ""
                            val status = data["status"] as? String ?: ""
                            val description = data["description"] as? String ?: ""
                            val photoPath = data["photoPath"] as? String
                            
                            // Handle isActive field specially
                            val isActive = when (val isActiveVal = data["isActive"]) {
                                is Boolean -> isActiveVal
                                is Long -> isActiveVal != 0L
                                is Int -> isActiveVal != 0
                                is String -> isActiveVal.equals("true", ignoreCase = true)
                                else -> true
                            }
                            
                            val lastModified = when (val lastModVal = data["lastModified"]) {
                                is Long -> lastModVal
                                is com.google.firebase.Timestamp -> lastModVal.seconds * 1000
                                else -> System.currentTimeMillis()
                            }
                            
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
                        } else null
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Error converting doc ${doc.id}: ${e.message}")
                        null
                    }
                }
                
                // Only update the flow if we have items and the current flow is empty
                if (items.isNotEmpty() && _itemsFlow.value.isEmpty()) {
                    android.util.Log.d(TAG, "Setting initial ${items.size} items to flow")
                    android.util.Log.d(TAG, "Initial items: ${items.map { "${it.name} (${it.idString})" }.joinToString()}")
                    _itemsFlow.value = items
                    android.util.Log.d(TAG, "Initial flow update complete, now has ${_itemsFlow.value.size} items")
                } else {
                    android.util.Log.d(TAG, "Skipping initial items update - current flow has ${_itemsFlow.value.size} items")
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error during initial data fetch: ${e.message}", e)
            }
        }
    }
    
    private fun setupItemsListener() {
        android.util.Log.d(TAG, "Setting up real-time listener for items collection")
        
        // Add a snapshot listener without metadata changes to reduce unnecessary updates
        itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e(TAG, "Error listening for item updates: ${error.message}")
                // Don't update the flow on error, keep existing items
                return@addSnapshotListener
            }
            
            if (snapshot == null) {
                android.util.Log.e(TAG, "Snapshot is null in item listener")
                // Don't update the flow on null snapshot, keep existing items
                return@addSnapshotListener
            }
            
            android.util.Log.d(TAG, "Items collection updated: ${snapshot.documents.size} documents")
            android.util.Log.d(TAG, "Current flow has ${_itemsFlow.value.size} items before update")
            
            try {
                // Process the documents into items
                val items = mutableListOf<Item>()
                
                for (document in snapshot.documents) {
                    try {
                        android.util.Log.d(TAG, "Processing document ID: ${document.id}")
                        
                        // Get the data as a map to ensure we can manually handle fields
                        val data = document.data
                        if (data != null) {
                            // Always use document ID as idString if it's not set explicitly
                            val idString = data["idString"] as? String ?: document.id
                            
                            // Get other fields
                            val name = data["name"] as? String ?: ""
                            val category = data["category"] as? String ?: ""
                            val type = data["type"] as? String ?: ""
                            val barcode = data["barcode"] as? String ?: ""
                            val condition = data["condition"] as? String ?: ""
                            val status = data["status"] as? String ?: ""
                            val description = data["description"] as? String ?: ""
                            val photoPath = data["photoPath"] as? String
                            
                            // Handle isActive field specially
                            val isActive = when (val isActiveVal = data["isActive"]) {
                                is Boolean -> isActiveVal
                                is Long -> isActiveVal != 0L
                                is Int -> isActiveVal != 0
                                is String -> isActiveVal.equals("true", ignoreCase = true)
                                else -> true
                            }
                            
                            val lastModified = when (val lastModVal = data["lastModified"]) {
                                is Long -> lastModVal
                                is com.google.firebase.Timestamp -> lastModVal.seconds * 1000
                                else -> System.currentTimeMillis()
                            }
                            
                            val item = Item(
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
                            
                            android.util.Log.d(TAG, "Document ${document.id} converted to Item: name=${item.name}, idString=${item.idString}, uuid=${item.id}, isActive=${item.isActive}")
                            items.add(item)
                        } else {
                            android.util.Log.e(TAG, "Document ${document.id} has null data")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Error processing document ${document.id}: ${e.message}")
                    }
                }
                
                // Only update the flow if we have items
                if (items.isNotEmpty()) {
                    android.util.Log.d(TAG, "Updating flow with ${items.size} items")
                    android.util.Log.d(TAG, "Items in update: ${items.map { "${it.name} (${it.idString})" }.joinToString()}")
                    _itemsFlow.value = items
                    android.util.Log.d(TAG, "Flow updated, now has ${_itemsFlow.value.size} items")
                } else {
                    android.util.Log.w(TAG, "No items to update in flow")
                }
                
                // Debug item status
                val activeItems = items.filter { it.isActive }
                val archivedItems = items.filter { !it.isActive }
                android.util.Log.d(TAG, "Active items: ${activeItems.size}, Archived items: ${archivedItems.size}")
                android.util.Log.d(TAG, "Active items: ${activeItems.map { "${it.name} (${it.idString})" }.joinToString()}")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error processing items snapshot: ${e.message}", e)
                // Don't update the flow on error, keep existing items
            }
        }
    }

    // Add a method to monitor the flow state
    private fun monitorFlowState() {
        CoroutineScope(Dispatchers.IO).launch {
            _itemsFlow.collect { items ->
                android.util.Log.d(TAG, "Flow state changed - items count: ${items.size}")
                android.util.Log.d(TAG, "Items in flow: ${items.map { "${it.name} (${it.idString})" }.joinToString()}")
            }
        }
    }

    override fun getAllItems(): Flow<List<Item>> = _itemsFlow

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
        try {
            android.util.Log.d(TAG, "Getting all categories from Firestore")
            val snapshot = itemsCollection.get().await()
            
            if (snapshot.isEmpty) {
                android.util.Log.d(TAG, "No items found when fetching categories")
                emit(emptyList<String>())
                return@flow
            }
            
            // Extract categories from all documents, filter out null or empty ones, and get distinct values
            val categories = snapshot.documents.mapNotNull { doc -> 
                val category = doc.getString("category")
                if (category.isNullOrBlank()) {
                    // Use type as fallback if category is not available
                    doc.getString("type")
                } else {
                    category
                }
            }.filter { it.isNotBlank() }.distinct().sorted()
            
            android.util.Log.d(TAG, "Found ${categories.size} distinct categories: ${categories.joinToString()}")
            emit(categories)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting categories: ${e.message}", e)
            // Emit empty list on error to avoid crashing
            emit(emptyList())
        }
    }

    override suspend fun getItemByBarcode(barcode: String): Item? {
        return try {
            Log.d(TAG, "Getting item by barcode: $barcode")
            val query = itemsCollection.whereEqualTo("barcode", barcode).get().await()
            if (query.isEmpty) {
                Log.w(TAG, "No item found with barcode: $barcode")
                return null
            }
            
            val doc = query.documents.first()
            val item = doc.toObject(Item::class.java)
            if (item == null) {
                Log.w(TAG, "Failed to convert item document to Item object: ${doc.id}")
            }
            item
        } catch (e: Exception) {
            Log.e(TAG, "Error getting item by barcode $barcode: ${e.message}", e)
            null
        }
    }

    override suspend fun getItemById(id: UUID): Item? {
        return try {
            Log.d(TAG, "Getting item by ID: $id")
            val doc = itemsCollection.document(id.toString()).get().await()
            if (!doc.exists()) {
                Log.w(TAG, "Item with ID $id not found")
                return null
            }
            
            val item = doc.toObject(Item::class.java)
            if (item == null) {
                Log.w(TAG, "Failed to convert item document to Item object: ${doc.id}")
            }
            item
        } catch (e: Exception) {
            Log.e(TAG, "Error getting item by ID $id: ${e.message}", e)
            null
        }
    }

    override suspend fun insertItem(item: Item) {
        try {
            Log.d(TAG, "Inserting new item: ${item.name}")
            itemsCollection.document(item.id.toString()).set(item).await()
            Log.d(TAG, "Successfully inserted item with ID: ${item.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting item: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateItem(item: Item) {
        try {
            Log.d(TAG, "Updating item with ID: ${item.id}")
            itemsCollection.document(item.id.toString()).set(item).await()
            Log.d(TAG, "Successfully updated item with ID: ${item.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating item: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteItem(item: Item) {
        try {
            Log.d(TAG, "Deleting item with ID: ${item.id}")
            itemsCollection.document(item.id.toString()).delete().await()
            Log.d(TAG, "Successfully deleted item with ID: ${item.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting item: ${e.message}", e)
            throw e
        }
    }

    override suspend fun refreshFromFirebase() {
        val snapshot = itemsCollection.get().await()
        val items = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Item::class.java)
        }
        _itemsFlow.value = items
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

    // Add a method to force refresh of items
    fun refreshItems() {
        android.util.Log.d(TAG, "Force refreshing items from Firestore")
        
        // Re-setup the listener to force refresh
        setupItemsListener()
        
        // Also do a one-time fetch
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = itemsCollection.get().await()
                android.util.Log.d(TAG, "One-time fetch retrieved ${snapshot.size()} documents")
                
                // Process documents to items
                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Item::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Error converting document ${doc.id}: ${e.message}")
                        null
                    }
                }
                
                // Update the flow
                _itemsFlow.value = items
                android.util.Log.d(TAG, "Refreshed ${items.size} items into flow")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error during one-time fetch: ${e.message}")
            }
        }
    }

    // Add debug method to check collection setup and retrieve items directly
    suspend fun checkItemsCollectionSetup() {
        try {
            android.util.Log.d(TAG, "====== COLLECTION SETUP DIAGNOSTICS ======")
            
            // Check if collection exists and get document count
            val snapshot = itemsCollection.get().await()
            android.util.Log.d(TAG, "Items collection exists with ${snapshot.size()} documents")
            
            // Check what's flowing through our StateFlow
            val flowItems = _itemsFlow.value
            android.util.Log.d(TAG, "Current StateFlow has ${flowItems.size} items")
            
            // Check if the snapshot listener is working
            android.util.Log.d(TAG, "Setting up a test snapshot listener")
            
            // Create a one-time test listener
            itemsCollection.addSnapshotListener { testSnapshot, testError ->
                if (testError != null) {
                    android.util.Log.e(TAG, "Test listener error: ${testError.message}")
                    return@addSnapshotListener
                }
                
                if (testSnapshot != null) {
                    android.util.Log.d(TAG, "Test listener received snapshot with ${testSnapshot.documents.size} documents")
                    
                    // Check the first few documents
                    testSnapshot.documents.take(3).forEach { doc ->
                        android.util.Log.d(TAG, "Sample document: ID=${doc.id}, data=${doc.data}")
                    }
                }
            }
            
            // Debug the setupItemsListener implementation
            android.util.Log.d(TAG, "Checking if setupItemsListener is properly registered")
            setupItemsListener() // Try re-registering to see if it helps
            
            android.util.Log.d(TAG, "====== END DIAGNOSTICS ======")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error in collection diagnostics: ${e.message}", e)
        }
    }
    
    // Add a diagnostic method to dump all items and their ID information
    suspend fun logAllItemsWithIds() {
        try {
            android.util.Log.d(TAG, "======= DUMPING ALL ITEMS WITH ID DETAILS =======")
            
            val snapshot = itemsCollection.get().await()
            android.util.Log.d(TAG, "Found ${snapshot.documents.size} items in database")
            
            snapshot.documents.forEachIndexed { index, doc ->
                // Get raw document data
                val data = doc.data
                val docId = doc.id
                val idString = data?.get("idString") as? String ?: "null"
                val name = data?.get("name") as? String ?: "Unknown"
                
                // Try to create Item and get its UUID
                val item = doc.toObject(Item::class.java)
                val uuid = item?.id?.toString() ?: "Failed to convert"
                
                // Log the details
                android.util.Log.d(TAG, "Item $index:")
                android.util.Log.d(TAG, "  Document ID: $docId")
                android.util.Log.d(TAG, "  idString field: $idString")
                android.util.Log.d(TAG, "  Name: $name")
                android.util.Log.d(TAG, "  Converted UUID: $uuid")
                
                // Calculate the expected UUID to verify our algorithm
                val expectedUuid = try {
                    if (idString.isNotBlank()) {
                        try {
                            UUID.fromString(idString)
                        } catch (e: IllegalArgumentException) {
                            UUID.nameUUIDFromBytes(idString.toByteArray())
                        }
                    } else {
                        UUID.nameUUIDFromBytes(docId.toByteArray())
                    }
                } catch (e: Exception) {
                    null
                }
                
                android.util.Log.d(TAG, "  Expected UUID: $expectedUuid")
                
                // Check if UUIDs match
                if (uuid != expectedUuid.toString()) {
                    android.util.Log.e(TAG, "  ⚠️ UUID MISMATCH ⚠️")
                }
                
                android.util.Log.d(TAG, "  ---")
            }
            
            android.util.Log.d(TAG, "======= END ITEM DUMP =======")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error dumping items: ${e.message}", e)
        }
    }
} 