plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.kotlin)

    api(project(":formula"))
    api(libs.rxjava)

    testImplementation(project(":formula-test"))
    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.rxrelays)
}
