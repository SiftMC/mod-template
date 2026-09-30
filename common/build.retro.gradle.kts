// Minecraft 1.7.10 has no toolchain for plain vanilla, so common compiles against Forge's MCP-named Minecraft.
plugins {
    id("multiloader-common")
    id("com.gtnewhorizons.retrofuturagradle")
}

minecraft {
    mcVersion = sc.current.version
}

repositories {
    maven("https://nexus.gtnewhorizons.com/repository/public/")
}

dependencies {
    // Only for Mixin and MixinExtras.
    compileOnly("io.github.legacymoddingmc:unimixins:${property("unimixins_version")}:dev")
}
