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

java {
  targetCompatibility = JavaVersion.VERSION_1_8
  sourceCompatibility = JavaVersion.VERSION_1_8
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
