@echo off
setlocal enabledelayedexpansion

echo ========================================
echo StudyWise Debug APK Builder
echo ========================================
echo.

:: Check if JAVA_HOME is set
if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME is not set!
    echo.
    echo Please set JAVA_HOME to your JDK 17 installation path
    echo Example: set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    echo.
    pause
    exit /b 1
)

:: Display Java location
echo Java Home: !JAVA_HOME!

:: Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat not found!
    echo Please run this script from the project root directory.
    pause
    exit /b 1
)

echo.
echo Cleaning previous builds...
call gradlew.bat clean

if !ERRORLEVEL! NEQ 0 (
    echo.
    echo ERROR: Clean failed!
    pause
    exit /b 1
)

echo.
echo Building debug APK...
call gradlew.bat assembleDebug

if !ERRORLEVEL! EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Debug APK location:
    echo %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on connected device, run:
    echo adb install app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    :: Check if APK exists and show size
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        for %%F in ("app\build\outputs\apk\debug\app-debug.apk") do (
            set size=%%~zF
            set /a sizeMB=!size! / 1048576
            echo APK Size: !sizeMB! MB
        )
    )
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Common issues:
    echo 1. JAVA_HOME not pointing to JDK 17
    echo 2. Android SDK not installed
    echo 3. Missing dependencies
    echo.
    echo Check the error messages above for details.
)

echo.
pause