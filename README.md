# Inventory Management App

This Android application manages inventory items using Firebase Firestore as the backend database.

## URGENT: UUID Implementation Issue

**Critical Update Required**: We've identified an important architectural issue regarding UUID handling in the application that needs immediate attention:

### Current Implementation Issue
The application is currently generating UUIDs locally and then storing them in Firebase, which creates several potential problems:
1. Unnecessary complexity in ID management
2. Potential conflicts in distributed systems
3. Additional data conversion overhead
4. Inconsistent ID handling across different parts of the application

### Required Changes

To resolve this, we need to choose ONE of these two approaches:

#### Option 1: Keep Using UUIDs (Current Implementation)
If keeping UUIDs is preferred, ensure:
1. All UUID generation happens in a single place (preferably in the model classes)
2. Consistent conversion between UUID and String using:
   ```kotlin
   // When storing
   val documentId = uuid.toString()
   
   // When retrieving
   val uuid = UUID.fromString(documentString)
   ```
3. Always store both the document ID and idString field with the same value
4. Use proper null safety checks when converting strings back to UUIDs

#### Option 2: Switch to Firebase Document IDs (Recommended)
To use Firebase's built-in ID generation:
1. Remove all UUID-related code
2. Let Firebase generate document IDs automatically
3. Update model classes to use String instead of UUID for IDs
4. Modify all repository methods to work with strings
5. Update UI components to handle string IDs

### Files Requiring Updates
If choosing Option 2 (Firebase IDs):
1. Model Classes:
   - `Staff.kt`: Change ID field from UUID to String
   - `Item.kt`: Change ID field from UUID to String
   - `CheckoutLog.kt`: Change ID fields from UUID to String

2. Repository Classes:
   - `FirebaseStaffRepository.kt`: Remove UUID conversions
   - `FirebaseRepository.kt`: Remove UUID conversions
   - Update all CRUD operations to work with strings

3. ViewModel Classes:
   - Update any code that generates or handles UUIDs

### Implementation Steps

1. **Decision Phase**:
   - Review both options
   - Choose the approach that best fits the project
   - Document the decision

2. **Implementation Phase**:
   - Make all changes in a separate branch
   - Update all affected files
   - Add proper error handling
   - Update tests

3. **Testing Phase**:
   - Test all CRUD operations
   - Verify ID consistency
   - Check null safety
   - Test error scenarios

4. **Deployment Phase**:
   - Review changes
   - Deploy updates
   - Monitor for issues

### Best Practices Moving Forward

- Maintain consistent ID handling throughout the codebase
- Document ID generation and storage approach
- Add proper validation for IDs
- Include error handling for ID conversion/generation
- Keep ID handling logic centralized

## Critical Database Issue Resolved

**Important Update (5-Day Resolution)**: We've fixed a critical database issue that affected ID handling across the application. The problem involved:

1. **ID Inconsistency**: The code was trying to use both Firebase-generated IDs and UUIDs in different places, causing mismatches between document IDs and the `idString` field.

2. **Root Cause**: 
   - Some operations were letting Firebase generate IDs but not storing them back in the document
   - Other operations were using UUIDs directly as document IDs without proper conversion
   - Inconsistent handling of IDs across different repositories

3. **Solution**:
   - Standardized on using UUIDs throughout the codebase
   - Ensured document IDs match their `idString` field
   - Fixed all repository operations to maintain ID consistency
   - Updated all CRUD operations to handle IDs correctly

This fix ensures that:
- All document IDs match their `idString` field
- References between documents (items, staff, checkouts) are properly maintained
- No more random UUID generation or ID mismatches

## Progress Update

**We've made massive progress in the last week!**

While the app isn't 100% finished, we've successfully:
- Completely migrated from Room database to Firebase Firestore
- Set up proper database collections and document structures
- Implemented all CRUD operations with Firebase endpoints
- Connected UI components to the Firebase backend
- Removed most legacy code and cleaned up the architecture

The most significant improvement is our understanding of the Firestore database structure - knowing exactly what fields are needed and how they should be formatted has solved most of our initial issues.

All database endpoints have been updated to reference Firestore collections directly rather than the previous REST API approach, which has simplified our code significantly.

## Technical Documentation

For developers working on this codebase:

- [Code Reference Guide](CODE_REFERENCE.md) - A comprehensive map of all Firebase endpoints, key files, and implementation details with line numbers
- [Firebase Database Guide](FIREBASE_DATABASE_GUIDE.md) - Guide to the database structure and field requirements
- [Migration Decision](MIGRATION_DECISION.md) - Why we chose Firebase over alternatives

The Code Reference Guide is particularly helpful as it provides exact file locations and line numbers for all Firebase collection references and key implementation details.

## Current Status

**What's Working:**
- Successfully migrated from Room database to Firebase Firestore
- Data can be added to the database through the app interface
- Items, staff, and checkouts are stored correctly in Firestore
- Basic CRUD operations function properly
- Archive system is operational

**What Needs Work:**
- Some UI elements don't update immediately after data changes
- Archived items occasionally display incorrectly
- Database document structure must be exactly as specified (see below)
- Error handling for network issues needs improvement
- Performance optimizations for larger inventories

The most critical requirement is that your database structure follows the exact format described in this README. The app is particularly sensitive to field names, data types, and the relationship between document IDs and the idString field.

## Development Roadmap

### Week 1: UI Improvements
- [ ] Fix UI update lag when data changes in Firestore
- [ ] Implement proper loading indicators
- [ ] Fix archive view display issues
- [ ] Test app with real-world data sets

### Week 2: Error Handling & Performance
- [ ] Add better network error messages
- [ ] Implement proper offline mode indicators
- [ ] Optimize Firestore queries for better performance
- [ ] Add retry mechanisms for failed operations

### Week 3: Cleanup & Polish
- [ ] Remove remaining legacy code
- [ ] Update remaining documentation
- [ ] Add final UI polish
- [ ] Final testing across different devices

## Firebase Migration

We've switched the app from using a local database to Firebase Firestore. This gives us:

- Real-time data updates
- No need for a separate backend server
- Works offline automatically
- Easier to scale as inventory grows

This migration required changing how we store and access data throughout the app. The main work was:
- Creating Firebase repositories to replace the old ones
- Setting up the right Firestore document structure
- Making sure the UI can handle real-time updates

See [MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md) for more details on what we did.

For an explanation of why we chose Firebase instead of Azure or other alternatives, see [MIGRATION_DECISION.md](MIGRATION_DECISION.md).

## Recent Cleanup

We've removed several unused files to keep the codebase clean:

- Old mock data files
- Unused repository implementations
- Test APIs that we don't need anymore

This makes the app easier to maintain and less confusing for new developers.

## Project Overview

The Inventory Management App is designed to streamline inventory tracking with:

- Firebase Firestore integration for real-time data storage and synchronization
- Barcode scanning for quick item identification 
- Staff management with checkout tracking
- Archive functionality to maintain historical records

## Migration to Firebase

This app has recently undergone a significant migration from a local Room database to Firebase Firestore. This transition brings several key benefits:

- **Real-time Data**: Instant synchronization across all devices
- **Scalability**: Better handling of growing inventory without performance issues
- **Simplified Architecture**: Eliminating complex sync logic and middleware
- **Reduced Maintenance**: No need for server management or custom API endpoints
- **Improved Reliability**: Built-in offline capabilities and conflict resolution

The migration process involved restructuring data models, implementing new repository patterns, and ensuring backward compatibility. While the core functionality is working, some aspects of the app are still being refined:

- Ongoing UI optimizations for improved user experience
- Enhanced error handling for edge cases
- Performance tuning for large datasets
- Additional automated testing

This represents a major architectural improvement that will make the app more maintainable and scalable in the long term.

## Key Documentation

This repository contains the following essential documents:

### 1. [Firebase Database Guide](FIREBASE_DATABASE_GUIDE.md)
**Critical document** explaining the required Firestore database structure and field requirements.

### 2. [Project Status Overview](PROJECT_STATUS.md)
Current implementation status, planned features, and release timeline.

## Project Architecture

The app follows a repository pattern architecture that abstracts data sources:

- **UI Layer**: Compose screens and ViewModels
- **Repository Layer**: Firebase Firestore repositories with local caching
- **Data Layer**: Models and mappers for Firestore documents
- **Firebase Layer**: Firestore and Firebase Storage integration

## Firestore Integration

The Firebase implementation provides:

1. **Real-time Synchronization**: Changes made on one device appear on all devices
2. **Offline Functionality**: App works without internet and syncs when connection returns
3. **Media Storage**: Firebase Storage for item and checkout photos
4. **Archive System**: Items can be archived rather than deleted

## Quick Setup Guide

1. **Configure Firebase**:
   - Follow the [Firebase Database Guide](FIREBASE_DATABASE_GUIDE.md) to set up your Firestore collections
   - Ensure proper document structure with all required fields

2. **Install the App**:
   - Deploy to Android devices
   - Verify Firestore connectivity through the status indicator

3. **Initial Data Entry**:
   - Add staff members first
   - Add inventory items
   - Create checkout records using existing staff and items

## Support and Troubleshooting

Common issues and troubleshooting steps are available in the [Firebase Database Guide](FIREBASE_DATABASE_GUIDE.md).

The app includes built-in debugging tools to help diagnose database issues.