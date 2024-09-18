plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
}

apply {
    from("$rootDir/.buildscript/jacoco-workaround.gradle")
}

android {
    namespace = "com.instacart.formula"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":formula-rxjava3"))
    implementation(project(":formula-android"))

    implementation(libs.kotlin)
    implementation(libs.androidx.appcompat)
    implementation(libs.lifecycle.extensions)
    implementation(libs.androidx.test.core.ktx)

    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.espresso.core)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(project(":test-utils:android"))
}
