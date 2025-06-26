@echo off
echo Testing Clean Build After Fixes
echo ===============================
echo.

echo Step 1: Cleaning...
call gradlew.bat clean

if %ERRORLEVEL% NEQ 0 (
    echo Clean failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Building Debug APK...
call gradlew.bat assembleDebug --stacktrace

if %ERRORLEVEL% NEQ 0 (
    echo Build failed! See errors above.
    pause
    exit /b 1
)

echo.
echo Build successful!
echo APK location: app\build\outputs\apk\debug\app-debug.apk
echo.
pause