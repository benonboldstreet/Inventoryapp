package com.example.inventory.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Item::class, Staff::class, CheckoutLog::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(UuidConverter::class)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun staffDao(): StaffDao
    abstract fun checkoutLogDao(): CheckoutLogDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        fun getDatabase(context: Context): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "inventory_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 