package com.example.inventory.data.repository

import com.example.inventory.data.firebase.FirebaseItemRepository
import com.example.inventory.data.local.AppDatabase
import com.example.inventory.data.local.dao.ItemDao
import com.example.inventory.data.model.Item
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

class ItemRepositoryImplTest {
    private lateinit var repository: ItemRepositoryImpl
    private lateinit var firebaseRepository: FirebaseItemRepository
    private lateinit var database: AppDatabase
    private lateinit var itemDao: ItemDao
    private lateinit var syncManager: SyncManager

    @Before
    fun setup() {
        firebaseRepository = mock()
        database = mock()
        itemDao = mock()
        syncManager = mock()
        repository = ItemRepositoryImpl(firebaseRepository, database, itemDao, syncManager)
    }

    @Test
    fun `getAllItems returns mapped items from DAO`() = runBlocking {
        // Given
        val localItems = listOf(
            createLocalItem("1", "Item 1"),
            createLocalItem("2", "Item 2")
        )
        whenever(itemDao.getAllItems()).thenReturn(flowOf(localItems))

        // When
        val result = repository.getAllItems().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Item 1", result[0].name)
        assertEquals("Item 2", result[1].name)
    }

    @Test
    fun `getItemsByType returns filtered items`() = runBlocking {
        // Given
        val localItems = listOf(
            createLocalItem("1", "Item 1", type = "Type A"),
            createLocalItem("2", "Item 2", type = "Type B")
        )
        whenever(itemDao.getItemsByType("Type A")).thenReturn(flowOf(localItems.filter { it.type == "Type A" }))

        // When
        val result = repository.getItemsByType("Type A").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Type A", result[0].type)
    }

    @Test
    fun `getItemByBarcode returns correct item`() = runBlocking {
        // Given
        val localItem = createLocalItem("1", "Item 1", barcode = "123456")
        whenever(itemDao.getItemByBarcode("123456")).thenReturn(flowOf(localItem))

        // When
        val result = repository.getItemByBarcode("123456").first()

        // Then
        assertEquals("123456", result?.barcode)
        assertEquals("Item 1", result?.name)
    }

    @Test
    fun `getItemById returns correct item`() = runBlocking {
        // Given
        val id = UUID.randomUUID()
        val localItem = createLocalItem(id.toString(), "Item 1")
        whenever(itemDao.getItemById(id.toString())).thenReturn(localItem)

        // When
        val result = repository.getItemById(id)

        // Then
        assertEquals("Item 1", result?.name)
    }

    @Test
    fun `insertItem saves to local and syncs`() = runBlocking {
        // Given
        val item = createItem("Item 1")
        doNothing().whenever(itemDao).insertItem(any())

        // When
        repository.insertItem(item)

        // Then
        verify(itemDao).insertItem(any())
        verify(syncManager).syncItem(item, "INSERT")
    }

    @Test
    fun `updateItem updates local and syncs`() = runBlocking {
        // Given
        val item = createItem("Item 1")
        doNothing().whenever(itemDao).updateItem(any())

        // When
        repository.updateItem(item)

        // Then
        verify(itemDao).updateItem(any())
        verify(syncManager).syncItem(item, "UPDATE")
    }

    @Test
    fun `deleteItem deletes from local and syncs`() = runBlocking {
        // Given
        val item = createItem("Item 1")
        doNothing().whenever(itemDao).deleteItem(any())

        // When
        repository.deleteItem(item)

        // Then
        verify(itemDao).deleteItem(any())
        verify(syncManager).syncItem(item, "DELETE")
    }

    @Test
    fun `refreshFromFirebase updates local database`() = runBlocking {
        // Given
        val remoteItems = listOf(
            createItem("Item 1"),
            createItem("Item 2")
        )
        whenever(firebaseRepository.getAllItems()).thenReturn(flowOf(remoteItems))
        doNothing().whenever(itemDao).insertItems(any())
        doNothing().whenever(itemDao).updateSyncTimestamp(any(), any())

        // When
        repository.refreshFromFirebase()

        // Then
        verify(itemDao).insertItems(any())
        verify(itemDao).updateSyncTimestamp(any(), any())
    }

    private fun createItem(name: String): Item {
        return Item(
            id = UUID.randomUUID(),
            name = name,
            description = "Description",
            quantity = 1,
            location = "Location",
            type = "Type",
            barcode = "123456",
            lastModified = System.currentTimeMillis()
        )
    }

    private fun createLocalItem(id: String, name: String, type: String = "Type", barcode: String? = null): LocalItem {
        return LocalItem(
            id = id,
            name = name,
            description = "Description",
            quantity = 1,
            location = "Location",
            type = type,
            barcode = barcode,
            lastModified = System.currentTimeMillis(),
            lastSyncTimestamp = System.currentTimeMillis()
        )
    }
} 