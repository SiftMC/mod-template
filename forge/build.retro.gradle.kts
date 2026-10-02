// Minecraft 1.7.10 has no Mojang mappings, so RetroFuturaGradle compiles against MCP names and reobfuscates the jar.
plugins {
    id("multiloader-loader")
    id("com.gtnewhorizons.retrofuturagradle")
}

// mods.toml and pack.mcmeta are for newer Forge. 1.7.10 reads mcmod.info from forge/versions/1.7.10.
tasks.processResources {
    exclude("META-INF/mods.toml", "pack.mcmeta")
}
