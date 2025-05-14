# Inventory Cloud (v2.0.0)

## Overview
Inventory Cloud is a cloud-connected version of the original Inventory app. Unlike the original app which used local storage, this version connects to Azure/cloud services for all data operations.

## Version Information
- **Version:** 2.0.0
- **Mode:** Cloud
- **Backend:** Azure Cloud Services
- **Original Repo:** [Local Version](https://github.com/benonboldstreet/Inventoryapp/tree/main)

## Repository Structure
This repository contains multiple versions of the Inventory application:

| Branch | Purpose |
|--------|---------|
| `main` | Original local-only implementation with Room database |
| `cloud-integration-labels` | Original code with cloud endpoint labels (preparation) |
| `cloud-implementation` | Full cloud implementation (current version) |

## Cloud Architecture Overview

### Data Flow
1. User interacts with the app UI
2. ViewModels call repository methods
3. Cloud repositories make API calls to Azure backend
4. Azure services handle data persistence and business logic
5. Results are returned to the app for display

### Key Components

#### API Layer (`app/src/main/java/com/example/inventory/api/`)
- **NetworkModule**: Central configuration for API services
- **DTOs**: Data Transfer Objects for API communication
- **API Services**: Retrofit interfaces defining all cloud endpoints

#### Repository Layer (`app/src/main/java/com/example/inventory/data/repository/`)
- **CloudItemRepository**: Handles inventory item operations
- **CloudStaffRepository**: Manages staff member data
- **CloudCheckoutRepository**: Controls checkout/checkin operations

#### Dependency Injection (`app/src/main/java/com/example/inventory/data/AppContainer.kt`)
- Provides cloud repository implementations to ViewModels

## Setup Instructions

### 1. Azure Resources Required
- **Azure App Service**: Hosts the API backend
- **Azure SQL Database**: Stores inventory data
- **Azure Blob Storage**: Stores item/checkout photos
- **Azure Active Directory**: Handles authentication

### 2. Configuration Steps
1. Set up the required Azure resources above
2. Update the `BASE_URL` in `NetworkModule.kt` with your Azure endpoint
3. Add authentication details (update the TODO in NetworkModule)
4. Configure error handling and monitoring as needed

### 3. Cloud API Endpoints
The app expects the following API endpoints to be available:

#### Item Endpoints
- `GET api/items` - List all items
- `GET api/items/{id}` - Get item by ID
- `GET api/items/barcode/{barcode}` - Get item by barcode
- `POST api/items` - Create new item
- `PUT api/items/{id}` - Update existing item
- `PATCH api/items/{id}/status` - Update item status
- `PATCH api/items/{id}/archive` - Archive item
- `PATCH api/items/{id}/unarchive` - Unarchive item

#### Staff Endpoints
- `GET api/staff` - List all staff
- `GET api/staff/{id}` - Get staff by ID
- `POST api/staff` - Create new staff
- `PUT api/staff/{id}` - Update existing staff
- `PATCH api/staff/{id}/archive` - Archive staff
- `PATCH api/staff/{id}/unarchive` - Unarchive staff

#### Checkout Endpoints
- `GET api/checkoutlogs` - List all checkout logs
- `GET api/checkoutlogs/item/{itemId}` - Get checkouts by item
- `GET api/checkoutlogs/staff/{staffId}` - Get checkouts by staff
- `GET api/checkoutlogs/current` - Get current checkouts
- `POST api/checkoutlogs` - Create checkout
- `PATCH api/checkoutlogs/{id}/checkin` - Check in item

## Key Differences from Local Version

1. **Storage**:
   - Local: Room database on device
   - Cloud: Azure SQL Database + Blob Storage

2. **Data Access**:
   - Local: Synchronous database operations
   - Cloud: Asynchronous API calls with error handling

3. **Authentication**:
   - Local: None
   - Cloud: Azure AD integration (to be implemented)

4. **Offline Operation**:
   - Local: Full functionality offline
   - Cloud: Limited functionality, requires network connection

## Implementation Notes

### Error Handling
- All cloud operations include try/catch blocks
- Empty results returned on network failure
- TODO markers indicate where proper error handling should be implemented

### Future Improvements
- Implement offline caching with sync
- Add user authentication
- Add network status indicators in UI
- Implement retry mechanisms for failed operations

## Contact Information
For questions or support, please contact me located on Mobile of email at work  