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
        val snapshot = checkoutsCollection.get().await()
        val checkouts = snapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
        emit(checkouts)
    }

    override fun getCheckoutLogsByItemId(itemId: UUID): Flow<List<CheckoutLog>> = flow {
        val snapshot = checkoutsCollection.whereEqualTo("itemIdString", itemId.toString()).get().await()
        val checkouts = snapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
        emit(checkouts)
    }

    override fun getCheckoutLogsByStaffId(staffId: UUID): Flow<List<CheckoutLog>> = flow {
        val snapshot = checkoutsCollection.whereEqualTo("staffIdString", staffId.toString()).get().await()
        val checkouts = snapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
        emit(checkouts)
    }

    override fun getCurrentCheckouts(): Flow<List<CheckoutLog>> = flow {
        val snapshot = checkoutsCollection.whereEqualTo("checkInTime", null).get().await()
        val checkouts = snapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
        emit(checkouts)
    }

    override suspend fun getCurrentCheckoutForItem(itemId: UUID): CheckoutLog? {
        val snapshot = checkoutsCollection.whereEqualTo("itemIdString", itemId.toString()).whereEqualTo("checkInTime", null).get().await()
        return snapshot.documents.firstOrNull()?.toObject(CheckoutLog::class.java)
    }

    override suspend fun getCheckoutLogById(id: UUID): CheckoutLog? {
        val doc = checkoutsCollection.document(id.toString()).get().await()
        return doc.toObject(CheckoutLog::class.java)
    }

    override suspend fun insertCheckoutLog(checkoutLog: CheckoutLog) {
        checkoutsCollection.document(checkoutLog.idString).set(checkoutLog).await()
    }

    override suspend fun updateCheckoutLog(checkoutLog: CheckoutLog) {
        checkoutsCollection.document(checkoutLog.idString).set(checkoutLog).await()
    }

    override suspend fun deleteCheckoutLog(checkoutLog: CheckoutLog) {
        checkoutsCollection.document(checkoutLog.idString).delete().await()
    }

    override suspend fun checkOutItem(itemId: UUID, staffId: UUID): CheckoutLog {
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
        return checkout
    }

    override suspend fun checkOutItemWithPhoto(itemId: UUID, staffId: UUID, photoPath: String): CheckoutLog {
        try {
            val newId = UUID.randomUUID()
            
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
            Log.e(TAG, "Error creating checkout with photo", e)
            throw e
        }
    }

    override suspend fun checkInItem(checkoutLog: CheckoutLog): CheckoutLog {
        val updated = checkoutLog.copy(checkInTime = com.google.firebase.Timestamp.now())
        checkoutsCollection.document(updated.idString).set(updated).await()
        return updated
    }
} 