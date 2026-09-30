plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.neoforged.net/releases")
}

// Loader toolchains live here so every subproject shares one classloader for them.
dependencies {
    implementation("net.fabricmc.fabric-loom:net.fabricmc.fabric-loom.gradle.plugin:1.18.2")
    implementation("net.neoforged.moddev:net.neoforged.moddev.gradle.plugin:2.0.148")
    implementation("net.minecraftforge.gradle:net.minecraftforge.gradle.gradle.plugin:7.0.40")
}
