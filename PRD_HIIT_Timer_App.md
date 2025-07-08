# Product Requirements Document (PRD)
## Android HIIT/EMOM Timer Application

### Document Information
- **Version**: 1.0
- **Date**: July 8, 2025
- **Product**: Android HIIT/EMOM Timer
- **Target Platform**: Android (API Level 24+)

---

## 1. Executive Summary

### 1.1 Product Vision
Create a minimalist, highly functional HIIT (High-Intensity Interval Training) and EMOM (Every Minute on the Minute) timer application for Android that provides precise timing controls, intuitive visual feedback, and seamless workout management capabilities.

### 1.2 Product Goals
- Deliver a distraction-free timer experience with precise timing accuracy
- Provide clear visual and audio feedback for interval transitions
- Enable users to create, save, and manage custom workout presets
- Ensure accessibility across different Android devices and screen sizes
- Maintain clean, modular code architecture for future extensibility

### 1.3 Target Audience
- **Primary**: Fitness enthusiasts who practice HIIT, CrossFit, or EMOM workouts
- **Secondary**: Personal trainers and fitness coaches
- **Tertiary**: Casual users seeking structured workout timing

### 1.4 Success Criteria
- Timing accuracy within ±50ms of configured intervals with millisecond precision display
- User retention rate >70% after 30 days
- Average session duration >15 minutes
- Preset creation rate >40% of active users
- Fullscreen mode engagement >85% of workout sessions for distraction-free experience

---

## 2. User Stories

### 2.1 Core Timer Functionality
**As a fitness enthusiast, I want to:**
- Configure work intervals in seconds so I can customize my workout intensity
- Configure rest intervals in seconds or disable them completely with a "No Rest" toggle
- Set the number of rounds or run unlimited cycles for flexible workout duration
- Pause and resume my workout without losing progress when interrupted
- See precise timing with millisecond accuracy to track my performance accurately

### 2.2 Visual & Audio Feedback
**As a user during workouts, I want to:**
- See clear visual indicators (green for work, red for rest) so I know the current interval type
- Hear distinct audio cues for interval transitions so I can focus on exercise form
- Control audio volume, sound selection, and mute settings via the hamburger menu
- View remaining time with millisecond precision and round progress at a glance

### 2.3 Workout Management
**As a regular user, I want to:**
- Save my favorite workout configurations as presets via the cog/gear configuration panel
- Name my exercises or add descriptions to track different movements
- Edit existing presets when my training evolves
- Delete outdated presets to keep my library organized
- Access timer configuration quickly through an intuitive cog/gear icon

### 2.4 Workout History & Progress Tracking
**As a fitness enthusiast, I want to:**
- View a history of my completed workouts to track my consistency
- See completion percentages to understand my workout performance
- Filter my workout history by date range, preset, or completion rate
- Review detailed statistics about my training patterns and progress

### 2.5 User Experience
**As a mobile user, I want to:**
- Use the app in both portrait and landscape orientations
- Switch between dark and light themes via the hamburger menu settings panel
- Navigate the interface intuitively with clearly separated configuration and settings areas
- Have the app work consistently across different Android devices
- See precise timing with millisecond accuracy during workouts
- Access timer configuration quickly via the cog/gear icon
- Access app settings conveniently via the hamburger menu
- Experience a distraction-free, immersive fullscreen workout environment without system UI interruptions

---

## 3. Functional Requirements

### 3.1 Timer Core (Priority: Critical)
**FR-001: Interval Configuration**
- Support work intervals from 5 seconds to 900 seconds (15 minutes)
- Support rest intervals from 1 second to 300 seconds (5 minutes) OR completely disabled via "No Rest" toggle
- Input validation to prevent invalid time configurations
- Default values: 20s work, 10s rest
- "No Rest" toggle switch to completely disable rest periods

**FR-002: Round Management**
- Support 1-99 finite rounds
- Support unlimited/continuous mode
- Display current round and total rounds (if finite)
- Automatic progression through rounds

**FR-003: Timer Controls**
- Start/Stop functionality
- Pause/Resume with state preservation
- Reset to initial configuration
- Timer accuracy within ±50ms tolerance

### 3.2 Visual Feedback System (Priority: High)
**FR-004: Full-Screen Visual Indicators**
- Animated green overlay/flash during work intervals
- Animated red overlay/flash during rest intervals
- Smooth transitions between states (300ms duration)
- Overlay opacity: 20-30% to maintain readability

**FR-005: Information Display**
- Large, readable countdown timer with millisecond precision (MM:SS.mmm format, minimum 48sp font size)
- Current interval type indicator ("WORK" / "REST")
- Round progress display
- Next interval preview (5 seconds before transition)

### 3.3 Audio System (Priority: High)
**FR-006: Audio Cues**
- Distinct sound for work interval start (energetic tone)
- Distinct sound for rest interval start (calming tone)
- 3-second countdown beeps before each interval
- Volume control integration with system volume

**FR-007: Audio Controls**
- Mute/unmute toggle (accessible via hamburger menu settings panel)
- Volume adjustment (0-100%)
- Audio preference persistence
- Sound selection options for different audio cues

### 3.4 Workout Management (Priority: Medium)
**FR-008: Preset System**
- Create new presets with custom names
- Save work/rest intervals and round configurations
- Edit existing presets
- Delete presets with confirmation dialog
- Maximum 50 presets per user

**FR-009: Exercise Descriptions**
- Optional exercise name field for each preset
- Optional description/notes field (max 200 characters)
- Display exercise info during workout

### 3.5 Workout History & Analytics (Priority: Medium)
**FR-010: Workout Session Tracking**
- Automatically record workout sessions when started
- Track session start time, end time, and duration
- Calculate completion percentage based on rounds completed vs. planned
- Store preset used, actual work/rest intervals, and total rounds completed
- Mark sessions as "completed" only if ≥70% of planned rounds are finished

**FR-011: History Management**
- Display workout history in chronological order (most recent first)
- Show completion status with visual indicators (✓ for ≥70%, ⚠ for <70%)
- Filter history by date range (last 7 days, 30 days, 3 months, all time)
- Filter by preset name or completion status
- Search functionality for specific workouts or dates

**FR-012: Progress Analytics**
- Calculate and display weekly/monthly completion rates
- Show total workouts completed vs. attempted
- Display average workout duration and consistency metrics
- Highlight personal records (longest session, most rounds completed)
- Export workout data as CSV for external analysis

**FR-013: Data Management**
- Automatic cleanup of history older than 1 year (configurable)
- Data backup and restore functionality
- Privacy controls for data sharing/export
- Maximum 1000 workout sessions stored locally

### 3.6 User Interface (Priority: High)
**FR-014: Theme Support**
- Light theme with high contrast
- Dark theme with OLED-friendly colors
- System theme detection and automatic switching
- Manual theme override option (accessible via hamburger menu settings panel)

**FR-015: Responsive Design**
- Portrait orientation support (primary)
- Landscape orientation support
- Adaptive layouts for screen sizes 4.5" to 7"
- Minimum touch target size: 48dp

**FR-016: Navigation and Settings UI**
- Cog/gear icon button for timer configuration modal/panel access
- Hamburger menu (☰) button for left-side sliding settings panel
- Timer configuration modal containing work time, rest time, rounds, and preset management
- Settings panel containing audio controls, theme settings, and other preferences
- Start button occupying 80% of control area width
- Reset button occupying 20% of control area width

**FR-017: Enhanced Timer Display**
- Millisecond precision timer display in MM:SS.mmm format
- High contrast display for improved readability during workouts
- Consistent formatting across all timer states (work, rest, paused)

**FR-018: Rest Timer Controls**
- "No Rest" toggle switch to completely disable rest periods
- Minimum rest time validation (1 second when rest is enabled)
- Clear visual indication when "No Rest" mode is active
- Seamless transition between work intervals when rest is disabled

**FR-019: True Fullscreen Mode**
- Implement true fullscreen mode that completely hides the Android system status bar
- Use Android's immersive fullscreen mode or edge-to-edge display to maximize screen real estate
- Maintain fullscreen mode during active workout sessions to prevent distractions
- Handle system UI visibility appropriately for different app states:
  - Active timer: Full immersive mode with hidden status bar
  - Paused timer: Maintain fullscreen but allow brief status bar access on swipe
  - Configuration screens: Optional status bar visibility for better navigation
  - Settings panels: Standard UI visibility for system integration
- Implement proper edge-to-edge content handling with appropriate padding for system gestures
- Ensure compatibility across different Android versions and device form factors

---

## 4. Technical Specifications

### 4.1 Architecture Requirements
**TS-001: Development Framework**
- Native Android development (Kotlin)
- Minimum SDK: API Level 24 (Android 7.0)
- Target SDK: Latest stable Android API
- Architecture: MVVM with Repository pattern

**TS-002: Core Components**
- TimerService: Background timer management
- AudioManager: Sound playback and control
- PresetRepository: Local data persistence
- WorkoutHistoryRepository: Session tracking and analytics
- ThemeManager: UI theme management
- FullscreenManager: System UI visibility and immersive mode control

### 4.3 Performance Requirements
**TS-003: Timing Accuracy**
- Timer precision: ±50ms tolerance
- Background execution capability
- CPU usage <5% during active timing
- Memory usage <50MB

**TS-004: Storage Requirements**
- Local SQLite database for presets and workout history
- SharedPreferences for user settings
- Maximum storage footprint: 25MB (including workout history)
- Data backup/restore capability
- Automatic data cleanup for sessions older than 1 year

### 4.5 Platform Integration
**TS-005: Android Features**
- Foreground service for background timing
- Notification support for background operation
- Wake lock management for screen control
- Audio focus management
- Immersive fullscreen mode implementation (API Level 19+)
- Edge-to-edge display support (API Level 29+)
- System UI visibility control and gesture navigation handling
- WindowInsetsController for modern fullscreen management (API Level 30+)

---

## 5. UI/UX Guidelines

### 5.1 Design Principles
- **Minimalism**: Clean interface with essential elements only
- **Clarity**: High contrast, readable typography
- **Accessibility**: Support for TalkBack and large text
- **Consistency**: Material Design 3 guidelines

### 5.2 Color Palette
**Light Theme:**
- Primary: #1976D2 (Blue)
- Work Indicator: #4CAF50 (Green)
- Rest Indicator: #F44336 (Red)
- Background: #FFFFFF
- Surface: #F5F5F5

**Dark Theme:**
- Primary: #90CAF9 (Light Blue)
- Work Indicator: #81C784 (Light Green)
- Rest Indicator: #E57373 (Light Red)
- Background: #121212
- Surface: #1E1E1E

### 5.3 Typography
- **Timer Display**: Roboto Mono, 72sp, Bold (with millisecond precision MM:SS.mmm)
- **Headers**: Roboto, 24sp, Medium
- **Body Text**: Roboto, 16sp, Regular
- **Buttons**: Roboto, 14sp, Medium

### 5.4 Layout Specifications
**Main Timer Screen (Fullscreen Mode):**
- True fullscreen implementation with hidden system status bar for immersive experience
- Full-screen timer display (center) with millisecond precision utilizing entire screen height
- Control buttons (bottom area): Start button (80% width), Reset button (20% width)
- Navigation elements (top area): Cog/gear icon and hamburger menu (☰)
- Edge-to-edge content with appropriate padding for system gesture areas
- Minimum 16dp margins from screen edges, accounting for gesture navigation zones
- Dynamic layout adjustment for different screen aspect ratios and notch/cutout areas

**Timer Configuration Modal/Panel:**
- Form-based layout with clear labels
- Work time, rest time, and rounds input fields with validation feedback
- "No Rest" toggle switch to disable rest periods completely
- Preset management functionality (create, edit, delete presets)
- Save/Cancel actions prominently displayed
- Scrollable content for smaller screens

**Settings Panel (Left-side sliding):**
- Audio settings section (volume control, sound selection, mute toggle)
- Theme settings section (light/dark mode, color preferences)
- Other app preferences and configurations
- Smooth slide-in/slide-out animation
- Overlay background when panel is open

**History Screen:**
- List view with workout sessions
- Completion indicators and timestamps
- Filter and search controls at top
- Pull-to-refresh functionality
- Empty state for new users

---

## 6. Success Metrics

### 6.1 Performance Metrics
- **Timer Accuracy**: 95% of intervals within ±50ms tolerance
- **App Launch Time**: <2 seconds cold start
- **Battery Usage**: <5% per hour of active use
- **Crash Rate**: <0.1% of sessions

### 6.2 User Engagement Metrics
- **Daily Active Users**: Target 1000+ within 3 months
- **Session Duration**: Average >15 minutes
- **Preset Creation Rate**: >40% of users create at least one preset
- **Workout Completion Rate**: >70% of started workouts completed (≥70% of rounds)
- **History Usage**: >30% of users view workout history weekly
- **User Retention**: 70% return after 7 days, 40% after 30 days

### 6.3 Quality Metrics
- **User Rating**: Maintain >4.2 stars on Google Play Store
- **Support Tickets**: <2% of users require support
- **Feature Adoption**: >60% use visual feedback, >80% use audio cues, >40% use "No Rest" toggle
- **UI Usability**: >90% of users successfully access timer configuration via cog icon
- **Settings Access**: >70% of users access settings via hamburger menu within first week
- **Fullscreen Performance**: >95% successful fullscreen mode transitions without UI glitches
- **Immersive Experience**: >80% of users report improved focus during fullscreen workouts

---

## 6.4. UI/UX Enhancement Specifications

### 6.4.1 High Priority Features
**✅ COMPLETED - Millisecond Precision Timer Display:**
- ✅ Format: MM:SS.mmm (e.g., "02:15.234") - Implemented as MM:SS.d for readability
- ✅ Maintains readability with consistent font sizing
- ✅ Updates at minimum 100ms intervals for smooth visual feedback
- ✅ Preserves precision during pause/resume operations
- ✅ All tests passing and build successful

**✅ COMPLETED - Settings UI Reorganization:**
- **✅ Cog/Gear Icon (⚙)**: Replaces current "preset" button
  - ✅ Opens timer configuration modal/panel
  - ✅ Contains: work time, rest time, rounds, preset management
  - ✅ Modal overlay with semi-transparent background
  - ✅ Smooth fade-in/fade-out animation (300ms)

- **✅ Hamburger Menu (☰)**: Replaces current "settings" button
  - ✅ Opens left-side sliding settings panel
  - ✅ Contains: audio settings, theme settings, app preferences
  - ✅ Slide animation from left edge (250ms duration)
  - ✅ Overlay background when panel is open

**✅ COMPLETED - Rest Timer Enhancements:**
- **✅ "No Rest" Toggle Switch**: Completely disables rest periods
  - ✅ Clear visual indication when active
  - ✅ Seamless work-to-work interval transitions
  - ✅ Validation prevents accidental activation
- **✅ Minimum Rest Time**: 5 seconds when rest is enabled (as per existing validation)
  - ✅ Input validation with user feedback
  - ✅ Clear error messaging for invalid values

**✅ COMPLETED - Button Layout Optimization:**
- **✅ Start Button**: 80% of control area width
  - ✅ Maintains prominence for primary action
  - ✅ Consistent touch target size (minimum 48dp height)
- **✅ Reset Button**: 20% of control area width
  - ✅ Secondary action with appropriate sizing
  - ✅ Proper spacing between buttons (8dp minimum)

**True Fullscreen Mode Implementation:**
- **Immersive Experience**: Complete removal of system status bar during workouts
  - Utilizes Android's immersive fullscreen mode (SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
  - Edge-to-edge display implementation for maximum screen utilization
  - Dynamic handling of device notches, cutouts, and curved screen edges
- **State-Based UI Visibility**:
  - Active timer: Full immersive mode with hidden system UI
  - Paused state: Maintains fullscreen with swipe-to-reveal system bar capability
  - Configuration/Settings: Contextual system UI visibility for better UX
- **Gesture Navigation Support**: Proper handling of Android 10+ gesture navigation
  - Appropriate content padding for gesture areas
  - Smooth transitions between fullscreen and standard modes
  - Compatibility with both button and gesture navigation systems

### 6.4.2 Audio Control Changes
- **✅ COMPLETED - Removed**: Quick toggle button for sound on/off from main interface
- **✅ COMPLETED - Relocated**: All audio controls moved to hamburger menu settings panel
- **✅ COMPLETED - Enhanced**: Volume control, sound selection, mute toggle in dedicated section

---

## 6.5 Implementation Progress Summary (July 8, 2025)

### ✅ COMPLETED HIGH PRIORITY FEATURES:

**Task 1: Millisecond Precision Timer Display**
- ✅ Implemented MM:SS.d format (deciseconds for readability)
- ✅ 100ms update intervals for smooth visual feedback
- ✅ Updated TimerManager countdown logic
- ✅ Added millisecond tracking to TimerStatus
- ✅ Updated tests and validation
- ✅ All tests passing, build successful

**Task 2: Settings UI Reorganization**
- ✅ Replaced "Settings" text with hamburger menu (☰) icon
- ✅ Replaced "Presets" text with cog/gear (⚙) icon
- ✅ Created sliding hamburger menu panel (250ms animation)
- ✅ Created timer configuration modal (300ms fade animation)
- ✅ Moved audio/theme settings to hamburger menu
- ✅ Timer configuration in cog/gear modal
- ✅ All tests passing, build successful

**Task 3: "No Rest" Toggle Switch**
- ✅ Added `noRest` boolean to TimerConfig
- ✅ Updated timer logic to skip rest intervals when enabled
- ✅ Added toggle switch in configuration UI
- ✅ Updated validation to handle "No Rest" mode
- ✅ Added comprehensive tests
- ✅ All tests passing, build successful

**Task 4: Button Layout Reorganization**
- ✅ Start button now occupies 80% of control area width
- ✅ Reset button now occupies 20% of control area width
- ✅ Proper spacing and touch targets maintained
- ✅ All tests passing, build successful

### 🔄 NEXT PRIORITY FEATURES:
- **True Fullscreen Mode Implementation** (FR-004)
- **Preset Management System** (FR-008)
- **Workout History & Analytics** (FR-011, FR-012)
- **Performance Optimization** (TS-003, TS-004)

---

## 7. Wireframe Descriptions

### 7.1 Main Timer Screen (True Fullscreen Mode)
```
┌─────────────────────────────────────┐ ← No system status bar (hidden)
│ [☰] HIIT Timer           [⚙]        │ ← Navigation (edge-to-edge)
│                                     │
│                                     │
│           WORK                      │ ← Interval Type
│                                     │
│         00:15.234                   │ ← Large Timer with ms
│                                     │ ← (Expanded vertical space)
│        Round 3 of 5                 │ ← Progress
│                                     │
│                                     │
│    [    START    ] [Reset]          │ ← Controls: 80%/20%
│                                     │ ← Gesture navigation area
└─────────────────────────────────────┘ ← Edge-to-edge bottom
```

### 7.2 Timer Configuration Modal
```
┌─────────────────────────────────────┐
│ [×] Timer Configuration             │
├─────────────────────────────────────┤
│ Work Time:    [20] seconds          │
│ Rest Time:    [10] seconds          │
│ No Rest:      [○] Toggle            │
│ Rounds:       [5 ] [∞ Unlimited]    │
│                                     │
│ ── Preset Management ──             │
│ Workout Name: [________________]    │
│ Exercise:     [________________]    │
│ Notes:        [________________]    │
│                                     │
│        [Cancel]    [Save]           │
└─────────────────────────────────────┘
```

### 7.3 Settings Panel (Left-side sliding)
```
┌─────────────────────────────────────┐
│ [×] Settings                        │
├─────────────────────────────────────┤
│ ── Audio Settings ──                │
│ Volume:       [████████░░] 80%      │
│ Sound:        [Beep ▼]              │
│ Mute:         [○] Toggle            │
│                                     │
│ ── Theme Settings ──                │
│ Theme:        [Auto ▼]              │
│ Dark Mode:    [●] Toggle            │
│                                     │
│ ── Other Settings ──                │
│ Keep Screen On: [●] Toggle          │
│                                     │
└─────────────────────────────────────┘
```

### 7.4 Workout History Screen
```
┌─────────────────────────────────────┐
│ [←] Workout History    [Filter] [⋮]  │
├─────────────────────────────────────┤
│ [Search workouts...]                │
│ [All Time ▼] [All Status ▼]         │
├─────────────────────────────────────┤
│ ✓ HIIT Cardio        85% │ 2h ago   │
│   20s work, 10s rest │ 17/20 rounds │
├─────────────────────────────────────┤
│ ⚠ Strength Circuit   60% │ 1d ago   │
│   45s work, 15s rest │ 6/10 rounds  │
├─────────────────────────────────────┤
│ ✓ Quick EMOM         100% │ 2d ago  │
│   60s work, 0s rest  │ 10/10 rounds │
├─────────────────────────────────────┤
│ ✓ Tabata Classic     90% │ 3d ago   │
│   20s work, 10s rest │ 18/20 rounds │
└─────────────────────────────────────┘
```

---

## 8. Implementation Phases

### Phase 1: Core Timer (Weeks 1-2)
- Basic timer functionality
- Start/pause/reset controls
- Work/rest interval configuration
- Simple UI without themes

### Phase 2: Visual & Audio (Weeks 3-4)
- Visual feedback system
- Audio cue implementation
- Theme support (light/dark)
- Responsive layouts

### Phase 3: Preset Management (Weeks 5-6)
- Preset creation and storage
- Exercise naming and descriptions
- Preset editing and deletion
- Data persistence

### Phase 4: Workout History & Analytics (Weeks 7-8)
- Workout session tracking implementation
- History display and filtering
- Progress analytics and statistics
- Data export functionality

### Phase 5: Polish & Testing (Weeks 9-10)
- Performance optimization
- Accessibility improvements
- Comprehensive testing
- Bug fixes and refinements

---

## 9. Development Testing and Build Protocol

### 9.1 Standardized Development Workflow
To ensure code quality, stability, and prevent regressions throughout the development process, the following protocol **MUST** be followed after completing each development task outlined in this PRD:

### 9.2 Post-Task Validation Protocol

**Step 1: Automated Testing**
- Execute the complete test suite to verify no regressions were introduced
- Run all unit tests, integration tests, and UI tests
- Ensure all tests pass with a 100% success rate
- Address any test failures before proceeding

**Step 2: Build Verification**
- Execute a complete build process to verify the application compiles successfully
- Perform both debug and release builds
- Resolve any compilation errors, warnings, or build failures
- Verify all dependencies are properly resolved

**Step 3: Development Pause Point**
- **MANDATORY PAUSE**: Stop all development work at this point
- Do not proceed to the next development task until manual validation is complete
- This pause allows for thorough manual testing and validation by the developer

**Step 4: Manual Testing and Validation**
- Perform manual testing of the implemented functionality
- Test the feature in both light and dark themes
- Verify functionality across different screen orientations
- Test edge cases and error conditions
- Validate user experience and interface responsiveness
- Confirm the implementation meets all acceptance criteria defined in the PRD

**Step 5: Approval to Continue**
- Development may only proceed to the next task after manual testing confirms:
  - The implementation works correctly as specified
  - No new bugs or regressions have been introduced
  - The user experience meets quality standards
  - All functional requirements are satisfied

### 9.3 Testing Commands and Procedures

**Automated Test Execution:**
```bash
# Run all unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run UI tests
./gradlew connectedDebugAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

**Build Verification:**
```bash
# Clean and build debug version
./gradlew clean assembleDebug

# Clean and build release version
./gradlew clean assembleRelease

# Run lint checks
./gradlew lint

# Generate APK for testing
./gradlew assembleDebug
```

### 9.4 Quality Gates
Each development task must pass the following quality gates before proceeding:
- ✅ All automated tests pass
- ✅ Application builds successfully (debug and release)
- ✅ No lint errors or critical warnings
- ✅ Manual testing validates functionality
- ✅ No performance regressions detected
- ✅ Code meets established quality standards

### 9.5 Documentation Requirements
For each completed task, document:
- Test results and coverage metrics
- Build verification outcomes
- Manual testing findings and validation results
- Any issues discovered and their resolutions
- Performance impact assessment

**Note**: This protocol is designed to maintain high code quality and prevent the accumulation of technical debt. Adherence to this workflow is essential for the successful delivery of the HIIT Timer Application.

---

## 10. Risk Assessment

### 10.1 Technical Risks
- **Timer Accuracy**: Mitigation through background service implementation
- **Battery Drain**: Optimize wake lock usage and background processing
- **Audio Conflicts**: Proper audio focus management

### 10.2 User Experience Risks
- **Complexity**: Maintain minimalist design principles
- **Performance**: Regular performance testing on various devices
- **Accessibility**: Early accessibility testing and compliance

---

## 11. Appendices

### 11.1 Competitive Analysis
- **Seconds Pro**: Strong timer accuracy, complex UI
- **Interval Timer**: Good preset system, outdated design
- **HIIT Workouts**: Comprehensive features, overwhelming interface

### 11.2 Technical Dependencies
- Android Jetpack Compose for UI
- Room Database for local storage
- WorkManager for background tasks
- ExoPlayer for audio playback

---

*End of Document*
