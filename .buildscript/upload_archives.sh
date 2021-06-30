./gradlew clean
./gradlew :formula:build -PreleaseBuild
./gradlew :formula-android:build -PreleaseBuild
./gradlew :formula-test:build -PreleaseBuild
./gradlew :formula-android-compose:build -PreleaseBuild

# Disabling parallelism and daemon sharing is required by the vanniktech maven publish plugin.
# Without those, the artifacts will be split across multiple (invalid) staging repositories.
./gradlew uploadArchives -PreleaseBuild --no-parallel --no-daemon
