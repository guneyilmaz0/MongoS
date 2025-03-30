plugins {
    kotlin("jvm") version "2.1.10"
}

group = "net.guneyilmaz0.mongos"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:4.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.mongodb:mongodb-driver-sync:5.3.1")
    implementation("org.mongodb:bson:5.3.1")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("ch.qos.logback:logback-classic:1.5.16")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.guneyilmaz0.MongoS"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.test {
    useJUnitPlatform()
}