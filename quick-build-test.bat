@echo off
echo Quick Build Test
echo ================
echo.

echo Compiling Kotlin sources...
call gradlew.bat compileDebugKotlin --stacktrace

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed! Check errors above.
    pause
    exit /b 1
)

echo.
echo Compilation successful!
pause