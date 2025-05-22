package com.example.inventory.di

import android.content.Context
import com.example.inventory.data.firebase.FirebaseConfig
import com.example.inventory.util.DatabaseBackup
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {
    
    @Provides
    @Singleton
    fun provideDatabaseBackup(
        @ApplicationContext context: Context,
        firebaseConfig: FirebaseConfig
    ): DatabaseBackup {
        return DatabaseBackup(context, firebaseConfig)
    }
} 