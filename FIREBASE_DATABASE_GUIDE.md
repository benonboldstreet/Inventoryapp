# Firebase Firestore Database Guide

This guide explains how to properly set up and maintain your Firebase Firestore database for the Inventory Management App.

## Quick Setup Checklist

- [ ] Create three collections: `items`, `staff`, `checkouts` (lowercase)
- [ ] For each document, copy its ID to the `idString` field
- [ ] Always use Boolean type (not String) for `isActive` field
- [ ] Include all required fields exactly as named in this guide
- [ ] Reference actual document IDs in checkout records
- [ ] Test database setup using the app's debug tools

Completing this checklist will ensure your database works correctly with the app.

## Migration Context

This app has recently migrated from a local Room database to Firebase Firestore. The migration represents a significant architectural change with numerous benefits but requires careful attention to database structure. The information in this guide is critical for proper app functionality during and after this transition period.

## Database Structure

The app uses three main collections in Firestore:

- `items` - Inventory items
- `staff` - Staff members
- `checkouts` - Checkout logs

## Critical Requirements

### Document IDs and idString Field

The most important requirement is that **each document's ID must exactly match its `idString` field**:

- When creating documents, either let Firestore auto-generate the ID or create your own
- Copy this exact ID to the `idString` field inside the document
- The app relies on this match to properly reference documents

## Collection Structure

### Items Collection

Each document in the `items` collection must have the following fields:

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| `idString` | String | Exact copy of document ID | Yes |
| `name` | String | Item name (e.g., "Dell Latitude 7490") | Yes |
| `category` | String | Category (e.g., "Laptop") | Yes |
| `type` | String | Type/Brand (e.g., "Dell") | Yes |
| `barcode` | String | Barcode identifier | Yes |
| `condition` | String | Item condition (e.g., "Good") | Yes |
| `status` | String | Status (e.g., "Available") | Yes |
| `description` | String | Item description | Yes |
| `photoPath` | String | Path to photo (can be null) | Yes |
| `isActive` | Boolean | Archive status (true = active) | Yes |
| `lastModified` | Timestamp | Last modification time | Yes |

### Staff Collection

Each document in the `staff` collection must have the following fields:

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| `idString` | String | Exact copy of document ID | Yes |
| `name` | String | Staff name | Yes |
| `department` | String | Department | Yes |
| `email` | String | Email address | Yes |
| `phone` | String | Phone number | Yes |
| `position` | String | Job position | Yes |
| `isActive` | Boolean | Archive status (true = active) | Yes |
| `lastModified` | Timestamp | Last modification time | Yes |

### Checkouts Collection

Each document in the `checkouts` collection must have the following fields:

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| `idString` | String | Exact copy of document ID | Yes |
| `itemIdString` | String | ID of an item document | Yes |
| `staffIdString` | String | ID of a staff document | Yes |
| `checkOutTime` | Timestamp | Checkout timestamp | Yes |
| `checkInTime` | Timestamp | Check-in timestamp (null if not returned) | Yes |
| `photoPath` | String | Path to checkout photo (can be null) | Yes |
| `status` | String | Status (e.g., "CHECKED_OUT") | Yes |
| `lastModified` | Timestamp | Last modification time | Yes |

## Step-by-Step Setup

1. **Create Collections**:
   - Create collections named exactly: `items`, `staff`, `checkouts` (all lowercase)

2. **Add Items**:
   - Create a new document in the `items` collection
   - Let Firestore auto-generate the document ID
   - Copy the generated ID into the `idString` field
   - Fill in all other required fields
   - Use Boolean type (not string) for `isActive` field

3. **Add Staff**:
   - Create a new document in the `staff` collection
   - Let Firestore auto-generate the document ID
   - Copy the generated ID into the `idString` field
   - Fill in all other required fields
   - Use Boolean type (not string) for `isActive` field

4. **Add Checkouts**:
   - Create a new document in the `checkouts` collection
   - Let Firestore auto-generate the document ID
   - Copy the generated ID into the `idString` field
   - For `itemIdString`, use an actual document ID from your `items` collection
   - For `staffIdString`, use an actual document ID from your `staff` collection
   - Set `checkOutTime` to current timestamp
   - Set `checkInTime` to null for active checkouts

## Common Issues and Troubleshooting

### Items Not Appearing in App

1. **Check Field Names**: Verify all required fields exist with exact spelling
2. **Check Field Types**: Especially `isActive` (must be Boolean)
3. **Verify idString Match**: The `idString` field must exactly match the document ID
4. **Check Required References**: For checkouts, verify `itemIdString` and `staffIdString` reference valid documents

### Archive Functionality Not Working

1. **Verify isActive Type**: Must be Boolean (true/false), not String ("true"/"false")
2. **Check Document References**: Ensure document IDs can be properly found

### Firebase Permissions Errors

If you encounter permission errors:
1. Verify your Firebase project has proper security rules
2. Make sure your app has proper authentication configured

## Testing the Database

The app includes debugging tools to help verify your database setup:

1. Use the "Debug: Check All Collections" button on the Items screen
2. Use the "Debug: Create Test Item" button to create a properly structured test item

This will help identify any issues with your database configuration. 

## Ongoing Development

The Firebase integration is functional but still undergoing refinement:

- The database structure described in this guide is the currently supported configuration
- Additional fields may be added in future updates, but the core fields will remain the same
- Performance optimizations are being implemented for handling larger datasets
- More sophisticated query caching is planned to improve offline performance

Developers are actively enhancing the Firebase integration to improve reliability and performance. If you encounter issues not addressed in this guide, please report them to the development team for investigation. 