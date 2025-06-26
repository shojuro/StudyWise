@echo off
echo StudyWise - Revert to Working State
echo ===================================
echo.

echo This will revert recent changes to get a working build.
echo.

set /p confirm="Are you sure you want to proceed? (Y/N): "
if /I not "%confirm%"=="Y" (
    echo Aborted.
    exit /b 0
)

echo.
echo Option 1: Revert security implementation only
echo Option 2: Full git reset to last working commit
echo.

set /p option="Choose option (1 or 2): "

if "%option%"=="1" (
    echo.
    echo Reverting security implementation files...
    
    REM Revert security-related files
    git checkout -- app/src/main/java/com/studywise/ai/security/
    git checkout -- app/src/main/java/com/studywise/ai/data/local/entity/UserEntity.kt
    git checkout -- app/src/main/java/com/studywise/ai/data/local/dao/UserDao.kt
    git checkout -- app/src/main/java/com/studywise/ai/data/repository/AuthRepositoryImpl.kt
    git checkout -- app/src/main/java/com/studywise/ai/data/local/migration/
    git checkout -- app/src/main/res/xml/network_security_config.xml
    git checkout -- app/src/main/AndroidManifest.xml
    git checkout -- app/build.gradle.kts
    git checkout -- app/proguard-rules.pro
    
    echo.
    echo Security files reverted. You may need to clean and rebuild.
) else if "%option%"=="2" (
    echo.
    echo Finding last working commit...
    
    REM Show recent commits
    git log --oneline -10
    
    echo.
    set /p commit="Enter commit hash to revert to (or 'cancel'): "
    
    if not "%commit%"=="cancel" (
        git reset --hard %commit%
        echo.
        echo Reverted to commit %commit%
    )
) else (
    echo Invalid option.
    exit /b 1
)

echo.
echo Next steps:
echo 1. Run: gradlew.bat clean
echo 2. Run: .\build-apk.ps1
echo.
pause