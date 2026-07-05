plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.141" apply false
    id("dev.kikugie.postprocess.jsonlang") version "2.1-beta.4" apply false
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT" apply false
    id("me.modmuss50.mod-publish-plugin") version("2.0.0-beta.2") apply false
}

stonecutter active "1.21.1-fabric"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "forge", "neoforge")
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
}

stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}