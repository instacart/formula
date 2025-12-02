# R8 Integration Test ProGuard Rules
#
# Strategy:
# - Keep interactors (test infrastructure) from being minified
# - Keep all test-related infrastructure (Kotlin stdlib, AndroidX test, etc.)
# - Allow formulas (code under test) to be fully minified by R8
# - This simulates real-world R8 behavior where Formula code is minified

# Keep custom test runner
-keep class com.instacart.formula.r8.runner.** { *; }

# Keep all interactor classes and their methods
# Tests reference these, so they must not be obfuscated
-keep class com.instacart.formula.r8.interactors.** { *; }

# Keep formula-test classes (used by interactors)
-keep class com.instacart.formula.test.** { *; }

# Keep Truth assertions (used by interactors)
-keep class com.google.common.truth.** { *; }

# Keep all Kotlin standard library (needed by test infrastructure)
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Keep JUnit classes (used by formula-test)
-keep class org.junit.** { *; }
-keep class junit.** { *; }
-keep class org.hamcrest.** { *; }

# Keep AndroidX test infrastructure
-keep class androidx.tracing.** { *; }
-keep class androidx.test.** { *; }
-keep class androidx.annotation.** { *; }
-dontwarn androidx.test.**

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Allow formulas and fixtures to be fully processed by R8
# (no keep rules - this is intentional to test R8 behavior)
