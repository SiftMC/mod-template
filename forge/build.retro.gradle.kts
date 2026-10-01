// Minecraft 1.7.10 has no Mojang mappings, so RetroFuturaGradle compiles against MCP names and reobfuscates the jar.
// Forge 1.7.10 has no Mixin either. UniMixins adds it, as a mod that players install too.
plugins {
    id("multiloader-loader")
    id("com.gtnewhorizons.retrofuturagradle")
}

val modId = "${property("mod_id")}"
val mixinTweaker = "org.spongepowered.asm.launch.MixinTweaker"
val unimixins = "io.github.legacymoddingmc:unimixins:${property("unimixins_version")}:dev"

minecraft {
    mcVersion = sc.current.version
    extraTweakClasses.add(mixinTweaker)
}

repositories {
    maven("https://nexus.gtnewhorizons.com/repository/public/")
}

dependencies {
    annotationProcessor("org.ow2.asm:asm-debug-all:5.0.4")
    annotationProcessor("com.google.guava:guava:24.1.1-jre")
    annotationProcessor("com.google.code.gson:gson:2.13.2")
    annotationProcessor(unimixins)
    implementation(modUtils.enableMixins(unimixins, "$modId.refmap.json"))
}

// Dev runs load the jar. The loader plugin adds MixinConfigs to this manifest.
tasks.jar {
    manifest.attributes(
        "TweakClass" to mixinTweaker,
        "FMLCorePluginContainsFMLMod" to true,
        "ForceLoadAsMod" to true,
    )
}
