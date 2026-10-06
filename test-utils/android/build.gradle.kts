plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.instacart.testutils.android"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":formula-rxjava3"))
    implementation(project(":formula-android"))
    api(libs.rxrelay)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)

    implementation(libs.kotlin)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.test.core.ktx)
    implementation(libs.lifecycle.extensions)
    implementation(libs.robolectric)
    implementation(libs.truth)
}
