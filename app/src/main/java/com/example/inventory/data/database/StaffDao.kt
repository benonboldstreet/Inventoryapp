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
interface StaffDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(staff: Staff)

    @Update
    suspend fun update(staff: Staff)

    @Delete
    suspend fun delete(staff: Staff)

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun getStaffById(id: UUID): Staff?

    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE department = :department")
    fun getStaffByDepartment(department: String): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff")
    suspend fun getAllStaffSync(): List<Staff>
} 