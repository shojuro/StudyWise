#!/bin/bash
# Collect compilation errors

echo "Searching for compilation issues..."

# Find files that might have issues based on the error messages
echo "=== Checking imports ==="
grep -r "import.*PromptExpansionService" app/src/main/java --include="*.kt" | grep "domain.usecase.education" || echo "No wrong PromptExpansionService imports found"

echo -e "\n=== Checking for SessionQuestion vs SessionQuestionEntity ==="
grep -r "SessionQuestion[^E]" app/src/main/java --include="*.kt" | grep -v "SessionQuestionEntity" | grep -v "SessionQuestionDao" || echo "No incorrect SessionQuestion usage found"

echo -e "\n=== Checking for missing data classes ==="
grep -r "ActivityCardData" app/src/main/java --include="*.kt" || echo "No ActivityCardData found"

echo -e "\n=== Checking for GamificationDashboard ==="
find app/src/main/java -name "*Gamification*" -type f

echo -e "\n=== Listing all Kotlin files with potential issues ==="
find app/src/main/java -name "*.kt" -exec grep -l "GamificationDashboard\|ActivityCardData\|trackSubjectProgressViewed\|trackProgressExported" {} \;

echo -e "\nDone."