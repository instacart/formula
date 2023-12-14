plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
}

dependencies {
    implementation(libs.kotlin)

    testImplementation(project(":formula-test"))
    testImplementation(project(":formula-rxjava3"))
    testImplementation(project(":formula-coroutines"))
    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.rxrelays)
    testImplementation(libs.coroutines.test)
}
