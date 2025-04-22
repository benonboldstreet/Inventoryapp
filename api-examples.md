# API Examples

This document provides example request and response payloads for the Inventory Management API.

## Items

### Create Item (POST /api/items)

Request:
```json
{
  "name": "ThinkPad X1 Carbon",
  "category": "Laptop",
  "type": "Lenovo",
  "barcode": "LP-2023-001",
  "condition": "Excellent",
  "status": "Available",
  "photoPath": "/storage/photos/laptops/thinkpad-x1.jpg"
}
```

Response:
```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "ThinkPad X1 Carbon",
  "category": "Laptop",
  "type": "Lenovo",
  "barcode": "LP-2023-001",
  "condition": "Excellent",
  "status": "Available",
  "photoPath": "/storage/photos/laptops/thinkpad-x1.jpg",
  "isActive": true,
  "lastModified": 1677721600000
}
```

### Get Items (GET /api/items)

Response:
```json
[
  {
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "name": "ThinkPad X1 Carbon",
    "category": "Laptop",
    "type": "Lenovo",
    "barcode": "LP-2023-001",
    "condition": "Excellent",
    "status": "Available",
    "photoPath": "/storage/photos/laptops/thinkpad-x1.jpg",
    "isActive": true,
    "lastModified": 1677721600000
  },
  {
    "id": "e01c6d74-3f37-4b7a-9a3c-7e23b819a1f5",
    "name": "iPhone 14 Pro",
    "category": "Mobile Phone",
    "type": "Apple",
    "barcode": "MP-2023-042",
    "condition": "Good",
    "status": "Checked Out",
    "photoPath": "/storage/photos/phones/iphone14-pro.jpg",
    "isActive": true,
    "lastModified": 1677808000000
  }
]
```

### Update Item Status (PATCH /api/items/{id}/status)

Request:
```json
{
  "status": "Checked Out"
}
```

Response:
```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "ThinkPad X1 Carbon",
  "category": "Laptop",
  "type": "Lenovo",
  "barcode": "LP-2023-001",
  "condition": "Excellent",
  "status": "Checked Out",
  "photoPath": "/storage/photos/laptops/thinkpad-x1.jpg",
  "isActive": true,
  "lastModified": 1677894400000
}
```

## Staff

### Create Staff (POST /api/staff)

Request:
```json
{
  "name": "Jane Smith",
  "department": "Engineering",
  "email": "jane.smith@example.com",
  "phone": "555-123-4567",
  "position": "Senior Developer"
}
```

Response:
```json
{
  "id": "2b612b9c-6d38-4b3e-92a4-6c2f6f87d892",
  "name": "Jane Smith",
  "department": "Engineering",
  "email": "jane.smith@example.com",
  "phone": "555-123-4567",
  "position": "Senior Developer",
  "isActive": true,
  "lastModified": 1677980800000
}
```

## Checkout Logs

### Create Checkout (POST /api/checkoutlogs)

Request:
```json
{
  "itemId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "staffId": "2b612b9c-6d38-4b3e-92a4-6c2f6f87d892",
  "photoPath": "/storage/photos/checkouts/carbon-checkout-20230304.jpg"
}
```

Response:
```json
{
  "id": "a7c86a15-95e7-4b2f-8c1a-c01a3d93422d",
  "itemId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "staffId": "2b612b9c-6d38-4b3e-92a4-6c2f6f87d892",
  "checkOutTime": 1677985200000,
  "checkInTime": null,
  "photoPath": "/storage/photos/checkouts/carbon-checkout-20230304.jpg",
  "lastModified": 1677985200000
}
```

### Check In Item (PATCH /api/checkoutlogs/{id}/checkin)

Request:
```json
{
  "photoPath": "/storage/photos/checkins/carbon-checkin-20230310.jpg"
}
```

Response:
```json
{
  "id": "a7c86a15-95e7-4b2f-8c1a-c01a3d93422d",
  "itemId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "staffId": "2b612b9c-6d38-4b3e-92a4-6c2f6f87d892",
  "checkOutTime": 1677985200000,
  "checkInTime": 1678496400000,
  "photoPath": "/storage/photos/checkouts/carbon-checkout-20230304.jpg",
  "checkInPhotoPath": "/storage/photos/checkins/carbon-checkin-20230310.jpg",
  "lastModified": 1678496400000
}
``` 