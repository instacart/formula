plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.instacart.formula.stopwatch"

    defaultConfig {
        applicationId = "com.instacart.formula.stopwatch"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":formula-coroutines"))
    implementation(project(":formula-android"))

    implementation(libs.kotlin)
    implementation(libs.androidx.appcompat)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
}
