plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
    from("$rootDir/.buildscript/jacoco-workaround.gradle")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerVersion = libs.versions.kotlin.get()
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

task<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

task<Javadoc>("javadoc") {
    isFailOnError = false
    source(android.sourceSets["main"].java.getSourceFiles())
    classpath += project.files(android.bootClasspath.joinToString(separator = File.pathSeparator))
    classpath += configurations.api
}

task<Jar>("javadocJar") {
    dependsOn("javadoc")
    archiveClassifier.set("javadoc")
    from(tasks["javadoc"].path)
}

artifacts {
    archives(tasks["sourcesJar"])
    archives(tasks["javadocJar"])
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
