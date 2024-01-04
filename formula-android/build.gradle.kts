import com.android.build.gradle.internal.coverage.JacocoReportTask
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
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
