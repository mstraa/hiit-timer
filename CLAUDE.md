# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a production-ready HIIT (High-Intensity Interval Training) timer Android app built with Kotlin and Jetpack Compose. The app features comprehensive timer functionality, workout tracking, audio cues, and background operation capabilities.

## Build Commands

### Using the Build Script (Recommended)
```bash
# Build debug APK
./build.sh build

# Run unit tests
./build.sh test

# Run full pipeline (clean, test, build, lint)
./build.sh all

# Install to connected device
./build.sh install

# Run lint checks
./build.sh lint

# Clean project
./build.sh clean
```

### Using Gradle Directly
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Install to connected device
./gradlew installDebug

# Run lint
./gradlew lintDebug
```

## Testing

Run the comprehensive test suite:
```bash
./build.sh test
```

Test reports are available at: `app/build/reports/tests/testDebugUnitTest/index.html`

## Project Architecture

### Core Components

**TimerManager** (`app/src/main/java/com/hiittimer/app/timer/TimerManager.kt`)
- Central timer logic with precise countdown mechanics
- Session tracking for workout history
- Audio cue coordination
- Performance optimization features

**TimerService** (`app/src/main/java/com/hiittimer/app/service/TimerService.kt`)
- Foreground service for background operation
- Notification management
- Wake lock management
- Service lifecycle coordination

**Main Activity** (`app/src/main/java/com/hiittimer/app/MainActivity.kt`)
- Navigation between timer, config, and history screens
- Theme management
- ViewModel coordination

### Data Layer

**TimerState & TimerConfig** (`app/src/main/java/com/hiittimer/app/data/TimerState.kt`)
- Timer state management with enum states: IDLE, BEGIN, RUNNING, PAUSED, FINISHED
- Configuration data classes for workout settings
- Serializable for service communication

**Workout History** (`app/src/main/java/com/hiittimer/app/data/WorkoutSession.kt`)
- Session tracking and persistence
- Analytics and progress tracking
- Export functionality

**Preset Management** (`app/src/main/java/com/hiittimer/app/data/Preset.kt`)
- Custom workout configurations
- Save/load/edit functionality
- Exercise descriptions

### UI Layer

**TimerScreen** (`app/src/main/java/com/hiittimer/app/ui/timer/TimerScreen.kt`)
- Main timer interface with fullscreen support
- Visual feedback components
- Control buttons and configuration access

**Config & History Screens** (`app/src/main/java/com/hiittimer/app/ui/config/`, `app/src/main/java/com/hiittimer/app/ui/history/`)
- Settings management
- Workout history viewing and analytics
- Preset management interfaces

## Key Features

### Timer States
- **IDLE**: Initial state, ready to start
- **BEGIN**: 5-second countdown before workout starts
- **RUNNING**: Active workout with work/rest intervals
- **PAUSED**: Timer paused during RUNNING state
- **FINISHED**: All rounds completed

### Audio System
- Uses `STREAM_MUSIC` for proper media integration
- Audio focus management for background apps
- Configurable sound effects for interval transitions
- Volume control integration

### Background Operation
- Foreground service for uninterrupted timing
- Notification with timer status and controls
- Wake lock management for screen control
- Performance-optimized for battery usage

### Visual Feedback
- Color-coded intervals (green for work, red for rest)
- Flash effects for interval transitions
- Fullscreen mode with status bar management
- Responsive design for different screen sizes

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34
- **Audio**: Media3 ExoPlayer for sound effects
- **Testing**: JUnit 4, Mockito, Coroutines Test

## Common Development Patterns

### Timer Coordination
When working with timer functionality, remember that `TimerManager` and `TimerService` work together:
- UI binds to `TimerService` for state updates
- Service manages `TimerManager` lifecycle
- Configuration changes flow through service to manager

### State Management
Timer state transitions follow strict rules:
- Only start from IDLE or FINISHED states
- BEGIN state cannot be paused
- Session tracking occurs during state transitions

### Audio Integration
Audio cues are coordinated through `AudioManager`:
- Different sounds for work/rest intervals
- Audio focus management prevents conflicts
- Settings integration for volume control

### Performance Considerations
- Timer accuracy maintained at ±50ms
- Memory usage optimized for <50MB
- Battery usage minimized through smart wake lock management
- Performance monitoring through `PerformanceManager`

## Development Notes

- All major features are implemented and tested
- Comprehensive error handling with `ErrorHandler`
- Extensive logging through custom `Logger` utility
- CI/CD pipeline configured for automated testing
- Code follows Android development best practices