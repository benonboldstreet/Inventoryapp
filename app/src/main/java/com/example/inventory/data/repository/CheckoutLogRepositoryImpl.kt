package com.example.inventory.data.repository

import android.util.Log
import com.example.inventory.data.firebase.FirebaseCheckoutLogRepository
import com.example.inventory.data.local.AppDatabase
import com.example.inventory.data.local.entity.LocalCheckoutLog
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.sync.SyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.example.inventory.data.local.dao.CheckoutLogDao
import kotlinx.coroutines.Dispatchers

@Singleton
class CheckoutLogRepositoryImpl @Inject constructor(
    private val firebaseRepository: FirebaseCheckoutLogRepository,
    private val database: AppDatabase,
    private val checkoutLogDao: CheckoutLogDao,
    private val syncManager: SyncManager
) : CheckoutLogRepository {

    companion object {
        private const val TAG = "CheckoutLogRepository"
    }

    init {
        // Start observing Firebase changes and sync to local database
        observeFirebaseChanges()
    }

    private fun observeFirebaseChanges() {
        firebaseRepository.getAllCheckoutLogs()
            .onEach { remoteLogs ->
                try {
                    // Convert remote logs to local logs
                    val localLogs = remoteLogs.map { LocalCheckoutLog.fromCheckoutLog(it) }
                    // Update local database
                    checkoutLogDao.insertCheckoutLogs(localLogs)
                    // Update sync timestamps
                    val timestamp = System.currentTimeMillis()
                    checkoutLogDao.updateSyncTimestamp(localLogs.map { it.id }, timestamp)
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing checkout logs from Firebase: ${e.message}", e)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error in Firebase stream: ${e.message}", e)
            }
            .launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.IO))
    }

    override fun getAllCheckoutLogs(): Flow<List<CheckoutLog>> {
        return checkoutLogDao.getAllCheckoutLogs().map { logs ->
            logs.map { it.toCheckoutLog() }
        }
    }

    override fun getCheckoutLogsByItem(itemId: UUID): Flow<List<CheckoutLog>> {
        return checkoutLogDao.getCheckoutLogsByItem(itemId.toString()).map { logs ->
            logs.map { it.toCheckoutLog() }
        }
    }

    override fun getCheckoutLogsByStaff(staffId: UUID): Flow<List<CheckoutLog>> {
        return checkoutLogDao.getCheckoutLogsByStaff(staffId.toString()).map { logs ->
            logs.map { it.toCheckoutLog() }
        }
    }

    override fun getCheckoutLogsByStatus(status: String): Flow<List<CheckoutLog>> {
        return checkoutLogDao.getCheckoutLogsByStatus(status).map { logs ->
            logs.map { it.toCheckoutLog() }
        }
    }

    override fun getCheckoutLogsByDateRange(startTime: Long, endTime: Long): Flow<List<CheckoutLog>> {
        return checkoutLogDao.getCheckoutLogsByDateRange(startTime, endTime).map { logs ->
            logs.map { it.toCheckoutLog() }
        }
    }

    override fun getActiveCheckouts(): Flow<List<CheckoutLog>> {
        return checkoutLogDao.getActiveCheckouts().map { logs ->
            logs.map { it.toCheckoutLog() }
        }
    }

    override suspend fun checkoutItem(itemId: UUID, staffId: UUID, photoPath: String?): CheckoutLog {
        return withContext(Dispatchers.IO) {
            try {
                // Create checkout log
                val checkoutLog = CheckoutLog(
                    itemId = itemId,
                    staffId = staffId,
                    checkOutTime = System.currentTimeMillis(),
                    status = "CHECKED_OUT",
                    photoPath = photoPath
                )

                // Save to local database
                checkoutLogDao.insertCheckoutLog(LocalCheckoutLog.fromCheckoutLog(checkoutLog))

                // Sync with Firebase
                syncManager.syncCheckoutLog(checkoutLog, "INSERT")

                checkoutLog
            } catch (e: Exception) {
                Log.e(TAG, "Error checking out item: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun checkinItem(checkoutLog: CheckoutLog) {
        withContext(Dispatchers.IO) {
            try {
                // Update checkout log
                val updatedLog = checkoutLog.copy(
                    checkInTime = System.currentTimeMillis(),
                    status = "CHECKED_IN",
                    lastModified = System.currentTimeMillis()
                )

                // Save to local database
                checkoutLogDao.updateCheckoutLog(LocalCheckoutLog.fromCheckoutLog(updatedLog))

                // Sync with Firebase
                syncManager.syncCheckoutLog(updatedLog, "UPDATE")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking in item: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun refreshFromFirebase() {
        withContext(Dispatchers.IO) {
            try {
                // Get all remote logs
                val remoteLogs = firebaseRepository.getAllCheckoutLogs().first()
                
                // Convert to local logs
                val localLogs = remoteLogs.map { LocalCheckoutLog.fromCheckoutLog(it) }
                
                // Update local database
                checkoutLogDao.insertCheckoutLogs(localLogs)
                
                // Update sync timestamps
                val timestamp = System.currentTimeMillis()
                checkoutLogDao.updateSyncTimestamp(localLogs.map { it.id }, timestamp)
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing from Firebase: ${e.message}", e)
                throw e
            }
        }
    }
} 