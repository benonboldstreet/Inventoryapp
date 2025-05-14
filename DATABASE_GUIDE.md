# Database Guide

## Mock Database Implementation

We've added a mock JSON database to test cloud functionality without requiring a real database setup.

## Files Added

- `app/src/main/assets/mock_data.json` - Sample inventory data in JSON format
- `app/src/main/java/com/inventory/model/Item.kt` - Data model for inventory items
- `app/src/main/java/com/inventory/data/Repository.kt` - Interface defining database operations
- `app/src/main/java/com/inventory/data/MockRepository.kt` - Implementation that reads from JSON file

## How to Test

1. Run the app in debug mode
2. The app will automatically load data from the mock JSON file
3. Network operations are simulated with delays to test loading indicators

## Replacing with Real Database

When ready for a real database implementation:

1. Create a new class that implements the Repository interface
2. Update the RepositoryModule to use your real implementation
3. No changes needed to UI code that uses the repository 