plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.instacart.formula.navigation"

    defaultConfig {
        applicationId = "com.instacart.formula.navigation.fragments"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":formula"))
    implementation(project(":formula-android"))

    implementation(platform(libs.compose.bom))
    implementation(libs.kotlin)
    implementation(libs.androidx.appcompat)
    implementation(libs.compose.material)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.reflect)
    testImplementation(project(":formula-test"))
}