// Minecraft 1.7.10 has no toolchain for plain vanilla, so common compiles against Forge's MCP-named Minecraft.
plugins {
    id("multiloader-common")
    id("com.gtnewhorizons.retrofuturagradle")
}

// Forge 1.7.10 has no Mixin, so the mixins and their config stay out of the 1.7.10 jars.
sourceSets.main {
    java.exclude("**/mixin/**")
    resources.exclude("*.mixins.json")
}
