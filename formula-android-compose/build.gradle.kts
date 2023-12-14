plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
    from("$rootDir/.buildscript/jacoco-workaround.gradle")
}

android {
    namespace = "com.instacart.formula.android.compose"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
}

task<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}

task<Javadoc>("javadoc") {
    isFailOnError = false
    source(android.sourceSets["main"].java.getSourceFiles())
    classpath = project.files(
        android.bootClasspath.joinToString(separator = File.pathSeparator),
        configurations.api,
        configurations.implementation
    )
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

    implementation(libs.compose.rxjava3)

    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.espresso.core)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.reflect)
}
