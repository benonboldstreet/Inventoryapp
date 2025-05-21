package com.example.inventory.api

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.inventory.data.model.User
import com.example.inventory.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Authentication Manager
 * 
 * Manages user authentication, session tokens, and login state.
 * Handles authentication with Azure/cloud services.
 */
object AuthManager {
    private const val TAG = "AuthManager"
    
    // Authentication state
    val isLoggedIn = mutableStateOf(false)
    val currentUser = mutableStateOf<User?>(null)
    val authError = mutableStateOf<String?>(null)
    
    // Keys for DataStore
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    
    // DataStore for persistent token storage
    private val Context.authDataStore by preferencesDataStore(name = "auth_preferences")
    
    /**
     * Initialize the auth manager and restore session if available
     */
    suspend fun initialize(context: Context) {
        restoreSession(context)
    }
    
    /**
     * Login with username and password
     * 
     * @return true if login successful, false otherwise
     */
    suspend fun login(context: Context, username: String, password: String): Boolean {
        // Clear any previous errors
        authError.value = null
        
        try {
            // TODO: In production, this would make an API call to your authentication server
            // For now, we're simulating a successful login with mock credentials
            if (username == "admin" && password == "password123") {
                // Simulate successful authentication
                val mockUser = User(
                    id = UUID.randomUUID(),
                    name = "Admin User",
                    email = "admin@example.com",
                    role = UserRole.ADMIN
                )
                
                // Generate a mock token (in production, this would come from your auth server)
                val mockToken = "mock-jwt-token-${System.currentTimeMillis()}"
                
                // Save the session data
                saveSession(context, mockToken, mockUser)
                
                // Update the authentication state
                currentUser.value = mockUser
                isLoggedIn.value = true
                
                Log.d(TAG, "Login successful for user: ${mockUser.name}")
                return true
            } else if (username == "user" && password == "password123") {
                // Simulate successful authentication with regular user
                val mockUser = User(
                    id = UUID.randomUUID(),
                    name = "Regular User",
                    email = "user@example.com",
                    role = UserRole.USER
                )
                
                // Generate a mock token (in production, this would come from your auth server)
                val mockToken = "mock-jwt-token-${System.currentTimeMillis()}"
                
                // Save the session data
                saveSession(context, mockToken, mockUser)
                
                // Update the authentication state
                currentUser.value = mockUser
                isLoggedIn.value = true
                
                Log.d(TAG, "Login successful for user: ${mockUser.name}")
                return true
            } else {
                // Invalid credentials
                authError.value = "Invalid username or password"
                Log.d(TAG, "Login failed: Invalid credentials")
                return false
            }
        } catch (e: Exception) {
            // Handle authentication errors
            authError.value = "Authentication error: ${e.message}"
            Log.e(TAG, "Login error", e)
            return false
        }
    }
    
    /**
     * Logout the current user
     */
    suspend fun logout(context: Context) {
        // Clear authentication state
        currentUser.value = null
        isLoggedIn.value = false
        
        // Clear stored session data
        context.authDataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_ROLE_KEY)
        }
        
        Log.d(TAG, "User logged out")
    }
    
    /**
     * Check if the current user has a specific role
     */
    fun hasRole(role: UserRole): Boolean {
        val user = currentUser.value ?: return false
        return user.role == role
    }
    
    /**
     * Get the current authentication token
     */
    suspend fun getAuthToken(context: Context): String? {
        return context.authDataStore.data.first()[AUTH_TOKEN_KEY]
    }
    
    /**
     * Save the session data to persistent storage
     */
    private suspend fun saveSession(context: Context, token: String, user: User) {
        context.authDataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = user.id.toString()
            preferences[USER_NAME_KEY] = user.name
            preferences[USER_EMAIL_KEY] = user.email
            preferences[USER_ROLE_KEY] = user.role.name
        }
    }
    
    /**
     * Restore the session from persistent storage
     */
    private suspend fun restoreSession(context: Context) {
        // Try to get saved session data
        val preferences = context.authDataStore.data.first()
        
        val userId = preferences[USER_ID_KEY]
        val userName = preferences[USER_NAME_KEY]
        val userEmail = preferences[USER_EMAIL_KEY]
        val userRole = preferences[USER_ROLE_KEY]
        
        // Check if we have a valid saved session
        if (userId != null && userName != null && userEmail != null && userRole != null) {
            // Restore the user
            val user = User(
                id = UUID.fromString(userId),
                name = userName,
                email = userEmail,
                role = UserRole.valueOf(userRole)
            )
            
            // Update authentication state
            currentUser.value = user
            isLoggedIn.value = true
            
            Log.d(TAG, "Session restored for user: ${user.name}")
        } else {
            Log.d(TAG, "No saved session found")
        }
    }
    
    /**
     * Get the auth token as a flow to observe changes
     */
    fun getAuthTokenFlow(context: Context): Flow<String?> {
        return context.authDataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }
    }
}

/**
 * User data class representing an authenticated user
 */
// This class has been moved to com.example.inventory.data.model.User

/**
 * User roles for access control
 */
// This enum has been moved to com.example.inventory.data.model.UserRole 