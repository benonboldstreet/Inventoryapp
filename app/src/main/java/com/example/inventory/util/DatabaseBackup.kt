package com.example.inventory.util

import android.content.Context
import com.example.inventory.data.database.InventoryDatabase
import com.example.inventory.data.database.Item
import com.example.inventory.data.database.Staff
import com.example.inventory.data.database.CheckoutLog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for backing up database contents to JSON files
 */
class DatabaseBackup(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    
    /**
     * Creates a backup of all database tables to JSON files
     * @return List of created backup file paths
     */
    suspend fun createBackup(): List<String> = withContext(Dispatchers.IO) {
        val database = InventoryDatabase.getDatabase(context)
        val backupDir = createBackupDirectory()
        val timestamp = dateFormat.format(Date())
        
        val backupFiles = mutableListOf<String>()
        
        // Backup Items
        val items = database.itemDao().getAllItemsSync()
        val itemsFile = File(backupDir, "items_$timestamp.json")
        itemsFile.writeText(gson.toJson(items))
        backupFiles.add(itemsFile.absolutePath)
        
        // Backup Staff
        val staff = database.staffDao().getAllStaffSync()
        val staffFile = File(backupDir, "staff_$timestamp.json")
        staffFile.writeText(gson.toJson(staff))
        backupFiles.add(staffFile.absolutePath)
        
        // Backup Checkout Logs
        val checkoutLogs = database.checkoutLogDao().getAllCheckoutLogsSync()
        val checkoutFile = File(backupDir, "checkout_logs_$timestamp.json")
        checkoutFile.writeText(gson.toJson(checkoutLogs))
        backupFiles.add(checkoutFile.absolutePath)
        
        backupFiles
    }
    
    /**
     * Creates a backup directory if it doesn't exist
     * @return File object representing the backup directory
     */
    private fun createBackupDirectory(): File {
        val backupDir = File(context.filesDir, "database_backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }
    
    /**
     * Gets a list of all available backups
     * @return List of backup file paths
     */
    fun getAvailableBackups(): List<String> {
        val backupDir = File(context.filesDir, "database_backups")
        return if (backupDir.exists()) {
            backupDir.listFiles()?.map { it.absolutePath } ?: emptyList()
        } else {
            emptyList()
        }
    }
} 