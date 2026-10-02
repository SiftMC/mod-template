// Minecraft before 26.1 is obfuscated, so Loom's remapping variant compiles against Mojang mappings.
plugins {
    id("multiloader-common")
    id("net.fabricmc.fabric-loom-remap")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    mappings(loom.officialMojangMappings())
    // Only for Mixin and MixinExtras. Fabric Loader API stays out of common.
    modCompileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
}
