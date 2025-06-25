@echo off
echo ========================================
echo Building StudyWise APK
echo ========================================

REM Set JAVA_HOME for Eclipse Adoptium Java 17
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"

REM Clean previous builds
echo Cleaning previous builds...
call gradlew.bat clean

REM Build debug APK
echo Building debug APK...
call gradlew.bat assembleDebug

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on a connected device:
    echo adb install app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo Please check the error messages above.
)

pause