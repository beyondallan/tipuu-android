# Tipuu

A cross-platform mobile application with hardware integration.

## Features

- Bluetooth BLE data transmission
- Audio recording and real-time calls
- WiFi connectivity management
- Local storage for offline support
- NFC support (planned)

## Architecture

This project follows Clean Architecture with modular design:

```
lib/
├── core/           # Core infrastructure
│   ├── constants/  # App constants
│   ├── errors/     # Error handling
│   ├── network/    # API client
│   ├── storage/    # Local storage
│   ├── router/     # Navigation
│   ├── hardware/   # Hardware abstraction
│   └── utils/      # Utilities
├── features/       # Feature modules
│   ├── auth/       # Authentication
│   ├── home/       # Home screen
│   ├── bluetooth/  # Bluetooth BLE
│   └── audio/      # Audio recording/calls
└── shared/         # Shared components
    ├── widgets/    # Common widgets
    └── themes/     # App themes
```

## Getting Started

1. Install Flutter (3.24+)
2. Run `flutter pub get`
3. Run `flutter run`

## Tech Stack

- Flutter 3.24+
- Riverpod (state management)
- Dio (network)
- sqflite (local database)
- flutter_blue_plus (BLE)
- flutter_sound (audio)
- go_router (navigation)