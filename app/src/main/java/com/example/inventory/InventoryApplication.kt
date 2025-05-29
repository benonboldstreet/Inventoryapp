package com.example.inventory

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp

/**
 * Inventory Application
 * 
 * This application connects to Firebase Firestore for all data operations.
 * All inventory items, staff data, and checkout logs are stored in Firestore.
 * Supports offline operation with data caching.
 */
@HiltAndroidApp
class InventoryApplication : Application() {
    
    companion object {
        private const val TAG = "InventoryApplication"
        
        // Pre-load disabling of Crashlytics
        init {
            // Set system properties to disable Crashlytics completely
            System.setProperty("firebase.crashlytics.collection.enabled", "false")
            System.setProperty("firebase.crashlytics.mapping.upload.enabled", "false")
            System.setProperty("firebase.crashlytics.auto.data.collection.enabled", "false")
            System.setProperty("firebase.performance.collection.enabled", "false")
        }
    }
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Disable Crashlytics collection through the API as early as possible
        try {
            // More direct approach to disable Crashlytics
            val crashlyticsClassLoader = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics")
            crashlyticsClassLoader.getMethod("setCrashlyticsCollectionEnabled", Boolean::class.java)
                .invoke(null, false)
            Log.d(TAG, "Disabled Crashlytics collection through the API")
        } catch (e: Exception) {
            // This is expected if the Crashlytics SDK is not available
            Log.d(TAG, "Crashlytics SDK not found, which is good")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase safely
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                try {
                    // Initialize Firebase without Crashlytics
                    Log.d(TAG, "Initializing Firebase app")
            FirebaseApp.initializeApp(this)
                    Log.d(TAG, "Firebase initialized successfully")
                    
                    // Configure Firestore with error handling
                    try {
                        val settings = FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)  // Enable offline persistence
                            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build()
                        
                        FirebaseFirestore.getInstance().firestoreSettings = settings
                        Log.d(TAG, "Firestore configured successfully")
        } catch (e: Exception) {
                        Log.e(TAG, "Error configuring Firestore: ${e.message}", e)
        }
            } catch (e: Exception) {
                    Log.e(TAG, "Error initializing Firebase: ${e.message}", e)
            }
            } else {
                Log.d(TAG, "Firebase already initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in Firebase initialization: ${e.message}", e)
        }
    }
} 