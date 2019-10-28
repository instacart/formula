./gradlew clean
./gradlew :formula:build
./gradlew :formula-android:build
./gradlew :formula-test:build

# Disabling parallelism and daemon sharing is required by the vanniktech maven publish plugin.
# Without those, the artifacts will be split across multiple (invalid) staging repositories.
./gradlew uploadArchives --no-parallel --no-daemon
