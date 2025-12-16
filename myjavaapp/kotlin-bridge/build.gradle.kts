plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Core Kotlin modules - use api() to expose them to Java
    api(project(":domain"))
    api(project(":common"))
    api(project(":kotlinYtmusicScraper"))

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}
