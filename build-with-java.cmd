@echo off
echo ========================================
echo StudyWise Debug APK Builder
echo ========================================
echo.

:: Set JAVA_HOME to Eclipse Adoptium JDK 17
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"

echo Using Java from: "%JAVA_HOME%"
echo.

:: Verify Java version
"%JAVA_HOME%\bin\java" -version 2>&1 | findstr /i "version"
echo.

:: Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat not found!
    echo Please run this script from the project root directory.
    pause
    exit /b 1
)

echo Cleaning previous builds...
call gradlew.bat clean

echo.
echo Building debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Debug APK location:
    echo %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on connected device:
    echo adb install app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    :: Show APK details if it exists
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        echo APK Details:
        dir "app\build\outputs\apk\debug\app-debug.apk" | findstr "app-debug.apk"
    )
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo Check the error messages above.
)

echo.
pause