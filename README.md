# Inventory Management App

This Android application manages inventory items using Firebase Firestore as the backend database.

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