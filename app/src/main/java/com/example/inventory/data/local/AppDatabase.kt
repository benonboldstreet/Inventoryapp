package com.example.inventory.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.inventory.data.local.converter.DateConverter
import com.example.inventory.data.local.converter.UUIDConverter
import com.example.inventory.data.local.dao.CheckoutLogDao
import com.example.inventory.data.local.dao.ItemDao
import com.example.inventory.data.local.dao.PendingOperationDao
import com.example.inventory.data.local.dao.StaffDao
import com.example.inventory.data.local.entity.LocalCheckoutLog
import com.example.inventory.data.local.entity.LocalItem
import com.example.inventory.data.local.entity.LocalPendingOperation
import com.example.inventory.data.local.entity.LocalStaff

@Database(
    entities = [
        LocalItem::class,
        LocalStaff::class,
        LocalCheckoutLog::class,
        LocalPendingOperation::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, UUIDConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun staffDao(): StaffDao
    abstract fun checkoutLogDao(): CheckoutLogDao
    abstract fun pendingOperationDao(): PendingOperationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
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