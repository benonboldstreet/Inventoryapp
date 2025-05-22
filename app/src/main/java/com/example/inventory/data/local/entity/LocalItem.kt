package com.example.inventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.inventory.data.model.Item
import java.util.UUID

@Entity(tableName = "items")
data class LocalItem(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val quantity: Int,
    val location: String,
    val type: String,
    val barcode: String?,
    val lastModified: Long,
    val lastSyncTimestamp: Long
) {
    companion object {
        fun fromItem(item: Item): LocalItem {
            return LocalItem(
                id = item.id.toString(),
                name = item.name,
                description = item.description,
                quantity = item.quantity,
                location = item.location,
                type = item.type,
                barcode = item.barcode,
                lastModified = item.lastModified,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun toItem(): Item {
        return Item(
            id = UUID.fromString(id),
            name = name,
            description = description,
            quantity = quantity,
            location = location,
            type = type,
            barcode = barcode,
            lastModified = lastModified
        )
    }
} 