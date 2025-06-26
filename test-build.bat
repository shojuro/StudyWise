@echo off
echo Testing Gradle Build...
echo.

REM Test if gradlew is working
call gradlew.bat --version

echo.
echo Attempting to build...
call gradlew.bat assembleDebug --stacktrace

pause