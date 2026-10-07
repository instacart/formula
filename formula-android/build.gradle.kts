plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.compose)
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
    from("$rootDir/.buildscript/jacoco-workaround.gradle")
}

android {
    namespace = "com.instacart.formula.android"

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    implementation(project(":formula-rxjava3"))
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)

    api(libs.compose.ui)
    api(libs.coroutines)
    api(libs.androidx.activity.compose)
    api(libs.lifecycle.runtime.ktx)
    api(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)

    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.coroutines.rx3)
    testImplementation(libs.espresso.core)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(project(":test-utils:android"))
}

