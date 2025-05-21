# Code Reference Guide

This document provides a comprehensive map of where to find key code elements in the Inventory app. It's designed to help anyone understand the structure and locate important parts of the codebase.

## Firebase Collections and Endpoints

All Firebase collection references are defined in the repository files:

| Collection | File Location | Line |
|------------|---------------|------|
| `items` | `app/src/main/java/com/example/inventory/data/firebase/FirebaseItemRepository.kt` | 18 |
| `staff` | `app/src/main/java/com/example/inventory/data/firebase/FirebaseStaffRepository.kt` | 17 |
| `checkouts` | `app/src/main/java/com/example/inventory/data/firebase/FirebaseCheckoutRepository.kt` | 22 |

## Key Code Components

### Firebase Configuration
- **Config Setup**: `app/src/main/java/com/example/inventory/data/firebase/FirebaseConfig.kt`
- **Storage Utilities**: `app/src/main/java/com/example/inventory/data/firebase/FirebaseStorageUtils.kt`

### Data Models
| Model | File Location | Key Fields |
|-------|---------------|------------|
| Item | `app/src/main/java/com/example/inventory/data/model/Item.kt` | idString, name, category, status, isActive |
| Staff | `app/src/main/java/com/example/inventory/data/model/Staff.kt` | idString, name, department, isActive |
| CheckoutLog | `app/src/main/java/com/example/inventory/data/model/CheckoutLog.kt` | idString, itemIdString, staffIdString, checkOutTime |

### Repository Implementation
| Operation | File | Method | Line |
|-----------|------|--------|------|
| Get All Items | `FirebaseItemRepository.kt` | `getAllItems()` | ~20 |
| Get Active Items | `FirebaseItemRepository.kt` | `getActiveItems()` | ~50 |
| Add New Item | `FirebaseItemRepository.kt` | `insertItem()` | ~80 |
| Archive Item | `FirebaseItemRepository.kt` | `archiveItem()` | ~120 |
| Get All Staff | `FirebaseStaffRepository.kt` | `getAllStaff()` | ~20 |
| Add Staff | `FirebaseStaffRepository.kt` | `insertStaff()` | ~60 |
| Create Checkout | `FirebaseCheckoutRepository.kt` | `createCheckoutLog()` | ~80 |
| Complete Checkout | `FirebaseCheckoutRepository.kt` | `completeCheckout()` | ~110 |

### ViewModels (Connect UI to Data)
| Screen | ViewModel | File Location |
|--------|-----------|---------------|
| Item List | ItemViewModel | `app/src/main/java/com/example/inventory/ui/viewmodel/ItemViewModel.kt` |
| Staff List | StaffViewModel | `app/src/main/java/com/example/inventory/ui/viewmodel/StaffViewModel.kt` |
| Checkout | CheckoutViewModel | `app/src/main/java/com/example/inventory/ui/viewmodel/CheckoutViewModel.kt` |

### UI Screens
| Screen | File Location | Purpose |
|--------|---------------|---------|
| Item List | `app/src/main/java/com/example/inventory/ui/screens/ItemListScreen.kt` | Shows all inventory items |
| Item Detail | `app/src/main/java/com/example/inventory/ui/screens/ItemDetailScreen.kt` | View/edit item details |
| Staff List | `app/src/main/java/com/example/inventory/ui/screens/StaffListScreen.kt` | Shows all staff members |
| Checkout | `app/src/main/java/com/example/inventory/ui/screens/CheckoutScreen.kt` | Manages item checkouts |

## Data Flow

The app follows this pattern for all operations:

1. **UI Layer** (Screens) → Calls methods on ViewModels
2. **ViewModel Layer** → Calls repository methods
3. **Repository Layer** → Interacts with Firebase Firestore
4. **Firebase** → Stores and retrieves data

## Critical Firebase Operations

### Adding an Item
```kotlin
// In FirebaseItemRepository.kt
override suspend fun insertItem(item: Item): String {
    val itemId = item.idString.ifEmpty { UUID.randomUUID().toString() }
    val itemWithId = if (item.idString.isEmpty()) item.copy(idString = itemId) else item
    
    itemsCollection.document(itemId).set(itemWithId).await()
    return itemId
}
```

### Querying Items
```kotlin
// In FirebaseItemRepository.kt
override fun getActiveItems(): Flow<List<Item>> = flow {
    val snapshot = itemsCollection.whereEqualTo("isActive", true).get().await()
    val items = snapshot.documents.mapNotNull { doc ->
        try {
            val data = doc.data
            // Convert document to Item object
            val item = Item(
                idString = doc.id,
                name = data?.get("name") as? String ?: "",
                // Other fields...
                isActive = data?.get("isActive") as? Boolean ?: true
            )
            item
        } catch (e: Exception) {
            null
        }
    }
    emit(items)
}
```

## App Initialization

The app initializes Firebase in:
`app/src/main/java/com/example/inventory/InventoryApplication.kt`

```kotlin
override fun onCreate() {
    super.onCreate()
    
    // Initialize Firebase
    FirebaseApp.initializeApp(this)
    
    // Initialize the AppContainer with Firebase repositories
    container = AppContainerImpl(this)
    
    // ... other initialization code
}
```

## Debugging Tips

- Firebase logs can be found by filtering LogCat with tag "FirebaseItemRepo"
- Data structure issues usually appear in UI but originate in repository conversion methods
- Most UI update issues are in the Flow collection in ViewModels

## Common Errors and Solutions

| Error | Location | Solution |
|-------|----------|----------|
| Items not appearing | FirebaseItemRepository, getActiveItems() | Check "isActive" field type (must be Boolean) |
| Can't archive items | FirebaseItemRepository, archiveItem() | Verify document ID matches idString field |
| Checkout fails | FirebaseCheckoutRepository, createCheckoutLog() | Ensure valid itemIdString and staffIdString |

This reference should help anyone navigate the codebase and understand how data flows through the app. 