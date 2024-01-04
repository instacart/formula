import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
    from("$rootDir/.buildscript/jacoco-workaround.gradle")
}

android {
    namespace = "com.instacart.formula.android"

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    implementation(project(":formula-rxjava3"))
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)

    api(libs.rxandroid)
    api(libs.rxrelay)

    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.espresso.core)
    testImplementation(libs.truth)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.reflect)
}

