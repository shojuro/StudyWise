# Build Instructions

## Prerequisites
- Java 17 (Eclipse Adoptium OpenJDK)
- Android SDK
- OpenAI API Key (see OPENAI_SETUP_GUIDE.md)

## Building on Windows

1. Open Command Prompt or PowerShell
2. Navigate to project directory:
   ```cmd
   cd C:\DEV\StudyWise-Fresh
   ```

3. Set JAVA_HOME (if not already set):
   ```cmd
   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot
   ```

4. Build the debug APK:
   ```cmd
   gradlew.bat assembleDebug
   ```

## Building on WSL/Linux

If using WSL, use the Windows gradlew.bat through cmd.exe:
```bash
cmd.exe /c "cd /d C:\\DEV\\StudyWise-Fresh && set JAVA_HOME=C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.15.6-hotspot && gradlew.bat assembleDebug"
```

## Output Location

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Troubleshooting

### JAVA_HOME Issues
- Ensure Java 17 is installed
- Path with spaces must be quoted in scripts
- Verify with: `java -version`

### Build Failures
1. Clean build: `gradlew.bat clean`
2. Sync project: `gradlew.bat sync`
3. Check for missing API keys in local.properties

### API Key Setup
See OPENAI_SETUP_GUIDE.md for setting up the OpenAI API key required for AI features.