plugins {
    id("java-library")
    id("kotlin")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
}

dependencies {
    implementation(libs.kotlin)
    implementation(libs.coroutines)

    api(project(":formula"))

    testImplementation(project(":formula-test"))
    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}