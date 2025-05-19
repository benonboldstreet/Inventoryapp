# Inventory Management App

This Android application manages inventory items with both offline and cloud functionality.

## Project Overview

The app is designed with a hybrid architecture that supports:
1. Local storage using Room database
2. Mock data testing using JSON files
3. Cloud integration with a REST API

## Documentation Structure

This repository contains several guides to help you understand and work with the app:

### 1. [Mock Data Testing Guide](MOCK_DATA_TESTING_GUIDE.md)
How to test the app using mock data without a backend.

### 2. [Cloud Integration Locations](CLOUD_INTEGRATION_LOCATIONS.md) ⭐
**Key document** showing exact file and line locations for cloud integration.

### 3. [Cloud Migration Guide](CLOUD_MIGRATION_GUIDE.md)
Detailed walkthrough for migrating from mock data to cloud APIs.

### 4. [Repository Pattern Guide](REPOSITORY_PATTERN_GUIDE.md)
Explanation of the app's repository architecture.

### 5. [Type Conversion Fixes](TYPE_CONVERSION_FIXES.md)
Solutions for model/entity type conversion issues.

## Project Structure

```
app/
├── src/main/
│   ├── assets/
│   │   └── mock_data.json           # Mock data for testing
│   ├── java/com/example/inventory/
│   │   ├── api/                     # API and network related code
│   │   │   ├── AuthManager.kt       # Authentication - CLOUD INTEGRATION POINT
│   │   │   ├── NetworkModule.kt     # Network config - CLOUD INTEGRATION POINT
│   │   │   └── SyncController.kt    # Data sync - CLOUD INTEGRATION POINT
│   │   ├── data/
│   │   │   ├── converters/          # Type conversion between models and entities
│   │   │   ├── database/            # Room database entities and DAOs
│   │   │   ├── model/               # Model classes for network operations
│   │   │   └── repository/          # Repository implementations - CLOUD INTEGRATION POINT
│   │   ├── ui/
│   │   │   ├── components/          # Reusable UI components
│   │   │   ├── screens/             # Screen composables
│   │   │   └── viewmodel/           # ViewModels for each feature
```

## Quick Start for Cloud Integration

1. **Configure Network Module**:
   - Open `app/src/main/java/com/example/inventory/api/NetworkModule.kt`
   - Set `useMockServices = false`
   - Update `BASE_URL` with your API endpoint

2. **Set Up API Clients**:
   - Implement the interfaces in `app/src/main/java/com/example/inventory/api/`
   - Ensure they match the endpoints defined in API_ENDPOINTS.md

3. **Update Authentication**:
   - Configure `AuthManager.kt` and `AuthNetworkModule.kt`
   - Implement login, token refresh, and auth error handling

4. **Modify Repositories**:
   - Update all repository classes to fetch from API when not in mock mode
   - Implement proper caching and offline fallback

# Inventory Cloud (v2.1.0)

## Overview
Inventory Cloud is a fully cloud-connected inventory management app. This version has been fully optimized for cloud operations with all local database code removed.

## Version Information
- **Version:** 2.1.0
- **Mode:** Cloud-Only with Offline Support
- **Backend:** Azure Cloud Services
- **Original Repo:** [Local Version](https://github.com/benonboldstreet/Inventoryapp/tree/main)

## Repository Structure
This repository contains multiple versions of the Inventory application:

| Branch | Purpose |
|--------|---------|
| `main` | Original local-only implementation with Room database |
| `cloud-integration-labels` | Original code with cloud endpoint labels (preparation) |
| `cloud-implementation` | Full cloud implementation (previous version) |
| `cloud-only` | Current version with all local database code removed |

## Cloud Architecture Overview

### Data Flow
1. User interacts with the app UI
2. ViewModels call repository methods
3. Cloud repositories make API calls to Azure backend
4. Azure services handle data persistence and business logic
5. Results are returned to the app for display

### Offline Support
1. All data is cached for offline use
2. Operations performed offline are queued for sync
3. Automatic background sync when connectivity is restored
4. UI indicators show sync status to the user

### Simplified Access
1. No login required - designed for trusted IT staff
2. Direct access to all functionality
3. Streamlined workflow without authentication barriers
4. Consistent experience with the original app

### Key Components

#### API Layer (`app/src/main/java/com/example/inventory/api/`)
- **NetworkModule**: Central configuration for API services
- **DTOs**: Data Transfer Objects for API communication
- **API Services**: Retrofit interfaces defining all cloud endpoints
- **NetworkErrorHandler**: Centralized error handling for all cloud operations
- **OfflineCache**: Handles data caching and sync for offline operation

#### Model Layer (`app/src/main/java/com/example/inventory/data/model/`)
- **Item**: Data model for inventory items
- **Staff**: Data model for staff members
- **CheckoutLog**: Data model for checkout logs

#### Repository Layer (`app/src/main/java/com/example/inventory/data/repository/`)
- **ItemRepository**: Interface defining item operations
- **CloudItemRepository**: Cloud implementation for items with offline support
- **StaffRepository**: Interface defining staff operations
- **CloudStaffRepository**: Cloud implementation for staff with offline support
- **CheckoutRepository**: Interface defining checkout operations
- **CloudCheckoutRepository**: Cloud implementation for checkouts with offline support

#### Dependency Injection (`app/src/main/java/com/example/inventory/data/AppContainer.kt`)
- Provides cloud repository implementations to ViewModels

#### UI Components (`app/src/main/java/com/example/inventory/ui/components/`)
- **NetworkStatusBar**: Visual indicator for network connectivity
- **SyncStatusIndicator**: Visual indicator for sync operations
- Various other UI components for inventory management

## Setup Instructions

### 1. Azure Resources Required
- **Azure App Service**: Hosts the API backend
- **Azure SQL Database**: Stores inventory data
- **Azure Blob Storage**: Stores item/checkout photos
- **Azure Active Directory**: Handles authentication

### 2. Configuration Steps
1. Set up the required Azure resources above
2. Update the `BASE_URL` in `NetworkModule.kt` with your Azure endpoint
3. Add authentication details (update the TODO in NetworkModule)
4. Configure error handling and monitoring as needed

### 3. Cloud API Endpoints
The app expects the following API endpoints to be available:

#### Item Endpoints
- `GET api/items` - List all items
- `GET api/items/{id}` - Get item by ID
- `GET api/items/barcode/{barcode}` - Get item by barcode
- `POST api/items` - Create new item
- `PUT api/items/{id}` - Update existing item
- `PATCH api/items/{id}/status` - Update item status
- `PATCH api/items/{id}/archive` - Archive item
- `PATCH api/items/{id}/unarchive` - Unarchive item

#### Staff Endpoints
- `GET api/staff` - List all staff
- `GET api/staff/{id}` - Get staff by ID
- `POST api/staff` - Create new staff
- `PUT api/staff/{id}` - Update existing staff
- `PATCH api/staff/{id}/archive` - Archive staff
- `PATCH api/staff/{id}/unarchive` - Unarchive staff

#### Checkout Endpoints
- `GET api/checkoutlogs` - List all checkout logs
- `GET api/checkoutlogs/item/{itemId}` - Get checkouts by item
- `GET api/checkoutlogs/staff/{staffId}` - Get checkouts by staff
- `GET api/checkoutlogs/current` - Get current checkouts
- `POST api/checkoutlogs` - Create checkout
- `PATCH api/checkoutlogs/{id}/checkin` - Check in item

## Key Improvements in This Version

1. **Codebase Simplification**:
   - Removed all Room database dependencies
   - Simplified repository structure with interfaces
   - Cleaner data models without annotations

2. **Improved User Experience**:
   - Enhanced search functionality
   - Category tabs for quick filtering
   - Recently viewed items section
   - Cloud connectivity indicators

3. **Network Awareness**:
   - Real-time cloud connection monitoring
   - Visual indicators for connection status
   - Graceful handling of connection issues

4. **Cloud-First Architecture**:
   - All operations target cloud endpoints
   - No local data persistence
   - Clear separation of concerns

5. **Robust Error Handling**:
   - Centralized error handling through NetworkErrorHandler
   - User-friendly error messages for all cloud operations
   - Consistent error reporting across repositories
   - Clear loading state indicators during network operations

6. **Offline Support**:
   - Comprehensive offline caching system
   - Background synchronization for offline changes
   - Visual indicators for sync status
   - Optimistic UI updates for better user experience

7. **Simplified Access**:
   - No authentication barriers
   - Direct access to all functionality
   - Consistent with original app experience
   - Designed for trusted IT staff

8. **Performance Optimizations**:
   - Background data prefetching
   - Intelligent network retry mechanism
   - Minimized redundant API calls
   - Resource-aware data loading

## Error Handling System

The app implements a robust error handling system through the `NetworkErrorHandler` class:

### Key Features:

1. **Centralized Error Management**: 
   - All network errors are handled through a single class
   - Consistent error messages across the entire app
   - Tracking of loading states during API calls

2. **User-Friendly Error Messages**:
   - Connection issues: "Cannot connect to cloud server. Please check your internet connection."
   - Server down: "Connection to cloud server failed. Server may be down."
   - Timeout: "Connection to cloud server timed out. Please try again."
   - Other errors: Descriptive messages with error details

3. **Repository Integration**:
   - All repository methods use the error handler
   - Both Flow-based and suspend function operations supported
   - Fallback values provided for network failures

4. **UI Integration**:
   - Loading indicators tied to network operations
   - Error messages can be displayed in the UI
   - Easy access to last error through observable state

## Offline Caching System

The app implements a comprehensive offline caching system through the `OfflineCache` class:

### Key Features:

1. **Transparent Caching**:
   - Automatically caches all data from cloud operations
   - Repositories seamlessly fallback to cached data when offline
   - Users can continue working without disruption

2. **Operation Queueing**:
   - Offline operations are queued for later synchronization
   - Each operation is stored with metadata for proper sync handling
   - Optimistic UI updates to appear responsive

3. **Background Synchronization**:
   - Automatic sync when connectivity is restored
   - Periodic sync attempts on a configurable schedule
   - App lifecycle awareness for optimal sync timing

4. **Conflict Resolution**:
   - Last-writer-wins strategy for data conflicts
   - Timestamp tracking for all entities
   - Smart merge capabilities for synchronizing changes

5. **UI Feedback**:
   - Clear visual indicators for sync status
   - Offline mode indicators throughout the app
   - Detailed sync status reporting

## Performance Optimization

The app implements various performance optimizations to ensure smooth user experience:

### Key Features:

1. **Data Prefetching**:
   - Background loading of frequently accessed data
   - Smart invalidation of prefetched data
   - Lifecycle-aware prefetching to minimize battery impact

2. **Network Resilience**:
   - Exponential backoff retry mechanism
   - Graceful handling of transient network issues
   - Configurable retry policies for different operations

3. **Resource Efficiency**:
   - Minimized redundant API calls
   - Optimized data loading patterns
   - Memory-efficient caching strategies

4. **Responsive UI**:
   - Background processing of network operations
   - Loading state indicators for in-progress operations
   - Optimistic UI updates for immediate feedback

## Implementation Notes

### Testing Mode
The app includes a testing mode that uses mock data instead of real cloud endpoints. This is useful for:
1. Testing the app functionality without an active cloud backend
2. Demonstrating features without requiring server setup
3. QA and validation of app behavior
4. Development of new features in isolation

To switch between testing mode and real cloud mode:
- Testing mode is enabled by default (useMockServices = true in NetworkModule)
- To use real cloud services, call NetworkModule.useRealServices()
- To configure mock behavior (like simulated network delays), use NetworkModule.getMockService()

Mock data is stored in app/src/main/assets/mock_data.json and can be edited to test with different scenarios.

### Offline Caching Implementation
- Uses Jetpack DataStore for persistent cache storage
- In-memory cache for performance optimization
- Typed operation queue for offline actions
- Lifecycle-aware synchronization

### Error Handling
- All cloud operations use the NetworkErrorHandler
- Empty results returned on network failure with user-friendly errors
- Network status indicators in the UI
- Loading state management for better UX

### Future Improvements
- ~~Implement offline caching with sync~~ (Implemented)
- ~~Implement retry mechanisms for failed operations~~ (Implemented)
- ~~Optimize performance with data prefetching~~ (Implemented)
- ~~Add barcode scanning functionality~~ (Implemented)
- Add comprehensive error reporting
- Implement analytics tracking
- Add export and reporting capabilities
- Add user authentication (if needed in the future)
- Implement smart inventory features

## Barcode Scanning

The app includes a robust barcode scanning system to streamline inventory operations:

### Key Features:

1. **Real-time Scanning**:
   - Live camera feed for scanning barcodes
   - Instant item lookup once barcode is detected
   - Flashlight toggle for low-light environments

2. **Multiple Barcode Formats**:
   - Supports EAN-13, UPC-A, Code 128, QR Codes, and more
   - Automatic format detection
   - Detailed barcode information display

3. **Scan Integration**:
   - Quick access from item listing via floating action button
   - Direct navigation to item details after scan
   - Option to check out item immediately following scan

4. **Scan History**:
   - Maintains list of recently scanned barcodes
   - Quick access to repeat common scans
   - Scan history management

5. **Error Handling**:
   - Graceful handling of unrecognized barcodes
   - Camera permission management
   - Network retry for item lookups

## Contact Information
For questions or support, please contact us via mobile.

## Application Class

In your Application class, you need to initialize NetworkModule and OfflineCache:

```kotlin
class InventoryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkModule.initWithMockServices(this)
        OfflineCache.initialize(this)
    }
}
```