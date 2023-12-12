buildscript {

    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

    dependencies {
        classpath(libs.android.gradle)
        classpath(libs.kotlin.gradle)
        classpath(libs.jacoco.gradle)
        classpath(libs.version.gradle)
        classpath(libs.dokka.gradle)
        classpath(libs.dokka.android.gradle)
        classpath(libs.maven.publish.gradle)
    }
}

apply(plugin = "com.github.ben-manes.versions")
apply(plugin = "org.jetbrains.dokka")
apply(from = "gradle/jacoco.gradle")
apply(from = "gradle/merge-reports.gradle")

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        dokkaSourceSets.named("main") {
            jdkVersion.set(8)
            skipDeprecated.set(true)
            skipEmptyPackages.set(true)
            reportUndocumented.set(false)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register("install") {
    val publishTasks = mutableListOf<Task>()
    project.subprojects.forEach { project ->
        val installTask = project.tasks.findByName("install") ?: project.tasks.findByName("publishToMavenLocal")
        if (installTask != null) {
            publishTasks.add(installTask)
        }
    }
    dependsOn(publishTasks)
}

