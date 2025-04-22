package com.example.inventory.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val department: String,
    val email: String = "", // Optional email address
    val phone: String = "", // Optional phone number
    val position: String = "", // Job title or position
    val isActive: Boolean = true, // Flag for active vs archived staff
    val lastModified: Long? = System.currentTimeMillis()
) 