# Repository Pattern Implementation Guide

This guide explains how the repository pattern is implemented in the Inventory app to bridge between different data sources and handle type conversions.

## Repository Pattern Overview

The repository pattern provides a clean separation between:
- Data sources (database, network, mock data)
- Business logic (ViewModels)
- UI components

### Key Components:

1. **Data Access Objects (DAOs)**: Interface with the local Room database
2. **API Clients**: Interface with the cloud API endpoints 
3. **Repositories**: Coordinate between DAOs and API clients
4. **Type Converters**: Convert between database entities and model objects

## Repository Implementation

### Item Repository Example

```kotlin
class ItemRepository @Inject constructor(
    private val itemDao: ItemDao,
    private val networkModule: NetworkModule,
    private val itemApiClient: ItemApiClient?,
    private val assetManager: AssetManager
) {
    // Get all items - returns a Flow that updates when data changes
    fun getAllItems(): Flow<List<Item>> {
        // Use the converters to transform database entities to model objects
        return itemDao.getAllItems().map { dbItems -> 
            dbItems.toModelList() 
        }
    }
    
    // Get item by ID
    suspend fun getItemById(id: UUID): Item? {
        val dbItem = itemDao.getItemById(id)
        return dbItem?.toModel()
    }
    
    // Insert or update an item
    suspend fun saveItem(item: Item) {
        // Convert model to database entity
        val dbItem = item.toEntity()
        itemDao.insert(dbItem)
        
        // If using cloud API, sync the change
        if (!networkModule.isUsingMockServices() && itemApiClient != null) {
            try {
                itemApiClient.updateItem(item)
            } catch (e: Exception) {
                // Store for later sync if API call fails
                offlineCache.storeOperation(
                    PendingOperation(
                        id = UUID.randomUUID(),
                        entityId = item.id,
                        type = OperationType.UPDATE,
                        entityType = "Item",
                        data = item.toJson()
                    )
                )
            }
        }
    }
    
    // Delete an item
    suspend fun deleteItem(item: Item) {
        val dbItem = item.toEntity()
        itemDao.delete(dbItem)
        
        // If using cloud API, sync the deletion
        if (!networkModule.isUsingMockServices() && itemApiClient != null) {
            try {
                itemApiClient.deleteItem(item.id)
            } catch (e: Exception) {
                // Store for later sync
                offlineCache.storeOperation(
                    PendingOperation(
                        id = UUID.randomUUID(),
                        entityId = item.id,
                        type = OperationType.DELETE,
                        entityType = "Item"
                    )
                )
            }
        }
    }
    
    // Load mock data
    suspend fun loadMockData() {
        if (networkModule.isUsingMockServices()) {
            val json = assetManager.open("mock_data.json").bufferedReader().use { it.readText() }
            val mockData = Gson().fromJson(json, MockData::class.java)
            
            // Convert and insert into database
            val items = mockData.items.map { it.toEntity() }
            itemDao.insertAll(items)
        }
    }
}
```

### Staff Repository 

Similar to the ItemRepository, but handles staff members:

```kotlin
class StaffRepository @Inject constructor(
    private val staffDao: StaffDao,
    private val networkModule: NetworkModule,
    private val staffApiClient: StaffApiClient?,
    private val assetManager: AssetManager
) {
    fun getAllStaff(): Flow<List<Staff>> {
        return staffDao.getAllStaff().map { dbStaff -> 
            dbStaff.toModelList() 
        }
    }
    
    // Similar methods for CRUD operations and mock data loading
}
```

### Checkout Repository

```kotlin
class CheckoutRepository @Inject constructor(
    private val checkoutLogDao: CheckoutLogDao,
    private val networkModule: NetworkModule,
    private val checkoutApiClient: CheckoutApiClient?,
    private val assetManager: AssetManager
) {
    fun getActiveCheckouts(): Flow<List<CheckoutLog>> {
        return checkoutLogDao.getActiveCheckouts().map { dbLogs -> 
            dbLogs.toModelList() 
        }
    }
    
    // Methods for checkout/return operations and history
}
```

## Type Conversion Strategy

The model converters (`ModelConverters.kt`) serve as the bridge between:
- **Database Entities**: Used with Room, contain database annotations
- **Model Objects**: Used for network operations and UI

```kotlin
// Database Entity to Model
fun DbItem.toModel(): ModelItem {
    return ModelItem(
        id = this.id,
        name = this.name,
        // other properties...
    )
}

// Model to Database Entity
fun ModelItem.toEntity(): DbItem {
    return DbItem(
        id = this.id,
        name = this.name,
        // other properties...
    )
}

// List conversions for convenience
fun List<DbItem>.toModelList(): List<ModelItem> = this.map { it.toModel() }
fun List<ModelItem>.toEntityList(): List<DbItem> = this.map { it.toEntity() }
```

## ViewModels and the Repository

ViewModels never interact directly with DAOs or API clients. They should always use repositories:

```kotlin
class ItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {
    
    // Expose items as LiveData or StateFlow
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    init {
        viewModelScope.launch {
            itemRepository.getAllItems().collect { items ->
                _items.value = items
            }
        }
    }
    
    // Functions to trigger repository operations
    fun saveItem(item: Item) {
        viewModelScope.launch {
            itemRepository.saveItem(item)
        }
    }
    
    // Other operations...
}
```

## Benefits of this Pattern

1. **Single Source of Truth**: The database serves as the canonical data source
2. **Type Safety**: Clear conversion between database and network types
3. **Testability**: Repositories can be mocked for testing ViewModels
4. **Offline Support**: Data operations work even without network connectivity
5. **Separation of Concerns**: Each component has a clear responsibility

## Common Pitfalls to Avoid

1. **Direct DAO Access**: ViewModels should never access DAOs directly
2. **Missed Conversions**: Always use the proper converters when crossing layers
3. **UI on Background Thread**: Remember to use proper coroutine dispatchers
4. **Ignoring Errors**: Handle network errors and provide fallbacks 