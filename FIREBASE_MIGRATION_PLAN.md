# Firebase Migration Plan

## Current Issues
- Multiple model classes with the same name in different packages
- Type mismatches between model classes and database entities
- Missing or incorrect mapper methods
- Redeclaration errors for some classes (User, UserRole, etc.)

## Strategy for Resolution

### 1. Model Structure Cleanup
- Consolidate all data models under `com.example.inventory.data.model`
- Remove duplicate class declarations
- Ensure models have proper Firestore serialization annotations

### 2. Repository Implementation
- Update repositories to use Firestore references instead of Room DAOs
- Implement consistent mapper methods between Firebase documents and model objects
- Remove Room-specific code that conflicts with Firestore operations

### 3. ViewModel Updates
- Update ViewModels to work with model objects consistently
- Add proper suspension functions for Firebase async operations
- Fix signature mismatches in service calls

### 4. UI Component Imports
- Fix missing Compose UI imports in screens
- Update references to model classes in UI code

### 5. Testing
- Create test cases for Firebase CRUD operations
- Verify data consistency between UI and Firestore

## Implementation Priority
1. Fix core model classes first (Item, Staff, CheckoutLog)
2. Update repository implementations
3. Fix ViewModels
4. Update UI components and screens 