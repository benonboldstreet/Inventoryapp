package com.example.inventory.data.model

import java.util.UUID

/**
 * Data model for staff members
 * 
 * Simplified model for cloud operations without Room database annotations
 */
data class Staff(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val department: String,
    val email: String = "", // Optional email address
    val phone: String = "", // Optional phone number
    val position: String = "", // Job title or position
    val isActive: Boolean = true, // Flag for active vs archived staff
    val lastModified: Long? = System.currentTimeMillis()
) 