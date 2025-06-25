#!/bin/bash

echo "Building StudyWise Debug APK..."
echo

# Set JAVA_HOME for Windows WSL environment
export JAVA_HOME="/mnt/c/Program Files/Eclipse Adoptium/jdk-17.0.15.6-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java from: $JAVA_HOME"
echo

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo "Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo
    echo "BUILD SUCCESSFUL!"
    echo
    echo "Debug APK location:"
    echo "app/build/outputs/apk/debug/app-debug.apk"
    echo
    echo "To install on connected device:"
    echo "adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo
    echo "BUILD FAILED!"
    echo "Check the error messages above."
fi