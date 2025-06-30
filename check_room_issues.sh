#!/bin/bash

echo "=== Checking Room Database Issues ==="

echo -e "\n1. Checking for mismatched entity references in foreign keys:"
grep -r "ForeignKey" app/src/main/java --include="*.kt" -A 3 -B 1

echo -e "\n2. Checking for SessionQuestion vs SessionQuestionEntity usage:"
grep -r "SessionQuestion[^E]" app/src/main/java --include="*.kt" | grep -v "SessionQuestionEntity" | grep -v "SessionQuestionDao" | grep -v "//"

echo -e "\n3. Checking all DAO return types that might cause Cursor conversion issues:"
grep -r "@Query" app/src/main/java --include="*.kt" -A 1 | grep -E "suspend fun|fun" | head -20

echo -e "\n4. Checking for entity class issues:"
find app/src/main/java -name "*Entity.kt" -exec basename {} \; | sort

echo -e "\n5. Checking Database class for entity registration:"
grep -A 20 "@Database" app/src/main/java/com/studywise/ai/data/local/database/StudyWiseDatabase.kt

echo -e "\nDone."