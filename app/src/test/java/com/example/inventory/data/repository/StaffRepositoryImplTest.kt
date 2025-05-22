package com.example.inventory.data.repository

import com.example.inventory.data.firebase.FirebaseStaffRepository
import com.example.inventory.data.local.AppDatabase
import com.example.inventory.data.local.dao.StaffDao
import com.example.inventory.data.model.Staff
import com.example.inventory.data.sync.SyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StaffRepositoryImplTest {
    private lateinit var repository: StaffRepositoryImpl
    private lateinit var firebaseRepository: FirebaseStaffRepository
    private lateinit var database: AppDatabase
    private lateinit var staffDao: StaffDao
    private lateinit var syncManager: SyncManager

    @Before
    fun setup() {
        firebaseRepository = mock()
        database = mock()
        staffDao = mock()
        syncManager = mock()
        repository = StaffRepositoryImpl(firebaseRepository, database, staffDao, syncManager)
    }

    @Test
    fun `getAllStaff returns mapped staff from DAO`() = runBlocking {
        // Given
        val localStaff = listOf(
            createLocalStaff("1", "Staff 1"),
            createLocalStaff("2", "Staff 2")
        )
        whenever(staffDao.getAllStaff()).thenReturn(flowOf(localStaff))

        // When
        val result = repository.getAllStaff().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Staff 1", result[0].name)
        assertEquals("Staff 2", result[1].name)
    }

    @Test
    fun `getStaffByRole returns filtered staff`() = runBlocking {
        // Given
        val localStaff = listOf(
            createLocalStaff("1", "Staff 1", role = "Role A"),
            createLocalStaff("2", "Staff 2", role = "Role B")
        )
        whenever(staffDao.getStaffByRole("Role A")).thenReturn(flowOf(localStaff.filter { it.role == "Role A" }))

        // When
        val result = repository.getStaffByRole("Role A").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Role A", result[0].role)
    }

    @Test
    fun `getStaffById returns correct staff`() = runBlocking {
        // Given
        val id = UUID.randomUUID()
        val localStaff = createLocalStaff(id.toString(), "Staff 1")
        whenever(staffDao.getStaffById(id.toString())).thenReturn(localStaff)

        // When
        val result = repository.getStaffById(id)

        // Then
        assertEquals("Staff 1", result?.name)
    }

    @Test
    fun `insertStaff saves to local and syncs`() = runBlocking {
        // Given
        val staff = createStaff("Staff 1")
        doNothing().whenever(staffDao).insertStaff(any())

        // When
        repository.insertStaff(staff)

        // Then
        verify(staffDao).insertStaff(any())
        verify(syncManager).syncStaff(staff, "INSERT")
    }

    @Test
    fun `updateStaff updates local and syncs`() = runBlocking {
        // Given
        val staff = createStaff("Staff 1")
        doNothing().whenever(staffDao).updateStaff(any())

        // When
        repository.updateStaff(staff)

        // Then
        verify(staffDao).updateStaff(any())
        verify(syncManager).syncStaff(staff, "UPDATE")
    }

    @Test
    fun `deleteStaff deletes from local and syncs`() = runBlocking {
        // Given
        val staff = createStaff("Staff 1")
        doNothing().whenever(staffDao).deleteStaff(any())

        // When
        repository.deleteStaff(staff)

        // Then
        verify(staffDao).deleteStaff(any())
        verify(syncManager).syncStaff(staff, "DELETE")
    }

    @Test
    fun `refreshFromFirebase updates local database`() = runBlocking {
        // Given
        val remoteStaff = listOf(
            createStaff("Staff 1"),
            createStaff("Staff 2")
        )
        whenever(firebaseRepository.getAllStaff()).thenReturn(flowOf(remoteStaff))
        doNothing().whenever(staffDao).insertStaff(any())
        doNothing().whenever(staffDao).updateSyncTimestamp(any(), any())

        // When
        repository.refreshFromFirebase()

        // Then
        verify(staffDao).insertStaff(any())
        verify(staffDao).updateSyncTimestamp(any(), any())
    }

    private fun createStaff(name: String): Staff {
        return Staff(
            id = UUID.randomUUID(),
            name = name,
            email = "email@example.com",
            phone = "1234567890",
            role = "Role",
            photoPath = null,
            lastModified = System.currentTimeMillis()
        )
    }

    private fun createLocalStaff(id: String, name: String, role: String = "Role"): LocalStaff {
        return LocalStaff(
            id = id,
            name = name,
            email = "email@example.com",
            phone = "1234567890",
            role = role,
            photoPath = null,
            lastModified = System.currentTimeMillis(),
            lastSyncTimestamp = System.currentTimeMillis()
        )
    }
} 