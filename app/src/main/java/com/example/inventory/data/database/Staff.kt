package com.example.inventory.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Database entity for staff members
 * 
 * Room database entity with annotations for database operations
 */
@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val department: String,
    val email: String = "",
    val isActive: Boolean = true,
    val lastModified: Long? = System.currentTimeMillis()
) 