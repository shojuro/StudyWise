#!/bin/bash
export JAVA_HOME="/mnt/c/Program Files/Eclipse Adoptium/jdk-17.0.15.6-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew "$@"