package com.example.inventory.data.local.dao

import androidx.room.*
import com.example.inventory.data.local.entity.LocalCheckoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckoutLogDao {
    @Query("SELECT * FROM checkout_logs ORDER BY checkOutTime DESC")
    fun getAllCheckoutLogs(): Flow<List<LocalCheckoutLog>>

    @Query("SELECT * FROM checkout_logs WHERE itemId = :itemId ORDER BY checkOutTime DESC")
    fun getCheckoutLogsByItem(itemId: String): Flow<List<LocalCheckoutLog>>

    @Query("SELECT * FROM checkout_logs WHERE staffId = :staffId ORDER BY checkOutTime DESC")
    fun getCheckoutLogsByStaff(staffId: String): Flow<List<LocalCheckoutLog>>

    @Query("SELECT * FROM checkout_logs WHERE status = :status ORDER BY checkOutTime DESC")
    fun getCheckoutLogsByStatus(status: String): Flow<List<LocalCheckoutLog>>

    @Query("SELECT * FROM checkout_logs WHERE checkOutTime BETWEEN :startTime AND :endTime ORDER BY checkOutTime DESC")
    fun getCheckoutLogsByDateRange(startTime: Long, endTime: Long): Flow<List<LocalCheckoutLog>>

    @Query("SELECT * FROM checkout_logs WHERE status = 'CHECKED_OUT' ORDER BY checkOutTime DESC")
    fun getActiveCheckouts(): Flow<List<LocalCheckoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckoutLog(checkoutLog: LocalCheckoutLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckoutLogs(checkoutLogs: List<LocalCheckoutLog>)

    @Update
    suspend fun updateCheckoutLog(checkoutLog: LocalCheckoutLog)

    @Delete
    suspend fun deleteCheckoutLog(checkoutLog: LocalCheckoutLog)

    @Query("UPDATE checkout_logs SET lastSyncTimestamp = :timestamp WHERE id IN (:ids)")
    suspend fun updateSyncTimestamp(ids: List<String>, timestamp: Long)

    @Query("DELETE FROM checkout_logs")
    suspend fun deleteAllCheckoutLogs()

    @Query("SELECT * FROM checkout_logs WHERE lastModified > :timestamp")
    suspend fun getCheckoutLogsModifiedSince(timestamp: Long): List<LocalCheckoutLog>

    @Query("SELECT * FROM checkout_logs WHERE lastSyncTimestamp < :timestamp")
    suspend fun getCheckoutLogsNeedingSync(timestamp: Long): List<LocalCheckoutLog>
} 