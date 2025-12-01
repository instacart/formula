plugins {
    id("com.android.application")
    id("kotlin-android")
}

apply {
    from("$rootDir/.buildscript/jacoco-workaround.gradle")
}

android {
    namespace = "com.instacart.formula.r8.tests"
    defaultConfig {
        testInstrumentationRunner = "com.instacart.formula.r8.runner.FormulaTestRunner"
    }

    buildTypes {
        debug {
            // Disable debuggable to allow R8 to actually minify
            // (debuggable builds ignore minification settings)
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "project-proguard-rules.pro"
            )

            // Optional: Keep debug symbols for better crash reports
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    // Disable lint for this test module
    lint {
        abortOnError = false
    }

    packaging {
        resources {
            excludes += setOf("META-INF/LICENSE.md", "META-INF/LICENSE-notice.md")
        }
    }
}

dependencies {
    // Formula libraries (will be minified by R8)
    implementation(project(":formula"))
    implementation(project(":formula-android"))
    implementation(libs.kotlin)
    implementation(libs.coroutines)

    // Interactor dependencies (in src/main, needs to compile against these)
    implementation(project(":formula-test"))
    implementation(libs.truth)

    // Test runner dependency (in src/main, needs to extend AndroidJUnitRunner)
    implementation("androidx.test:runner:1.5.2")

    // Instrumented test dependencies (run on device/emulator)
    androidTestImplementation(project(":formula-test"))
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.truth)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.tracing:tracing:1.0.0")  // Required by AndroidJUnitRunner
    androidTestImplementation(libs.coroutines.test)
}
