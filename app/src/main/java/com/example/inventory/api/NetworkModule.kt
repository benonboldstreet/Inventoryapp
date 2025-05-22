package com.example.inventory.api

import android.content.Context
import android.util.Log
import com.example.inventory.BuildConfig
import com.example.inventory.data.model.Item
import com.example.inventory.data.model.Staff
import com.example.inventory.data.model.CheckoutLog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Data Transfer Objects for API communication
 */
data class ItemDto(
    val id: String? = null,
    val name: String,
    val category: String,
    val type: String,
    val barcode: String,
    val condition: String,
    val status: String,
    val photoPath: String? = null,
    val isActive: Boolean = true,
    val lastModified: Long? = null
)

data class StaffDto(
    val id: String? = null,
    val name: String,
    val department: String,
    val email: String,
    val phone: String,
    val position: String,
    val isActive: Boolean = true,
    val lastModified: Long? = null
)

data class CheckoutLogDto(
    val id: String? = null,
    val itemId: String,
    val staffId: String,
    val checkOutTime: Long? = null,
    val checkInTime: Long? = null,
    val photoPath: String? = null,
    val lastModified: Long? = null
)

/**
 * Extension functions to convert between DTOs and domain models
 */
fun ItemDto.toModel(): Item = Item(
    idString = id ?: UUID.randomUUID().toString(),
    name = name,
    category = category,
    type = type,
    barcode = barcode,
    condition = condition,
    status = status,
    photoPath = photoPath,
    isActive = isActive,
    lastModified = lastModified ?: System.currentTimeMillis()
)

fun Item.toNetworkDto(): ItemDto = ItemDto(
    id = idString,
    name = name,
    category = category,
    type = type,
    barcode = barcode,
    condition = condition,
    status = status,
    photoPath = photoPath,
    isActive = isActive,
    lastModified = getLastModifiedTime()
)

fun StaffDto.toModel(): Staff = Staff(
    idString = id ?: UUID.randomUUID().toString(),
    name = name,
    department = department,
    email = email,
    phone = phone,
    position = position,
    isActive = isActive,
    lastModified = lastModified ?: System.currentTimeMillis()
)

fun Staff.toNetworkDto(): StaffDto = StaffDto(
    id = idString,
    name = name,
    department = department,
    email = email,
    phone = phone,
    position = position,
    isActive = isActive,
    lastModified = getLastModifiedTime()
)

fun CheckoutLogDto.toModel(): CheckoutLog = CheckoutLog(
    idString = id ?: UUID.randomUUID().toString(),
    itemIdString = itemId,
    staffIdString = staffId,
    checkOutTime = checkOutTime ?: System.currentTimeMillis(),
    checkInTime = checkInTime,
    photoPath = photoPath,
    lastModified = lastModified ?: System.currentTimeMillis()
)

fun CheckoutLog.toNetworkDto(): CheckoutLogDto = CheckoutLogDto(
    id = idString,
    itemId = itemIdString,
    staffId = staffIdString,
    checkOutTime = getCheckOutTimeAsLong(),
    checkInTime = getCheckInTimeAsLong(),
    photoPath = photoPath,
    lastModified = getLastModifiedTime()
)

/**
 * Retrofit API service interfaces for cloud connectivity
 * These services map directly to Azure/cloud endpoints
 */
interface ItemApiService {
    @GET("api/items")
    suspend fun getAllItems(): List<ItemDto>
    
    @GET("api/items/{id}")
    suspend fun getItemById(@Path("id") id: String): ItemDto
    
    @GET("api/items/barcode/{barcode}")
    suspend fun getItemByBarcode(@Path("barcode") barcode: String): ItemDto
    
    @POST("api/items")
    suspend fun createItem(@Body item: ItemDto): ItemDto
    
    @PUT("api/items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: ItemDto): ItemDto
    
    @PATCH("api/items/{id}/status")
    suspend fun updateItemStatus(@Path("id") id: String, @Body statusUpdate: Map<String, String>): ItemDto
    
    @PATCH("api/items/{id}/archive")
    suspend fun archiveItem(@Path("id") id: String): ItemDto
    
    @PATCH("api/items/{id}/unarchive")
    suspend fun unarchiveItem(@Path("id") id: String): ItemDto
}

interface StaffApiService {
    @GET("api/staff")
    suspend fun getAllStaff(): List<StaffDto>
    
    @GET("api/staff/{id}")
    suspend fun getStaffById(@Path("id") id: String): StaffDto
    
    @POST("api/staff")
    suspend fun createStaff(@Body staff: StaffDto): StaffDto
    
    @PUT("api/staff/{id}")
    suspend fun updateStaff(@Path("id") id: String, @Body staff: StaffDto): StaffDto
    
    @PATCH("api/staff/{id}/archive")
    suspend fun archiveStaff(@Path("id") id: String): StaffDto
    
    @PATCH("api/staff/{id}/unarchive")
    suspend fun unarchiveStaff(@Path("id") id: String): StaffDto
}

interface CheckoutApiService {
    @GET("api/checkoutlogs")
    suspend fun getAllCheckoutLogs(): List<CheckoutLogDto>
    
    @GET("api/checkoutlogs/item/{itemId}")
    suspend fun getCheckoutLogsByItemId(@Path("itemId") itemId: String): List<CheckoutLogDto>
    
    @GET("api/checkoutlogs/staff/{staffId}")
    suspend fun getCheckoutLogsByStaffId(@Path("staffId") staffId: String): List<CheckoutLogDto>
    
    @GET("api/checkoutlogs/current")
    suspend fun getCurrentCheckouts(): List<CheckoutLogDto>
    
    @POST("api/checkoutlogs")
    suspend fun createCheckoutLog(@Body checkoutLog: CheckoutLogDto): CheckoutLogDto
    
    @PATCH("api/checkoutlogs/{id}/checkin")
    suspend fun checkInItem(@Path("id") id: String, @Body checkInData: Map<String, String>? = null): CheckoutLogDto
}

/**
 * Network module for creating API services to connect to cloud/Azure backend
 */
object NetworkModule {
    private const val CONNECTION_TIMEOUT = 30L
    
    // CLOUD ENDPOINT: Azure API base URL
    // TODO: Replace this with the actual Azure API URL when available
    private const val BASE_URL = "https://your-azure-api.azurewebsites.net/"
    
    // Flag to use mock services for testing
    private var useMockServices = true
    
    // Mock service instance - removed reference to MockApiService
    private var mockApiService: Any? = null
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    // Configure the HTTP client with appropriate timeouts and logging for cloud connectivity
    private var httpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .build()
    
    private var retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    // Create API service instances for cloud connectivity
    @Volatile
    private var _itemApiService: ItemApiService = retrofit.create(ItemApiService::class.java)
    
    @Volatile
    private var _staffApiService: StaffApiService = retrofit.create(StaffApiService::class.java)
    
    @Volatile
    private var _checkoutApiService: CheckoutApiService = retrofit.create(CheckoutApiService::class.java)
    
    // Public access to API services
    val itemApiService: ItemApiService
        get() = _itemApiService
    
    val staffApiService: StaffApiService
        get() = _staffApiService
    
    val checkoutApiService: CheckoutApiService
        get() = _checkoutApiService
    
    /**
     * Initialize with mock services for testing
     */
    fun initWithMockServices(context: Context) {
        // MockApiService implementation removed
        Log.d("NetworkModule", "Mock services are not available")
        
        useMockServices = false
        
        Log.d("NetworkModule", "Defaulting to real services")
    }
    
    /**
     * Switch to real services
     */
    fun useRealServices() {
        _itemApiService = retrofit.create(ItemApiService::class.java)
        _staffApiService = retrofit.create(StaffApiService::class.java)
        _checkoutApiService = retrofit.create(CheckoutApiService::class.java)
        
        useMockServices = false
        
        Log.d("NetworkModule", "Switched to real cloud services")
    }
    
    /**
     * Check if using mock services
     */
    fun isUsingMockServices(): Boolean {
        return useMockServices
    }
    
    /**
     * Get the mock service for test configuration
     */
    fun getMockService(): Any? {
        return mockApiService
    }
    
    /**
     * Replace the existing services with the provided ones
     * This is used by AuthNetworkModule to replace services with authenticated ones
     */
    internal fun replaceServices(
        itemApiService: ItemApiService,
        staffApiService: StaffApiService,
        checkoutApiService: CheckoutApiService
    ) {
        _itemApiService = itemApiService
        _staffApiService = staffApiService
        _checkoutApiService = checkoutApiService
    }
} 