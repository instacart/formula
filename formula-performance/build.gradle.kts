plugins {
    id("kotlin")
    id("me.champeau.jmh") version "0.7.2"
}

dependencies {
    implementation(libs.kotlin)
    api(project(":formula"))
    api(project(":formula-test"))

    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

jmh {
    jmhVersion.set("1.37")
    includeTests.set(false)
    resultFormat.set("JSON")
    resultsFile.set(project.file("${project.buildDir}/results/jmh/results.json"))
}
