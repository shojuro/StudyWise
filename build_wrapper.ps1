# PowerShell script to build with proper JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot"
.\gradlew.bat assembleDebug