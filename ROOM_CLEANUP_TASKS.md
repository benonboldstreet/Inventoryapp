# Room Code Cleanup Tasks

As part of the migration from Room to Firestore, the following files and code should be cleaned up:

## Files to Delete

### Database Classes
- [ ] `app/src/main/java/com/example/inventory/data/database/InventoryDatabase.kt`

### DAO Interfaces
- [ ] `app/src/main/java/com/example/inventory/data/database/ItemDao.kt`
- [ ] `app/src/main/java/com/example/inventory/data/database/StaffDao.kt`
- [ ] `app/src/main/java/com/example/inventory/data/database/CheckoutLogDao.kt`

### Entity Classes
- [ ] `app/src/main/java/com/example/inventory/data/database/Item.kt`
- [ ] `app/src/main/java/com/example/inventory/data/database/Staff.kt`
- [ ] `app/src/main/java/com/example/inventory/data/database/CheckoutLog.kt`

### Type Converters
- [ ] Any files containing Room's `@TypeConverter` annotations

## Code to Update

### DatabaseBackup Class
This should be updated to work with Firestore instead of Room:
- [x] `app/src/main/java/com/example/inventory/util/DatabaseBackup.kt`

### Remove Room from Gradle
In the future, when the migration is fully complete, remove Room dependencies:
```kotlin
// Room dependencies to remove from build.gradle.kts
implementation(libs.room.runtime)
implementation(libs.room.ktx)
ksp(libs.room.compiler)
```

## Testing

After removing Room code:
1. Verify that the app builds successfully
2. Test that the app functions correctly with Firestore only
3. Confirm there are no references to Room-related classes

## Important Notes

- Make sure all references to Room entities are replaced with Firestore model classes
- Ensure that all ViewModels are updated to use Firestore repositories
- Update any migration-related or database-related code in the app to use Firestore concepts
- Backup any important data before removing Room completely 