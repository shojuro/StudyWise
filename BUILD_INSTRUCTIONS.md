# StudyWise Build Instructions

## Prerequisites

1. **Java Development Kit (JDK)**
   - Install JDK 17 or higher
   - Set JAVA_HOME environment variable
   - Example: `JAVA_HOME=C:\Program Files\Java\jdk-17`

2. **Android SDK**
   - Install via Android Studio or command line tools
   - Ensure SDK platform 35 is installed

3. **API Keys**
   - Create a `local.properties` file in the project root
   - Add your API keys:
     ```
     OPENAI_API_KEY=your_openai_api_key_here
     MISTRAL_API_KEY=your_mistral_api_key_here
     ```

## Building the APK

### Option 1: Using PowerShell (Recommended)
```powershell
.\build-apk.ps1
```

### Option 2: Using Command Prompt
```cmd
build-debug-apk.bat
```

### Option 3: Manual Gradle Commands
```cmd
# Clean build
gradlew.bat clean

# Build debug APK
gradlew.bat assembleDebug

# Build release APK (unsigned)
gradlew.bat assembleRelease
```

## Build Output

APKs will be generated in:
- **Debug APK**: `app\build\outputs\apk\debug\app-debug.apk`
- **Release APK**: `app\build\outputs\apk\release\app-release-unsigned.apk`

## Installing the APK

### On Physical Device
1. Enable Developer Options and USB Debugging on your Android device
2. Connect device via USB
3. Run: `adb install app\build\outputs\apk\debug\app-debug.apk`

### On Emulator
1. Start Android emulator
2. Run: `adb install app\build\outputs\apk\debug\app-debug.apk`

## Troubleshooting

### JAVA_HOME not set
Set the environment variable:
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

### Build fails with "SDK location not found"
Create `local.properties` with:
```
sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

### API key errors
Ensure `local.properties` contains valid API keys

### Out of memory errors
Add to `gradle.properties`:
```
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m
```

## Release Build

For production release:
1. Generate a signing key
2. Configure signing in `app/build.gradle.kts`
3. Build signed APK
4. Test thoroughly before distribution

## Features Included

- ✅ AI-powered photo learning with Mistral 7B integration
- ✅ Enhanced object detection with confidence thresholds
- ✅ Manual object entry fallback
- ✅ Offline content database
- ✅ Progress tracking and analytics
- ✅ Parent and teacher dashboards
- ✅ Security enhancements (PBKDF2 passwords, input validation)
- ✅ COPPA compliance features
- ✅ Network security configuration
- ✅ Comprehensive test coverage