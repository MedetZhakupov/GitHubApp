# GitHub Repositories Android App

A modern Android application that displays ABN AMRO's GitHub repositories with offline support and a
beautiful Material 3 UI.

## Features

### Part 1: Core Functionality

- **Repository List Screen**
    - Paginated list of ABN AMRO GitHub repositories
    - Each repository item displays:
        - Repository name
        - Owner's avatar image
        - Visibility status
        - Private/Public indicator
    - Pull-to-refresh functionality
    - Smooth pagination with automatic loading
    - Beautiful Material 3 design

- **Repository Detail Screen**
    - Complete repository information:
        - Name
        - Full name
        - Description
        - Owner's avatar image
        - Visibility status
        - Private/Public access level
    - CTA button to open repository in external browser
    - Clean, card-based layout

### Part 2: Offline Support

- **Local Database Caching**
    - All repository data is cached in Room database
    - App works offline with cached data
    - Automatic sync when connection is available
    - Data refresh strategy (5-minute cache validity)

## Architecture

The app follows **Clean Architecture** principles with **MVVM** pattern:

```
├── data/
│   ├── database/          # Room database, DAOs, entities
│   ├── model/             # Network DTOs
│   ├── network/           # Retrofit API interface
│   └── repository/        # Repository implementation
├── domain/
│   ├── model/             # Domain models
│   ├── repository/        # Repository interface
│   └── usecase/           # Business logic use cases
└── presentation/
    ├── detail/            # Detail screen
    ├── repositories/      # List screen
    └── navigation/        # Navigation setup
```

## Tech Stack

### Core

- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system

### Networking

- **Retrofit** - HTTP client
- **Moshi** - JSON serialization
- **OkHttp** - HTTP logging

### Database

- **Room** - Local data persistence
- **Coroutines Flow** - Reactive data streams

### Dependency Injection

- **Hilt** - Dependency injection framework

### Navigation

- **Navigation Compose** - In-app navigation

### Image Loading

- **Coil** - Image loading library

### Architecture Components

- **ViewModel** - UI state management
- **StateFlow** - State management
- **Coroutines** - Asynchronous programming

## Project Setup

### Prerequisites

- Android Studio Hedgehog or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36
- Kotlin 2.0.21

### Installation

1. Clone the repository:

```bash
git clone <repository-url>
cd GitHubApp
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device

## API Endpoint

The app uses GitHub's public API:

```
https://api.github.com/users/abnamrocoesd/repos
```

Parameters:

- `page`: Page number for pagination
- `per_page`: Number of items per page (default: 10)

## Key Features Implementation

### Pagination

- Automatic loading when scrolling near the end of the list
- Configurable items per page (currently 10)
- Smooth UX with loading indicators

### Offline Support

- Repository pattern with dual data sources (network + database)
- Automatic cache invalidation after 5 minutes
- Fallback to cached data on network errors
- Background sync when connection is restored

### Error Handling

- Graceful error states with retry options
- User-friendly error messages
- Automatic error dismissal

### UI/UX Highlights

- Material 3 design language
- Edge-to-edge display support
- Smooth animations and transitions
- Pull-to-refresh gesture
- Loading states and progress indicators
- Empty and error states

## Building for Release

To create a release build:

```bash
./gradlew assembleRelease
```

The APK will be generated in:

```
app/build/outputs/apk/release/app-release.apk
```

## Testing

The app follows a clean architecture which makes it easily testable:

- **Unit Tests**: ViewModels, Use Cases, Repository
- **Integration Tests**: Database operations, API calls
- **UI Tests**: Navigation, User interactions

Run tests:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Future Enhancements

- Search and filter repositories
- Sort options (name, date, stars)
- Repository statistics
- Dark theme support
- Additional repository details (stars, forks, issues)
- Share repository functionality

## License

This project is created for assessment purposes.

## Author

Developed as part of an Android assessment test for ABN AMRO.
