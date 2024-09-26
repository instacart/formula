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
  testImplementation(libs.truth)
}
