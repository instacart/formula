plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.instacart.formula.samples.composition"

    defaultConfig {
        applicationId = "com.instacart.formula.samples.composition"
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
    implementation(project(":formula-rxjava3"))
    implementation(project(":formula-android"))

    implementation(libs.kotlin)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.rxrelay)
    implementation(libs.rxandroid)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.espresso.core)
    testImplementation(libs.robolectric)
    testImplementation(project(":formula-test"))
}
