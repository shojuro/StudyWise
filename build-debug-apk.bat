@echo off
echo StudyWise Debug APK Build
echo =========================
echo.

REM Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set!
    echo Please set JAVA_HOME to your JDK installation path
    echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-17
    pause
    exit /b 1
)

echo JAVA_HOME: %JAVA_HOME%
echo.

REM Clean and build debug APK
echo Cleaning project...
call gradlew.bat clean

if %ERRORLEVEL% NEQ 0 (
    echo Clean failed!
    pause
    exit /b 1
)

echo.
echo Building Debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Build completed successfully!
echo.
echo Debug APK location: app\build\outputs\apk\debug\app-debug.apk
echo.
echo To install on connected device: adb install app\build\outputs\apk\debug\app-debug.apk
echo.
pause