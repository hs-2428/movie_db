#!/bin/bash

echo "=== Java Version Upgrade Verification ==="
echo ""

echo "1. Current JAVA_HOME:"
echo "$JAVA_HOME"
echo ""

echo "2. Java Version:"
java -version
echo ""

echo "3. Gradle Version and JVM:"
./gradlew --version
echo ""

echo "4. Java Toolchains detected by Gradle:"
./gradlew javaToolchains --no-daemon
echo ""

echo "5. Build configuration (checking for Java 21):"
grep -n "VERSION_21\|jvmTarget.*21" app/build.gradle.kts
echo ""

echo "=== Verification Complete ==="