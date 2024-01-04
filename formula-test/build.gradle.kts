plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
}

dependencies {
    implementation(libs.kotlin)
    api(project(":formula-rxjava3"))

    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.rxrelays)
}
