plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.instacart.testutils.android"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(project(":formula-rxjava3"))
    implementation(project(":formula-android"))
    api(libs.rxrelay)

    implementation(libs.kotlin)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.test.core.ktx)
    implementation(libs.lifecycle.extensions)
    implementation(libs.robolectric)
    implementation(libs.truth)
}
