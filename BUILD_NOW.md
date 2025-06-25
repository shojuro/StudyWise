# Build Instructions - Run Now

## Quick Build (Recommended)

Open **Command Prompt** or **PowerShell** and run:

```cmd
cd C:\DEV\StudyWise-Fresh
build-apk.bat
```

## Manual Build

If the batch file doesn't work, run these commands:

```cmd
cd C:\DEV\StudyWise-Fresh
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
gradlew.bat clean
gradlew.bat assembleDebug
```

## Expected Output

The build should take 2-5 minutes. You'll see:
- Downloading dependencies (first time only)
- Compiling Kotlin code
- Processing resources
- Building APK

## Success Message

When successful, you'll see:
```
BUILD SUCCESSFUL in XXs
```

## APK Location

Your APK will be at:
```
C:\DEV\StudyWise-Fresh\app\build\outputs\apk\debug\app-debug.apk
```

## Next Steps After Build

1. Install on your device
2. Test the new features
3. Report any issues
4. We'll continue with analytics integration and testing

---

**Please run the build now and let me know when it's complete!**