package com.example.inventory.data.firebase

import android.util.Log
import com.example.inventory.data.model.Staff
import com.example.inventory.data.repository.StaffRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStaffRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : StaffRepository {
    
    private val TAG = "FirebaseStaffRepo"
    private val collection = firestore.collection("staff")
    
    override fun getAllStaff(): Flow<List<Staff>> = flow {
        try {
            val snapshot = collection.get().await()
            val staffList = snapshot.documents.mapNotNull { doc ->
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
                    Log.e(TAG, "Error mapping document to Staff: ${e.message}", e)
                    null
                }
            }
            emit(staffList)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all staff: ${e.message}", e)
            emit(emptyList())
        }
    }
    
    override fun getActiveStaff(): Flow<List<Staff>> = flow {
        try {
            val snapshot = collection
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val staffList = snapshot.documents.mapNotNull { doc ->
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
                    Log.e(TAG, "Error mapping document to Staff: ${e.message}", e)
                    null
                }
            }
            emit(staffList)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active staff: ${e.message}", e)
            emit(emptyList())
        }
    }
    
    override fun getStaffById(id: String): Flow<Staff?> = flow {
        try {
            val docRef = collection.document(id)
            val doc = docRef.get().await()
            
            if (doc.exists()) {
                try {
                    emit(Staff(
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
                    ))
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping document to Staff: ${e.message}", e)
                    emit(null)
                }
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting staff by ID: ${e.message}", e)
            emit(null)
        }
    }
    
    override fun getStaffByRole(role: String): Flow<List<Staff>> = flow {
        try {
            val snapshot = collection
                .whereEqualTo("role", role)
                .get()
                .await()
            
            val staffList = snapshot.documents.mapNotNull { doc ->
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
                    Log.e(TAG, "Error mapping document to Staff: ${e.message}", e)
                    null
                }
            }
            emit(staffList)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting staff by role: ${e.message}", e)
            emit(emptyList())
        }
    }
    
    override suspend fun insertStaff(staff: Staff) {
        try {
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
            collection.add(data).await()
            Log.d(TAG, "Successfully inserted staff")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting staff: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun createStaff(staff: Staff): Staff {
        try {
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
            
            // Let Firebase generate the ID and get the new document
            val docRef = collection.add(data).await()
            return staff.copy(id = docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating staff: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun updateStaff(staff: Staff): Staff {
        try {
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
            
            collection.document(staff.id).set(data).await()
            Log.d(TAG, "Successfully updated staff: ${staff.id}")
            return staff
        } catch (e: Exception) {
            Log.e(TAG, "Error updating staff: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun deleteStaff(staff: Staff) {
        try {
            collection.document(staff.id).delete().await()
            Log.d(TAG, "Successfully deleted staff: ${staff.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting staff: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun archiveStaff(staffId: String): Staff {
        try {
            // Get the staff member
            val staffFlow = getStaffById(staffId)
            var staff: Staff? = null
            
            staffFlow.collect {
                staff = it
            }
            
            if (staff == null) {
                throw IllegalArgumentException("Staff not found with ID: $staffId")
            }
            
            // Update to archived
            val updatedStaff = staff!!.copy(
                isActive = false,
                lastModified = System.currentTimeMillis()
            )
            
            // Save the change
            updateStaff(updatedStaff)
            
            return updatedStaff
        } catch (e: Exception) {
            Log.e(TAG, "Error archiving staff: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun refreshFromFirebase() {
        // Already using Firestore directly, so no need to refresh
        Log.d(TAG, "Using Firestore directly, no refresh needed")
    }
    
    override suspend fun getStaffByFirebaseUid(uid: String): Staff? {
        try {
            val snapshot = collection
                .whereEqualTo("firebaseUid", uid)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                return null
            }
            
            val doc = snapshot.documents.first()
            return try {
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
                Log.e(TAG, "Error mapping document to Staff: ${e.message}", e)
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting staff by Firebase UID: ${e.message}", e)
            return null
        }
    }
} 