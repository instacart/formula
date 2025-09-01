plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
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

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
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
    implementation(project(":formula-android-compose"))

    implementation(libs.kotlin)
    implementation(libs.androidx.appcompat)
    implementation(libs.compose.material)
    implementation(libs.androidx.activity.compose)
}