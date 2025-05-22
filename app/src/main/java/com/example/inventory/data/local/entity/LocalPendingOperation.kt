package com.example.inventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class LocalPendingOperation(
    @PrimaryKey
    val id: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val timestamp: Long,
    val retryCount: Int = 0
) 