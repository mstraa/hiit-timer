# HIIT Timer Android App

A minimalist, highly functional HIIT (High-Intensity Interval Training) and EMOM (Every Minute on the Minute) timer application for Android.

## Features

### ✅ Phase 1 - Core Timer (Completed)
- ✅ **Basic Timer Functionality**: Work/rest interval timing with precise countdown
- ✅ **Timer Controls**: Start, pause, resume, and reset functionality
- ✅ **Configurable Intervals**: Set work time (5-900 seconds) and rest time (5-300 seconds)
- ✅ **Round Management**: Support for 1-99 finite rounds or unlimited mode
- ✅ **Visual Feedback**: Color-coded intervals (green for work, red for rest)
- ✅ **Clean UI**: Material Design 3 with large, readable timer display
- ✅ **Dark/Light Theme**: Automatic system theme detection

### ✅ Phase 2 - Enhanced Features (Completed)
- ✅ **Millisecond Precision**: Timer display with MM:SS.d format for precise timing
- ✅ **Settings UI Reorganization**: Cog/gear icon for timer config, hamburger menu for settings
- ✅ **"No Rest" Toggle**: Option to disable rest periods for continuous work intervals
- ✅ **True Fullscreen Mode**: Immersive fullscreen experience during workouts
- ✅ **Audio Controls**: Sound effects for interval transitions with volume control
- ✅ **Preset Management**: Create, save, edit, and delete custom workout presets
- ✅ **Exercise Descriptions**: Optional exercise names and descriptions for presets

### ✅ Phase 3 - Workout History & Analytics (Completed)
- ✅ **Workout Session Tracking**: Automatic recording of workout sessions
- ✅ **Completion Tracking**: Calculate completion percentage (≥70% = completed)
- ✅ **History Management**: View workout history with filtering and search
- ✅ **Progress Analytics**: Weekly/monthly completion rates and consistency metrics
- ✅ **Personal Records**: Track longest sessions and most rounds completed
- ✅ **Data Export**: Export workout data as CSV for external analysis

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
│   ├── TimerState.kt          # Timer data classes and enums
│   ├── Preset.kt              # Preset data classes and repository
│   ├── WorkoutSession.kt      # Workout history data classes and repository
│   └── PreferencesManager.kt  # User preferences and settings
├── timer/
│   └── TimerManager.kt        # Core timer logic with session tracking
├── audio/
│   └── AudioManager.kt        # Audio cues and sound management
├── ui/
│   ├── timer/
│   │   ├── TimerScreen.kt     # Main timer UI with fullscreen support
│   │   └── TimerViewModel.kt  # Timer screen ViewModel
│   ├── config/
│   │   └── ConfigScreen.kt    # Configuration UI
│   ├── history/
│   │   ├── WorkoutHistoryScreen.kt    # Workout history and analytics UI
│   │   └── WorkoutHistoryViewModel.kt # History screen ViewModel
│   ├── presets/
│   │   └── PresetViewModel.kt # Preset management ViewModel
│   ├── components/
│   │   ├── PresetComponents.kt        # Preset UI components
│   │   ├── WorkoutHistoryComponents.kt # History UI components
│   │   ├── WorkoutAnalyticsComponents.kt # Analytics UI components
│   │   ├── HamburgerMenu.kt           # Settings menu panel
│   │   └── TimerConfigModal.kt        # Timer configuration modal
│   ├── fullscreen/
│   │   └── FullscreenManager.kt       # Fullscreen mode management
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

### Basic Timer Operation
1. **Start Timer**: Tap the "Start" button to begin the workout
2. **Pause/Resume**: Tap "Pause" to pause the timer, then "Resume" to continue
3. **Reset**: Tap "Reset" to return to the initial state

### Configuration
4. **Timer Settings**: Tap the cog/gear icon (⚙) to configure work time, rest time, rounds, and presets
5. **App Settings**: Tap the hamburger menu (☰) to access audio settings, theme preferences, and navigation

### Preset Management
6. **Create Presets**: Save your favorite workout configurations with custom names and descriptions
7. **Use Presets**: Select from saved presets for quick workout setup
8. **Edit/Delete**: Modify or remove existing presets as needed

### Workout History
9. **View History**: Access workout history from the hamburger menu to see past sessions
10. **Analytics**: View completion rates, personal records, and consistency metrics
11. **Filter & Search**: Filter workouts by date, completion status, or search by name

## Default Configuration

- Work Time: 20 seconds
- Rest Time: 10 seconds
- Rounds: 5
- Mode: Finite rounds

## Upcoming Features (Future Phases)

### Phase 4 - Advanced Features
- Background operation with notifications
- Enhanced visual feedback with animations
- Wake lock management for screen control
- Data backup and restore functionality
- Performance optimizations

### Phase 5 - Polish & Testing
- Comprehensive accessibility improvements
- Advanced analytics and insights
- Social features and sharing
- Widget support for home screen
- Wear OS companion app

## License

This project is part of a development exercise following the PRD specifications.
