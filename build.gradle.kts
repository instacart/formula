import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestExtension

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

    val libJavaVersion = JavaVersion.VERSION_18
    val lintJavaVersion = JavaVersion.VERSION_17

    /**
     * We explicitly target JDK 17 for the lint module in order to be compatible with current Android Studio JRE version (JBR17),
     * as lint checks compiled with JDK 18 are failing to be run by the code inspector.
     *
     * We should be able to update back to JDK 18 once AS ships with JBR21 (likely IDEA 2024.1):
     * https://github.com/JetBrains/JetBrainsRuntime?tab=readme-ov-file#releases-based-on-jdk-21
     */
    fun Project.javaVersion(): JavaVersion {
        return if (name == "formula-lint") {
            logger.lifecycle("")
            lintJavaVersion
        } else {
            libJavaVersion
        }
    }

    tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
        dokkaSourceSets.named("main") {
            jdkVersion.set(11)
            skipDeprecated.set(true)
            skipEmptyPackages.set(true)
            reportUndocumented.set(false)
        }
    }

    // Common android config
    val commonAndroidConfig: CommonExtension<*, *, *, *, *>.() -> Unit = {
        compileSdk = 34

        compileOptions {
            sourceCompatibility = libJavaVersion
            targetCompatibility = libJavaVersion
        }
    }

    // Android library config
    pluginManager.withPlugin("com.android.library") {
        with(extensions.getByType<LibraryExtension>()) {
            commonAndroidConfig()
            defaultConfig { minSdk = 21 }
        }
    }

    // Android app config
    pluginManager.withPlugin("com.android.application") {
        with(extensions.getByType<ApplicationExtension>()) {
            commonAndroidConfig()
            defaultConfig {
                minSdk = 21
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
        sourceCompatibility = project.javaVersion().toString()
        targetCompatibility = project.javaVersion().toString()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = project.javaVersion().toString()
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
