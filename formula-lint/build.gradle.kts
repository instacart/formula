import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask

plugins {
  id("java-library")
  id("kotlin")
  id("org.jetbrains.kotlin.kapt")
  id("com.android.lint")
  id("org.jetbrains.dokka")
}

apply {
  from("$rootDir/.buildscript/configure-signing.gradle")
}

dependencies {
  compileOnly(libs.lint.api)
  compileOnly(libs.lint.checks)
  compileOnly(libs.auto.service.annotations)
  kapt(libs.auto.service)
  testImplementation(libs.lint.core)
  testImplementation(libs.lint.tests)
  testImplementation(libs.junit)
}

// Need to register direct task dependencies since jacocoTestReport is
// accessing the files produced by those lint tasks
plugins.withId("jacoco") {
  tasks.named("jacocoTestReport") {
    dependsOn(tasks.withType(AndroidLintAnalysisTask::class.java))
    dependsOn(tasks.withType(LintModelWriterTask::class.java))
  }
}
