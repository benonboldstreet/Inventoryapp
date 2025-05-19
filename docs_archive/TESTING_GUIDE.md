# Inventory App Testing Guide

This guide explains how to test the Inventory App with mock data and how to prepare for moving to cloud integration.

## Testing with Mock Data

The app has been configured to work with mock data for testing without requiring a cloud backend. To test the app:

1. Make sure the app is using the mock services:
   - In `NetworkModule.kt`, the `useMockServices` flag should be set to `true`
   - This will cause the app to load data from `mock_data.json` in the assets folder

2. Run the app and you should see:
   - Inventory items from the mock data
   - Staff members from the mock data
   - Checkout logs showing item status

3. You can test features like:
   - Viewing items by category
   - Checking out and returning items
   - Viewing staff details
   - Searching for items by barcode

## Model Converters

The app uses two sets of models:

1. **Database Models** (`data.database.*`):
   - Used for local Room database storage
   - Have Room annotations like `@Entity`, `@PrimaryKey`, etc.
   - Located in `com.example.inventory.data.database` package

2. **Cloud Models** (`data.model.*`):
   - Used for cloud API operations
   - Simplified versions without Room annotations
   - Located in `com.example.inventory.data.model` package

The `ModelConverters.kt` file provides extension functions to convert between these two formats:
- `DbItem.toModel()` and `ModelItem.toEntity()`
- `DbStaff.toModel()` and `ModelStaff.toEntity()`
- `DbCheckoutLog.toModel()` and `ModelCheckoutLog.toEntity()`

## Preparing for Cloud Integration

When you're ready to connect to a real cloud backend:

1. Update the base URL in `NetworkModule.kt`:
   ```kotlin
   private const val BASE_URL = "https://your-actual-api.com/"
   ```

2. Set the mock services flag to false:
   ```kotlin
   private var useMockServices = false
   ```

3. Configure authentication in `AuthNetworkModule.kt`

4. Ensure your cloud API matches the endpoints defined in `API_ENDPOINTS.md`

5. Test the integration with a small subset of data first

## Troubleshooting

If you encounter issues:

1. **Build Errors**: Make sure you're using Kotlin 1.8.21 and AGP 8.1.4 as configured in the Gradle files

2. **Type Mismatches**: Use the converters in `ModelConverters.kt` to properly convert between model types

3. **Missing Classes**: If you see "Unresolved reference" errors, check that all required model classes exist in both the `database` and `model` packages

4. **Material Icon Issues**: The app uses Material Icons Extended, which should be included in the dependencies 