package com.example.inventory.data.firebase

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
    private val itemsCollection = firebaseConfig.firestore.collection("items")
    private val staffCollection = firebaseConfig.firestore.collection("staff")
    private val checkoutsCollection = firebaseConfig.firestore.collection("checkouts")

    // Item Operations
    suspend fun addItem(item: Item) {
        itemsCollection.document(item.id.toString()).set(item).await()
    }

    suspend fun updateItem(item: Item) {
        itemsCollection.document(item.id.toString()).set(item).await()
    }

    suspend fun deleteItem(itemId: String) {
        itemsCollection.document(itemId).delete().await()
    }

    fun getItems(): Flow<List<Item>> = flow {
        val snapshot = itemsCollection.get().await()
        val items = snapshot.documents.mapNotNull { it.toObject(Item::class.java) }
        emit(items)
    }

    // Staff Operations
    suspend fun addStaff(staff: Staff) {
        staffCollection.document(staff.id.toString()).set(staff).await()
    }

    suspend fun updateStaff(staff: Staff) {
        staffCollection.document(staff.id.toString()).set(staff).await()
    }

    suspend fun deleteStaff(staffId: String) {
        staffCollection.document(staffId).delete().await()
    }

    fun getStaff(): Flow<List<Staff>> = flow {
        val snapshot = staffCollection.get().await()
        val staff = snapshot.documents.mapNotNull { it.toObject(Staff::class.java) }
        emit(staff)
    }

    // Checkout Operations
    suspend fun addCheckout(checkout: CheckoutLog) {
        checkoutsCollection.document(checkout.id.toString()).set(checkout).await()
    }

    suspend fun updateCheckout(checkout: CheckoutLog) {
        checkoutsCollection.document(checkout.id.toString()).set(checkout).await()
    }

    suspend fun deleteCheckout(checkoutId: String) {
        checkoutsCollection.document(checkoutId).delete().await()
    }

    fun getCheckouts(): Flow<List<CheckoutLog>> = flow {
        val snapshot = checkoutsCollection.get().await()
        val checkouts = snapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
        emit(checkouts)
    }

    fun getActiveCheckouts(): Flow<List<CheckoutLog>> = flow {
        val snapshot = checkoutsCollection
            .whereEqualTo("status", "Active")
            .get()
            .await()
        val checkouts = snapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
        emit(checkouts)
    }

    // Helper function to convert DocumentSnapshot to model class
    private inline fun <reified T> DocumentSnapshot.toObject(): T? {
        return try {
            toObject(T::class.java)
        } catch (e: Exception) {
            null
        }
    }
} 