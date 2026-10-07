import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestExtension

val versionName = file("VERSION").readText().trim()
allprojects {
    extra["VERSION_NAME"] = versionName
}

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

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    val javaVersion = JavaVersion.VERSION_17

    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        dokkaSourceSets.named("main") {
            jdkVersion.set(11)
            skipDeprecated.set(true)
            skipEmptyPackages.set(true)
            reportUndocumented.set(false)
        }
    }

    // Robolectric on JDK 17+ needs access to these JDK internals.
    val robolectricJvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.text=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.net=ALL-UNNAMED",
        "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
    )

    // Common android config
    val commonAndroidConfig: CommonExtension<*, *, *, *, *, *>.() -> Unit = {
        compileSdk = 36

        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }

        testOptions.unitTests.all {
            it.jvmArgs(robolectricJvmArgs)
        }
    }

    // Android library config
    pluginManager.withPlugin("com.android.library") {
        with(extensions.getByType<LibraryExtension>()) {
            commonAndroidConfig()
            defaultConfig { minSdk = 23 }
        }
    }

    // Android app config
    pluginManager.withPlugin("com.android.application") {
        with(extensions.getByType<ApplicationExtension>()) {
            commonAndroidConfig()
            defaultConfig {
                minSdk = 23
                //noinspection ExpiredTargetSdkVersion
                targetSdk = 30
            }
        }
    }

    // Android test config
    pluginManager.withPlugin("com.android.test") {
        with(extensions.getByType<TestExtension>()) {
            commonAndroidConfig()
            defaultConfig {
                // Using sdk 28 for robolectric tests.
                minSdk = 28
                //noinspection ExpiredTargetSdkVersion
                targetSdk = 28
            }
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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

// We disable jacoco report when running buildTask
val isBuild = gradle.startParameter.taskNames.any { it.contains("build") }
if (isBuild) {
    subprojects {
        plugins.withType<JacocoPlugin> {
            tasks.withType<JacocoReport> {
                enabled = false
            }
        }
    }
}

apply(from = "gradle/merge-reports.gradle")
