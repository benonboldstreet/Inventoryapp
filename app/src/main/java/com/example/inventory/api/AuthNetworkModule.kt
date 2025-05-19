package com.example.inventory.api

import android.content.Context
import com.example.inventory.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Authentication interceptor for adding auth token to requests
 */
class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Only add auth header if user is logged in
        if (!AuthManager.isLoggedIn.value) {
            return chain.proceed(originalRequest)
        }
        
        // Get the auth token (this is a blocking call, but should be quick)
        val token = runBlocking { AuthManager.getAuthToken(context) }
        
        // If no token, proceed with the original request
        if (token == null) {
            return chain.proceed(originalRequest)
        }
        
        // Add auth header to the request
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}

/**
 * Network module for creating API services to connect to cloud/Azure backend
 * This version includes authentication support
 */
object AuthNetworkModule {
    private const val CONNECTION_TIMEOUT = 30L
    
    // CLOUD ENDPOINT: Azure API base URL
    // TODO: Replace this with the actual Azure API URL when available
    private const val BASE_URL = "https://your-azure-api.azurewebsites.net/"
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    private var httpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    
    // Lazy initialized API services
    private var _itemApiService: ItemApiService? = null
    private var _staffApiService: StaffApiService? = null
    private var _checkoutApiService: CheckoutApiService? = null
    
    // Access to API services
    val itemApiService: ItemApiService
        get() = _itemApiService ?: throw IllegalStateException("AuthNetworkModule must be initialized before use")
    
    val staffApiService: StaffApiService
        get() = _staffApiService ?: throw IllegalStateException("AuthNetworkModule must be initialized before use")
    
    val checkoutApiService: CheckoutApiService
        get() = _checkoutApiService ?: throw IllegalStateException("AuthNetworkModule must be initialized before use")
    
    /**
     * Initialize the network module with application context
     * This should be called from the Application class
     */
    fun initialize(context: Context) {
        if (httpClient != null) return // Already initialized
        
        // Configure the HTTP client with appropriate timeouts and logging for cloud connectivity
        httpClient = OkHttpClient.Builder()
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
            // Add authentication interceptor
            .addInterceptor(AuthInterceptor(context))
            .build()
        
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient!!)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        // Create API service instances
        _itemApiService = retrofit!!.create(ItemApiService::class.java)
        _staffApiService = retrofit!!.create(StaffApiService::class.java)
        _checkoutApiService = retrofit!!.create(CheckoutApiService::class.java)
    }
    
    /**
     * Replace the existing services in NetworkModule with authenticated ones
     * This should be called after successful login
     */
    fun updateNetworkModuleServices() {
        if (_itemApiService == null || _staffApiService == null || _checkoutApiService == null) {
            throw IllegalStateException("AuthNetworkModule must be initialized before updating services")
        }
        
        // Replace the existing services with authenticated ones
        NetworkModule.replaceServices(
            itemApiService = _itemApiService!!,
            staffApiService = _staffApiService!!,
            checkoutApiService = _checkoutApiService!!
        )
    }
} 