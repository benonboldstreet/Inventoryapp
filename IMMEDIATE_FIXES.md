# Immediate Fixes for Compilation Issues

## 1. Resolve Duplicate Class Declarations
- Remove duplicate `User` and `UserRole` classes
- Keep only one definition in `com.example.inventory.data.model` package
- Update all references to point to the centralized model

## 2. Fix Model/Entity Confusion
- Create proper mapper functions between Firestore documents and model objects
- Update repositories to handle Firebase data correctly
- Update ViewModels to work with consistent model types
- Fix `toDatabase()` and `toModel()` methods

## 3. Address Missing BuildConfig
- Create a BuildConfig file with required constants
- Or replace BuildConfig references with direct values/constants

## 4. Add Missing Compose Imports
- Fix imports in UI screens, especially in:
  - `LoginScreen.kt`
  - `SettingsScreen.kt`
  - Other screens with UI component errors

## 5. Fix Field Mapping Issues
- Address missing fields (`phone`, `position`, `status`)
- Add these fields to appropriate model classes
- Update mappers to handle these fields correctly

## Implementation Steps
1. Start by consolidating the model classes
2. Fix the repositories to properly map between Firestore and models
3. Update ViewModels to use correct model types
4. Fix UI component imports
5. Test each modification incrementally

This approach will systematically resolve the compilation errors while maintaining the Firebase integration. 