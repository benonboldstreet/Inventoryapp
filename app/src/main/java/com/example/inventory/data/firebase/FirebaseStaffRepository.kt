package com.example.inventory.data.firebase

import com.example.inventory.data.model.Staff
import com.example.inventory.data.repository.StaffRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStaffRepository @Inject constructor(
    firebaseConfig: FirebaseConfig
) : StaffRepository {
    private val staffCollection = firebaseConfig.firestore.collection("staff")

    override fun getAllStaff(): Flow<List<Staff>> = flow {
        val snapshot = staffCollection.get().await()
        val staff = snapshot.documents.mapNotNull { it.toObject(Staff::class.java) }
        emit(staff)
    }

    override fun getStaffByDepartment(department: String): Flow<List<Staff>> = flow {
        val snapshot = staffCollection.whereEqualTo("department", department).get().await()
        val staff = snapshot.documents.mapNotNull { it.toObject(Staff::class.java) }
        emit(staff)
    }

    override suspend fun getStaffById(id: UUID): Staff? {
        val doc = staffCollection.document(id.toString()).get().await()
        return doc.toObject(Staff::class.java)
    }

    override suspend fun insertStaff(staff: Staff) {
        staffCollection.document(staff.id.toString()).set(staff).await()
    }

    override suspend fun updateStaff(staff: Staff) {
        staffCollection.document(staff.id.toString()).set(staff).await()
    }

    override suspend fun deleteStaff(staff: Staff) {
        staffCollection.document(staff.id.toString()).delete().await()
    }
} 