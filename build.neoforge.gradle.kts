import org.gradle.kotlin.dsl.stonecutter

plugins {
    id("net.neoforged.moddev")
    id("dev.kikugie.postprocess.jsonlang")
}

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

    val props = HashMap<String, String>().apply {
        this["id"] = prop("mod.id")
        this["name"] = prop("mod.name")
        this["version"] = prop("mod.version")
        this["minecraft"] = project.property("mod.mc_dep") as String
    }

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
        expand(props)
    }
}

version = "${property("mod.version")}+${sc.current.version}-neoforge"
base.archivesName = property("mod.id") as String

jsonlang {
    languageDirectories = listOf("assets/${property("mod.id")}/lang")
    prettyPrint = true
}

repositories {
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    maven("https://repo.sleeping.town/")
}

neoForge {
    version = property("deps.neoforge") as String
    validateAccessTransformers = true

    if (hasProperty("deps.parchment")) parchment {
        val (mc, ver) = (property("deps.parchment") as String).split(':')
        mappingsVersion = ver
        minecraftVersion = mc
    }

    runs {
        register("client") {
            ideName = "Minecraft Client (:${sc.current.version}-neoforge)"
            gameDirectory = file("run/")
            client()
        }
        register("server") {
            ideName = "Minecraft Server (:${sc.current.version}-neoforge)"
            gameDirectory = file("run/")
            server()
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
    sourceSets["main"].resources.srcDir("src/main/generated")
}

dependencies {
    implementation("folk.sisby:kaleido-config:${property("deps.kaleido_config")}")
    jarJar("folk.sisby:kaleido-config:${property("deps.kaleido_config")}")
    "additionalRuntimeClasspath"("folk.sisby:kaleido-config:${property("deps.kaleido_config")}")
}

tasks {
    processResources {
        exclude("**/fabric.mod.json", "**/*.accesswidener", "**/mods.toml")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    withSourcesJar()
    val javaCompat = if (stonecutter.eval(stonecutter.current.version, ">=1.20.5")) {
        JavaVersion.VERSION_21
    } else {
        JavaVersion.VERSION_17
    }
    sourceCompatibility = javaCompat
    targetCompatibility = javaCompat
}