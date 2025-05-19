# Cloud Migration Guide

This document provides a detailed roadmap for migrating the Inventory app from mock data to cloud-based functionality.

## Current State: Mock Data Implementation

The app is designed to work with local mock data for testing and development purposes. 

### Key Mock Data Components:

1. **Mock Data Source**: 
   - Location: `app/src/main/assets/mock_data.json`
   - Contains test data for items, staff, and checkout logs

2. **Mock Data Loading**:
   - Implemented in the Repository classes (ItemRepository, StaffRepository, CheckoutRepository)
   - Controlled via feature flag in NetworkModule.kt

## Migration Process

### Step 1: Network Module Configuration

**File**: `app/src/main/java/com/example/inventory/api/NetworkModule.kt`

This is the central configuration point for switching between mock and cloud data:

```kotlin
// Change this flag to switch to cloud API
private val useMockServices = false 

// Update API base URL
private const val BASE_URL = "https://your-api-server.com/api/v1/"
```

### Step 2: Authentication Integration

**Files**:
- `app/src/main/java/com/example/inventory/api/AuthManager.kt`
- `app/src/main/java/com/example/inventory/api/AuthNetworkModule.kt`

1. Configure authentication endpoints:
   ```kotlin
   // In AuthNetworkModule.kt
   private const val AUTH_ENDPOINT = "/auth"
   ```

2. Set up API key or credentials:
   ```kotlin
   // In AuthNetworkModule.kt
   private const val API_KEY_HEADER = "X-API-Key"
   ```

### Step 3: API Client Implementation

**Files**:
- `app/src/main/java/com/example/inventory/api/ItemApiClient.kt`
- `app/src/main/java/com/example/inventory/api/StaffApiClient.kt` 
- `app/src/main/java/com/example/inventory/api/CheckoutApiClient.kt`

These clients need to implement the API endpoints defined in `API_ENDPOINTS.md`:

```kotlin
// Example for ItemApiClient.kt:
interface ItemApiClient {
    @GET("items")
    suspend fun getAllItems(): Response<List<Item>>
    
    @GET("items/{id}")
    suspend fun getItemById(@Path("id") id: UUID): Response<Item>
    
    @POST("items")
    suspend fun createItem(@Body item: Item): Response<Item>
    
    // Additional methods for other endpoints
}
```

### Step 4: Repository Layer Updates

**Files**:
- `app/src/main/java/com/example/inventory/data/repository/ItemRepository.kt`
- `app/src/main/java/com/example/inventory/data/repository/StaffRepository.kt`
- `app/src/main/java/com/example/inventory/data/repository/CheckoutRepository.kt`

Each repository needs to be updated to:
1. Use the appropriate API client when in cloud mode
2. Apply model converters between database and API models
3. Handle caching and offline operation

Example structure:
```kotlin
class ItemRepository @Inject constructor(
    private val itemDao: ItemDao,
    private val itemApiClient: ItemApiClient,
    private val networkModule: NetworkModule,
    private val offlineCache: OfflineCache
) {
    suspend fun getItems(): Flow<List<Item>> {
        return if (networkModule.isUsingMockServices()) {
            // Load from mock data or local database
            itemDao.getAllItems().map { it.toModelList() }
        } else {
            // Load from API and cache in database
            try {
                val response = itemApiClient.getAllItems()
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    itemDao.insertAll(items.toEntityList())
                    itemDao.getAllItems().map { it.toModelList() }
                } else {
                    // Fallback to cached data
                    itemDao.getAllItems().map { it.toModelList() }
                }
            } catch (e: Exception) {
                // Fallback to cached data
                itemDao.getAllItems().map { it.toModelList() }
            }
        }
    }
    
    // Additional methods for CRUD operations
}
```

### Step 5: Synchronization Controller

**File**: `app/src/main/java/com/example/inventory/api/SyncController.kt`

The SyncController handles offline operations and syncing with the cloud:

1. Track changes made while offline
2. Push changes when connectivity is restored
3. Handle conflict resolution

Key methods to implement:
- `synchronize()`: Push local changes and pull remote changes
- `scheduleSync()`: Set up periodic syncing
- `handleSyncConflicts()`: Resolve conflicts between local and remote changes

### Step 6: Error Handling

**File**: `app/src/main/java/com/example/inventory/api/NetworkErrorHandler.kt`

Implement comprehensive error handling for:
- Network connectivity issues
- API errors and status codes
- Authentication failures
- Request timeouts

### Step 7: UI Updates

**Files**: Various UI components and ViewModels

Ensure the UI reflects:
- Sync status indicators
- Loading states
- Error messages for network issues
- Login/logout functionality

## Testing the Migration

1. Test each API endpoint individually:
   - Use Postman or similar tool to verify API responses
   - Compare with expected formats

2. Test offline functionality:
   - Turn on airplane mode
   - Make changes in the app
   - Turn off airplane mode and verify sync

3. Test authentication flow:
   - Login/logout process
   - Token refresh
   - Authorization on protected endpoints

## Type Conversion Strategy

Throughout this migration, use the model converters in `ModelConverters.kt` to handle the transitions between:
- Database entities (Room) 
- Model classes (Network)

This maintains separation between local storage and network communication. 