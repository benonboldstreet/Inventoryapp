package com.example.inventory.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase module for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    private const val TAG = "FirebaseModule"
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    /**
     * Provide FirebaseFirestore instance
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        Log.d(TAG, "Providing FirebaseFirestore instance")
        
        // Configure Firestore settings
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        
        // Get and configure Firestore instance
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = settings
        
        Log.d(TAG, "FirebaseFirestore instance configured: $firestore")
        return firestore
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
} 