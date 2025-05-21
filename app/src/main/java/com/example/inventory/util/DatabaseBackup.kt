package com.example.inventory.util

import android.content.Context
import com.example.inventory.data.firebase.FirebaseConfig
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.model.CheckoutLog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Utility class for backing up Firestore contents to JSON files
 */
class DatabaseBackup @Inject constructor(
    private val context: Context,
    private val firebaseConfig: FirebaseConfig
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    
    /**
     * Creates a backup of all Firestore collections to JSON files
     * @return List of created backup file paths
     */
    suspend fun createBackup(): List<String> = withContext(Dispatchers.IO) {
        val backupDir = createBackupDirectory()
        val timestamp = dateFormat.format(Date())
        
        val backupFiles = mutableListOf<String>()
        
        try {
            // Backup Items
            val itemsSnapshot = firebaseConfig.firestore.collection("items").get().await()
            val items = itemsSnapshot.documents.mapNotNull { it.toObject(Item::class.java) }
            val itemsFile = File(backupDir, "items_$timestamp.json")
            itemsFile.writeText(gson.toJson(items))
            backupFiles.add(itemsFile.absolutePath)
            
            // Backup Staff
            val staffSnapshot = firebaseConfig.firestore.collection("staff").get().await()
            val staff = staffSnapshot.documents.mapNotNull { it.toObject(Staff::class.java) }
            val staffFile = File(backupDir, "staff_$timestamp.json")
            staffFile.writeText(gson.toJson(staff))
            backupFiles.add(staffFile.absolutePath)
            
            // Backup Checkout Logs
            val checkoutSnapshot = firebaseConfig.firestore.collection("checkouts").get().await()
            val checkoutLogs = checkoutSnapshot.documents.mapNotNull { it.toObject(CheckoutLog::class.java) }
            val checkoutFile = File(backupDir, "checkout_logs_$timestamp.json")
            checkoutFile.writeText(gson.toJson(checkoutLogs))
            backupFiles.add(checkoutFile.absolutePath)
        } catch (e: Exception) {
            // Create an error log file if there's an issue
            val errorFile = File(backupDir, "error_$timestamp.txt")
            errorFile.writeText("Error creating backup: ${e.message}\n${e.stackTraceToString()}")
            backupFiles.add(errorFile.absolutePath)
        }
        
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