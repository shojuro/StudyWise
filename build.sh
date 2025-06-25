#!/bin/bash

echo "Building StudyWise Debug APK..."
echo

# Check if JAVA_HOME is set
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME is not set!"
    echo "Please set JAVA_HOME to your JDK 17 installation path"
    echo "Example: export JAVA_HOME=/usr/lib/jvm/java-17-openjdk"
    exit 1
fi

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