package com.example.inventory.data.firebase

import com.example.inventory.data.model.Staff
import com.example.inventory.data.repository.StaffRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class FirebaseStaffRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : StaffRepository {
    private val staffCollection = firestore.collection("staff")
    private val TAG = "FirebaseStaffRepository"
    
    // MutableStateFlow to hold the latest staff list
    private val _staffFlow = MutableStateFlow<List<Staff>>(emptyList())
    
    init {
        // Set up a real-time listener for the staff collection
        setupStaffListener()
    }
    
    private fun setupStaffListener() {
        staffCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening for staff updates: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(TAG, "Staff collection updated, processing ${snapshot.documents.size} documents")
                    
                    val staffList = snapshot.documents.mapNotNull { doc ->
                        try {
                            Staff(
                                id = UUID.fromString(doc.id),
                                name = doc.getString("name") ?: "",
                                department = doc.getString("department") ?: "",
                                email = doc.getString("email") ?: "",
                                phone = doc.getString("phone") ?: "",
                                position = doc.getString("position") ?: "",
                                isActive = doc.getBoolean("isActive") ?: true,
                                lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting document to Staff: ${e.message}", e)
                            null
                        }
                    }
                    
                    // Update the flow with the new list
                    Log.d(TAG, "Emitting ${staffList.size} staff members from snapshot listener")
                    _staffFlow.value = staffList
                }
            }
    }

    override fun getAllStaff(): Flow<List<Staff>> = _staffFlow

    override fun getActiveStaff(): Flow<List<Staff>> = flow {
        try {
            val snapshot = staffCollection.whereEqualTo("isActive", true).get().await()
            val staff = snapshot.documents.mapNotNull { doc ->
                try {
                    Staff(
                        id = UUID.fromString(doc.id),
                        name = doc.getString("name") ?: "",
                        department = doc.getString("department") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        position = doc.getString("position") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Staff: ${e.message}", e)
                    null
                }
            }
            emit(staff)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active staff: ${e.message}", e)
            emit(emptyList())
        }
    }

    override fun getStaffByDepartment(department: String): Flow<List<Staff>> = flow {
        try {
            val snapshot = staffCollection.whereEqualTo("department", department).get().await()
            val staff = snapshot.documents.mapNotNull { doc ->
                try {
                    Staff(
                        id = UUID.fromString(doc.id),
                        name = doc.getString("name") ?: "",
                        department = doc.getString("department") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        position = doc.getString("position") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Staff: ${e.message}", e)
                    null
                }
            }
            emit(staff)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting staff by department: ${e.message}", e)
            emit(emptyList())
        }
    }

    override suspend fun getStaffById(id: UUID): Staff? {
        return try {
            Log.d(TAG, "Getting staff by ID: $id")
            val doc = staffCollection.document(id.toString()).get().await()
            if (!doc.exists()) {
                Log.w(TAG, "Staff with ID $id not found")
                return null
            }
            
            Staff(
                id = UUID.fromString(doc.id),
                name = doc.getString("name") ?: "",
                department = doc.getString("department") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                position = doc.getString("position") ?: "",
                isActive = doc.getBoolean("isActive") ?: true,
                lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting staff by ID $id: ${e.message}", e)
            null
        }
    }

    override suspend fun getStaffByEmail(email: String): Staff? {
        return try {
            Log.d(TAG, "Getting staff by email: $email")
            val query = staffCollection.whereEqualTo("email", email).get().await()
            if (query.isEmpty) {
                Log.w(TAG, "No staff found with email: $email")
                return null
            }
            
            val doc = query.documents.first()
            Staff(
                id = UUID.fromString(doc.id),
                name = doc.getString("name") ?: "",
                department = doc.getString("department") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                position = doc.getString("position") ?: "",
                isActive = doc.getBoolean("isActive") ?: true,
                lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting staff by email $email: ${e.message}", e)
            null
        }
    }

    override suspend fun insertStaff(staff: Staff) {
        try {
            Log.d(TAG, "Inserting new staff: ${staff.name}")
            staffCollection.document(staff.id.toString()).set(staff).await()
            Log.d(TAG, "Successfully inserted staff with ID: ${staff.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting staff: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateStaff(staff: Staff) {
        try {
            Log.d(TAG, "Updating staff with ID: ${staff.id}")
            staffCollection.document(staff.id.toString()).set(staff).await()
            Log.d(TAG, "Successfully updated staff with ID: ${staff.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating staff: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteStaff(staff: Staff) {
        try {
            Log.d(TAG, "Deleting staff with ID: ${staff.id}")
            staffCollection.document(staff.id.toString()).delete().await()
            Log.d(TAG, "Successfully deleted staff with ID: ${staff.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting staff: ${e.message}", e)
            throw e
        }
    }

    override suspend fun refreshFromFirebase() {
        val snapshot = staffCollection.get().await()
        val staff = snapshot.documents.mapNotNull { doc ->
            try {
                Staff(
                    id = UUID.fromString(doc.id),
                    name = doc.getString("name") ?: "",
                    department = doc.getString("department") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    position = doc.getString("position") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    lastModified = doc.getLong("lastModified") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error converting document to Staff: ${e.message}", e)
                null
            }
        }
        _staffFlow.value = staff
    }
} 