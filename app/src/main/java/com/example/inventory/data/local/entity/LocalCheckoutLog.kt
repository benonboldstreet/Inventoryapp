package com.example.inventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.inventory.data.model.CheckoutLog
import java.util.UUID

@Entity(tableName = "checkout_logs")
data class LocalCheckoutLog(
    @PrimaryKey
    val id: String,
    val itemId: String,
    val staffId: String,
    val checkOutTime: Long,
    val checkInTime: Long?,
    val photoPath: String?,
    val status: String,
    val lastModified: Long,
    val lastSyncTimestamp: Long
) {
    companion object {
        fun fromCheckoutLog(log: CheckoutLog): LocalCheckoutLog {
            return LocalCheckoutLog(
                id = log.id.toString(),
                itemId = log.itemId.toString(),
                staffId = log.staffId.toString(),
                checkOutTime = log.checkOutTime,
                checkInTime = log.checkInTime,
                photoPath = log.photoPath,
                status = log.status,
                lastModified = log.lastModified,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun toCheckoutLog(): CheckoutLog {
        return CheckoutLog(
            id = UUID.fromString(id),
            itemId = UUID.fromString(itemId),
            staffId = UUID.fromString(staffId),
            checkOutTime = checkOutTime,
            checkInTime = checkInTime,
            photoPath = photoPath,
            status = status,
            lastModified = lastModified
        )
    }
} 