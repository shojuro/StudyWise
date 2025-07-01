@echo off
echo Building StudyWise Debug APK...
echo.

REM Set JAVA_HOME
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot

REM Clean previous builds
echo Cleaning previous builds...
call gradlew.bat clean

REM Build debug APK
echo Building debug APK...
call gradlew.bat assembleDebug

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    
    REM Copy APK with timestamp
    for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
    for /f "tokens=1-2 delims=/:" %%a in ("%TIME%") do (set mytime=%%a%%b)
    copy app\build\outputs\apk\debug\app-debug.apk StudyWise-debug-%mydate%-%mytime%.apk
    
    echo APK copied to: StudyWise-debug-%mydate%-%mytime%.apk
) else (
    echo.
    echo Build failed!
    echo Please check the error messages above.
)

pause