plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = providers.gradleProperty("mod_id").get()

// Every ["<version>"] table in stonecutter.properties.toml is a Minecraft version, and a loader is built for it
// when the table sets that loader's version. The first table is the version whose sources are committed.
val loaders = mapOf("fabric" to "fabric_api_version", "neoforge" to "neoforge_version", "forge" to "forge_version")
val targets = dev.kikugie.stonecutter.data.deserialization.TomlConverter
    .convert(file("stonecutter.properties.toml").readText()).asMap()
    .mapNotNull { (version, table) -> table.asMapOrNull()?.let { version to loaders.filterValues(it::containsKey).keys } }
    .toMap()
check(targets.isNotEmpty()) { "stonecutter.properties.toml lists no Minecraft version" }

stonecutter {
    create(rootProject) {
        branch("common") { versions(targets.keys) }
        for (loader in loaders.keys) {
            val versions = targets.filterValues { loader in it }.keys
            if (versions.isNotEmpty()) branch(loader) { versions(versions) }
        }
        // Older Minecraft needs other toolchains. Before 26.1 the game is obfuscated and build.legacy.gradle.kts builds it.
        // Before 1.14 there are no Mojang mappings either and build.retro.gradle.kts builds it.
        mapBuilds { branch, node ->
            val script = when {
                // NeoForge builds with ModDevGradle on every version. Forge has run on Mojang names since 1.20.6.
                branch == "neoforge" || node.parsed >= (if (branch == "forge") "1.20.6" else "26.1") -> "build.gradle.kts"
                node.parsed >= "1.14" -> "build.legacy.gradle.kts"
                else -> "build.retro.gradle.kts"
            }
            check(rootDir.resolve("$branch/$script").exists()) { "Minecraft ${node.version} needs $branch/$script" }
            script
        }
        vcsVersion = targets.keys.first()
    }
}
