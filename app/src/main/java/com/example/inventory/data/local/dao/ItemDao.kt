package com.example.inventory.data.local.dao

import androidx.room.*
import com.example.inventory.data.local.entity.LocalItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): Flow<List<LocalItem>>

    @Query("SELECT * FROM items WHERE status = :status ORDER BY name ASC")
    fun getItemsByStatus(status: String): Flow<List<LocalItem>>

    @Query("SELECT * FROM items WHERE category = :category ORDER BY name ASC")
    fun getItemsByCategory(category: String): Flow<List<LocalItem>>

    @Query("SELECT DISTINCT category FROM items ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: String): Flow<LocalItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LocalItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<LocalItem>)

    @Update
    suspend fun updateItem(item: LocalItem)

    @Delete
    suspend fun deleteItem(item: LocalItem)

    @Query("UPDATE items SET lastSyncTimestamp = :timestamp WHERE id IN (:itemIds)")
    suspend fun updateSyncTimestamp(itemIds: List<String>, timestamp: Long)
} 