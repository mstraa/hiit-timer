#!/bin/bash

# HIIT Timer Build Script
# This script helps build and test the HIIT Timer Android app

set -e

echo "🏃‍♂️ HIIT Timer Build Script"
echo "=========================="

# Function to print colored output
print_status() {
    echo -e "\033[1;34m$1\033[0m"
}

print_success() {
    echo -e "\033[1;32m✅ $1\033[0m"
}

print_error() {
    echo -e "\033[1;31m❌ $1\033[0m"
}

# Check if we're in the right directory
if [ ! -f "gradlew" ]; then
    print_error "gradlew not found. Please run this script from the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x gradlew

# Parse command line arguments
case "${1:-build}" in
    "clean")
        print_status "Cleaning project..."
        ./gradlew clean
        print_success "Project cleaned successfully!"
        ;;
    "test")
        print_status "Running unit tests..."
        ./gradlew testDebugUnitTest
        print_success "Tests completed successfully!"
        echo ""
        echo "📊 Test report available at: app/build/reports/tests/testDebugUnitTest/index.html"
        ;;
    "build")
        print_status "Building debug APK..."
        ./gradlew assembleDebug
        print_success "Debug APK built successfully!"
        echo ""
        echo "📱 APK location: app/build/outputs/apk/debug/app-debug.apk"
        ;;
    "install")
        print_status "Installing debug APK to connected device..."
        ./gradlew installDebug
        print_success "App installed successfully!"
        ;;
    "lint")
        print_status "Running lint checks..."
        ./gradlew lintDebug
        print_success "Lint checks completed!"
        echo ""
        echo "📋 Lint report available at: app/build/reports/lint-results-debug.html"
        ;;
    "all")
        print_status "Running full build pipeline..."
        ./gradlew clean testDebugUnitTest assembleDebug lintDebug
        print_success "Full build pipeline completed successfully!"
        echo ""
        echo "📊 Test report: app/build/reports/tests/testDebugUnitTest/index.html"
        echo "📱 APK location: app/build/outputs/apk/debug/app-debug.apk"
        echo "📋 Lint report: app/build/reports/lint-results-debug.html"
        ;;
    "help"|"-h"|"--help")
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  clean     Clean the project"
        echo "  test      Run unit tests"
        echo "  build     Build debug APK (default)"
        echo "  install   Install debug APK to connected device"
        echo "  lint      Run lint checks"
        echo "  all       Run full build pipeline (clean, test, build, lint)"
        echo "  help      Show this help message"
        ;;
    *)
        print_error "Unknown command: $1"
        echo "Run '$0 help' for usage information."
        exit 1
        ;;
esac
