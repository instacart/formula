#!/bin/bash

# Optional target parameter, defaults to "artifactoryRelease"
# Use "mavenCentralRelease" for Maven Central
TARGET="${1:-artifactoryRelease}"
GRADLE_ARGS="-P$TARGET"

./gradlew clean
./gradlew :formula:build $GRADLE_ARGS
./gradlew :formula-android:build $GRADLE_ARGS
./gradlew :formula-test:build $GRADLE_ARGS
./gradlew :formula-android-compose:build $GRADLE_ARGS
./gradlew :formula-lint:build $GRADLE_ARGS
./gradlew :formula-rxjava3:build $GRADLE_ARGS

# Disabling parallelism and daemon sharing is required by the vanniktech maven publish plugin.
# Without those, the artifacts will be split across multiple (invalid) staging repositories.
./gradlew uploadArchives $GRADLE_ARGS --no-parallel --no-daemon
