package com.example.inventory.data.network.dto

import com.example.inventory.data.model.Staff
import java.util.UUID

data class StaffDto(
    val id: String? = null,
    val name: String,
    val department: String,
    val email: String,
    val phone: String,
    val position: String,
    val isActive: Boolean = true,
    val lastModified: Long? = null
)

fun StaffDto.toModel(): Staff = Staff(
    id = if (id != null) UUID.fromString(id) else UUID.randomUUID(),
    name = name,
    department = department,
    email = email,
    phone = phone,
    position = position,
    isActive = isActive,
    lastModified = lastModified ?: System.currentTimeMillis()
)

fun Staff.toNetworkDto(): StaffDto = StaffDto(
    id = id.toString(),
    name = name,
    department = department,
    email = email,
    phone = phone,
    position = position,
    isActive = isActive,
    lastModified = lastModified
) 