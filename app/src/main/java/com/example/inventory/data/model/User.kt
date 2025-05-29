package com.example.inventory.data.model

import java.util.UUID

/**
 * Represents a user in the inventory system
 */
data class User(
    /**
     * Unique identifier for the user
     */
    val id: UUID,
    
    /**
     * User's full name
     */
    val name: String,
    
    /**
     * User's email address
     */
    val email: String,
    
    /**
     * User's role in the system
     */
    val role: UserRole = UserRole.USER
) 