#!/bin/bash

# Set JAVA_HOME for Windows Java installation accessed from WSL
export JAVA_HOME="/mnt/c/Program Files/Eclipse Adoptium/jdk-17.0.15.6-hotspot"

# Run the Windows gradle wrapper
echo "Building APK..."
/mnt/c/Windows/System32/cmd.exe /c "cd C:\\DEV\\StudyWise-Fresh && gradlew.bat clean assembleDebug"

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    ls -la app/build/outputs/apk/debug/
else
    echo "Build failed!"
    exit 1
fi