plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("me.modmuss50.mod-publish-plugin")
}

version = "${property("mod.version")}+${sc.current.version}-fabric"
base.archivesName = property("mod.id") as String

val requiredJava = when {
    sc.current.parsed >= "1.20.6" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.19" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
    maven("https://repo.sleeping.town/")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    implementation("folk.sisby:kaleido-config:${property("deps.kaleido_config")}")
    include("folk.sisby:kaleido-config:${property("deps.kaleido_config")}")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection

    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep")
        )

        filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml", "pack.mcmeta")) {
            expand(props)
        }

        exclude("**/*mods.toml")
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }

    publishMods {
        file = remapJar.map { it.archiveFile.get() }
        modLoaders.add("fabric")
        type = STABLE
        displayName = "[${property("mod.release_prefix")}] v${property("mod.version")}"
        changelog = provider { rootProject.file("CHANGELOG.md").readText() }
        dryRun = providers.environmentVariable("MODRINTH_API_KEY").getOrNull() == null
        val mcDep = property("mod.mc_dep") as String
        val startMcVersion = mcDep.split(" ")[0].replace("=|>|<".toRegex(), "")
        val endMcVersion = if(mcDep.contains(" ")) mcDep.split(" ")[1].replace("=|>|<".toRegex(), "") else mcDep

        modrinth {
            accessToken = providers.environmentVariable("MODRINTH_API_KEY")
            projectId = property("release.modrinth") as String
            requires("fabric-api")

            minecraftVersionRange {
                start = startMcVersion
                end = endMcVersion
            }
        }

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
            projectId = property("release.curseforge") as String
            requires("fabric-api")

            minecraftVersionRange {
                start = startMcVersion
                end = endMcVersion
            }
        }
    }
}