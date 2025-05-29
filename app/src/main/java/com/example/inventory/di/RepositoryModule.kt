package com.example.inventory.di

import com.example.inventory.data.firebase.FirebaseCheckoutRepository
import com.example.inventory.data.firebase.FirebaseItemRepository
import com.example.inventory.data.firebase.FirebaseStaffRepository
import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing repository implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Provide ItemRepository implementation
     */
    @Provides
    @Singleton
    fun provideItemRepository(
        firestore: FirebaseFirestore
    ): ItemRepository {
        return FirebaseItemRepository(firestore)
    }
    
    /**
     * Provide StaffRepository implementation
     */
    @Provides
    @Singleton
    fun provideStaffRepository(
        firestore: FirebaseFirestore
    ): StaffRepository {
        return FirebaseStaffRepository(firestore)
    }
    
    /**
     * Provide CheckoutRepository implementation
     */
    @Provides
    @Singleton
    fun provideCheckoutRepository(
        firestore: FirebaseFirestore,
        itemRepository: ItemRepository
    ): CheckoutRepository {
        return FirebaseCheckoutRepository(firestore, itemRepository)
    }
} 