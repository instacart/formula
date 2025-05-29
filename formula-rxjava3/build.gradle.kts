plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
}

apply {
    from("$rootDir/.buildscript/configure-signing.gradle")
}

dependencies {
    implementation(libs.kotlin)
    implementation(libs.coroutines.rx3)

    api(project(":formula"))
    api(libs.rxjava)

    testImplementation(project(":formula-test"))
    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.rxrelays)
}
