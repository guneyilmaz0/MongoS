plugins {
    kotlin("jvm") version "2.0.0"
}

group = "net.guneyilmaz0.mongos"
version = "4.3.0"
description = "A MongoDB client library for Kotlin & Java."

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}