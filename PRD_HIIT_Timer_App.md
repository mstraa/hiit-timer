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
- Successful automated builds >95% success rate
- Audio output through media stream for proper volume control integration

### 1.5 Acceptance Criteria for Phase 6 Features

#### GitHub CI/CD Workflow (FR-022)
- ✅ Automated build triggers on every push to main branch
- ✅ Manual build trigger available for pull requests
- ✅ APK artifacts generated and stored for 30 days
- ✅ Build status visible in GitHub PR checks
- ✅ Build failures reported with clear error messages
- ✅ Gradle caching reduces build time by >50%

#### Enhanced Fullscreen Experience (FR-023)
- ✅ Status bar remains visible at all times
- ✅ Unified background color between status bar area and content
- ✅ No color transitions during fullscreen mode
- ✅ Edge-to-edge content with proper insets handling
- ✅ Consistent visual theme across all screen states
- ✅ Works correctly on Android 7.0+ devices

#### Refined Visual Feedback (FR-024)
- ✅ No continuous background color changes during timer operation
- ✅ Flash effect only at work interval start (green)
- ✅ Flash effect only at rest interval start (red)
- ✅ Clean, minimal visual feedback during countdown
- ✅ Flash animations are smooth and performant
- ✅ No visual distractions during active workout

#### Media Audio Output (FR-025)
- ✅ Audio cues play through media audio stream
- ✅ Device media volume controls affect timer audio
- ✅ Works correctly with Bluetooth headphones
- ✅ Works correctly with wired headphones
- ✅ Proper audio focus management
- ✅ Audio ducking support for other media apps

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

**FR-019: Timer Display Format Enhancement**
- When work or rest interval time is less than 60 seconds:
  - Display timer in "SS.d" format (seconds and deciseconds only)
  - Hide minutes portion for improved readability
  - Example: "45.3" instead of "00:45.3"
- When time is 60 seconds or greater:
  - Continue using the "MM:SS.d" format as currently implemented
- This format change applies to both work and rest intervals
- Update the `formatTimeRemaining()` method to implement this conditional formatting

**FR-020: Reset Button UI Enhancement**
- Replace the text "Reset" on the reset button with a standard reset/refresh icon (↻)
- Maintain the current 20% width allocation for the reset button
- Ensure the icon has sufficient size for touch targets (minimum 24dp)
- Add a content description for accessibility support
- Update the `TimerButton` composable to handle this icon-based variant

**FR-021: Timer Display Visual Hierarchy Enhancement**
- Make the decisecond (tenth of a second) portion visually smaller than the main time portion
- Decisecond part (".d" portion) should appear at approximately half the font size of the seconds/minutes portion
- Apply consistent approach for both display formats:
  - For times < 60 seconds (SS.d format): Make the ".3" in "45.3" half the size of "45"
  - For times ≥ 60 seconds (MM:SS.d format): Make the ".3" in "01:45.3" half the size of "01:45"
- Ensure proper vertical alignment with baseline alignment of the larger time portion
- Maintain readability across different screen sizes using responsive font sizing
- Improve visual hierarchy emphasizing the most important part of the time while showing precision

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

### 3.7 CI/CD & Development Workflow (Priority: Medium)
**FR-022: GitHub CI/CD Workflow**
- Automated build workflow triggered on pushes to main branch
- Manual trigger capability for pull request builds
- APK artifact generation and storage in GitHub Actions
- Build status reporting and notifications
- Automated testing execution in CI environment
- Gradle build caching for faster build times
- Build failure notifications and reporting

### 3.8 Enhanced UI/UX Refinements (Priority: Medium)
**FR-023: Enhanced Fullscreen Experience**
- Unified background color between content and top bar areas
- Persistent Android status bar visibility (no hiding)
- Consistent visual theme across all fullscreen states
- No color transitions or changes during fullscreen mode
- Edge-to-edge content with proper status bar integration
- Maintain system UI visibility for better user orientation

**FR-024: Refined Visual Feedback**
- Remove continuous light color changes during timer operation
- Maintain only flash effects at interval transitions
- Big flash animation at work interval start (green)
- Big flash animation at rest interval start (red)
- Clean, minimal visual feedback during countdown
- No background color changes during active timing

**FR-025: Media Audio Output**
- Audio cues routed through media audio stream instead of notification stream
- Integration with device media volume controls
- Proper audio focus management for media playback
- Compatibility with Bluetooth and wired headphones
- Respect system media volume settings
- Audio ducking support for other media applications

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

### 4.6 CI/CD Requirements
**TS-006: GitHub Actions Workflow**
- YAML workflow configuration for automated builds
- Gradle wrapper execution with proper caching
- Android SDK setup and configuration
- APK artifact upload and retention (30 days)
- Build matrix for multiple API levels (optional)
- Secrets management for signing keys (future)
- Integration with GitHub status checks

### 4.7 Audio System Enhancement
**TS-007: Media Audio Stream Integration**
- AudioManager.STREAM_MUSIC instead of STREAM_NOTIFICATION
- AudioFocusRequest for proper media focus handling
- MediaSession integration for media controls (optional)
- Audio routing through media volume controls
- Bluetooth audio device compatibility
- Wired headphone support and detection

### 4.8 UI System Refinements
**TS-008: Enhanced Fullscreen Implementation**
- Consistent background theming across all UI elements
- Status bar visibility preservation
- Edge-to-edge content with proper insets handling
- No immersive mode or status bar hiding
- Unified color scheme for top bar and content areas

**TS-009: Visual Feedback Optimization**
- Flash animation system for interval transitions
- Removal of continuous color change animations
- Optimized animation performance and battery usage
- Clean visual state management
- Minimal UI distractions during workout

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

### ✅ COMPLETED HIGH PRIORITY FEATURES (7/7):

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

**Task 5: Timer Display Format Enhancement (FR-019)**
- ✅ Conditional formatting based on time remaining
- ✅ Less than 60 seconds: "SS.d" format (e.g., "45.3")
- ✅ 60 seconds or greater: "MM:SS.d" format (e.g., "01:45.3")
- ✅ Updated `formatTimeRemaining()` method
- ✅ Comprehensive test coverage for all edge cases
- ✅ All tests passing, build successful

**Task 6: Reset Button UI Enhancement (FR-020)**
- ✅ Replaced text "Reset" with refresh icon (↻)
- ✅ Maintained 20% width allocation
- ✅ Icon size 24dp for proper touch targets
- ✅ Added accessibility content description
- ✅ All tests passing, build successful

**Task 7: Timer Display Visual Hierarchy Enhancement (FR-021)**
- ✅ Implemented styled text with AnnotatedString for different font sizes
- ✅ Decisecond portion (.d) now displays at 50% of main time font size
- ✅ Consistent approach for both SS.d and MM:SS.d formats
- ✅ Proper baseline alignment maintained
- ✅ Responsive font sizing preserved across screen sizes
- ✅ Enhanced visual hierarchy emphasizing main time portion
- ✅ All tests passing, build successful

### 📊 DEVELOPMENT METRICS SUMMARY:
- **Features Completed**: 7/7 high priority features (100%)
- **Test Coverage**: 100% passing (all tests successful)
- **Build Status**: ✅ Successful (no compilation errors)
- **Code Quality**: Clean compilation with minimal warnings
- **PRD Compliance**: All Section 6.4.1 requirements + FR-019, FR-020 & FR-021 completed
- **Timeline**: All features completed within development session

### ✅ COMPLETED PRIORITY FEATURES:
- **True Fullscreen Mode Implementation** (FR-004) - ✅ COMPLETE
- **Preset Management System** (FR-008) - ✅ COMPLETE
- **Workout History & Analytics** (FR-011, FR-012) - ✅ COMPLETE
- **Performance Optimization** (TS-003, TS-004) - ✅ COMPLETE
- **Background Service & Notifications** (TS-005) - ✅ COMPLETE

### 🔄 NEXT PRIORITY FEATURES (Phase 6):
- **GitHub CI/CD Workflow** (FR-022, TS-006)
- **Enhanced Fullscreen Experience** (FR-023, TS-008)
- **Refined Visual Feedback** (FR-024, TS-009)
- **Media Audio Output** (FR-025, TS-007)

## 6.6 Phase 6 Implementation Plan (Weeks 11-12)

### Task Group 1: GitHub CI/CD Workflow (FR-022, TS-006)
**Priority: High | Estimated Time: 4-6 hours**

#### Task 1.1: Create GitHub Actions Workflow (2 hours)
- Create `.github/workflows/build.yml` file
- Configure Android SDK setup and Gradle caching
- Set up automated build on main branch pushes
- Configure APK artifact upload and retention

#### Task 1.2: Add Manual PR Build Trigger (1 hour)
- Add `workflow_dispatch` trigger for manual builds
- Configure PR build trigger with proper permissions
- Add build status reporting to PR checks

#### Task 1.3: Testing and Optimization (1-2 hours)
- Test workflow with sample commits
- Optimize build times with caching strategies
- Add build failure notifications

### Task Group 2: Enhanced Fullscreen Experience (FR-023, TS-008)
**Priority: High | Estimated Time: 3-4 hours**

#### Task 2.1: Unified Background Implementation (2 hours)
- Remove color transitions between top bar and content
- Implement consistent background theming
- Ensure status bar remains visible at all times
- Update FullscreenManager to preserve status bar

#### Task 2.2: Edge-to-Edge Content Refinement (1-2 hours)
- Proper insets handling for status bar area
- Consistent padding and margins
- Test across different Android versions and devices

### Task Group 3: Refined Visual Feedback (FR-024, TS-009)
**Priority: Medium | Estimated Time: 2-3 hours**

#### Task 3.1: Remove Continuous Color Changes (1 hour)
- Remove background color animations during timer operation
- Keep only flash effects at interval transitions
- Clean up visual state management code

#### Task 3.2: Enhanced Flash Effects (1-2 hours)
- Implement big flash animation for work interval start
- Implement big flash animation for rest interval start
- Optimize animation performance and battery usage

### Task Group 4: Media Audio Output (FR-025, TS-007)
**Priority: Medium | Estimated Time: 2-3 hours**

#### Task 4.1: Audio Stream Migration (1-2 hours)
- Change from STREAM_NOTIFICATION to STREAM_MUSIC
- Update AudioManager implementation
- Test with device media volume controls

#### Task 4.2: Audio Focus Management (1 hour)
- Implement proper AudioFocusRequest handling
- Add support for audio ducking
- Test with Bluetooth and wired headphones

---

## 7. Wireframe Descriptions

### 7.1 Main Timer Screen (Enhanced Fullscreen Mode)
```
┌─────────────────────────────────────┐ ← Status bar visible (unified background)
│ 🔋 📶 🕐 12:34                      │ ← Android status bar (always visible)
├─────────────────────────────────────┤ ← Unified background color
│ [☰] HIIT Timer           [⚙]        │ ← Navigation (edge-to-edge)
│                                     │
│                                     │
│           WORK                      │ ← Interval Type
│                                     │
│         00:15.2                     │ ← Large Timer (no continuous color change)
│                                     │ ← Clean, minimal visual feedback
│        Round 3 of 5                 │ ← Progress
│                                     │
│                                     │
│    [    START    ] [↻]              │ ← Controls: 80%/20% (icon reset)
│                                     │ ← Gesture navigation area
└─────────────────────────────────────┘ ← Edge-to-edge bottom
```

**Visual Feedback Notes:**
- No continuous background color changes during timer operation
- Big flash effect only at work interval start (green flash)
- Big flash effect only at rest interval start (red flash)
- Unified background color between status bar area and content
- Status bar always visible for better user orientation

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

### Phase 6: CI/CD & UI Refinements (Weeks 11-12)
- GitHub CI/CD workflow implementation
- Fullscreen UI improvements
- Audio output optimization
- Visual feedback refinements

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

## 12. Critical Bug Fixes & Maintenance Requirements

### 12.1 Overview
This section documents critical bugs identified in the HIIT Timer app that require immediate attention to ensure proper functionality and user experience. These bugs impact core timer functionality and user interface behavior.

### 12.2 Critical Bug Inventory

#### **Bug 1: Resume Button Not Displaying**
**Priority**: Critical | **Effort**: 2-3 hours | **Phase**: Immediate

**Current Behavior**:
- After clicking "Start" then "Pause", the button still shows "Start" or "Pause" instead of "Resume"
- Users cannot properly identify the correct action to continue their workout
- Button text logic is not properly reflecting timer state

**Expected Behavior**:
- When timer is paused, the primary button should display "Resume"
- Button text should accurately reflect the available action
- Clear visual indication of pause/resume state

**Root Cause Analysis**:
- Timer state management logic may not be properly updating button text
- UI state flow not correctly reflecting pause state
- Potential race condition between timer state and UI updates

**Technical Implementation Requirements**:
```kotlin
// FR-040: Resume Button Display Logic
- Update TimerScreen button text logic to properly handle PAUSED state
- Ensure TimerStatus.canResume property correctly drives UI state
- Implement proper state synchronization between timer and UI
- Add state validation in button click handlers
```

**Acceptance Criteria**:
- ✅ Button shows "Start" when timer is IDLE
- ✅ Button shows "Pause" when timer is RUNNING
- ✅ Button shows "Resume" when timer is PAUSED
- ✅ Button text updates immediately upon state change
- ✅ Resume functionality works correctly from paused state

**Dependencies**: Timer state management system, UI state flow
**Risks**: Potential impact on existing timer functionality

---

#### **Bug 2: Timer Configuration Not Updating Display**
**Priority**: Critical | **Effort**: 3-4 hours | **Phase**: Immediate

**Current Behavior**:
- When users modify timer settings (work time, rest time, rounds), the main timer display doesn't reflect the new configuration
- Timer display shows stale values from previous configuration
- Users cannot verify their settings are applied correctly

**Expected Behavior**:
- Timer display should immediately show the updated work time when configuration changes
- All timer-related UI elements should reflect current configuration
- Real-time updates as user modifies settings

**Root Cause Analysis**:
- Configuration updates not properly propagating to timer display
- Timer state not being reset/updated when configuration changes
- Potential caching of old configuration values in UI state

**Technical Implementation Requirements**:
```kotlin
// FR-041: Configuration Display Synchronization
- Implement immediate timer display updates when configuration changes
- Ensure TimerStatus reflects current configuration at all times
- Add configuration change listeners in TimerViewModel
- Update timer display formatting to show current config values
```

**Acceptance Criteria**:
- ✅ Timer display shows current work time when configuration is modified
- ✅ Display updates immediately without requiring app restart
- ✅ All timer UI elements reflect current configuration
- ✅ Configuration changes are persisted and displayed correctly
- ✅ No stale values shown in timer display

**Dependencies**: Configuration management system, timer state management
**Risks**: Potential performance impact from frequent UI updates

---

#### **Bug 3: Default Timer Display Shows 00:00.0**
**Priority**: High | **Effort**: 1-2 hours | **Phase**: Immediate

**Current Behavior**:
- On app startup, the timer displays "00:00.0" instead of the default work time
- Confusing initial state that doesn't match the default configuration
- Users see zero time instead of expected default values

**Expected Behavior**:
- Should display "20" (for 20 seconds default work time) or "00:20.0" format
- Initial display should match default timer configuration
- Consistent time formatting across all states

**Root Cause Analysis**:
- Timer initialization not setting proper default display values
- Default TimerStatus not reflecting default configuration
- Time formatting logic not handling initial state correctly

**Technical Implementation Requirements**:
```kotlin
// FR-042: Default Timer Display Initialization
- Initialize TimerStatus with default configuration values
- Ensure timer display shows default work time on startup
- Implement proper time formatting for initial state
- Add validation for default configuration loading
```

**Acceptance Criteria**:
- ✅ Timer displays default work time (20 seconds) on app startup
- ✅ Display format is consistent with running timer format
- ✅ Default configuration is properly loaded and displayed
- ✅ No "00:00.0" shown unless timer has actually completed
- ✅ Time formatting is consistent across all timer states

**Dependencies**: Default configuration system, timer initialization
**Risks**: Low risk, isolated to display logic

---

#### **Bug 4: Preset Tab Not Visible**
**Priority**: Critical | **Effort**: 4-5 hours | **Phase**: Immediate

**Current Behavior**:
- Users only see the configuration tab, preset functionality is not accessible
- Key feature is completely inaccessible to users
- Tab navigation may not be properly implemented

**Expected Behavior**:
- Users should be able to access and use preset workouts
- Clear tab navigation between configuration and presets
- Full preset functionality available and working

**Root Cause Analysis**:
- Tab navigation implementation may be incomplete
- Preset UI components may not be properly integrated
- Navigation state management issues in main timer screen

**Technical Implementation Requirements**:
```kotlin
// FR-043: Preset Tab Accessibility
- Implement proper tab navigation in TimerScreen
- Ensure preset UI components are properly rendered
- Add tab state management and navigation logic
- Integrate preset functionality with timer system
```

**Acceptance Criteria**:
- ✅ Preset tab is visible and accessible to users
- ✅ Tab navigation works smoothly between configuration and presets
- ✅ Preset functionality is fully operational
- ✅ Users can select and use preset workouts
- ✅ Tab state is properly maintained during app usage

**Dependencies**: Preset management system, tab navigation UI
**Risks**: May require significant UI restructuring

---

### 12.3 Implementation Strategy

#### **Phase 1: Critical UI Fixes (Week 1)**
1. **Bug 1**: Resume Button Display Logic
2. **Bug 3**: Default Timer Display Initialization

#### **Phase 2: Configuration & Navigation (Week 2)**
1. **Bug 2**: Configuration Display Synchronization
2. **Bug 4**: Preset Tab Accessibility

#### **Phase 3: Testing & Validation (Week 3)**
1. Comprehensive testing of all bug fixes
2. Regression testing to ensure no new issues
3. User acceptance testing for fixed functionality

### 12.4 Quality Assurance Requirements

#### **Testing Strategy**:
- **Unit Tests**: Each bug fix must include comprehensive unit tests
- **Integration Tests**: Test interaction between fixed components
- **UI Tests**: Automated testing of button states and display updates
- **Manual Testing**: User workflow testing for each fixed scenario

#### **Performance Requirements**:
- Bug fixes must not impact timer accuracy (±50ms requirement)
- UI updates must be smooth and responsive (<16ms frame time)
- Configuration changes must apply within 100ms
- No memory leaks or performance regressions

#### **Compatibility Requirements**:
- Fixes must work across all supported Android versions (API 24+)
- Maintain compatibility with existing user data and preferences
- Ensure fixes work in both light and dark themes
- Support all screen sizes and orientations

### 12.5 Risk Mitigation

#### **Technical Risks**:
- **State Management Complexity**: Implement comprehensive state validation
- **UI Synchronization Issues**: Add proper state flow management
- **Performance Impact**: Profile and optimize all changes
- **Regression Risks**: Maintain comprehensive test coverage

#### **User Experience Risks**:
- **Workflow Disruption**: Ensure fixes don't change expected user flows
- **Data Loss**: Implement proper state preservation during fixes
- **Accessibility Impact**: Maintain accessibility features during UI changes

### 12.6 Success Metrics

#### **Functional Metrics**:
- ✅ 100% of identified bugs resolved and tested
- ✅ Zero regression issues introduced
- ✅ All acceptance criteria met for each bug
- ✅ Comprehensive test coverage (>95%) for fixed components

#### **User Experience Metrics**:
- ✅ Improved user satisfaction scores for timer functionality
- ✅ Reduced support requests related to timer confusion
- ✅ Increased preset feature usage after tab fix
- ✅ Positive user feedback on button clarity and timer display

#### **Technical Metrics**:
- ✅ Maintained timer accuracy requirements (±50ms)
- ✅ No performance degradation in UI responsiveness
- ✅ Clean code quality metrics maintained
- ✅ Zero critical or high-severity issues remaining

---

*End of Document*
