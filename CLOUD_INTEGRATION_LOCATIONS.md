# Cloud Integration: Key Code Locations

This document identifies the exact locations of code that must be modified to enable cloud integration.

## 1. Network Configuration

**File**: `app/src/main/java/com/example/inventory/api/NetworkModule.kt`
**Key Lines**:
- Line ~15: `private val useMockServices = true` - Change to `false` to use cloud
- Line ~18: `private const val BASE_URL = "..."` - Update with your API URL

## 2. API Client Interfaces

**Files**:
- `app/src/main/java/com/example/inventory/api/ItemApiClient.kt` - Define item endpoints
- `app/src/main/java/com/example/inventory/api/StaffApiClient.kt` - Define staff endpoints
- `app/src/main/java/com/example/inventory/api/CheckoutApiClient.kt` - Define checkout endpoints

Each should implement the API endpoints described in `API_ENDPOINTS.md`.

## 3. Authentication

**Files**:
- `app/src/main/java/com/example/inventory/api/AuthManager.kt`
  - Line ~50: `login()` method - Add cloud authentication logic
  - Line ~80: `refreshToken()` method - Add token refresh logic

- `app/src/main/java/com/example/inventory/api/AuthNetworkModule.kt`
  - Line ~20: Auth endpoint configuration
  - Line ~30: API key/token configuration

## 4. Repository Cloud Integration

**Files**:
- `app/src/main/java/com/example/inventory/data/repository/ItemRepository.kt`
  - Line ~50: Cloud data fetching logic
  - Line ~80: Cloud data saving logic
  
- `app/src/main/java/com/example/inventory/data/repository/StaffRepository.kt`
  - Similar to ItemRepository with staff-specific operations
  
- `app/src/main/java/com/example/inventory/data/repository/CheckoutRepository.kt`
  - Checkout-specific cloud operations

## 5. Synchronization

**File**: `app/src/main/java/com/example/inventory/api/SyncController.kt`
- Line ~40: `synchronize()` method - Implement cloud sync logic
- Line ~70: `handleSyncConflicts()` - Add conflict resolution logic
- Line ~100: `scheduleSyncJob()` - Configure periodic syncing

## 6. Error Handling

**File**: `app/src/main/java/com/example/inventory/api/NetworkErrorHandler.kt`
- Line ~30: Error handling for network requests
- Line ~60: API error code handling
- Line ~90: Authentication failure handling

## 7. Database Migrations

**File**: `app/src/main/java/com/example/inventory/data/database/AppDatabase.kt`
- Line ~30: Migration logic if cloud schema differs

## 8. Type Conversion for Network Data

**File**: `app/src/main/java/com/example/inventory/data/converters/ModelConverters.kt`
- All methods convert between database entities and network models

## 9. UI Components Requiring Updates

- `app/src/main/java/com/example/inventory/ui/screens/LoginScreen.kt` - Authentication UI
- `app/src/main/java/com/example/inventory/ui/components/SyncStatusIndicator.kt` - Sync status UI
- `app/src/main/java/com/example/inventory/ui/components/NetworkErrorDialog.kt` - Network error UI

## Implementation Process

1. Start by setting up authentication (points 1, 3)
2. Implement API clients (point 2)
3. Update repositories to use cloud data (point 4)
4. Add synchronization (point 5)
5. Implement error handling (point 6)
6. Update UI components (point 9)

## Testing Process

1. Test authentication flow first
2. Test each API endpoint individually (GET, POST, etc.)
3. Test offline functionality and sync 