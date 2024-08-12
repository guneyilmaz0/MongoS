plugins {
    kotlin("jvm") version "2.0.0"
}

group = "net.guneyilmaz0.mongos"
version = "4.4.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.mongodb:mongodb-driver-sync:5.1.2")
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}