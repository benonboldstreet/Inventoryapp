package com.example.inventory.data.local.dao

import androidx.room.*
import com.example.inventory.data.local.entity.LocalPendingOperation
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    @Query("SELECT * FROM pending_operations ORDER BY timestamp ASC")
    fun getPendingOperations(): Flow<List<LocalPendingOperation>>

    @Query("SELECT * FROM pending_operations WHERE id = :id")
    suspend fun getPendingOperationById(id: String): LocalPendingOperation?

    @Query("SELECT * FROM pending_operations WHERE entityType = :entityType")
    fun getPendingOperationsByEntityType(entityType: String): Flow<List<LocalPendingOperation>>

    @Query("SELECT * FROM pending_operations WHERE operation = :operation")
    fun getPendingOperationsByOperation(operation: String): Flow<List<LocalPendingOperation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingOperation(operation: LocalPendingOperation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingOperations(operations: List<LocalPendingOperation>)

    @Update
    suspend fun updatePendingOperation(operation: LocalPendingOperation)

    @Delete
    suspend fun deletePendingOperation(operation: LocalPendingOperation)

    @Query("DELETE FROM pending_operations")
    suspend fun deleteAllPendingOperations()

    @Query("SELECT * FROM pending_operations WHERE retryCount < :maxRetries")
    suspend fun getPendingOperationsForSync(maxRetries: Int): List<LocalPendingOperation>

    @Query("UPDATE pending_operations SET retryCount = retryCount + 1 WHERE id = :operationId")
    suspend fun incrementRetryCount(operationId: String)

    @Query("DELETE FROM pending_operations WHERE retryCount >= :maxRetries")
    suspend fun deleteFailedOperations(maxRetries: Int)
} 