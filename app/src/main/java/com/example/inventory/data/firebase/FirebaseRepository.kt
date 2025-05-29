package com.example.inventory.data.firebase

import android.util.Log
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.model.CheckoutLog
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val firebaseConfig: FirebaseConfig
) {
    private val TAG = "FirebaseRepository"
    private val itemsCollection = firebaseConfig.firestore.collection("items")
    private val staffCollection = firebaseConfig.firestore.collection("staff")
    private val checkoutsCollection = firebaseConfig.firestore.collection("checkout_logs")

    init {
        // Test database connectivity
        Log.d(TAG, "Testing Firestore connectivity...")
        try {
            // Try to get a single document from each collection
            itemsCollection.limit(1).get().addOnSuccessListener { snapshot ->
                Log.d(TAG, "Items collection test: ${if (snapshot.isEmpty) "Empty" else "Has data"}")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error accessing items collection: ${e.message}", e)
            }

            staffCollection.limit(1).get().addOnSuccessListener { snapshot ->
                Log.d(TAG, "Staff collection test: ${if (snapshot.isEmpty) "Empty" else "Has data"}")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error accessing staff collection: ${e.message}", e)
            }

            checkoutsCollection.limit(1).get().addOnSuccessListener { snapshot ->
                Log.d(TAG, "Checkout logs collection test: ${if (snapshot.isEmpty) "Empty" else "Has data"}")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error accessing checkout logs collection: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error testing Firestore connectivity: ${e.message}", e)
        }
    }

    // Item Operations
    suspend fun addItem(item: Item) {
        val data = mapOf(
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
        
        // Let Firebase generate the ID
        itemsCollection.add(data).await()
    }

    suspend fun updateItem(item: Item) {
        val data = mapOf(
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
        itemsCollection.document(item.id).set(data).await()
    }

    suspend fun deleteItem(itemId: String) {
        itemsCollection.document(itemId).delete().await()
    }

    fun getItems(): Flow<List<Item>> = flow {
        val snapshot = itemsCollection.get().await()
        val items = snapshot.documents.mapNotNull { doc ->
            try {
                Item(
                    id = doc.id,
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
                null
            }
        }
        emit(items)
    }

    // Staff Operations
    suspend fun addStaff(staff: Staff) {
        val data = mapOf(
            "name" to staff.name,
            "department" to staff.department,
            "position" to staff.position,
            "email" to staff.email,
            "phone" to staff.phone,
            "isActive" to staff.isActive,
            "lastModified" to staff.lastModified,
            "firebaseUid" to staff.firebaseUid,
            "role" to staff.role,
            "photoPath" to staff.photoPath
        )
        
        // Let Firebase generate the ID
        staffCollection.add(data).await()
    }

    suspend fun updateStaff(staff: Staff) {
        val data = mapOf(
            "name" to staff.name,
            "department" to staff.department,
            "position" to staff.position,
            "email" to staff.email,
            "phone" to staff.phone,
            "isActive" to staff.isActive,
            "lastModified" to staff.lastModified,
            "firebaseUid" to staff.firebaseUid,
            "role" to staff.role,
            "photoPath" to staff.photoPath
        )
        staffCollection.document(staff.id).set(data).await()
    }

    suspend fun deleteStaff(staffId: String) {
        staffCollection.document(staffId).delete().await()
    }

    fun getStaff(): Flow<List<Staff>> = flow {
        val snapshot = staffCollection.get().await()
        val staff = snapshot.documents.mapNotNull { doc ->
            try {
                Staff(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    department = doc.getString("department") ?: "",
                    position = doc.getString("position") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis(),
                    firebaseUid = doc.getString("firebaseUid"),
                    role = doc.getString("role") ?: "User",
                    photoPath = doc.getString("photoPath")
                )
            } catch (e: Exception) {
                null
            }
        }
        emit(staff)
    }

    // Checkout Operations
    suspend fun addCheckout(checkout: CheckoutLog) {
        val data = mapOf(
            "itemId" to checkout.itemId,
            "staffId" to checkout.staffId,
            "checkoutTimestamp" to checkout.checkoutTimestamp,
            "checkinTimestamp" to checkout.checkinTimestamp,
            "checkoutPhotoPath" to checkout.checkoutPhotoPath,
            "checkinPhotoPath" to checkout.checkinPhotoPath,
            "notes" to checkout.notes
        )
        
        // Let Firebase generate the ID
        checkoutsCollection.add(data).await()
    }

    suspend fun updateCheckout(checkout: CheckoutLog) {
        val data = mapOf(
            "itemId" to checkout.itemId,
            "staffId" to checkout.staffId,
            "checkoutTimestamp" to checkout.checkoutTimestamp,
            "checkinTimestamp" to checkout.checkinTimestamp,
            "checkoutPhotoPath" to checkout.checkoutPhotoPath,
            "checkinPhotoPath" to checkout.checkinPhotoPath,
            "notes" to checkout.notes
        )
        checkoutsCollection.document(checkout.id).set(data).await()
    }

    suspend fun deleteCheckout(checkoutId: String) {
        checkoutsCollection.document(checkoutId).delete().await()
    }

    fun getCheckouts(): Flow<List<CheckoutLog>> = flow {
        val snapshot = checkoutsCollection.get().await()
        val checkouts = snapshot.documents.mapNotNull { doc ->
            try {
                CheckoutLog(
                    id = doc.id,
                    itemId = doc.getString("itemId") ?: return@mapNotNull null,
                    staffId = doc.getString("staffId") ?: return@mapNotNull null,
                    checkoutTimestamp = doc.getLong("checkoutTimestamp") ?: System.currentTimeMillis(),
                    checkinTimestamp = doc.getLong("checkinTimestamp"),
                    checkoutPhotoPath = doc.getString("checkoutPhotoPath"),
                    checkinPhotoPath = doc.getString("checkinPhotoPath"),
                    notes = doc.getString("notes") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
        emit(checkouts)
    }

    fun getActiveCheckouts(): Flow<List<CheckoutLog>> = flow {
        val snapshot = checkoutsCollection
            .whereEqualTo("checkinTimestamp", null)
            .get()
            .await()
        val checkouts = snapshot.documents.mapNotNull { doc ->
            try {
                CheckoutLog(
                    id = doc.id,
                    itemId = doc.getString("itemId") ?: return@mapNotNull null,
                    staffId = doc.getString("staffId") ?: return@mapNotNull null,
                    checkoutTimestamp = doc.getLong("checkoutTimestamp") ?: System.currentTimeMillis(),
                    checkinTimestamp = null,
                    checkoutPhotoPath = doc.getString("checkoutPhotoPath"),
                    checkinPhotoPath = null,
                    notes = doc.getString("notes") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
        emit(checkouts)
    }
} 