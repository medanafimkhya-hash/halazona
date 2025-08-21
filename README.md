# Free VPN Demo - Android App

An Android VPN application built with Java that integrates with VPN Gate API to provide free VPN services.

## Features

- **Main Dashboard**: Clean UI with connection button, timer, and server info
- **VPN Server List**: Browse and select from available VPN Gate servers
- **IP Address Test**: Check your current IP address and location information
- **Connection Timer**: Track VPN connection duration
- **Server Information**: Display country, speed, and user count for each server
- **Notification Service**: Background VPN service with persistent notification

## Screenshots

The app includes three main screens:
1. **Main Screen**: Central connection button with server info and statistics
2. **Server List**: Browse available VPN servers with country flags and speeds
3. **IP Test**: View current IP address, location, and ISP information

## Technical Details

### Architecture
- **Language**: Java
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Dependencies**:
  - OkHttp for API calls
  - Gson for JSON parsing
  - RecyclerView for server lists
  - Material Design components

### API Integration
- **VPN Gate API**: `https://www.vpngate.net/api/iphone/`
- **IP Geolocation**: `https://ipapi.co/json/` (fallback to sample data)

### Key Components

1. **MainActivity**: Main dashboard with connection controls
2. **VpnLocationsActivity**: Server selection screen
3. **IpAddressTestActivity**: IP information display
4. **MyVpnService**: VPN service implementation
5. **VpnGateApiService**: API integration for server data
6. **VpnServer**: Data model for server information

### Permissions Required
- `INTERNET`: Network access
- `ACCESS_NETWORK_STATE`: Network state monitoring
- `BIND_VPN_SERVICE`: VPN service binding
- `FOREGROUND_SERVICE`: Background service operation

## Setup Instructions

1. Clone the repository
2. Open in Android Studio
3. Build and run on Android device/emulator
4. Grant VPN permission when prompted
5. Select a server and connect

## Important Notes

- This is a **demo application** for educational purposes
- The VPN implementation is simplified and not production-ready
- For a production VPN app, implement proper encryption and security measures
- Some features use sample data when API calls fail
- VPN Gate servers are community-provided and may vary in reliability

## Project Structure

```
app/src/main/
├── java/com/example/myvsouf/
│   ├── MainActivity.java
│   ├── VpnLocationsActivity.java
│   ├── IpAddressTestActivity.java
│   ├── MyVpnService.java
│   ├── VpnGateApiService.java
│   ├── VpnServer.java
│   └── VpnServerAdapter.java
├── res/
│   ├── layout/
│   ├── drawable/
│   └── values/
└── AndroidManifest.xml
```

## Development

This project demonstrates:
- Android VPN service implementation
- REST API integration
- Material Design UI principles
- Background service management
- RecyclerView with custom adapters
- Asynchronous network operations

Feel free to extend and modify the code for learning purposes!