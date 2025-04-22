package com.example.inventory.data.database

import androidx.room.TypeConverter
import java.util.UUID

class UuidConverter {
    @TypeConverter
    fun fromUuid(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUuid(uuidString: String): UUID {
        return UUID.fromString(uuidString)
    }
} 