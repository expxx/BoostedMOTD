plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.gradleup.shadow")
}

group = "dev.camyzed"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "faststatsReleases"
        url = uri("https://repo.faststats.dev/releases")
    }
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    implementation("dev.faststats.metrics:bukkit:0.18.1")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveFileName.set("BoostedMOTD-Paper.jar")

        relocate("com.charleskorn.kaml", "shadow.kaml")
        relocate("dev.faststats", "shadow.faststats")
        relocate("dev.camyzed.boostedmotd.core", "shadow.core")
        relocate("kotlin", "shadow.kotlin")
        relocate("okio", "shadow.okio")
        relocate("net.thauvin.erik.urlencoder", "shadow.urlencoder")
        relocate("it.krzeminski.snakeyaml", "shadow.snakeyaml")
    }
}