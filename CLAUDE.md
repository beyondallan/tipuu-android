# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Install dependencies
flutter pub get

# Generate freezed/json_serializable code (required after modifying entities)
flutter pub run build_runner build --delete-conflicting-outputs

# Run the app
flutter run

# Run on specific device
flutter run -d <device_id>

# List available devices
flutter devices

# Build release APK (Android)
flutter build apk --release

# Build release iOS
flutter build ios --release
```

## Test & Lint Commands

```bash
# Run all tests
flutter test

# Run specific test file
flutter test test/path/to/test.dart

# Run tests with coverage
flutter test --coverage

# Analyze code
flutter analyze

# Fix lint issues where possible
dart fix --apply
```

## Architecture

This project uses **Clean Architecture** with three layers:

```
┌─────────────────────────────────────────────────────────────┐
│  Presentation Layer (Pages, Widgets, Providers)              │
│  → Uses Riverpod for state management                        │
└─────────────────────────────┬───────────────────────────────┘
                              │ calls UseCases
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  Domain Layer (Entities, Repository Interfaces, UseCases)    │
│  → Pure Dart, no Flutter dependencies                        │
└─────────────────────────────┬───────────────────────────────┘
                              │ implements Repository interfaces
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  Data Layer (Services, DataSources, Repository Impls)        │
│  → Handles API, database, hardware                           │
└─────────────────────────────────────────────────────────────┘
```

### Key Patterns

**Dependency Injection**: All dependencies are provided via Riverpod. Override providers in `main.dart`:
```dart
ProviderScope(
  overrides: [
    sharedPreferencesProvider.overrideWithValue(prefs),
    databaseProvider.overrideWithValue(db),
  ],
  child: const App(),
)
```

**Error Handling**: Exceptions flow through three stages:
1. Data layer throws `DioException`, `SQLiteException`, etc.
2. Convert to `AppException` using `toAppException()` extension
3. Map to `Failure` for domain layer (no technical details exposed to UI)

**Hardware Abstraction**: All hardware services implement `HardwareManager` interface:
```dart
abstract class HardwareManager {
  String get hardwareType;
  Stream<HardwareState> get state;
  Future<Result<void>> initialize();
  Future<Result<void>> release();
}
```

### Module Structure

Each feature module follows this structure:
```
features/<name>/
├── domain/
│   ├── entities/        # Business objects (freezed classes)
│   ├── repositories/    # Repository interfaces (abstract classes)
│   └── usecases/        # Business logic operations
├── data/                # Repository implementations, data sources
└── presentation/
    ├── providers/       # Riverpod StateNotifiers
    └── pages/           # UI pages
```

**Module Dependency Rule**: Feature modules never depend on other feature modules directly. They communicate through `core/` layer providers.

### Storage Layers

| Layer | Technology | Use Case |
|-------|------------|----------|
| KV Store | `shared_preferences` | User settings, tokens |
| Database | `sqflite` | Structured data, API cache |
| Files | `path_provider` | Audio files, media |
| Secure | `flutter_secure_storage` | Sensitive tokens |

### Code Generation

Entities use `freezed` for immutable classes with `copyWith`. After modifying any entity:
```dart
// Entity file
@freezed
class User with _$User {
  const factory User({...}) = _User;
  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
}

// Run generator
flutter pub run build_runner build --delete-conflicting-outputs
```

Generated files (`*.freezed.dart`, `*.g.dart`) are excluded from analysis via `analysis_options.yaml`.

## Project Status

Core infrastructure and domain interfaces are complete. Pending:
- Data layer implementations for each feature
- Hardware service implementations (BLE, Audio)
- API client generation from server documentation

See `docs/概要设计.md` for full design specification.