package com.example.inventory.data.firebase

import android.util.Log
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.ItemRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of CheckoutRepository
 */
@Singleton
class FirebaseCheckoutRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val itemRepository: ItemRepository
) : CheckoutRepository {
    
    private val TAG = "FirebaseCheckoutRepo"
    private val collection = firestore.collection("checkout_logs")
    
    override fun getAllCheckoutLogs(): Flow<List<CheckoutLog>> = flow {
        try {
            val snapshot = collection.get().await()
            val checkoutLogs = snapshot.documents.mapNotNull { doc ->
                try {
                    CheckoutLog(
                        id = UUID.fromString(doc.id),
                        itemId = UUID.fromString(doc.getString("itemIdString") ?: return@mapNotNull null),
                        staffId = UUID.fromString(doc.getString("staffIdString") ?: return@mapNotNull null),
                        checkoutTimestamp = doc.getLong("checkoutTimestamp") ?: System.currentTimeMillis(),
                        checkinTimestamp = doc.getLong("checkinTimestamp"),
                        checkoutPhotoPath = doc.getString("checkoutPhotoPath"),
                        checkinPhotoPath = doc.getString("checkinPhotoPath"),
                        notes = doc.getString("notes") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping document to CheckoutLog: ${e.message}", e)
                    null
                }
            }
            emit(checkoutLogs)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all checkout logs: ${e.message}", e)
            emit(emptyList())
        }
    }
    
    override fun getCheckoutLogById(id: UUID): Flow<CheckoutLog?> = flow {
        try {
            val doc = collection.document(id.toString()).get().await()
            if (doc.exists()) {
                try {
                    val checkoutLog = CheckoutLog(
                        id = UUID.fromString(doc.id),
                        itemId = UUID.fromString(doc.getString("itemIdString") ?: throw IllegalStateException("Missing itemId")),
                        staffId = UUID.fromString(doc.getString("staffIdString") ?: throw IllegalStateException("Missing staffId")),
                        checkoutTimestamp = doc.getLong("checkoutTimestamp") ?: System.currentTimeMillis(),
                        checkinTimestamp = doc.getLong("checkinTimestamp"),
                        checkoutPhotoPath = doc.getString("checkoutPhotoPath"),
                        checkinPhotoPath = doc.getString("checkinPhotoPath"),
                        notes = doc.getString("notes") ?: ""
                    )
                    emit(checkoutLog)
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping document to CheckoutLog: ${e.message}", e)
                    emit(null)
                }
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting checkout log by ID: ${e.message}", e)
            emit(null)
        }
    }
    
    override fun getCheckoutsByItemId(itemId: UUID): Flow<List<CheckoutLog>> = flow {
        try {
            val snapshot = collection.whereEqualTo("itemIdString", itemId.toString()).get().await()
            val checkoutLogs = snapshot.documents.mapNotNull { doc ->
                try {
                    CheckoutLog(
                        id = UUID.fromString(doc.id),
                        itemId = UUID.fromString(doc.getString("itemIdString") ?: return@mapNotNull null),
                        staffId = UUID.fromString(doc.getString("staffIdString") ?: return@mapNotNull null),
                        checkoutTimestamp = doc.getLong("checkoutTimestamp") ?: System.currentTimeMillis(),
                        checkinTimestamp = doc.getLong("checkinTimestamp"),
                        checkoutPhotoPath = doc.getString("checkoutPhotoPath"),
                        checkinPhotoPath = doc.getString("checkinPhotoPath"),
                        notes = doc.getString("notes") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping document to CheckoutLog: ${e.message}", e)
                    null
                }
            }
            emit(checkoutLogs)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting checkout logs by item ID: ${e.message}", e)
            emit(emptyList())
        }
    }
    
    override fun getCheckoutsByStaffId(staffId: UUID): Flow<List<CheckoutLog>> = flow {
        try {
            val snapshot = collection.whereEqualTo("staffIdString", staffId.toString()).get().await()
            val checkoutLogs = snapshot.documents.mapNotNull { doc ->
                try {
                    CheckoutLog(
                        id = UUID.fromString(doc.id),
                        itemId = UUID.fromString(doc.getString("itemIdString") ?: return@mapNotNull null),
                        staffId = UUID.fromString(doc.getString("staffIdString") ?: return@mapNotNull null),
                        checkoutTimestamp = doc.getLong("checkoutTimestamp") ?: System.currentTimeMillis(),
                        checkinTimestamp = doc.getLong("checkinTimestamp"),
                        checkoutPhotoPath = doc.getString("checkoutPhotoPath"),
                        checkinPhotoPath = doc.getString("checkinPhotoPath"),
                        notes = doc.getString("notes") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping document to CheckoutLog: ${e.message}", e)
                    null
                }
            }
            emit(checkoutLogs)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting checkout logs by staff ID: ${e.message}", e)
            emit(emptyList())
        }
    }
    
    override fun getActiveCheckouts(): Flow<List<CheckoutLog>> = flow {
        try {
            val snapshot = collection.whereEqualTo("checkinTimestamp", null).get().await()
            val checkoutLogs = snapshot.documents.mapNotNull { doc ->
                try {
                    CheckoutLog(
                        id = UUID.fromString(doc.id),
                        itemId = UUID.fromString(doc.getString("itemIdString") ?: return@mapNotNull null),
                        staffId = UUID.fromString(doc.getString("staffIdString") ?: return@mapNotNull null),
                        checkoutTimestamp = doc.getLong("checkoutTimestamp") ?: System.currentTimeMillis(),
                        checkinTimestamp = null,
                        checkoutPhotoPath = doc.getString("checkoutPhotoPath"),
                        checkinPhotoPath = null,
                        notes = doc.getString("notes") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping document to CheckoutLog: ${e.message}", e)
                    null
                }
            }
            emit(checkoutLogs)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active checkouts: ${e.message}", e)
            emit(emptyList())
        }
    }
    
    override suspend fun insertCheckoutLog(checkoutLog: CheckoutLog) {
        try {
            val data = mapOf(
                "itemIdString" to checkoutLog.itemIdString,
                "staffIdString" to checkoutLog.staffIdString,
                "checkoutTimestamp" to checkoutLog.checkoutTimestamp,
                "checkinTimestamp" to checkoutLog.checkinTimestamp,
                "checkoutPhotoPath" to checkoutLog.checkoutPhotoPath,
                "checkinPhotoPath" to checkoutLog.checkinPhotoPath,
                "notes" to checkoutLog.notes
            )
            
            collection.document(checkoutLog.idString).set(data).await()
            Log.d(TAG, "Checkout log inserted: ${checkoutLog.idString}")
            
            // Update the item status in the items collection
            updateItemStatus(checkoutLog.itemId, "Checked Out")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting checkout log: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun updateCheckoutLog(checkoutLog: CheckoutLog) {
        try {
            val data = mapOf(
                "itemIdString" to checkoutLog.itemIdString,
                "staffIdString" to checkoutLog.staffIdString,
                "checkoutTimestamp" to checkoutLog.checkoutTimestamp,
                "checkinTimestamp" to checkoutLog.checkinTimestamp,
                "checkoutPhotoPath" to checkoutLog.checkoutPhotoPath,
                "checkinPhotoPath" to checkoutLog.checkinPhotoPath,
                "notes" to checkoutLog.notes
            )
            
            collection.document(checkoutLog.idString).update(data).await()
            Log.d(TAG, "Checkout log updated: ${checkoutLog.idString}")
            
            // Update the item status if it was checked in
            if (checkoutLog.checkinTimestamp != null) {
                updateItemStatus(checkoutLog.itemId, "Available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating checkout log: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun deleteCheckoutLog(checkoutLog: CheckoutLog) {
        try {
            collection.document(checkoutLog.idString).delete().await()
            Log.d(TAG, "Checkout log deleted: ${checkoutLog.idString}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting checkout log: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun checkOutItem(itemId: UUID, staffId: UUID, notes: String): CheckoutLog {
        try {
            // Create a new checkout log
            val checkoutLog = CheckoutLog(
                id = UUID.randomUUID(),
                itemId = itemId,
                staffId = staffId,
                checkoutTimestamp = System.currentTimeMillis(),
                checkinTimestamp = null,
                checkoutPhotoPath = null,
                checkinPhotoPath = null,
                notes = notes
            )
            
            // Insert the checkout log
            insertCheckoutLog(checkoutLog)
            
            // Update the item status
            updateItemStatus(itemId, "Checked Out")
            
            return checkoutLog
        } catch (e: Exception) {
            Log.e(TAG, "Error checking out item: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun checkInItem(checkoutLogId: UUID, notes: String): CheckoutLog {
        try {
            // Get the current checkout log
            val checkoutLogFlow = getCheckoutLogById(checkoutLogId)
            var checkoutLog: CheckoutLog? = null
            
            // Collect the flow to get the checkout log
            checkoutLogFlow.collect {
                checkoutLog = it
            }
            
            if (checkoutLog == null) {
                throw IllegalArgumentException("Checkout log not found: $checkoutLogId")
            }
            
            // Update the checkout log with check-in information
            val updatedCheckoutLog = checkoutLog!!.copy(
                checkinTimestamp = System.currentTimeMillis(),
                notes = if (notes.isNotBlank()) "${checkoutLog!!.notes} | Check-in: $notes" else checkoutLog!!.notes
            )
            
            // Update the checkout log
            updateCheckoutLog(updatedCheckoutLog)
            
            // Update the item status
            updateItemStatus(checkoutLog!!.itemId, "Available")
            
            return updatedCheckoutLog
        } catch (e: Exception) {
            Log.e(TAG, "Error checking in item: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun refreshFromFirebase() {
        // This method is already using Firebase directly, so no action needed
        Log.d(TAG, "Using Firebase directly, no refresh needed")
    }
    
    /**
     * Helper method to update an item's status
     */
    private suspend fun updateItemStatus(itemId: UUID, status: String) {
        try {
            // Get the item
            var itemObj: com.example.inventory.data.model.Item? = null
            itemRepository.getItemById(itemId).collect {
                itemObj = it
            }
            
            if (itemObj != null) {
                // Update the item status
                val updatedItem = itemObj!!.copy(status = status)
                itemRepository.updateItem(updatedItem)
                Log.d(TAG, "Updated item ${itemId} status to $status")
            } else {
                Log.w(TAG, "Item not found: $itemId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating item status: ${e.message}", e)
        }
    }
} 