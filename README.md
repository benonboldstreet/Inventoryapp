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

## Backend Integration

The app is designed to work with a backend service built on:
- Azure SQL Database
- .NET 8 API endpoints 
- Authentication and synchronization capabilities

*Note: Backend implementation is in progress by separate team.*

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