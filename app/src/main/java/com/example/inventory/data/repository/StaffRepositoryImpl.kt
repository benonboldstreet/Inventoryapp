package com.example.inventory.data.repository

import android.util.Log
import com.example.inventory.data.firebase.FirebaseStaffRepository
import com.example.inventory.data.local.AppDatabase
import com.example.inventory.data.local.entity.LocalStaff
import com.example.inventory.data.model.Staff
import com.example.inventory.data.sync.SyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.example.inventory.data.local.dao.StaffDao
import kotlinx.coroutines.Dispatchers

@Singleton
class StaffRepositoryImpl @Inject constructor(
    private val firebaseRepository: FirebaseStaffRepository,
    private val database: AppDatabase,
    private val staffDao: StaffDao,
    private val syncManager: SyncManager
) : StaffRepository {

    companion object {
        private const val TAG = "StaffRepository"
    }

    init {
        // Start observing Firebase changes and sync to local database
        observeFirebaseChanges()
    }

    private fun observeFirebaseChanges() {
        firebaseRepository.getAllStaff()
            .onEach { remoteStaff ->
                try {
                    // Convert remote staff to local staff
                    val localStaff = remoteStaff.map { LocalStaff.fromStaff(it) }
                    // Update local database
                    staffDao.insertStaff(localStaff)
                    // Update sync timestamps
                    val timestamp = System.currentTimeMillis()
                    staffDao.updateSyncTimestamp(localStaff.map { it.id }, timestamp)
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing staff from Firebase: ${e.message}", e)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error in Firebase stream: ${e.message}", e)
            }
            .launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.IO))
    }

    override fun getAllStaff(): Flow<List<Staff>> {
        return staffDao.getAllStaff().map { staff ->
            staff.map { it.toStaff() }
        }
    }

    override fun getStaffByRole(role: String): Flow<List<Staff>> {
        return staffDao.getStaffByRole(role).map { staff ->
            staff.map { it.toStaff() }
        }
    }

    override suspend fun getStaffById(id: UUID): Staff? {
        return withContext(Dispatchers.IO) {
            try {
                staffDao.getStaffById(id.toString())?.toStaff()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting staff by ID: ${e.message}", e)
                null
            }
        }
    }

    override suspend fun insertStaff(staff: Staff) {
        withContext(Dispatchers.IO) {
            try {
                // Save to local database
                staffDao.insertStaff(LocalStaff.fromStaff(staff))

                // Sync with Firebase
                syncManager.syncStaff(staff, "INSERT")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting staff: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun updateStaff(staff: Staff) {
        withContext(Dispatchers.IO) {
            try {
                // Update local database
                staffDao.updateStaff(LocalStaff.fromStaff(staff))

                // Sync with Firebase
                syncManager.syncStaff(staff, "UPDATE")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating staff: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun deleteStaff(staff: Staff) {
        withContext(Dispatchers.IO) {
            try {
                // Delete from local database
                staffDao.deleteStaff(LocalStaff.fromStaff(staff))

                // Sync with Firebase
                syncManager.syncStaff(staff, "DELETE")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting staff: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun refreshFromFirebase() {
        withContext(Dispatchers.IO) {
            try {
                // Get all remote staff
                val remoteStaff = firebaseRepository.getAllStaff().first()
                
                // Convert to local staff
                val localStaff = remoteStaff.map { LocalStaff.fromStaff(it) }
                
                // Update local database
                staffDao.insertStaff(localStaff)
                
                // Update sync timestamps
                val timestamp = System.currentTimeMillis()
                staffDao.updateSyncTimestamp(localStaff.map { it.id }, timestamp)
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing from Firebase: ${e.message}", e)
                throw e
            }
        }
    }
} 