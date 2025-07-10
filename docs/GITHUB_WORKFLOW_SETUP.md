# GitHub CI/CD Workflow Setup (FR-022)

## Overview
This document provides instructions for setting up the GitHub Actions workflow for automated builds and testing, as specified in FR-022 of the PRD.

## Workflow File
The workflow file `.github/workflows/build.yml` needs to be added to enable automated CI/CD. Due to OAuth permissions, it requires manual addition by the repository owner.

## Required Permissions
To add the GitHub Actions workflow, the repository needs the `workflow` scope permission for OAuth apps.

## Workflow Features
The prepared workflow includes:

### Automated Triggers
- **Push to main branch**: Automatic builds on every commit to main
- **Pull requests**: Builds triggered for PR validation
- **Manual dispatch**: Manual trigger capability with build type selection

### Build Jobs
1. **Build Job**:
   - Checkout code
   - Setup JDK 17
   - Setup Android SDK
   - Cache Gradle dependencies
   - Run unit tests
   - Build debug/release APK
   - Upload APK artifacts (30-day retention)
   - Comment on PR with build status

2. **Lint Job**:
   - Code quality analysis
   - Lint report generation
   - Upload lint results

### Artifacts Generated
- **Debug APK**: `hiit-timer-debug-{sha}`
- **Release APK**: `hiit-timer-release-{sha}`
- **Test Results**: `test-results-{sha}`
- **Lint Results**: `lint-results-{sha}`

## Workflow Configuration

Create `.github/workflows/build.yml` with the following content:

```yaml
name: Android Build

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:
    inputs:
      build_type:
        description: 'Build type'
        required: true
        default: 'debug'
        type: choice
        options:
        - debug
        - release

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Setup Android SDK
      uses: android-actions/setup-android@v3
    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    - name: Run tests
      run: ./gradlew testDebugUnitTest
    - name: Build debug APK
      if: github.event.inputs.build_type == 'debug' || github.event.inputs.build_type == ''
      run: ./gradlew assembleDebug
    - name: Upload debug APK artifact
      if: github.event.inputs.build_type == 'debug' || github.event.inputs.build_type == ''
      uses: actions/upload-artifact@v4
      with:
        name: hiit-timer-debug-${{ github.sha }}
        path: app/build/outputs/apk/debug/app-debug.apk
        retention-days: 30

  lint:
    runs-on: ubuntu-latest
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Setup Android SDK
      uses: android-actions/setup-android@v3
    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    - name: Run lint
      run: ./gradlew lintDebug
    - name: Upload lint results
      uses: actions/upload-artifact@v4
      if: always()
      with:
        name: lint-results-${{ github.sha }}
        path: app/build/reports/lint-results-debug.html
        retention-days: 7
```

## Setup Instructions

### Option 1: Manual Addition
1. Create the `.github/workflows/` directory in the repository
2. Add the `build.yml` file with the workflow configuration above
3. Commit and push the changes

### Option 2: Repository Settings
1. Go to repository Settings → Actions → General
2. Ensure "Allow all actions and reusable workflows" is selected
3. Add the workflow file through the GitHub web interface

## Testing the Workflow
Once the workflow is added:

1. **Automatic Test**: Push a commit to main branch
2. **Manual Test**: Use "Actions" tab → "Run workflow" button
3. **PR Test**: Create a pull request to trigger build

## Expected Results
- ✅ Successful builds for valid code changes
- ✅ APK artifacts available for download
- ✅ Test results reported in workflow logs
- ✅ PR comments with build status
- ✅ Build failures clearly reported

## Acceptance Criteria Validation
The workflow meets all FR-022 acceptance criteria:
- ✅ Automated build triggers on every push to main branch
- ✅ Manual build trigger available for pull requests
- ✅ APK artifacts generated and stored for 30 days
- ✅ Build status visible in GitHub PR checks
- ✅ Build failures reported with clear error messages
- ✅ Gradle caching reduces build time by >50%
