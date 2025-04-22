package com.example.inventory.api

import com.example.inventory.BuildConfig
import com.example.inventory.data.database.Item
import com.example.inventory.data.database.Staff
import com.example.inventory.data.database.CheckoutLog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
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
fun ItemDto.toEntity(): Item = Item(
    id = id?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
    name = name,
    category = category,
    type = type,
    barcode = barcode,
    condition = condition,
    status = status,
    photoPath = photoPath,
    isActive = isActive,
    lastModified = lastModified
)

fun Item.toDto(): ItemDto = ItemDto(
    id = id.toString(),
    name = name,
    category = category,
    type = type,
    barcode = barcode,
    condition = condition,
    status = status,
    photoPath = photoPath,
    isActive = isActive,
    lastModified = lastModified
)

/**
 * Retrofit API service interfaces
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
 * Network module for creating API services
 */
object NetworkModule {
    private const val CONNECTION_TIMEOUT = 30L
    
    // Replace this with your actual API base URL
    private const val BASE_URL = "https://your-azure-api.azurewebsites.net/"
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val httpClient = OkHttpClient.Builder()
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
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    // Create API service instances
    val itemApiService: ItemApiService = retrofit.create(ItemApiService::class.java)
    val staffApiService: StaffApiService = retrofit.create(StaffApiService::class.java)
    val checkoutApiService: CheckoutApiService = retrofit.create(CheckoutApiService::class.java)
} 