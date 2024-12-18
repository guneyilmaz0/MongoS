plugins {
    kotlin("jvm") version "2.0.20"
    application
    `maven-publish`
}

group = "net.guneyilmaz0.mongos"
version = "5.0.0-beta.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.mongodb:mongodb-driver-sync:5.2.0")
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "MongoS"
            version = this.version

            from(components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MongoSKt")
}