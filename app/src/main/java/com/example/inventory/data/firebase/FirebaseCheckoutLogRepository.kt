package com.example.inventory.data.firebase

import android.util.Log
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.repository.CheckoutLogRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCheckoutLogRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CheckoutLogRepository {
    private val TAG = "FirebaseCheckoutLogRepo"
    private val collection = firestore.collection("checkout_logs")

    override fun getAllCheckoutLogs(): Flow<List<CheckoutLog>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error getting checkout logs: ${error.message}", error)
                trySend(emptyList())
                return@addSnapshotListener
            }

            val logs = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    CheckoutLog(
                        id = UUID.fromString(doc.id),
                        itemId = UUID.fromString(data["itemIdString"] as String),
                        staffId = UUID.fromString(data["staffIdString"] as String),
                        checkoutTimestamp = (data["checkoutTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        checkinTimestamp = (data["checkinTimestamp"] as? Number)?.toLong(),
                        checkoutPhotoPath = data["checkoutPhotoPath"] as? String,
                        checkinPhotoPath = data["checkinPhotoPath"] as? String,
                        notes = data["notes"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to CheckoutLog: ${e.message}", e)
                    null
                }
            } ?: emptyList()
            trySend(logs)
        }
        awaitClose { listener.remove() }
    }

    override fun getCheckoutLogsByItem(itemId: UUID): Flow<List<CheckoutLog>> = callbackFlow {
        val listener = collection.whereEqualTo("itemIdString", itemId.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting checkout logs by item: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        CheckoutLog(
                            id = UUID.fromString(doc.id),
                            itemId = UUID.fromString(data["itemIdString"] as String),
                            staffId = UUID.fromString(data["staffIdString"] as String),
                            checkoutTimestamp = (data["checkoutTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkinTimestamp = (data["checkinTimestamp"] as? Number)?.toLong(),
                            checkoutPhotoPath = data["checkoutPhotoPath"] as? String,
                            checkinPhotoPath = data["checkinPhotoPath"] as? String,
                            notes = data["notes"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to CheckoutLog: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    override fun getCheckoutLogsByStaff(staffId: UUID): Flow<List<CheckoutLog>> = callbackFlow {
        val listener = collection.whereEqualTo("staffIdString", staffId.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting checkout logs by staff: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        CheckoutLog(
                            id = UUID.fromString(doc.id),
                            itemId = UUID.fromString(data["itemIdString"] as String),
                            staffId = UUID.fromString(data["staffIdString"] as String),
                            checkoutTimestamp = (data["checkoutTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkinTimestamp = (data["checkinTimestamp"] as? Number)?.toLong(),
                            checkoutPhotoPath = data["checkoutPhotoPath"] as? String,
                            checkinPhotoPath = data["checkinPhotoPath"] as? String,
                            notes = data["notes"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to CheckoutLog: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    override fun getCheckoutLogsByStatus(status: String): Flow<List<CheckoutLog>> = callbackFlow {
        val listener = collection.whereEqualTo("status", status)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting checkout logs by status: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        CheckoutLog(
                            id = UUID.fromString(doc.id),
                            itemId = UUID.fromString(data["itemIdString"] as String),
                            staffId = UUID.fromString(data["staffIdString"] as String),
                            checkoutTimestamp = (data["checkoutTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkinTimestamp = (data["checkinTimestamp"] as? Number)?.toLong(),
                            checkoutPhotoPath = data["checkoutPhotoPath"] as? String,
                            checkinPhotoPath = data["checkinPhotoPath"] as? String,
                            notes = data["notes"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to CheckoutLog: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    override fun getCheckoutLogsByDateRange(startTime: Long, endTime: Long): Flow<List<CheckoutLog>> = callbackFlow {
        val listener = collection.whereGreaterThanOrEqualTo("checkoutTimestamp", startTime)
            .whereLessThanOrEqualTo("checkoutTimestamp", endTime)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting checkout logs by date range: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        CheckoutLog(
                            id = UUID.fromString(doc.id),
                            itemId = UUID.fromString(data["itemIdString"] as String),
                            staffId = UUID.fromString(data["staffIdString"] as String),
                            checkoutTimestamp = (data["checkoutTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkinTimestamp = (data["checkinTimestamp"] as? Number)?.toLong(),
                            checkoutPhotoPath = data["checkoutPhotoPath"] as? String,
                            checkinPhotoPath = data["checkinPhotoPath"] as? String,
                            notes = data["notes"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to CheckoutLog: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    override fun getActiveCheckouts(): Flow<List<CheckoutLog>> = callbackFlow {
        val listener = collection.whereEqualTo("checkinTimestamp", null)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting active checkouts: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        CheckoutLog(
                            id = UUID.fromString(doc.id),
                            itemId = UUID.fromString(data["itemIdString"] as String),
                            staffId = UUID.fromString(data["staffIdString"] as String),
                            checkoutTimestamp = (data["checkoutTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkinTimestamp = (data["checkinTimestamp"] as? Number)?.toLong(),
                            checkoutPhotoPath = data["checkoutPhotoPath"] as? String,
                            checkinPhotoPath = data["checkinPhotoPath"] as? String,
                            notes = data["notes"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to CheckoutLog: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun checkoutItem(itemId: UUID, staffId: UUID, photoPath: String?): CheckoutLog {
        val checkoutLog = CheckoutLog(
            id = UUID.randomUUID(),
            itemId = itemId,
            staffId = staffId,
            checkoutTimestamp = System.currentTimeMillis(),
            checkoutPhotoPath = photoPath
        )
        val data = mapOf(
            "idString" to checkoutLog.idString,
            "itemIdString" to checkoutLog.itemIdString,
            "staffIdString" to checkoutLog.staffIdString,
            "checkoutTimestamp" to checkoutLog.checkoutTimestamp,
            "checkoutPhotoPath" to checkoutLog.checkoutPhotoPath,
            "notes" to checkoutLog.notes
        )
        collection.document(checkoutLog.idString).set(data).await()
        return checkoutLog
    }

    override suspend fun checkinItem(checkoutLog: CheckoutLog) {
        val updatedLog = checkoutLog.copy(
            checkinTimestamp = System.currentTimeMillis()
        )
        val data = mapOf(
            "idString" to updatedLog.idString,
            "itemIdString" to updatedLog.itemIdString,
            "staffIdString" to updatedLog.staffIdString,
            "checkoutTimestamp" to updatedLog.checkoutTimestamp,
            "checkinTimestamp" to updatedLog.checkinTimestamp,
            "checkoutPhotoPath" to updatedLog.checkoutPhotoPath,
            "checkinPhotoPath" to updatedLog.checkinPhotoPath,
            "notes" to updatedLog.notes
        )
        collection.document(updatedLog.idString).set(data).await()
    }

    override suspend fun refreshFromFirebase() {
        // This is handled by the Firebase repository
    }
} 