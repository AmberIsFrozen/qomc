pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8"
}

stonecutter {
    create(rootProject) {
        var combos = listOf("1.16.5-fabric", "1.18.2-fabric", "1.19.4-fabric", "1.20.1-forge", "1.21.1-fabric", "1.21.5-fabric", "1.21.11-fabric",
            "1.21.1-neoforge")

        combos.forEach {
            val ver = it.split("-")[0]
            val majorVer = ver.split(".")[0]
            val loaderString = it.split("-")[1]
            val loader = if(majorVer.toInt() >= 26) "$loaderString-unobf" else loaderString

            version(it, ver).buildscript("build.${loader}.gradle.kts")
        }

        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        vcsVersion = "1.21.1-fabric"
    }
}

rootProject.name = "QoMC"