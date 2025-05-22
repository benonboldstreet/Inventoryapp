package com.example.inventory.data.repository

import android.util.Log
import com.example.inventory.data.firebase.FirebaseItemRepository
import com.example.inventory.data.local.AppDatabase
import com.example.inventory.data.local.entity.LocalItem
import com.example.inventory.data.model.Item
import com.example.inventory.data.sync.SyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.example.inventory.data.local.dao.ItemDao
import kotlinx.coroutines.Dispatchers

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val firebaseRepository: FirebaseItemRepository,
    private val database: AppDatabase,
    private val itemDao: ItemDao,
    private val syncManager: SyncManager
) : ItemRepository {

    companion object {
        private const val TAG = "ItemRepository"
    }

    init {
        // Start observing Firebase changes and sync to local database
        observeFirebaseChanges()
    }

    private fun observeFirebaseChanges() {
        firebaseRepository.getAllItems()
            .onEach { remoteItems ->
                try {
                    // Convert remote items to local items
                    val localItems = remoteItems.map { LocalItem.fromItem(it) }
                    // Update local database
                    itemDao.insertItems(localItems)
                    // Update sync timestamps
                    val timestamp = System.currentTimeMillis()
                    itemDao.updateSyncTimestamp(localItems.map { it.id }, timestamp)
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing items from Firebase: ${e.message}", e)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error in Firebase stream: ${e.message}", e)
            }
            .launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.IO))
    }

    override fun getAllItems(): Flow<List<Item>> {
        return itemDao.getAllItems().map { items ->
            items.map { it.toItem() }
        }
    }

    override fun getItemsByType(type: String): Flow<List<Item>> {
        return itemDao.getItemsByType(type).map { items ->
            items.map { it.toItem() }
        }
    }

    override fun getItemByBarcode(barcode: String): Flow<Item?> {
        return itemDao.getItemByBarcode(barcode).map { it?.toItem() }
    }

    override suspend fun getItemById(id: UUID): Item? {
        return withContext(Dispatchers.IO) {
            try {
                itemDao.getItemById(id.toString())?.toItem()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting item by ID: ${e.message}", e)
                null
            }
        }
    }

    override suspend fun insertItem(item: Item) {
        withContext(Dispatchers.IO) {
            try {
                // Save to local database
                itemDao.insertItem(LocalItem.fromItem(item))

                // Sync with Firebase
                syncManager.syncItem(item, "INSERT")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting item: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun updateItem(item: Item) {
        withContext(Dispatchers.IO) {
            try {
                // Update local database
                itemDao.updateItem(LocalItem.fromItem(item))

                // Sync with Firebase
                syncManager.syncItem(item, "UPDATE")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating item: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun deleteItem(item: Item) {
        withContext(Dispatchers.IO) {
            try {
                // Delete from local database
                itemDao.deleteItem(LocalItem.fromItem(item))

                // Sync with Firebase
                syncManager.syncItem(item, "DELETE")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting item: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun refreshFromFirebase() {
        withContext(Dispatchers.IO) {
            try {
                // Get all remote items
                val remoteItems = firebaseRepository.getAllItems().first()
                
                // Convert to local items
                val localItems = remoteItems.map { LocalItem.fromItem(it) }
                
                // Update local database
                itemDao.insertItems(localItems)
                
                // Update sync timestamps
                val timestamp = System.currentTimeMillis()
                itemDao.updateSyncTimestamp(localItems.map { it.id }, timestamp)
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing from Firebase: ${e.message}", e)
                throw e
            }
        }
    }
} 