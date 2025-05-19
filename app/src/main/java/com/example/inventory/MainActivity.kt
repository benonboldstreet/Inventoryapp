package com.example.inventory

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.api.OfflineCache
import com.example.inventory.ui.components.NetworkStatusBar
import com.example.inventory.ui.components.SmallSyncIndicator
import com.example.inventory.ui.components.SyncStatusIndicator
import com.example.inventory.ui.navigation.InventoryDestinations
import com.example.inventory.ui.navigation.InventoryNavHost
import com.example.inventory.ui.viewmodel.SharedViewModel
import com.example.inventory.ui.theme.InventoryTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main Activity for the Inventory Cloud application
 */
class MainActivity : ComponentActivity() {
    // Network callback for monitoring connectivity
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up global exception handler
        setupExceptionHandler()
        
        // Initialize the container early to prepare cloud connections
        val application = applicationContext as InventoryApplication
        application.container
        
        // Set up network connectivity monitoring
        setupNetworkMonitoring()
        
        enableEdgeToEdge()
        setContent {
            InventoryTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                
                // Tracking if we're showing the welcome guidance
                var showWelcomeMessage by remember { mutableStateOf(true) }
                
                // Add navigation listener for debugging
                LaunchedEffect(navController) {
                    navController.addOnDestinationChangedListener { _, destination, arguments ->
                        Log.d("Navigation", "Navigated to: ${destination.route}, args: $arguments")
                    }
                    
                    // Initial navigation to items list
                    navController.navigate(InventoryDestinations.ITEMS_ROUTE) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
                
                // Show welcome guidance if it's the first time
                LaunchedEffect(Unit) {
                    if (showWelcomeMessage) {
                        delay(500) // Small delay for UI to settle
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Welcome! Find all inventory items on the main screen",
                                actionLabel = "Got it"
                            )
                            showWelcomeMessage = false
                        }
                    }
                    
                    // Trigger initial sync
                    scope.launch {
                        OfflineCache.attemptSync(applicationContext)
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Show network status bar at the top of the UI
                            NetworkStatusBar()
                            
                            // Show sync status indicator
                            SyncStatusIndicator(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            
                            InventoryNavHost(
                                navController = navController,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Small sync indicator in the corner
                        SmallSyncIndicator(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        )
                        
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) { data ->
                            Snackbar(
                                snackbarData = data
                            )
                        }
                    }
                }
            }
        }
    }
    
    private fun setupNetworkMonitoring() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Create network callback
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Update the shared view model when network becomes available
                SharedViewModel.updateConnectivity(true)
                
                // Try to sync when connection is restored
                lifecycleScope.launch {
                    OfflineCache.attemptSync(applicationContext)
                }
            }
            
            override fun onLost(network: Network) {
                // Update the shared view model when network is lost
                SharedViewModel.updateConnectivity(false)
            }
        }
        
        // Register the callback
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        SharedViewModel.updateConnectivity(isConnected)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Unregister network callback to prevent leaks
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e("InventoryApp", "Error unregistering network callback", e)
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