# Formula R8 Integration Tests
This module contains formula integration tests that run with R8 enabled. Formula uses class types 
as part of the key for child formulas, actions, and listeners. When R8 merges lambda classes, 
previously distinct types become the same, causing bugs

## Running Tests
```bash
./gradlew :formula-r8-tests:connectedDebugAndroidTest
```

Requires a connected Android device or running emulator.

## Module Structure

```
formula-r8-tests/
├── src/main/java/
│   ├── fixtures/              # Formula classes to be minified by R8
│   ├── interactors/           # Test helpers (kept from minification)
│   └── runner/                # Custom test runner
├── src/androidTest/java/      # Instrumented tests
├── project-proguard-rules.pro # ProGuard configuration
└── build.gradle.kts           # R8 configuration
```
