plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
}

group = "dev.camyzed"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))


    api("engineering.swat:java-watch:0.9.7") // File Watching
    api("io.heapy.kotaml:kotaml:0.106.0") // YAML Parsing
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}