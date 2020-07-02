#!/bin/bash
# Should match with what we run in .circleci/config.yml
./gradlew clean
./gradlew :formula:test
./gradlew :formula-android:testRelease
./gradlew :formula-android-tests:testRelease
./gradlew jacocoTestReportMerged
open build/reports/jacoco/index.html
