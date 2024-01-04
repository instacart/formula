plugins {
    id("java-library")
    id("kotlin")
}

dependencies {
    implementation(libs.kotlin)
    implementation(libs.coroutines)

    api(project(":formula"))

    testImplementation(project(":formula-test"))
    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}