package com.example.inventory.data.sync

import android.util.Log
import com.example.inventory.data.local.dao.PendingOperationDao
import com.example.inventory.data.local.entity.LocalPendingOperation
import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.repository.CheckoutLogRepository
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val itemRepository: ItemRepository,
    private val staffRepository: StaffRepository,
    private val checkoutLogRepository: CheckoutLogRepository
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val MAX_RETRIES = 3
    }

    suspend fun syncItem(item: Item, operation: String) {
        try {
            val pendingOp = LocalPendingOperation(
                id = UUID.randomUUID().toString(),
                entityType = "ITEM",
                entityId = item.id.toString(),
                operation = operation,
                timestamp = System.currentTimeMillis(),
                retryCount = 0
            )
            pendingOperationDao.insertPendingOperation(pendingOp)
            syncPendingOperations()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating pending operation for item: ${e.message}", e)
            throw e
        }
    }

    suspend fun syncStaff(staff: Staff, operation: String) {
        try {
            val pendingOp = LocalPendingOperation(
                id = UUID.randomUUID().toString(),
                entityType = "STAFF",
                entityId = staff.id.toString(),
                operation = operation,
                timestamp = System.currentTimeMillis(),
                retryCount = 0
            )
            pendingOperationDao.insertPendingOperation(pendingOp)
            syncPendingOperations()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating pending operation for staff: ${e.message}", e)
            throw e
        }
    }

    suspend fun syncCheckoutLog(checkoutLog: CheckoutLog, operation: String) {
        try {
            val pendingOp = LocalPendingOperation(
                id = UUID.randomUUID().toString(),
                entityType = "CHECKOUT_LOG",
                entityId = checkoutLog.id.toString(),
                operation = operation,
                timestamp = System.currentTimeMillis(),
                retryCount = 0
            )
            pendingOperationDao.insertPendingOperation(pendingOp)
            syncPendingOperations()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating pending operation for checkout log: ${e.message}", e)
            throw e
        }
    }

    private suspend fun syncPendingOperations() {
        try {
            val pendingOps = pendingOperationDao.getPendingOperations().first()
            
            for (op in pendingOps) {
                if (op.retryCount >= MAX_RETRIES) {
                    Log.w(TAG, "Operation ${op.id} exceeded max retries, skipping")
                    continue
                }

                try {
                    when (op.entityType) {
                        "ITEM" -> syncItemOperation(op)
                        "STAFF" -> syncStaffOperation(op)
                        "CHECKOUT_LOG" -> syncCheckoutLogOperation(op)
                        else -> {
                            Log.w(TAG, "Unknown entity type: ${op.entityType}")
                            continue
                        }
                    }
                    pendingOperationDao.deletePendingOperation(op)
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing operation ${op.id}: ${e.message}", e)
                    pendingOperationDao.incrementRetryCount(op.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing pending operations: ${e.message}", e)
            throw e
        }
    }

    private suspend fun syncItemOperation(op: LocalPendingOperation) {
        val item = itemRepository.getItemById(UUID.fromString(op.entityId)).first()
            ?: throw IllegalArgumentException("Item not found: ${op.entityId}")
        
        when (op.operation) {
            "INSERT" -> itemRepository.insertItem(item)
            "UPDATE" -> itemRepository.updateItem(item)
            "DELETE" -> itemRepository.deleteItem(item)
            else -> throw IllegalArgumentException("Unknown operation: ${op.operation}")
        }
    }

    private suspend fun syncStaffOperation(op: LocalPendingOperation) {
        val staff = staffRepository.getStaffById(UUID.fromString(op.entityId)).first()
            ?: throw IllegalArgumentException("Staff not found: ${op.entityId}")
        
        when (op.operation) {
            "INSERT" -> staffRepository.insertStaff(staff)
            "UPDATE" -> staffRepository.updateStaff(staff)
            "DELETE" -> staffRepository.deleteStaff(staff)
            else -> throw IllegalArgumentException("Unknown operation: ${op.operation}")
        }
    }

    private suspend fun syncCheckoutLogOperation(op: LocalPendingOperation) {
        val checkoutLog = checkoutLogRepository.getAllCheckoutLogs().first()
            .find { it.id.toString() == op.entityId }
            ?: throw IllegalArgumentException("Checkout log not found: ${op.entityId}")
        
        when (op.operation) {
            "INSERT" -> checkoutLogRepository.checkoutItem(checkoutLog.itemId, checkoutLog.staffId)
            "UPDATE" -> checkoutLogRepository.checkinItem(checkoutLog)
            else -> throw IllegalArgumentException("Unknown operation: ${op.operation}")
        }
    }
} 