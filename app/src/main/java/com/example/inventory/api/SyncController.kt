package com.example.inventory.api

import com.example.inventory.data.model.CheckoutLog
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Controller handling sync operations with Azure backend
 */
class SyncController(
    private val itemRepository: ItemRepository,
    private val staffRepository: StaffRepository,
    private val checkoutRepository: CheckoutRepository
) {
    /**
     * Register all routes for the sync controller
     */
    fun registerRoutes(routing: Routing) {
        routing.apply {
            // Items sync endpoints
            get("/sync/items") {
                val items = itemRepository.getAllItems().first()
                val itemDtos = items.map { it.toItemDto() }
                call.respond(itemDtos)
            }
            
            post("/sync/items") {
                val itemDtos = call.receive<List<ItemDto>>()
                
                // Handle each item (create, update based on UUID and lastModified)
                itemDtos.forEach { itemDto ->
                    val existingItem = itemRepository.getItemById(itemDto.id)
                    
                    if (existingItem == null) {
                        // New item, insert it
                        val newItem = itemDto.toItem()
                        itemRepository.insertItem(newItem)
                    } else {
                        // Existing item, check last modified timestamp for conflict resolution
                        val existingLastModified = existingItem.getLastModifiedTime()
                        val incomingLastModified = itemDto.lastModified?.let { 
                            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(it)).toEpochMilli()
                        } ?: 0
                        
                        if (incomingLastModified > existingLastModified) {
                            // Incoming is newer, update
                            val updatedItem = itemDto.toItem()
                            itemRepository.updateItem(updatedItem)
                        }
                        // If local is newer or same, do nothing
                    }
                }
                
                call.respond(HttpStatusCode.OK)
            }
            
            // Staff sync endpoints
            get("/sync/staff") {
                val staffList = staffRepository.getAllStaff().first()
                val staffDtos = staffList.map { it.toStaffDto() }
                call.respond(staffDtos)
            }
            
            post("/sync/staff") {
                val staffDtos = call.receive<List<StaffDto>>()
                
                // Handle each staff (create, update based on UUID and lastModified)
                staffDtos.forEach { staffDto ->
                    val existingStaff = staffRepository.getStaffById(staffDto.id)
                    
                    if (existingStaff == null) {
                        // New staff, insert it
                        val newStaff = staffDto.toStaff()
                        staffRepository.insertStaff(newStaff)
                    } else {
                        // Existing staff, check last modified timestamp for conflict resolution
                        val existingLastModified = existingStaff.getLastModifiedTime()
                        val incomingLastModified = staffDto.lastModified?.let { 
                            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(it)).toEpochMilli()
                        } ?: 0
                        
                        if (incomingLastModified > existingLastModified) {
                            // Incoming is newer, update
                            val updatedStaff = staffDto.toStaff()
                            staffRepository.updateStaff(updatedStaff)
                        }
                        // If local is newer or same, do nothing
                    }
                }
                
                call.respond(HttpStatusCode.OK)
            }
            
            // Checkout logs sync endpoints
            get("/sync/logs") {
                val logs = checkoutRepository.getAllCheckoutLogs().first()
                val logDtos = logs.map { it.toCheckoutLogDto() }
                call.respond(logDtos)
            }
            
            post("/sync/logs") {
                val logDtos = call.receive<List<CheckoutLogDto>>()
                
                // Handle each log (create, update based on UUID and lastModified)
                logDtos.forEach { logDto ->
                    val existingLog = checkoutRepository.getCheckoutLogById(logDto.id)
                    
                    if (existingLog == null) {
                        // New log, insert it
                        val newLog = logDto.toCheckoutLog()
                        checkoutRepository.insertCheckoutLog(newLog)
                    } else {
                        // Existing log, check last modified timestamp for conflict resolution
                        val existingLastModified = existingLog.getLastModifiedTime()
                        val incomingLastModified = logDto.lastModified?.let { 
                            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(it)).toEpochMilli()
                        } ?: 0
                        
                        if (incomingLastModified > existingLastModified) {
                            // Incoming is newer, update
                            val updatedLog = logDto.toCheckoutLog()
                            checkoutRepository.updateCheckoutLog(updatedLog)
                        }
                        // If local is newer or same, do nothing
                    }
                }
                
                call.respond(HttpStatusCode.OK)
            }
        }
    }
    
    // DTO classes for sync
    @Serializable
    data class ItemDto(
        val id: UUID,
        val name: String,
        val category: String,
        val type: String,
        val barcode: String,
        val condition: String,
        val status: String,
        val photoPath: String?,
        val lastModified: String? // ISO-8601 format timestamp
    )
    
    @Serializable
    data class StaffDto(
        val id: UUID,
        val name: String,
        val department: String,
        val email: String = "",
        val phone: String = "",
        val position: String = "",
        val lastModified: String? // ISO-8601 format timestamp
    )
    
    @Serializable
    data class CheckoutLogDto(
        val id: UUID,
        val itemId: UUID,
        val staffId: UUID,
        val checkOutTime: String, // ISO-8601 format timestamp
        val checkInTime: String?, // ISO-8601 format timestamp
        val lastModified: String? // ISO-8601 format timestamp
    )
    
    // Extension functions for conversion between entity and DTO
    private fun Item.toItemDto(): ItemDto = ItemDto(
        id = id,
        name = name,
        category = category,
        type = type,
        barcode = barcode,
        condition = condition,
        status = status,
        photoPath = photoPath,
        lastModified = getLastModifiedTime().let { Instant.ofEpochMilli(it).toString() }
    )
    
    private fun ItemDto.toItem(): Item = Item(
        idString = id.toString(),
        name = name,
        category = category,
        type = type,
        barcode = barcode,
        condition = condition,
        status = status,
        photoPath = photoPath,
        lastModified = lastModified?.let { 
            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(it)).toEpochMilli()
        } ?: System.currentTimeMillis()
    )
    
    private fun Staff.toStaffDto(): StaffDto = StaffDto(
        id = id,
        name = name,
        department = department,
        email = email,
        phone = phone,
        position = position,
        lastModified = getLastModifiedTime().let { Instant.ofEpochMilli(it).toString() }
    )
    
    private fun StaffDto.toStaff(): Staff = Staff(
        idString = id.toString(),
        name = name,
        department = department,
        email = email,
        phone = phone,
        position = position,
        lastModified = lastModified?.let { 
            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(it)).toEpochMilli()
        } ?: System.currentTimeMillis()
    )
    
    private fun CheckoutLog.toCheckoutLogDto(): CheckoutLogDto = CheckoutLogDto(
        id = id,
        itemId = itemId,
        staffId = staffId,
        checkOutTime = Instant.ofEpochMilli(getCheckOutTimeAsLong()).toString(),
        checkInTime = getCheckInTimeAsLong()?.let { Instant.ofEpochMilli(it).toString() },
        lastModified = getLastModifiedTime().let { Instant.ofEpochMilli(it).toString() }
    )
    
    private fun CheckoutLogDto.toCheckoutLog(): CheckoutLog = CheckoutLog(
        idString = id.toString(),
        itemIdString = itemId.toString(),
        staffIdString = staffId.toString(),
        checkOutTime = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(checkOutTime)).toEpochMilli(),
        checkInTime = checkInTime?.let { 
            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(it)).toEpochMilli() 
        },
        lastModified = lastModified?.let { 
            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(it)).toEpochMilli()
        } ?: System.currentTimeMillis()
    )
} 