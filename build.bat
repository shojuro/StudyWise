@echo off
echo Building StudyWise Debug APK...
echo.

REM Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set!
    echo Please set JAVA_HOME to your JDK 17 installation path
    echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-17
    exit /b 1
)

echo Using Java from: "%JAVA_HOME%"
echo.

REM Clean previous builds
echo Cleaning previous builds...
call gradlew.bat clean

REM Build debug APK
echo Building debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo BUILD SUCCESSFUL!
    echo.
    echo Debug APK location:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on connected device:
    echo adb install app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo BUILD FAILED!
    echo Check the error messages above.
)

pause