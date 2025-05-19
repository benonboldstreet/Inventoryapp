package com.example.inventory.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CheckoutLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkoutLog: CheckoutLog)

    @Update
    suspend fun update(checkoutLog: CheckoutLog)

    @Delete
    suspend fun delete(checkoutLog: CheckoutLog)

    @Query("SELECT * FROM checkout_logs WHERE id = :id")
    suspend fun getCheckoutLogById(id: UUID): CheckoutLog?

    @Query("SELECT * FROM checkout_logs ORDER BY checkOutTime DESC")
    fun getAllCheckoutLogs(): Flow<List<CheckoutLog>>

    @Query("SELECT * FROM checkout_logs WHERE itemId = :itemId ORDER BY checkOutTime DESC")
    fun getCheckoutLogsByItemId(itemId: UUID): Flow<List<CheckoutLog>>

    @Query("SELECT * FROM checkout_logs WHERE staffId = :staffId ORDER BY checkOutTime DESC")
    fun getCheckoutLogsByStaffId(staffId: UUID): Flow<List<CheckoutLog>>

    @Query("SELECT * FROM checkout_logs WHERE checkInTime IS NULL ORDER BY checkOutTime DESC")
    fun getActiveCheckouts(): Flow<List<CheckoutLog>>

    @Query("SELECT * FROM checkout_logs WHERE itemId = :itemId AND checkInTime IS NULL")
    suspend fun getCurrentCheckoutForItem(itemId: UUID): CheckoutLog?
} 