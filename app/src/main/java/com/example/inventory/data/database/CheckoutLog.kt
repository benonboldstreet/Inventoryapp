package com.example.inventory.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "checkout_logs",
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId"),
        Index("staffId")
    ]
)
data class CheckoutLog(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val itemId: UUID,
    val staffId: UUID,
    val checkOutTime: Long,
    val checkInTime: Long?,
    val photoPath: String? = null,
    val lastModified: Long? = System.currentTimeMillis()
) 