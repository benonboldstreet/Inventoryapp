package com.example.inventory.api

import com.example.inventory.data.repository.CheckoutRepository
import com.example.inventory.data.repository.ItemRepository
import com.example.inventory.data.repository.StaffRepository
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

/**
 * Configuration for the Ktor API server
 */
object ApiServer {
    private var server: ApplicationEngine? = null
    
    /**
     * Start the API server
     */
    fun start(
        itemRepository: ItemRepository,
        staffRepository: StaffRepository,
        checkoutRepository: CheckoutRepository,
        port: Int = 8080
    ) {
        if (server != null) {
            return // Server already running
        }
        
        server = embeddedServer(Netty, port = port) {
            // Configure JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            
            // Configure routes
            routing {
                // Set up sync controller routes
                val syncController = SyncController(
                    itemRepository = itemRepository,
                    staffRepository = staffRepository,
                    checkoutRepository = checkoutRepository
                )
                syncController.registerRoutes(this)
            }
        }.start(wait = false)
    }
    
    /**
     * Stop the API server
     */
    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }
} 