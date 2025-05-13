# Cloud Migration Guide

## Introduction
This document explains how to migrate the Inventory app from a local Room database to an Azure cloud backend. It is intended for developers who need to understand the architectural changes and implementation details.

## Migration Path

### 1. Initial Preparation (Completed)
- Branch: `cloud-integration-labels`
- Added labels to all data modification points
- No functional changes, just documentation

### 2. Full Cloud Implementation (Current)
- Branch: `cloud-implementation`
- Complete replacement of local storage with cloud API calls
- Added DTOs, API service interfaces, and network configuration
- Implemented error handling stubs

### 3. Future Work (Planned)
- Implement authentication with Azure AD
- Add offline mode with local caching
- Implement proper error handling and retries

## Technical Implementation Details

### Repository Pattern Changes

**Local Version:**
```kotlin
// Direct database access
class ItemRepository(private val itemDao: ItemDao) {
    suspend fun insertItem(item: Item) = itemDao.insert(item)
}
```

**Cloud Version:**
```kotlin
// API-based implementation
class CloudItemRepository(private val apiService: ItemApiService) : ItemRepository {
    override suspend fun insertItem(item: Item) {
        try {
            apiService.createItem(item.toDto())
        } catch (e: Exception) {
            // Error handling
        }
    }
}
```

### Flow Transformation Changes

**Local Version:**
```kotlin
// Direct database query as Flow
fun getAllItems(): Flow<List<Item>> = itemDao.getAllItems()
```

**Cloud Version:**
```kotlin
// API call wrapped in Flow
override fun getAllItems(): Flow<List<Item>> = flow {
    try {
        val items = apiService.getAllItems().map { it.toDto() }
        emit(items)
    } catch (e: Exception) {
        emit(emptyList())
    }
}
```

### DTO Mapping

All entities require mapping between domain models and DTOs:

```kotlin
// Entity to DTO
fun Item.toDto(): ItemDto = ItemDto(
    id = id.toString(),
    name = name,
    // Other properties
)

// DTO to Entity
fun ItemDto.toEntity(): Item = Item(
    id = id?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
    name = name,
    // Other properties
)
```

## Required Azure Resources

| Resource Type | Purpose | Configuration Notes |
|---------------|---------|---------------------|
| App Service | Hosts REST API | Standard tier or higher recommended |
| SQL Database | Stores all app data | Basic tier for testing, Standard for production |
| Blob Storage | Stores photos | Consider lifecycle management for old photos |
| Azure AD | Authentication | B2C for multi-tenant scenarios |

## API Implementation Requirements

The backend API must implement the following endpoints with exact URL patterns:

### Item API
- `GET api/items`
- `GET api/items/{id}`
- `GET api/items/barcode/{barcode}`
- `POST api/items`
- `PUT api/items/{id}`
- `PATCH api/items/{id}/status`
- `PATCH api/items/{id}/archive`
- `PATCH api/items/{id}/unarchive`

### Staff API
- `GET api/staff`
- `GET api/staff/{id}`
- `POST api/staff`
- `PUT api/staff/{id}`
- `PATCH api/staff/{id}/archive`
- `PATCH api/staff/{id}/unarchive`

### Checkout API
- `GET api/checkoutlogs`
- `GET api/checkoutlogs/item/{itemId}`
- `GET api/checkoutlogs/staff/{staffId}`
- `GET api/checkoutlogs/current`
- `POST api/checkoutlogs`
- `PATCH api/checkoutlogs/{id}/checkin`

## Security Considerations

1. **API Authentication**
   - Implement Azure AD authentication
   - Add auth token to all API requests
   - Consider role-based access control

2. **Data Security**
   - Ensure TLS for all API communication
   - Implement proper access controls in the backend
   - Consider encryption for sensitive data

3. **Blob Storage Security**
   - Use SAS tokens for limited-time access
   - Consider private access only through the API

## Performance Considerations

1. **Network Latency**
   - Add loading indicators in UI
   - Implement caching for frequently used data
   - Consider batching requests where possible

2. **Error Handling**
   - Implement exponential backoff for retries
   - Add offline support for critical operations
   - Provide clear error messages to users

## Testing the Migration

1. **Parallel Testing**
   - Run both versions side by side
   - Compare data integrity between systems
   - Test performance differences

2. **Validation Tests**
   - Verify all CRUD operations work correctly
   - Test error scenarios and network failures
   - Validate data consistency

## Rollback Plan

If critical issues are encountered:

1. Revert to the `main` branch
2. Restore from local database if needed
3. Document issues for future migration attempts 