// Forge before 1.20.6 runs on SRG names. ModDevGradle's legacy plugin compiles against Mojang mappings and
// reobfuscates the jar to SRG names. The mixin annotation processor writes the refmap that Mixin needs for that.
plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev.legacyforge")
}

val modId = "${property("mod_id")}"
val mixinConfig = "$modId.mixins.json"

legacyForge {
    enable { forgeVersion = "${sc.current.version}-${property("forge_version")}" }

    runs {
        register("client") { client() }
        register("server") {
            server()
            programArgument("--nogui")
        }
        configureEach { disableIdeRun() }
    }

    mods {
        register(modId) { sourceSet(sourceSets.main.get()) }
    }
}

// Forge metadata has no mixin entry. Dev runs get the config from here, the jar through its manifest.
mixin {
    config(mixinConfig)
    add(sourceSets.main.get(), "$modId.refmap.json")
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

tasks.jar {
    manifest.attributes["MixinConfigs"] = mixinConfig
}

// Mixin finds the refmap through this key. The shared config leaves it out, because the other toolchains write none.
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
