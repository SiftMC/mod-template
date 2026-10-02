// Minecraft before 26.1 is obfuscated, so Loom's remapping variant compiles against Mojang mappings and remaps the jar.
plugins {
    id("multiloader-loader")
    id("net.fabricmc.fabric-loom-remap")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}
