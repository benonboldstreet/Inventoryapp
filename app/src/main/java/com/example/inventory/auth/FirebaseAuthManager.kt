package com.example.inventory.auth

import android.util.Log
import com.example.inventory.data.model.User
import com.example.inventory.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirebaseAuthManager"
        private const val USERS_COLLECTION = "users"
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    init {
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Use coroutine scope to fetch user data
                MainScope().launch {
                    try {
                        fetchUserData(firebaseUser)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching user data: ${e.message}", e)
                    }
                }
            } else {
                _currentUser.value = null
                _isLoggedIn.value = false
            }
        }
    }

    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            _authError.value = null
            
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(IllegalStateException("Login failed: No user returned"))
            
            val user = fetchUserData(firebaseUser)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            _authError.value = "Login failed: ${e.message}"
            Result.failure(e)
        }
    }

    /**
     * Register a new user
     */
    suspend fun register(email: String, password: String, name: String, role: UserRole = UserRole.STAFF): Result<User> {
        return try {
            _authError.value = null
            
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(IllegalStateException("Registration failed: No user created"))
            
            // Create user document in Firestore
            val user = User(
                id = UUID.fromString(firebaseUser.uid),
                name = name,
                email = email,
                role = role
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(mapOf(
                    "id" to user.id.toString(),
                    "name" to user.name,
                    "email" to user.email,
                    "role" to user.role.name
                ))
                .await()
            
            _currentUser.value = user
            _isLoggedIn.value = true
            
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            _authError.value = "Registration failed: ${e.message}"
            Result.failure(e)
        }
    }

    /**
     * Logout the current user
     */
    suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            _currentUser.value = null
            _isLoggedIn.value = false
            _authError.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Logout error: ${e.message}", e)
            _authError.value = "Logout failed: ${e.message}"
            Result.failure(e)
        }
    }

    /**
     * Check if the current user has a specific role
     */
    fun hasRole(role: UserRole): Boolean {
        return _currentUser.value?.role == role
    }

    /**
     * Get the current Firebase user's ID token
     */
    suspend fun getIdToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ID token: ${e.message}", e)
            null
        }
    }

    /**
     * Fetch user data from Firestore
     */
    private suspend fun fetchUserData(firebaseUser: FirebaseUser): User {
        val userDoc = firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .await()

        val user = if (userDoc.exists()) {
            User(
                id = UUID.fromString(userDoc.getString("id") ?: firebaseUser.uid),
                name = userDoc.getString("name") ?: firebaseUser.displayName ?: "",
                email = userDoc.getString("email") ?: firebaseUser.email ?: "",
                role = UserRole.valueOf(userDoc.getString("role") ?: UserRole.STAFF.name)
            )
        } else {
            // Create default user document if it doesn't exist
            User(
                id = UUID.fromString(firebaseUser.uid),
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                role = UserRole.STAFF
            )
        }

        _currentUser.value = user
        _isLoggedIn.value = true
        return user
    }
} 