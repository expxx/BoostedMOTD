plugins {
    kotlin("jvm") version "2.3.0"
    id("com.google.devtools.ksp") version "2.3.0" apply false
    id("com.gradleup.shadow") version "9.3.0" apply false
}

group = "dev.camyzed"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
}

kotlin {
    jvmToolchain(21)
}