#!/bin/bash
# Script to build Android app from WSL using Windows Gradle

cd /mnt/c/DEV/StudyWise-Fresh

# Convert WSL path to Windows path
WIN_PATH=$(pwd | sed 's|/mnt/c|C:|' | sed 's|/|\\|g')

echo "Building from: $WIN_PATH"

# Use Windows PowerShell to run the batch file
powershell.exe -Command "cd '$WIN_PATH'; .\gradlew.bat clean assembleDebug"