# Build APK Instructions

## Quick Build Command

Open PowerShell or Command Prompt in the StudyWise-Fresh directory and run:

```batch
.\build-apk.bat
```

## Alternative Manual Build

If the batch file doesn't work, use these commands:

```batch
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
gradlew.bat clean
gradlew.bat assembleDebug
```

## Build Output

After a successful build, your APK will be located at:
```
app\build\outputs\apk\debug\app-debug.apk
```

## Installing the APK

### Option 1: Using ADB (Android Debug Bridge)
```batch
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Option 2: Manual Installation
1. Copy the APK file to your Android device
2. Open the file on your device
3. Allow installation from unknown sources if prompted
4. Install the app

## Troubleshooting

If you encounter issues:

1. **JAVA_HOME Error**: Ensure Java 17 is installed at the specified path
2. **Gradle Error**: Run `gradlew.bat --version` to check Gradle setup
3. **Build Failures**: Check the error messages and ensure all dependencies are resolved

## Testing the App

Once installed:
1. Open StudyWise on your device
2. Test the login with demo mode
3. Try the photo learning feature
4. Test voice input
5. Check analytics tracking
6. Verify all screens are accessible