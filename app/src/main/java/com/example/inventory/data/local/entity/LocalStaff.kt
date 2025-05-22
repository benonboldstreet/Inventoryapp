package com.example.inventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.inventory.data.model.Staff
import java.util.UUID

@Entity(tableName = "staff")
data class LocalStaff(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val photoPath: String?,
    val lastModified: Long,
    val lastSyncTimestamp: Long
) {
    companion object {
        fun fromStaff(staff: Staff): LocalStaff {
            return LocalStaff(
                id = staff.id.toString(),
                name = staff.name,
                email = staff.email,
                phone = staff.phone,
                role = staff.role,
                photoPath = staff.photoPath,
                lastModified = staff.lastModified,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun toStaff(): Staff {
        return Staff(
            id = UUID.fromString(id),
            name = name,
            email = email,
            phone = phone,
            role = role,
            photoPath = photoPath,
            lastModified = lastModified
        )
    }
} 