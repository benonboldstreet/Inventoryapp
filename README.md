# Inventory Management App

This Android application manages inventory items with both offline and cloud functionality.

## Project Overview

The Inventory Management App is designed to streamline inventory tracking with:

- Seamless cloud and local storage integration
- Barcode scanning for quick item identification 
- Staff management with checkout tracking
- Offline-first functionality that works without internet

## Key Documentation

This repository contains the following essential documents:

### 1. [Cloud Integration Locations](CLOUD_INTEGRATION_LOCATIONS.md)
**Key document** showing exact file and line locations for all cloud integration points.

### 2. [Cloud Migration Guide](CLOUD_MIGRATION_GUIDE.md)
Step-by-step guide for transitioning from local-only to cloud-integrated deployment.

### 3. [Project Status Overview](PROJECT_STATUS.md)
Current implementation status, planned features, and release timeline.

## Project Architecture

The app follows a repository pattern architecture that abstracts data sources:

- **UI Layer**: Compose screens and ViewModels
- **Repository Layer**: Abstract interfaces with cloud and local implementations
- **Data Layer**: Models, entities, and network models
- **API Layer**: Network services and authentication

## Cloud Integration

The cloud implementation provides:

1. **Real-time Synchronization**: Changes made on one device appear on all devices
2. **Offline Functionality**: App works without internet and syncs when connection returns
3. **Centralized Reporting**: Cloud dashboard for inventory analytics
4. **Enterprise Security**: Role-based access control and secure API endpoints

## Quick Setup Guide

1. **Configure Network Module**:
   - Set `useMockServices = false` in NetworkModule.kt
   - Update `BASE_URL` with your API endpoint

2. **Authentication**:
   - Configure authentication parameters in AuthNetworkModule.kt
   - Set up user roles in RoleBasedAccessControl.kt

3. **Deploy**:
   - Build and deploy to Android devices
   - Verify cloud connectivity through the status indicator

## Support and Maintenance

For technical questions or to access additional documentation, please contact the development team.

More detailed technical documentation is available in the `docs_archive` directory.