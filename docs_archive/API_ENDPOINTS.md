# Inventory System API Endpoints

This document outlines the API endpoints required for moving from the mock data testing version to the cloud-based implementation.

## Base URL

For development: `https://dev-api.inventory-system.com/v1`  
For production: `https://api.inventory-system.com/v1`

## Authentication

All endpoints except authentication require a valid JWT token in the Authorization header:
```
Authorization: Bearer {token}
```

## Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | Authenticate user and get token |
| POST | `/auth/refresh` | Refresh expired token |
| POST | `/auth/logout` | Invalidate token |

### Items

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/items` | Get all items |
| GET | `/items?category={category}` | Filter items by category |
| GET | `/items?type={type}` | Filter items by type |
| GET | `/items?status={status}` | Filter items by status |
| GET | `/items/{id}` | Get item by ID |
| GET | `/items/barcode/{barcode}` | Get item by barcode |
| POST | `/items` | Create new item |
| PUT | `/items/{id}` | Update item |
| DELETE | `/items/{id}` | Delete item |
| GET | `/items/categories` | Get all categories |

### Staff

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/staff` | Get all staff |
| GET | `/staff?role={role}` | Filter staff by role |
| GET | `/staff?department={department}` | Filter staff by department |
| GET | `/staff/{id}` | Get staff by ID |
| POST | `/staff` | Create new staff |
| PUT | `/staff/{id}` | Update staff |
| DELETE | `/staff/{id}` | Delete staff |

### Checkout Logs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/checkoutlogs` | Get all checkout logs |
| GET | `/checkoutlogs?itemId={itemId}` | Get logs for specific item |
| GET | `/checkoutlogs?staffId={staffId}` | Get logs for specific staff |
| GET | `/checkoutlogs/active` | Get all active checkouts (not returned) |
| GET | `/checkoutlogs/{id}` | Get log by ID |
| POST | `/checkoutlogs` | Create checkout log (check out item) |
| PUT | `/checkoutlogs/{id}` | Update log (check in item) |
| DELETE | `/checkoutlogs/{id}` | Delete log |

### Synchronization

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/sync` | Synchronize local changes with server |
| GET | `/sync/status` | Get last sync status |

## Request/Response Formats

### Item Object

```json
{
  "id": "uuid-string",
  "name": "Item Name",
  "category": "Laptop",
  "type": "Dell",
  "barcode": "1234567890",
  "condition": "Good",
  "status": "Available",
  "photoPath": "https://storage.inventory-system.com/photos/item-123.jpg",
  "isActive": true,
  "lastModified": 1652345678000
}
```

### Staff Object

```json
{
  "id": "uuid-string",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "role": "Developer",
  "department": "Engineering",
  "phone": "555-1234",
  "position": "Senior Developer",
  "photoPath": "https://storage.inventory-system.com/photos/staff-123.jpg",
  "isActive": true,
  "lastModified": 1652345678000
}
```

### CheckoutLog Object

```json
{
  "id": "uuid-string",
  "itemId": "item-uuid-string",
  "staffId": "staff-uuid-string",
  "checkOutTime": 1652345678000,
  "checkInTime": null,
  "photoPath": "https://storage.inventory-system.com/photos/checkout-123.jpg",
  "lastModified": 1652345678000
}
```

## Migration from Mock Data

To migrate from mock data to the cloud API:

1. In `NetworkModule.kt`, update the base URL to point to your API server
2. Update `AuthNetworkModule.kt` to use your authentication endpoints
3. Configure `SyncController.kt` to handle offline-online transitions
4. Update environment variables or BuildConfig values:
   - `API_BASE_URL`
   - `API_KEY` (if needed)
   - `ENABLE_CLOUD_SYNC` 