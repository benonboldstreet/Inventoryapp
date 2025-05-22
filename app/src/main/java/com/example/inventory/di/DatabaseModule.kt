package com.example.inventory.di

import android.content.Context
import com.example.inventory.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideItemDao(database: AppDatabase) = database.itemDao()

    @Provides
    fun provideStaffDao(database: AppDatabase) = database.staffDao()

    @Provides
    fun provideCheckoutLogDao(database: AppDatabase) = database.checkoutLogDao()

    @Provides
    fun providePendingOperationDao(database: AppDatabase) = database.pendingOperationDao()
} 