# Inventory Management App

An Android mobile application for tracking and managing inventory items with check-in/check-out functionality.

## Features

- **Item Management**: Add, edit, archive, and manage inventory items
- **Staff Management**: Track staff members who can check out items
- **Check-in/Check-out System**: Log when items are checked out and returned
- **Photo Documentation**: Capture photos of items during checkout for condition tracking
- **Categorization**: Organize items by category with custom category support
- **Search & Filter**: Find items quickly with search and status filtering
- **Barcode Scanning**: Scan barcodes to quickly look up or add items
- **Offline Support**: Use the app without constant internet connection
- **Archiving**: Archive items and staff instead of deleting them to preserve history

## Technical Details

- **Platform**: Android (Kotlin)
- **Architecture**: MVVM with Repository pattern
- **UI**: Jetpack Compose
- **Database**: Room database locally, with planned Azure SQL integration
- **API**: REST API integration (under development)

## Required Software & Tools

### Frontend Development
- Android Studio (latest version)
- JDK 11 or newer
- Kotlin 1.7+
- Gradle 7.0+
- Git for version control

### Backend Development
- Visual Studio 2022 with .NET 8 SDK
- SQL Server Management Studio
- Azure account with subscription
- Azure Data Studio
- Postman (for API testing)

## Backend Integration

The app is designed to work with a backend service built on:
- Azure SQL Database
- .NET 8 API endpoints 
- Authentication and synchronization capabilities

*Note: Backend implementation is in progress by separate team.*

## Integration Steps

### Backend Setup
1. Create Azure SQL Database with these tables:
   ```sql
   CREATE TABLE items (
       id UNIQUEIDENTIFIER PRIMARY KEY,
       name NVARCHAR(255) NOT NULL,
       category NVARCHAR(100) NOT NULL,
       type NVARCHAR(100) NOT NULL,
       barcode NVARCHAR(100) NOT NULL,
       condition NVARCHAR(50) NOT NULL,
       status NVARCHAR(50) NOT NULL,
       photoPath NVARCHAR(MAX),
       isActive BIT DEFAULT 1,
       lastModified BIGINT
   );

   CREATE TABLE staff (
       id UNIQUEIDENTIFIER PRIMARY KEY,
       name NVARCHAR(255) NOT NULL,
       department NVARCHAR(100) NOT NULL,
       email NVARCHAR(255) NOT NULL,
       phone NVARCHAR(50) NOT NULL,
       position NVARCHAR(100) NOT NULL,
       isActive BIT DEFAULT 1,
       lastModified BIGINT
   );

   CREATE TABLE checkout_logs (
       id UNIQUEIDENTIFIER PRIMARY KEY,
       itemId UNIQUEIDENTIFIER NOT NULL,
       staffId UNIQUEIDENTIFIER NOT NULL,
       checkOutTime BIGINT NOT NULL,
       checkInTime BIGINT,
       photoPath NVARCHAR(MAX),
       lastModified BIGINT,
       FOREIGN KEY (itemId) REFERENCES items(id),
       FOREIGN KEY (staffId) REFERENCES staff(id)
   );
   ```

2. Implement these API endpoints in a .NET 8 Web API project:
   - **Items API**
     - `GET /api/items` – Get all items
     - `GET /api/items/{id}` – Get item by ID
     - `GET /api/items/barcode/{barcode}` – Get item by barcode
     - `GET /api/items/category/{category}` – Get items by category
     - `GET /api/items/status/{status}` – Get items by status
     - `GET /api/items/categories` – Get all unique categories
     - `POST /api/items` – Add new item
     - `PUT /api/items/{id}` – Update item
     - `PATCH /api/items/{id}/status` – Update item status
     - `PATCH /api/items/{id}/archive` – Archive item
     - `PATCH /api/items/{id}/unarchive` – Unarchive item

   - **Staff API**
     - `GET /api/staff` – Get all staff
     - `GET /api/staff/{id}` – Get staff by ID
     - `POST /api/staff` – Add new staff
     - `PUT /api/staff/{id}` – Update staff
     - `PATCH /api/staff/{id}/archive` – Archive staff
     - `PATCH /api/staff/{id}/unarchive` – Unarchive staff

   - **Checkout Logs API**
     - `GET /api/checkoutlogs` – Get all checkout logs
     - `GET /api/checkoutlogs/{id}` – Get log by ID
     - `GET /api/checkoutlogs/item/{itemId}` – Get logs by item ID
     - `GET /api/checkoutlogs/staff/{staffId}` – Get logs by staff ID
     - `GET /api/checkoutlogs/current` – Get all active checkouts
     - `POST /api/checkoutlogs` – Create new checkout
     - `PATCH /api/checkoutlogs/{id}/checkin` – Check in an item

3. Set up authentication and user management (optional for first phase)

4. Deploy the API to Azure App Service and provide the API URL to the frontend developer

### Frontend Integration
1. Update network configuration in `NetworkModule.kt` with API URL
2. Implement API service interfaces
3. Modify repositories to use network services
4. Add offline synchronization
5. Test with backend services

## Installation

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run on emulator or physical device

## Setup for Development

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 21+
- Gradle 7.0+

### Build Instructions
```
./gradlew assembleDebug
```

## Screenshots

[Screenshots to be added]

## Future Enhancements

- Multi-device synchronization 
- Advanced reporting and analytics
- QR code generation for items
- Automated notifications for overdue items

## License

[License to be determined] 