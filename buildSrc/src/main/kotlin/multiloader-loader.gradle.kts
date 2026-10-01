import dev.kikugie.stonecutter.build.StonecutterBuildExtension

plugins {
    id("multiloader-common")
}

// Loaders compile common's sources themselves, so the final jar contains everything and each toolchain processes it.
// The common node's main source set is common/src/main for the active version. For every other version it is the
// output of that node's stonecutterGenerate task, which runs first because Stonecutter marks the directory as built by it.
// The project dependency is only for IDE navigation.
val sc = the<StonecutterBuildExtension>()
val common = checkNotNull(sc.node.sibling("common")) { "No common node for ${sc.current.version}" }.project
evaluationDependsOn(common.path)
val commonMain = common.the<SourceSetContainer>()["main"]

dependencies {
    // Loom's remapping variant publishes the remapped jar. The Mojang-mapped one is in namedElements.
    compileOnly(project(common.path, common.configurations.findByName("namedElements")?.name))
}

tasks.compileJava {
    source(commonMain.java)
}

tasks.processResources {
    from(commonMain.resources)
}

val modId = providers.gradleProperty("mod_id").get()

// Forge has no mixin entry in mods.toml, so the jar manifest names the config.
val mixinConfig = "$modId.mixins.json"
if (sc.branch.id == "forge") {
    tasks.jar { manifest.attributes["MixinConfigs"] = mixinConfig }
}

// The toolchains that reobfuscate the jar write a refmap, and Mixin looks it up by this key.
// The shared config leaves the key out, because the other toolchains write no refmap.
for (plugin in listOf("net.neoforged.moddev.legacyforge", "com.gtnewhorizons.retrofuturagradle")) {
    pluginManager.withPlugin(plugin) {
        tasks.processResources {
            val config = mixinConfig
            val refmap = "\"refmap\": \"$modId.refmap.json\",\n  \"package\":"
            filesMatching(config) {
                filter { it.replace("\"package\":", refmap) }
            }
            doLast {
                check("\"refmap\"" in destinationDir.resolve(config).readText()) { "$config did not get its refmap key" }
            }
        }
    }
}

// -Pjoin makes runClient connect to the dev server on localhost as soon as the game is up.
// An argument provider puts the arguments last. ModDevGradle reads its first argument as the main class.
if (providers.gradleProperty("join").isPresent) {
    val join = if (sc.current.parsed >= "1.20") listOf("--quickPlayMultiplayer", "localhost") else listOf("--server", "localhost")
    tasks.withType<JavaExec>().named { it == "runClient" }.configureEach {
        argumentProviders.add(CommandLineArgumentProvider { join })
    }
}
