# HIIT Timer Android App

A minimalist, highly functional HIIT (High-Intensity Interval Training) and EMOM (Every Minute on the Minute) timer application for Android.

## Features (Phase 1 - Core Timer)

- ✅ **Basic Timer Functionality**: Work/rest interval timing with precise countdown
- ✅ **Timer Controls**: Start, pause, resume, and reset functionality
- ✅ **Configurable Intervals**: Set work time (5-900 seconds) and rest time (5-300 seconds)
- ✅ **Round Management**: Support for 1-99 finite rounds or unlimited mode
- ✅ **Visual Feedback**: Color-coded intervals (green for work, red for rest)
- ✅ **Clean UI**: Material Design 3 with large, readable timer display
- ✅ **Dark/Light Theme**: Automatic system theme detection

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Minimum SDK**: API Level 24 (Android 7.0)
- **Target SDK**: API Level 34

## Project Structure

```
app/src/main/java/com/hiittimer/app/
├── data/
│   └── TimerState.kt          # Data classes and enums
├── timer/
│   └── TimerManager.kt        # Core timer logic
├── ui/
│   ├── timer/
│   │   ├── TimerScreen.kt     # Main timer UI
│   │   └── TimerViewModel.kt  # Timer screen ViewModel
│   ├── config/
│   │   └── ConfigScreen.kt    # Configuration UI
│   └── theme/
│       ├── Theme.kt           # Material Design theme
│       └── Type.kt            # Typography definitions
└── MainActivity.kt            # Main activity with navigation
```

## Building the Project

### Option 1: Using the Build Script (Recommended)
```bash
# Make the build script executable
chmod +x build.sh

# Build debug APK
./build.sh build

# Run tests
./build.sh test

# Run full build pipeline (clean, test, build, lint)
./build.sh all

# Install to connected device
./build.sh install

# Show help
./build.sh help
```

### Option 2: Using Gradle directly
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Install to connected device
./gradlew installDebug
```

### Option 3: Using Android Studio
1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Run the app on an emulator or physical device

## Testing

The project includes unit tests for the core timer functionality:

```bash
# Run unit tests
./build.sh test
# or
./gradlew testDebugUnitTest
```

Test reports are generated at: `app/build/reports/tests/testDebugUnitTest/index.html`

## Usage

1. **Start Timer**: Tap the "Start" button to begin the workout
2. **Pause/Resume**: Tap "Pause" to pause the timer, then "Resume" to continue
3. **Reset**: Tap "Reset" to return to the initial state
4. **Configure**: Tap "Settings" to adjust work time, rest time, and number of rounds

## Default Configuration

- Work Time: 20 seconds
- Rest Time: 10 seconds
- Rounds: 5
- Mode: Finite rounds

## Upcoming Features (Future Phases)

- Audio cues for interval transitions
- Preset management system
- Workout history and analytics
- Background operation with notifications
- Enhanced visual feedback with animations

## License

This project is part of a development exercise following the PRD specifications.
