plugins {
    application
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.myjavaapp.gui.MusicPlayerApp")
}

// Fix Kotlin Multiplatform dependency resolution
configurations.all {
    attributes {
        attribute(
            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
        )
    }
}

dependencies {
    // Use Kotlin bridge module for Java-friendly API
    implementation(project(":myjavaapp:kotlin-bridge"))

    // Kotlin runtime (transitively included from bridge)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")

    // Ktor client (used by kotlinYtmusicScraper)
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-okhttp:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // VLCJ for media playback (optional)
    implementation("uk.co.caprica:vlcj:4.8.2")

    // Gson for JSON (optional - for your Java code)
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Handle duplicate files in distributions (from transitive dependencies)
tasks.withType<Tar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Task to run the app
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

