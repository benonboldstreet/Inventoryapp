package com.example.inventory.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.firebase.FirestoreTestDataGenerator
import com.example.inventory.util.DatabaseBackup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val context: Context,
    private val databaseBackup: DatabaseBackup,
    private val testDataGenerator: FirestoreTestDataGenerator
) : ViewModel() {
    
    fun loadSettings() {
        // Load any settings from SharedPreferences or other sources
    }
    
    fun isCloudConnected(): Boolean {
        return SharedViewModel.isCloudConnected.value
    }
    
    fun createBackup(onStatusUpdate: (String) -> Unit) {
        viewModelScope.launch {
            try {
                onStatusUpdate("Creating backup...")
                val backupFiles = databaseBackup.createBackup()
                onStatusUpdate("Backup created successfully!\nFiles: ${backupFiles.joinToString("\n")}")
            } catch (e: Exception) {
                onStatusUpdate("Backup failed: ${e.message}")
            }
        }
    }
    
    fun generateTestData(onStatusUpdate: (String) -> Unit) {
        viewModelScope.launch {
            try {
                onStatusUpdate("Generating test data...")
                
                // Use the Firestore test data generator
                val result = testDataGenerator.generateTestData()
                
                onStatusUpdate("$result")
            } catch (e: Exception) {
                onStatusUpdate("Failed to generate test data: ${e.message}")
            }
        }
    }
} 