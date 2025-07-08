#!/bin/bash

# Android HIIT Timer App Setup Script
set -e

echo "Setting up Android HIIT Timer development environment..."

# Update system packages
sudo apt-get update

# Install required packages
sudo apt-get install -y \
    openjdk-17-jdk \
    wget \
    unzip \
    curl \
    git

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> $HOME/.profile

# Download and install Android SDK Command Line Tools
ANDROID_HOME=$HOME/android-sdk
mkdir -p $ANDROID_HOME
cd $ANDROID_HOME

# Download Android SDK Command Line Tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q commandlinetools-linux-11076708_latest.zip
rm commandlinetools-linux-11076708_latest.zip

# Create proper directory structure
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true

# Set Android environment variables
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Add to profile for persistence
echo "export ANDROID_HOME=$HOME/android-sdk" >> $HOME/.profile
echo "export ANDROID_SDK_ROOT=\$ANDROID_HOME" >> $HOME/.profile
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> $HOME/.profile

# Accept Android SDK licenses
yes | sdkmanager --licenses

# Install required Android SDK components
sdkmanager "platform-tools"
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"

# Navigate to project directory
cd /mnt/persist/workspace

# Make gradlew executable
chmod +x gradlew

# Clean any previous builds
./gradlew clean

echo "Android development environment setup complete!"
echo "Java version:"
java -version
echo "Android SDK location: $ANDROID_HOME"