# TODO for Next Session

## Planned Features and Improvements

### 1. Config Recap Display
- **Task**: Add a recap of the config when timer is not started
- **Description**: Display current settings summary on timer screen so users can see their configuration at a glance
- **Priority**: Medium

### 2. Basic Preset Functionality
- **Task**: Add a preset icon under the cog and implement basic preset functionality
- **Description**: 
  - Add preset icon/button in the UI (under the settings cog)
  - Implement create, edit, delete preset functionality
  - Include all configuration fields with a title for each preset
  - When applied, change the current configuration to the preset configuration
- **Priority**: High

### 3. Complex Presets with Multiple Round Groups
- **Task**: Add complex preset support with multiple groups of rounds
- **Description**:
  - Add support for multiple groups of rounds within a single preset
  - Add title and description for each round group
  - Rework the app to display these new informations throughout the UI
  - Update timer logic to handle complex workout structures
- **Priority**: High

### 4. Landscape Layout Improvements
- **Task**: Rework the landscape layout for better usability
- **Description**: 
  - Move the state display to the top of the screen
  - Make the timer display bigger and more prominent
  - Optimize layout for landscape orientation
- **Priority**: Medium

### 5. F-Droid Compliance Tasks
- **Task**: Make app ready for F-Droid submission
- **Description**: 
  - Add LICENSE file to repository root with FSF-approved open-source license (GPL-3.0, MIT, Apache-2.0, etc.)
  - Create F-Droid metadata structure with fastlane directory and app description
  - Add screenshots for F-Droid store listing in fastlane/metadata/android/en-US/images/
  - Create app description files for F-Droid (short_description.txt, full_description.txt)
  - Verify build reproducibility by testing build process on clean environment
  - Add clear statement about no tracking/analytics in README.md
  - Submit app to F-Droid by creating merge request on fdroiddata repository
- **Priority**: Low (especially LICENSE file)
- **Status**: App is 95% compliant - only missing LICENSE file is blocking submission

## Notes
Choose any of these tasks to work on during our next session. Tasks are organized by priority but can be tackled in any order based on your preferences.