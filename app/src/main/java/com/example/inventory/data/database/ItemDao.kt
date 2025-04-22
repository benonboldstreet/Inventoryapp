package com.example.inventory.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: UUID): Item?

    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE status = :status")
    fun getItemsByStatus(status: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE type = :type")
    fun getItemsByType(type: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE category = :category")
    fun getItemsByCategory(category: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE barcode = :barcode")
    suspend fun getItemByBarcode(barcode: String): Item?

    @Query("SELECT DISTINCT category FROM items WHERE category NOT IN ('Laptop', 'Mobile Phone', 'Tablet', 'Accessory') ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
} 