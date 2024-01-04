plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
    from("$rootDir/.buildscript/jacoco-workaround.gradle")
}

android {
    namespace = "com.instacart.formula.android.compose"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    api(project(":formula-android"))
    api(libs.compose.ui)

    api(libs.compose.rxjava3)

    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.espresso.core)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.reflect)
}
