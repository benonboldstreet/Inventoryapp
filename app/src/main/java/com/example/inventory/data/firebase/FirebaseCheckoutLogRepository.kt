package com.example.inventory.data.firebase

import android.util.Log
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.repository.CheckoutLogRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
                        itemId = UUID.fromString(data["itemId"] as String),
                        staffId = UUID.fromString(data["staffId"] as String),
                        checkOutTime = (data["checkOutTime"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        checkInTime = (data["checkInTime"] as? Number)?.toLong(),
                        status = data["status"] as? String ?: "CHECKED_OUT",
                        photoPath = data["photoPath"] as? String,
                        lastModified = (data["lastModified"] as? Number)?.toLong() ?: System.currentTimeMillis()
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
        val listener = collection.whereEqualTo("itemId", itemId.toString())
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
                            itemId = UUID.fromString(data["itemId"] as String),
                            staffId = UUID.fromString(data["staffId"] as String),
                            checkOutTime = (data["checkOutTime"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkInTime = (data["checkInTime"] as? Number)?.toLong(),
                            status = data["status"] as? String ?: "CHECKED_OUT",
                            photoPath = data["photoPath"] as? String,
                            lastModified = (data["lastModified"] as? Number)?.toLong() ?: System.currentTimeMillis()
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
        val listener = collection.whereEqualTo("staffId", staffId.toString())
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
                            itemId = UUID.fromString(data["itemId"] as String),
                            staffId = UUID.fromString(data["staffId"] as String),
                            checkOutTime = (data["checkOutTime"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkInTime = (data["checkInTime"] as? Number)?.toLong(),
                            status = data["status"] as? String ?: "CHECKED_OUT",
                            photoPath = data["photoPath"] as? String,
                            lastModified = (data["lastModified"] as? Number)?.toLong() ?: System.currentTimeMillis()
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
                            itemId = UUID.fromString(data["itemId"] as String),
                            staffId = UUID.fromString(data["staffId"] as String),
                            checkOutTime = (data["checkOutTime"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkInTime = (data["checkInTime"] as? Number)?.toLong(),
                            status = data["status"] as? String ?: "CHECKED_OUT",
                            photoPath = data["photoPath"] as? String,
                            lastModified = (data["lastModified"] as? Number)?.toLong() ?: System.currentTimeMillis()
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
        val listener = collection.whereGreaterThanOrEqualTo("checkOutTime", startTime)
            .whereLessThanOrEqualTo("checkOutTime", endTime)
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
                            itemId = UUID.fromString(data["itemId"] as String),
                            staffId = UUID.fromString(data["staffId"] as String),
                            checkOutTime = (data["checkOutTime"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkInTime = (data["checkInTime"] as? Number)?.toLong(),
                            status = data["status"] as? String ?: "CHECKED_OUT",
                            photoPath = data["photoPath"] as? String,
                            lastModified = (data["lastModified"] as? Number)?.toLong() ?: System.currentTimeMillis()
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
        val listener = collection.whereEqualTo("status", "CHECKED_OUT")
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
                            itemId = UUID.fromString(data["itemId"] as String),
                            staffId = UUID.fromString(data["staffId"] as String),
                            checkOutTime = (data["checkOutTime"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            checkInTime = (data["checkInTime"] as? Number)?.toLong(),
                            status = data["status"] as? String ?: "CHECKED_OUT",
                            photoPath = data["photoPath"] as? String,
                            lastModified = (data["lastModified"] as? Number)?.toLong() ?: System.currentTimeMillis()
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
            itemId = itemId,
            staffId = staffId,
            checkOutTime = System.currentTimeMillis(),
            status = "CHECKED_OUT",
            photoPath = photoPath
        )
        collection.document(checkoutLog.id.toString()).set(checkoutLog)
        return checkoutLog
    }

    override suspend fun checkinItem(checkoutLog: CheckoutLog) {
        val updatedLog = checkoutLog.copy(
            checkInTime = System.currentTimeMillis(),
            status = "CHECKED_IN",
            lastModified = System.currentTimeMillis()
        )
        collection.document(updatedLog.id.toString()).set(updatedLog)
    }

    override suspend fun refreshFromFirebase() {
        // This is handled by the Firebase repository
    }
} 