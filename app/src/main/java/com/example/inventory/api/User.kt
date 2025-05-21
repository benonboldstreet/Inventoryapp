package com.example.inventory.api

// This file is now deprecated.
// Use com.example.inventory.data.model.User and com.example.inventory.data.model.UserRole instead
// 
// To avoid breaking existing code, this import statement provides backward compatibility
// while the migration is in progress
import com.example.inventory.data.model.User
import com.example.inventory.data.model.UserRole
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