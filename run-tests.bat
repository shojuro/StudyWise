@echo off
echo ========================================
echo Running StudyWise Unit Tests
echo ========================================

REM Set JAVA_HOME for Eclipse Adoptium Java 17
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"

REM Run unit tests
echo Running unit tests...
call gradlew.bat test

REM Check if tests passed
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ALL TESTS PASSED!
    echo ========================================
    echo Test reports available at:
    echo app\build\reports\tests\testDebugUnitTest\index.html
) else (
    echo.
    echo ========================================
    echo TESTS FAILED!
    echo ========================================
    echo Check the test report for details:
    echo app\build\reports\tests\testDebugUnitTest\index.html
)

pause