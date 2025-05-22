package com.example.inventory.data.local.dao

import androidx.room.*
import com.example.inventory.data.local.entity.LocalStaff
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<LocalStaff>>

    @Query("SELECT * FROM staff WHERE department = :department ORDER BY name ASC")
    fun getStaffByDepartment(department: String): Flow<List<LocalStaff>>

    @Query("SELECT * FROM staff WHERE role = :role ORDER BY name ASC")
    fun getStaffByRole(role: String): Flow<List<LocalStaff>>

    @Query("SELECT * FROM staff WHERE id = :id")
    fun getStaffById(id: String): Flow<LocalStaff?>

    @Query("SELECT * FROM staff WHERE email = :email LIMIT 1")
    suspend fun getStaffByEmail(email: String): LocalStaff?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: LocalStaff)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: List<LocalStaff>)

    @Update
    suspend fun updateStaff(staff: LocalStaff)

    @Delete
    suspend fun deleteStaff(staff: LocalStaff)

    @Query("UPDATE staff SET lastSyncTimestamp = :timestamp WHERE id IN (:staffIds)")
    suspend fun updateSyncTimestamp(staffIds: List<String>, timestamp: Long)
} 