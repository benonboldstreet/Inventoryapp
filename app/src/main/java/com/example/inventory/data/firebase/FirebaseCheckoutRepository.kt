package com.example.inventory.data.firebase

import android.util.Log
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.repository.CheckoutRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCheckoutRepository @Inject constructor(
    firebaseConfig: FirebaseConfig,
    private val storageUtils: FirebaseStorageUtils
) : CheckoutRepository {
    private val checkoutsCollection = firebaseConfig.firestore.collection("checkouts")
    private val TAG = "FirebaseCheckoutRepo"

    override fun getAllCheckoutLogs(): Flow<List<CheckoutLog>> = flow {
        try {
            Log.d(TAG, "Getting all checkout logs from Firestore")
            val snapshot = checkoutsCollection.get().await()
            Log.d(TAG, "Retrieved ${snapshot.documents.size} checkout documents from Firestore")
            
            val checkouts = snapshot.documents.mapNotNull { doc ->
                try {
                    val checkout = doc.toObject(CheckoutLog::class.java)
                    if (checkout != null) {
                        Log.d(TAG, "Successfully converted checkout document: ${doc.id}")
                    } else {
                        Log.w(TAG, "Failed to convert checkout document to CheckoutLog object: ${doc.id}")
                    }
                    checkout
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting checkout document ${doc.id}: ${e.message}", e)
                    null
                }
            }
            
            Log.d(TAG, "Emitting ${checkouts.size} checkout logs")
            emit(checkouts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all checkout logs: ${e.message}", e)
            // Still emit an empty list so the UI can handle it gracefully
            emit(emptyList())
        }
    }

    override fun getCheckoutLogsByItemId(itemId: UUID): Flow<List<CheckoutLog>> = flow {
        try {
            Log.d(TAG, "Getting checkout logs for item ID: $itemId")
            val snapshot = checkoutsCollection.whereEqualTo("itemIdString", itemId.toString()).get().await()
            val checkouts = snapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
            Log.d(TAG, "Found ${checkouts.size} checkout logs for item: $itemId")
            emit(checkouts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting checkout logs for item $itemId: ${e.message}", e)
            emit(emptyList())
        }
    }

    override fun getCheckoutLogsByStaffId(staffId: UUID): Flow<List<CheckoutLog>> = flow {
        try {
            Log.d(TAG, "Getting checkout logs for staff ID: $staffId")
            val snapshot = checkoutsCollection.whereEqualTo("staffIdString", staffId.toString()).get().await()
            val checkouts = snapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
            Log.d(TAG, "Found ${checkouts.size} checkout logs for staff: $staffId")
            emit(checkouts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting checkout logs for staff $staffId: ${e.message}", e)
            emit(emptyList())
        }
    }

    override fun getCurrentCheckouts(): Flow<List<CheckoutLog>> = flow {
        try {
            Log.d(TAG, "Getting current checkouts (with null checkInTime)")
            val snapshot = checkoutsCollection.whereEqualTo("checkInTime", null).get().await()
            val checkouts = snapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
            Log.d(TAG, "Found ${checkouts.size} current checkouts")
            emit(checkouts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current checkouts: ${e.message}", e)
            emit(emptyList())
        }
    }

    override suspend fun getCurrentCheckoutForItem(itemId: UUID): CheckoutLog? {
        try {
            Log.d(TAG, "Getting current checkout for item ID: $itemId")
            val snapshot = checkoutsCollection
                .whereEqualTo("itemIdString", itemId.toString())
                .whereEqualTo("checkInTime", null)
                .get()
                .await()
            
            return snapshot.documents.firstOrNull()?.let { doc ->
                try {
                    val checkout = doc.toObject(CheckoutLog::class.java)
                    if (checkout != null) {
                        Log.d(TAG, "Found current checkout for item $itemId: ${doc.id}")
                    } else {
                        Log.w(TAG, "Failed to convert checkout document to CheckoutLog: ${doc.id}")
                    }
                    checkout
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting checkout document ${doc.id}: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current checkout for item $itemId: ${e.message}", e)
            return null
        }
    }

    override suspend fun getCheckoutLogById(id: UUID): CheckoutLog? {
        try {
            Log.d(TAG, "Getting checkout log by ID: $id")
            val doc = checkoutsCollection.document(id.toString()).get().await()
            
            if (!doc.exists()) {
                Log.w(TAG, "Checkout log with ID $id not found")
                return null
            }
            
            val checkout = doc.toObject(CheckoutLog::class.java)
            if (checkout == null) {
                Log.w(TAG, "Failed to convert checkout document to CheckoutLog: ${doc.id}")
            } else {
                Log.d(TAG, "Successfully retrieved checkout log: $id")
            }
            return checkout
        } catch (e: Exception) {
            Log.e(TAG, "Error getting checkout log by ID $id: ${e.message}", e)
            return null
        }
    }

    override suspend fun insertCheckoutLog(checkoutLog: CheckoutLog) {
        try {
            Log.d(TAG, "Inserting new checkout log with ID: ${checkoutLog.idString}")
            checkoutsCollection.document(checkoutLog.idString).set(checkoutLog).await()
            Log.d(TAG, "Successfully inserted checkout log: ${checkoutLog.idString}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting checkout log: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateCheckoutLog(checkoutLog: CheckoutLog) {
        try {
            Log.d(TAG, "Updating checkout log with ID: ${checkoutLog.idString}")
            checkoutsCollection.document(checkoutLog.idString).set(checkoutLog).await()
            Log.d(TAG, "Successfully updated checkout log: ${checkoutLog.idString}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating checkout log: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteCheckoutLog(checkoutLog: CheckoutLog) {
        try {
            Log.d(TAG, "Deleting checkout log with ID: ${checkoutLog.idString}")
            checkoutsCollection.document(checkoutLog.idString).delete().await()
            Log.d(TAG, "Successfully deleted checkout log: ${checkoutLog.idString}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting checkout log: ${e.message}", e)
            throw e
        }
    }

    override suspend fun checkOutItem(itemId: UUID, staffId: UUID): CheckoutLog {
        try {
            Log.d(TAG, "Creating new checkout for item $itemId and staff $staffId")
            val newId = UUID.randomUUID()
            val checkout = CheckoutLog(
                idString = newId.toString(),
                itemIdString = itemId.toString(),
                staffIdString = staffId.toString(),
                checkOutTime = com.google.firebase.Timestamp.now(),
                checkInTime = null,
                lastModified = com.google.firebase.Timestamp.now()
            )
            checkoutsCollection.document(checkout.idString).set(checkout).await()
            Log.d(TAG, "Successfully checked out item $itemId to staff $staffId with checkout ID ${checkout.idString}")
            return checkout
        } catch (e: Exception) {
            Log.e(TAG, "Error checking out item $itemId to staff $staffId: ${e.message}", e)
            throw e
        }
    }

    override suspend fun checkOutItemWithPhoto(itemId: UUID, staffId: UUID, photoPath: String): CheckoutLog {
        try {
            val newId = UUID.randomUUID()
            Log.d(TAG, "Creating new checkout with photo for item $itemId and staff $staffId")
            
            // First, upload the photo to Firebase Storage
            val photoFile = File(photoPath)
            if (!photoFile.exists()) {
                Log.e(TAG, "Photo file does not exist: $photoPath")
                throw IllegalArgumentException("Photo file does not exist: $photoPath")
            }
            
            // Upload the photo and get the download URL
            val downloadUrl = storageUtils.uploadCheckoutPhoto(photoFile)
            Log.d(TAG, "Photo uploaded to Firebase Storage: $downloadUrl")
            
            // Create checkout log with the download URL
            val checkout = CheckoutLog(
                idString = newId.toString(),
                itemIdString = itemId.toString(),
                staffIdString = staffId.toString(),
                checkOutTime = com.google.firebase.Timestamp.now(),
                checkInTime = null,
                photoPath = downloadUrl, // Use the download URL instead of local path
                lastModified = com.google.firebase.Timestamp.now()
            )
            
            // Save the checkout log in Firestore
            checkoutsCollection.document(checkout.idString).set(checkout).await()
            Log.d(TAG, "Checkout log created with photo: ${checkout.idString}")
            
            return checkout
        } catch (e: Exception) {
            Log.e(TAG, "Error creating checkout with photo for item $itemId and staff $staffId: ${e.message}", e)
            throw e
        }
    }

    override suspend fun checkInItem(checkoutLog: CheckoutLog): CheckoutLog {
        try {
            Log.d(TAG, "Checking in item for checkout log: ${checkoutLog.idString}")
            val updated = checkoutLog.copy(checkInTime = com.google.firebase.Timestamp.now())
            checkoutsCollection.document(updated.idString).set(updated).await()
            Log.d(TAG, "Successfully checked in item for checkout log: ${updated.idString}")
            return updated
        } catch (e: Exception) {
            Log.e(TAG, "Error checking in item for checkout log ${checkoutLog.idString}: ${e.message}", e)
            throw e
        }
    }
} 