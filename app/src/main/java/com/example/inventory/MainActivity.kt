package com.example.inventory

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.navigation.InventoryNavHost
import com.example.inventory.ui.theme.InventoryTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up global exception handler
        setupExceptionHandler()
        
        // Clear database cache if needed for version updates
        if (needsDatabaseReset()) {
            clearDatabaseCache()
        }
        
        // Initialize the database early
        val application = applicationContext as InventoryApplication
        application.container // Access container to initialize it
        
        enableEdgeToEdge()
        setContent {
            InventoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Add navigation listener for debugging
                    LaunchedEffect(navController) {
                        navController.addOnDestinationChangedListener { _, destination, arguments ->
                            Log.d("Navigation", "Navigated to: ${destination.route}, args: $arguments")
                        }
                    }
                    
                    InventoryNavHost(navController = navController)
                }
            }
        }
    }
    
    private fun needsDatabaseReset(): Boolean {
        // Check if this is the first run after an update
        val prefs = getSharedPreferences("inventory_prefs", Context.MODE_PRIVATE)
        val lastVersion = prefs.getInt("last_db_version", 1)
        val currentVersion = 2 // Make sure this matches your database version
        
        if (lastVersion < currentVersion) {
            // Update the stored version
            prefs.edit().putInt("last_db_version", currentVersion).apply()
            return true
        }
        
        return false
    }
    
    private fun clearDatabaseCache() {
        try {
            // Delete the Room database file
            val databases = File(applicationContext.dataDir, "databases")
            if (databases.exists()) {
                File(databases, "inventory_database").delete()
                File(databases, "inventory_database-shm").delete()
                File(databases, "inventory_database-wal").delete()
                Log.d("InventoryApp", "Database files deleted for clean migration")
            }
        } catch (e: Exception) {
            Log.e("InventoryApp", "Error clearing database cache", e)
        }
    }
    
    private fun setupExceptionHandler() {
        // Set up global exception handler
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("InventoryApp", "Uncaught exception", throwable)
            // Optional: Show a crash report dialog or send logs
            // Then pass to the default handler
            defaultExceptionHandler?.uncaughtException(thread, throwable)
        }
        
        // Set up coroutine exception handler
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                try {
                    // Nothing to do here, just catching exceptions
                } catch (e: Exception) {
                    Log.e("InventoryApp", "Coroutine exception", e)
                }
            }
        }
    }
}