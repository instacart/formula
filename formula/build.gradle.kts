plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
}

dependencies {
    implementation(libs.kotlin)
    implementation(libs.coroutines)

    testImplementation(project(":formula-test"))
    testImplementation(project(":formula-rxjava3"))
    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.rxrelays)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}
