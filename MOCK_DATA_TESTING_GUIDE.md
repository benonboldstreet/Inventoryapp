# Mock Data Testing Guide

This guide provides detailed instructions for testing the Inventory app with mock data before implementing cloud functionality.

## Understanding the Mock Data Architecture

The app uses a hybrid architecture that supports both local and cloud operations:

1. **Database Layer**: Room database for local storage of `Item`, `Staff`, and `CheckoutLog` entities
2. **Model Layer**: Non-annotated model classes for network operations
3. **Converter Layer**: Conversion functions between database entities and model classes
4. **Repository Layer**: Handles data operations and decides between mock and cloud sources
5. **Mock Data**: JSON file with test data in `assets/mock_data.json`

## Setup for Mock Data Testing

### 1. Configure Network Module

**File**: `app/src/main/java/com/example/inventory/api/NetworkModule.kt`

Ensure the mock data flag is enabled:
```kotlin
// Set to true for mock data testing
private val useMockServices = true
```

### 2. Verify Mock Data JSON

**File**: `app/src/main/assets/mock_data.json`

The mock data file contains test data for:
- Items (inventory items with various properties)
- Staff (personnel who can check out items)
- Checkouts (history of item checkouts and returns)

Sample structure:
```json
{
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "ThinkPad X1 Carbon",
      "category": "Laptop",
      "type": "Lenovo",
      "barcode": "LP-X1C-001",
      "condition": "Good",
      "status": "Available"
    },
    // More items...
  ],
  "staff": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "John Smith",
      "department": "Engineering",
      "email": "john.smith@example.com",
      "phone": "555-1234",
      "position": "Senior Developer"
    },
    // More staff...
  ],
  "checkoutLogs": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "itemId": "550e8400-e29b-41d4-a716-446655440000",
      "staffId": "550e8400-e29b-41d4-a716-446655440001",
      "checkOutTime": 1652345678000,
      "checkInTime": null
    },
    // More logs...
  ]
}
```

### 3. Understand How Mock Data is Loaded

**Files**:
- `app/src/main/java/com/example/inventory/data/repository/ItemRepository.kt`
- `app/src/main/java/com/example/inventory/data/repository/StaffRepository.kt`
- `app/src/main/java/com/example/inventory/data/repository/CheckoutRepository.kt`

Each repository loads mock data conditionally based on the NetworkModule configuration:

```kotlin
// Example from ItemRepository
fun loadItems() {
    if (networkModule.isUsingMockServices()) {
        val mockData = assetManager.readMockData()
        val items = mockData.items.map { it.toEntity() }
        itemDao.insertAll(items)
    }
}
```

## Testing Scenarios

### 1. View All Inventory Items

Test that:
- All items from mock_data.json appear in the item list
- Filtering by category works correctly
- Sorting options function as expected

### 2. Item Details

Test that:
- Item details display correctly
- Images load if provided in mock data
- Status is accurately displayed

### 3. Staff Management

Test that:
- Staff list shows all staff from mock data
- Staff details display correctly
- Department filtering works

### 4. Checkout Process

Test that:
- You can check out an available item to a staff member
- The item status changes to "Checked Out"
- A checkout log is created

### 5. Return Process

Test that:
- You can return a checked-out item
- The item status changes back to "Available"
- The checkout log is updated with a return time

### 6. Barcode Scanning

Test that:
- The barcode scanner can scan mock barcodes
- Scanning a valid barcode loads the correct item
- Invalid barcodes show appropriate error messages

### 7. Search Functionality

Test that:
- Search by item name works
- Search by barcode works
- Search filters apply correctly

## Debugging Mock Data Issues

If you encounter issues with mock data:

1. **Check JSON Format**:
   - Verify that the mock_data.json file is valid JSON
   - Check that UUIDs are properly formatted
   - Ensure required fields are present

2. **Repository Loading**:
   - Put a breakpoint in the repository's load method
   - Verify that mock data is being read correctly
   - Check that conversion to entities works

3. **Database Operations**:
   - Verify that entities are properly saved to the Room database
   - Check queries in the DAO interfaces

4. **Model Converters**:
   - Verify that `toModel()` and `toEntity()` converters handle all fields
   - Test with different types of mock data

## Adding Custom Mock Data

To test specific scenarios:

1. Edit the `mock_data.json` file to add custom test cases
2. Ensure any new items have unique UUIDs
3. Restart the app to reload the mock data

## Next Steps

After successfully testing with mock data, refer to the `CLOUD_MIGRATION_GUIDE.md` for detailed instructions on:
- Switching to cloud APIs
- Implementing authentication
- Handling offline/online synchronization
- Error handling for network operations 