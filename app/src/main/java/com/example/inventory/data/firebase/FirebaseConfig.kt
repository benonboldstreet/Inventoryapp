package com.example.inventory.data.firebase

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase configuration class
 * Provides access to Firestore and Firebase Storage
 */
@Singleton
class FirebaseConfig @Inject constructor() {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore by lazy {
        // Enable Firestore debugging
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        
        // Get Firestore instance and apply settings
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = settings
        Log.d("FirebaseConfig", "Firestore initialized with persistence enabled")
        
        // Debug Firestore instance
        try {
            Log.d("FirebaseConfig", "Firestore instance: $db")
            Log.d("FirebaseConfig", "Firestore app: ${db.app}")
            Log.d("FirebaseConfig", "Firestore settings: ${db.firestoreSettings}")
        } catch (e: Exception) {
            Log.e("FirebaseConfig", "Error accessing Firestore instance: ${e.message}", e)
        }
        
        db
    }
    val storage: FirebaseStorage = FirebaseStorage.getInstance()

    init {
        Log.d("FirebaseConfig", "FirebaseConfig initialized")
    }
} 