@echo off
echo ====================================
echo StudyWise Build and Install Script
echo ====================================
echo.

REM Set JAVA_HOME if not already set
if "%JAVA_HOME%"=="" (
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot
    echo Setting JAVA_HOME to: %JAVA_HOME%
)

echo Step 1: Cleaning previous builds...
call gradlew.bat clean

echo.
echo Step 2: Building debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Step 3: Build successful! APK location:
echo app\build\outputs\apk\debug\app-debug.apk

REM Check if device is connected
echo.
echo Step 4: Checking for connected devices...
adb devices -l

echo.
echo Step 5: Installing APK on connected device...
adb install -r app\build\outputs\apk\debug\app-debug.apk

if %ERRORLEVEL% EQ 0 (
    echo.
    echo Step 6: Launching StudyWise...
    adb shell am start -n com.studywise.ai/.MainActivity
    echo.
    echo ====================================
    echo BUILD AND INSTALL SUCCESSFUL!
    echo ====================================
) else (
    echo.
    echo WARNING: Could not install APK. Make sure a device is connected.
    echo You can manually install the APK from:
    echo app\build\outputs\apk\debug\app-debug.apk
)

echo.
pause