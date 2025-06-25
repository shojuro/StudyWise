@echo off
echo Quick Build - StudyWise Debug APK
echo.

:: Try to build with gradlew directly
echo Building debug APK...
gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo BUILD SUCCESSFUL!
    echo APK: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo BUILD FAILED!
    echo.
    echo If you see JAVA_HOME errors, try:
    echo 1. Close this window
    echo 2. Set JAVA_HOME in PowerShell:
    echo    $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
    echo 3. Run: .\gradlew.bat assembleDebug
)

pause