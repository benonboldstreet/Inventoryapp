package com.example.inventory.data.model

/**
 * Enum representing user roles in the inventory system
 */
enum class UserRole {
    ADMIN,   // Administrator with full access
    MANAGER, // Manager with elevated privileges
    USER,    // Regular user with basic access
    STAFF    // Staff member with inventory access
} 