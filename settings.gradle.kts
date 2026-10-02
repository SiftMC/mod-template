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
for ((version, built) in targets) check(built.isNotEmpty()) {
    "[\"$version\"] in stonecutter.properties.toml sets no loader version. A section name needs its quotes."
}

stonecutter {
    create(rootProject) {
        branch("common") { versions(targets.keys) }
        for (loader in loaders.keys) {
            val versions = targets.filterValues { loader in it }.keys
            if (versions.isNotEmpty()) branch(loader) { versions(versions) }
        }
        // Before 26.1 the game is obfuscated. Before 1.14.4 there are no Mojang mappings.
        mapBuilds { branch, node ->
            val version = node.parsed
            val retro = "build.retro.gradle.kts".takeIf { node.version == "1.7.10" }
            val script = when (branch) {
                "neoforge" -> "build.gradle.kts".takeIf { version >= "1.21" }
                "forge" -> when {
                    version >= "1.20.6" -> "build.gradle.kts"
                    version >= "1.17" && version < "1.20.2" -> "build.legacy.gradle.kts"
                    else -> retro
                }
                else -> when {
                    version >= "26.1" -> "build.gradle.kts"
                    version >= "1.14.4" -> "build.legacy.gradle.kts"
                    else -> retro
                }
            }
            checkNotNull(script?.takeIf { rootDir.resolve("$branch/$it").exists() }) {
                "Minecraft ${node.version} has no $branch build script. See the version table in README.md."
            }
        }
        vcsVersion = targets.keys.first()
    }
}
