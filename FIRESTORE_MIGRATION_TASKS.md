# Firebase Firestore Migration Tasks

## Overview

We're in the process of migrating our Android inventory app from Room database to Firebase Firestore. Significant progress has been made on updating the ViewModels and consolidating the data models, but further work is needed to complete the migration.

## Completed Work

- Consolidated duplicate model classes (User, UserRole, PendingOperation, etc.)
- Updated ViewModels (ItemViewModel, StaffViewModel, CheckoutViewModel)
- Fixed DTO conversion naming inconsistencies (standardized on toModel/toNetworkDto)
- Updated UI component imports for LoginScreen and SettingsScreen
- Fixed CloudStaffRepository to use the correct model conversion methods
- Fixed FirebaseCheckoutRepository to use the correct field names (itemIdString/staffIdString)
- Configured Firestore for offline persistence in FirebaseConfig
- Created test data generator for Firestore
- Updated SettingsViewModel to use FirestoreTestDataGenerator
- Verified Firebase dependencies are correctly configured
- Created Firestore security rules reference
- Created Room cleanup task list
- Updated DatabaseBackup utility to work with Firestore

## Remaining Tasks

### 1. Repository Implementation

- [x] Update the dependency injection to use FirebaseItemRepository instead of CloudItemRepository
- [x] Update the dependency injection to use FirebaseStaffRepository instead of CloudStaffRepository
- [x] Update the dependency injection to use FirebaseCheckoutRepository instead of CloudCheckoutRepository
- [x] Ensure that FirebaseConfig is properly injected in all Firebase repositories

### 2. Offline Support

- [x] Configure Firestore for offline persistence
- [ ] Test syncing operations when going from offline to online
- [ ] Verify that the PendingOperation system works with Firebase repositories

### 3. UI Components

- [x] Fix missing Compose imports in SettingsScreen.kt
- [ ] Update any remaining screens that still reference old entity classes
- [ ] Ensure all UI components display data correctly from the Firestore models

### 4. Testing

- [x] Create test data generator for Firestore
- [ ] Test CRUD operations through the app
- [ ] Test offline operation
- [ ] Test synchronization when coming back online

### 5. Cleanup

- [ ] Remove deprecated Room-specific code (see ROOM_CLEANUP_TASKS.md)
- [x] Update DatabaseBackup utility to work with Firestore
- [ ] Update comments and documentation
- [ ] Delete unused files and classes

### 6. Security

- [x] Create reference Firestore security rules
- [ ] Copy security rules to Firebase console
- [ ] Test security rules

## Implementation Notes

### Firebase Collections Structure

The Firestore database has the following structure:

- **items**: Collection for inventory items
- **staff**: Collection for staff members
- **checkouts**: Collection for checkout logs

### Key Implementation Patterns

1. **Repository Pattern**: All data access goes through repository interfaces
2. **Adapter Pattern**: We've used adapters to help with the transition
3. **Offline Support**: Firestore provides built-in offline capabilities, which we augment with our OfflineCache for complex operations

### Firestore-Specific Considerations

- Firestore automatically serializes/deserializes data classes with @DocumentId
- We're using a UUID-based ID system converted to strings for Firestore compatibility
- Model classes now follow a consistent pattern with string IDs and UUID properties that convert between them
- All model classes have appropriate constructors for both UUID and string-based initialization
- Firestore offline persistence is enabled in FirebaseConfig class

### Integration with Hilt Dependency Injection

- The AppContainer is now configured to provide Firebase repository implementations
- Firebase repositories are annotated with @Singleton and use @Inject constructors
- FirebaseConfig provides centralized access to Firebase services

### Security Considerations

- Security rules are essential for protecting your Firestore data
- The reference rules in app/src/main/assets/firestore_security_rules.txt should be copied to the Firebase console
- These rules require authentication for all operations, which means the app's auth system must be properly set up

## Next Immediate Steps

1. Sync the project in Android Studio and test with the updated DatabaseBackup
2. Address any remaining compilation errors
3. Test the application with the test data generator
4. Copy security rules to Firebase console
5. Begin removing Room-related code according to ROOM_CLEANUP_TASKS.md 