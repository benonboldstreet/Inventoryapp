package com.example.inventory.api

import java.util.UUID

/**
 * Represents a user in the system
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
    val role: UserRole
)

/**
 * Available user roles in the system
 */
enum class UserRole {
    /**
     * Administrator with full access
     */
    ADMIN,
    
    /**
     * Regular user with limited access
     */
    USER
} 