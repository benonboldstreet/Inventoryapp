package com.example.inventory.data.converters

import com.example.inventory.data.database.Item as DbItem
import com.example.inventory.data.database.Staff as DbStaff
import com.example.inventory.data.database.CheckoutLog as DbCheckoutLog
import com.example.inventory.data.model.Item as ModelItem
import com.example.inventory.data.model.Staff as ModelStaff
import com.example.inventory.data.model.CheckoutLog as ModelCheckoutLog

/**
 * Converter functions between database entities and model classes
 * 
 * These functions help bridge the gap between local Room database entities
 * and model classes used for cloud operations.
 */

// Item conversions
fun DbItem.toModel(): ModelItem {
    return ModelItem(
        id = this.id,
        name = this.name,
        category = this.category,
        type = this.type,
        barcode = this.barcode,
        condition = this.condition,
        status = this.status,
        photoPath = this.photoPath,
        isActive = this.isActive,
        lastModified = this.lastModified
    )
}

fun ModelItem.toEntity(): DbItem {
    return DbItem(
        id = this.id,
        name = this.name,
        category = this.category,
        type = this.type,
        barcode = this.barcode,
        condition = this.condition,
        status = this.status,
        photoPath = this.photoPath,
        isActive = this.isActive,
        lastModified = this.lastModified
    )
}

// Staff conversions
fun DbStaff.toModel(): ModelStaff {
    return ModelStaff(
        id = this.id,
        name = this.name,
        department = this.department,
        email = this.email,
        // These fields are in model but not in db entity
        phone = "",
        position = "",
        isActive = this.isActive,
        lastModified = this.lastModified
    )
}

fun ModelStaff.toEntity(): DbStaff {
    return DbStaff(
        id = this.id,
        name = this.name,
        department = this.department,
        email = this.email,
        isActive = this.isActive,
        lastModified = this.lastModified
    )
}

// CheckoutLog conversions
fun DbCheckoutLog.toModel(): ModelCheckoutLog {
    return ModelCheckoutLog(
        id = this.id,
        itemId = this.itemId,
        staffId = this.staffId,
        checkOutTime = this.checkOutTime,
        checkInTime = this.checkInTime,
        photoPath = this.photoPath,
        lastModified = this.lastModified
    )
}

fun ModelCheckoutLog.toEntity(): DbCheckoutLog {
    return DbCheckoutLog(
        id = this.id,
        itemId = this.itemId,
        staffId = this.staffId,
        checkOutTime = this.checkOutTime,
        checkInTime = this.checkInTime,
        photoPath = this.photoPath,
        lastModified = this.lastModified
    )
}

// List Conversions
fun List<DbItem>.toModelList(): List<ModelItem> = this.map { it.toModel() }
fun List<ModelItem>.toEntityList(): List<DbItem> = this.map { it.toEntity() }

fun List<DbStaff>.toModelList(): List<ModelStaff> = this.map { it.toModel() }
fun List<ModelStaff>.toEntityList(): List<DbStaff> = this.map { it.toEntity() }

fun List<DbCheckoutLog>.toModelList(): List<ModelCheckoutLog> = this.map { it.toModel() }
fun List<ModelCheckoutLog>.toEntityList(): List<DbCheckoutLog> = this.map { it.toEntity() } 